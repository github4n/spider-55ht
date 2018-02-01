package com.haitao55.spider.crawler.service.impl;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.haitao55.spider.crawler.core.model.Rules;
import com.haitao55.spider.crawler.service.XmlParseService;
import com.haitao55.spider.crawler.utils.ClassUtils;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.PackageUtils;

/**
 * 
 * 功能：用于解析爬虫配置文件
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午11:11:42
 * @version 1.0
 */
public class XmlParseServiceImpl implements XmlParseService {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	private String packName = "com.haitao55.spider.crawler.core";// 先有个默认值

	// key:simpleName,value CanonicalName
	private Map<String, String> classMap = new HashMap<String, String>();

	// key:simpleName,value CanonicalName
	private Map<String, String> nodeClassMap = new HashMap<String, String>();

	public void init() {
		logger.info("package scan {} ...", packName);
		Set<String> clazzes = PackageUtils.getClasses(packName);
		for (String clazz : clazzes) {
			int index = clazz.lastIndexOf('.');
			if (index == -1) {
				continue;
			}
			String clazzName = clazz.substring(index + 1);
			// 处理内部类
			index = clazzName.indexOf('$');
			if (index != -1) {
				clazzName = clazzName.substring(index + 1);
			}
			// 匿名内部类,忽略
			if (StringUtils.isNumeric(clazzName)) {
				continue;
			}
			classMap.put(clazzName.toLowerCase(), clazz);
		}
		if (MapUtils.isEmpty(classMap)) {
			logger.error("package scan error,got nothing");
			throw new RuntimeException("package scan error,got nothing");
		} else {
			logger.info("package scan finished,found {} classes in package {}:{}", classMap.size(), packName,
					classMap.toString());
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T parse(Document doc) throws Exception {
		Map<Element, Object> elementObjectMap = new HashMap<Element, Object>();
		Queue<Element> elements = new ArrayDeque<Element>();
		Element root = doc.getRootElement();
		elements.add(root);
		for (;;) {// 按层次访问树型结构，亦即“广度优先”
			Element element = elements.poll();
			if (element == null) {
				break;
			}
			parseNode(elementObjectMap, element);
			elements.addAll(element.elements());
		}
		return (T) elementObjectMap.get(root);
	}

	private void parseNode(Map<Element, Object> elementObjectMap, Element node) throws Exception {
		String nodeName = node.getName();
		if (StringUtils.isBlank(nodeName)) {
			return;
		}
		// 根据配置中设定的class的名字反射一个节点对象出来。
		Object object = createObjectForNode(nodeName);
		if (object == null) {
			return;
		}
		elementObjectMap.put(node, object);

		// 把节点的attribute放到节点对象里
		setAttr(object, node);

		// 把节点的内容放到节点对象里
		setContent(object, node);

		// 放到父节点的对象里面去
		putObjectToParent(object, node, elementObjectMap);

	}

	private void putObjectToParent(Object object, Element node, Map<Element, Object> elementObjectMap) {
		Element parentElement = node.getParent();
		if (parentElement == null) {// root，也有可能是一些其他情况。详见dom4j文档
			return;
		}
		Object parent = elementObjectMap.get(parentElement);
		if (parent == null) {
			return;
		}
		ClassUtils.setField(parent, object);
	}

	private void setContent(Object object, Element node) {
		String content = node.getText();
		Method method = ClassUtils.findMethodIgnoreCase(object.getClass(), "set", String.class);
		if (method != null) {
			ClassUtils.invokeMethod(object, method, content);
		}
	}

	private Object createObjectForNode(String nodeName) throws Exception {
		String className = nodeClassMap.get(nodeName);
		// map里没有特殊指定映射类型，就用package
		if (className == null) {
			className = classMap.get(nodeName.toLowerCase());
		}
		if (className == null) {
			throw new RuntimeException("cann't find class for node " + nodeName);
		}

		return ClassUtils.newInstance(className);
	}

	@SuppressWarnings("unchecked")
	private void setAttr(Object object, Element node) throws Exception {
		String className = object.getClass().getCanonicalName();
		List<Attribute> attributes = node.attributes();
		if (CollectionUtils.isEmpty(attributes)) {
			return;
		}

		for (Attribute attribute : attributes) {
			String attrName = attribute.getName();
			String attrValue = attribute.getValue();
			String setter = ClassUtils.toSetterName(attrName);
			Method[] methods = ClassUtils.findMethodsIgnoreCase(object.getClass(), setter, 1);
			if (methods.length == 0) {
				logger.warn("cann't find method {} in class {}", setter, className);
			} else if (methods.length > 1) {
				throw new RuntimeException("duplicate method " + setter + " in class " + className);
			} else {
				Method method = methods[0];
				Class<?>[] clazzes = method.getParameterTypes();
				Class<?> clazz = clazzes[0];
				Object value = tryConvert(clazz, attrValue);
				method.invoke(object, value);
			}

		}
	}

	/**
	 * 尝试将string对象转为对应class类型<br>
	 * 目前只支持数字，布尔，string类型转型
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	public Object tryConvert(Class<?> type, String value) {
		if (type.equals(String.class)) {
			return value;
		} else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
			return StringUtils.equalsIgnoreCase("true", value) || StringUtils.equalsIgnoreCase("yes", value)
					|| StringUtils.equalsIgnoreCase("y", value);
		} else if (NumberUtils.isNumber(value)) {
			if (type.equals(byte.class) || type.equals(Byte.class)) {
				return NumberUtils.toByte(value);
			} else if (type.equals(short.class) || type.equals(Short.class)) {
				return NumberUtils.toShort(value);
			} else if (type.equals(int.class) || type.equals(Integer.class)) {
				return NumberUtils.toInt(value);
			} else if (type.equals(long.class) || type.equals(Long.class)) {
				return NumberUtils.toLong(value);
			} else if (type.equals(float.class) || type.equals(Float.class)) {
				return NumberUtils.toFloat(value);
			} else if (type.equals(double.class) || type.equals(Double.class)) {
				return NumberUtils.toDouble(value);
			} else if (type.equals(BigDecimal.class)) {
				return NumberUtils.createBigDecimal(value);
			}
		}

		throw new RuntimeException("cann't convert string " + value + " to type " + type.getName());

	}

	public <T> T parse(String doc) throws Exception {
		return parse(DocumentHelper.parseText(doc));
	}

	public <T> T parse(File file) throws Exception {
		List<String> lines = null;
		try {
			lines = IOUtils.readLines(new FileReader(file));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (CollectionUtils.isEmpty(lines)) {
			return null;
		}
		StringBuilder doc = new StringBuilder();
		for (String line : lines) {
			doc.append(line);
		}
		return parse(doc.toString());
	}

	public String getPackName() {
		return packName;
	}

	public void setPackName(String packName) {
		this.packName = packName;
	}

	/**
	 * 测试
	 * 
	 * @param args
	 */
	public static void main(String... args) throws Exception {
		StringBuilder builder = new StringBuilder();
		// builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		// builder.append(
		// "<rules xmlns=\"http://www.zamplus.com\"
		// xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		// builder.append(" <rule regex=\"http://search.jumei.com.*\">");
		// builder.append(" <css input=\"${doc}\" query=\"div.s_l_name > a\"
		// attr=\"href\" output=\"${newurls}\" />");
		// builder.append(" </rule>");
		// builder.append(" <rule regex=\"http://mall.jumei.com.*\">");
		// builder.append(
		// " <css input=\"${doc}\" required=\"true\" query=\"div#detail_top >
		// h1.title > span\" output=\"Title\" />");
		// builder.append(" <css input=\"${doc}\" required=\"true\"
		// query=\"span#mall_price\" output=\"Price\" />");
		// builder.append(" <constant key=\"Priority\" value=\"8\" />");
		// builder.append(" <output channel=\"file\" />");
		// builder.append(" </rule>");
		// builder.append("</rules>");

		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		builder.append(
				"<rules xmlns=\"http://www.55haitao.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		builder.append("<rule regex=\".*www.selfridges.com.*\" grade=\"3\" >");
		builder.append("<concat input=\"BreadCrumbTemp1\" suffix=\",\" output=\"BreadCrumbTemp2\" />");
		builder.append("<concatbykey input=\"BreadCrumbTemp2\" suffix=\"Brand.en\"　output=\"BreadCrumbTemp3\" />");
//		builder.append("<array input=\"BreadCrumbTemp\"　splitChar=\",\" output=\"BreadCrumb\" />");
//		builder.append("<array input=\"CategoryTemp\" splitChar=\",\" output=\"Category\" />");
		builder.append("</rule>");
		builder.append("</rules>");

		XmlParseServiceImpl parser = new XmlParseServiceImpl();
		parser.init();
		Rules rules = parser.parse(builder.toString());
		System.out.println("rules::" + rules);
	}
}