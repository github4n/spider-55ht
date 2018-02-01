package com.haitao55.spider.crawler.core.callable;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.xobject.XDocument;
import com.haitao55.spider.crawler.core.model.xobject.XHtml;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
 * 功能：通过多个css选择器,选取页面上的多块数据,然后使用分隔符链接成一条数据
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午12:05:55
 * @version 1.0
 */
@Deprecated
public class MultiSelect extends AbstractCallable {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String RULE_FIELDS_SEPARATOR = ";";
	private static final String RESULT_FIELDS_SEPARATOR = "#,#";// @see
																// Split.java

	private static final String ATTR_TYPE_TEXT = "text";
	private static final String ATTR_TYPE_HTML = "html";
	private static final String ATTR_TYPE_OWN_TEXT = "own_text";

	// css选择器
	private String css;

	// 多块数据选择器
	private String csses;
	private String attrs;

	@Override
	public void invoke(Context context) throws Exception {
		// 11111.验证输入
		if (StringUtils.isBlank(csses) || !StringUtils.contains(csses, RULE_FIELDS_SEPARATOR)) {
			logger.error("input increct:url={},queries={}", context.getUrl(), csses);
			return;
		}
		if (StringUtils.isBlank(attrs) || !StringUtils.contains(attrs, RULE_FIELDS_SEPARATOR)) {
			logger.error("input increct:url={},attrs={}", context.getUrl(), attrs);
			return;
		}

		String[] queryArr = StringUtils.splitByWholeSeparator(csses, RULE_FIELDS_SEPARATOR);
		String[] attrArr = StringUtils.splitByWholeSeparator(attrs, RULE_FIELDS_SEPARATOR);
		if (queryArr.length != attrArr.length) {
			logger.error("input increct:url={},names={},queries={},attrs={}", context.getUrl(), attrs, csses, attrs);
		}

		// 22222.抓取数据
		Object obj = getInputObject(context);
		Document doc = null;
		if (obj instanceof XDocument) {
			XDocument xdoc = (XDocument) obj;
			doc = xdoc.getDoc();
		} else if (obj instanceof XHtml) {
			XHtml xhtml = (XHtml) obj;
			// set base uri for document,so as to use
			// element.attr("abs:href")
			doc = Jsoup.parse(xhtml.getHtml(), xhtml.getUrl());
		} else if (obj instanceof Document) {
			doc = (Document) obj;
		} else {
			doc = Jsoup.parse(ObjectUtils.toString(obj));
		}

		List<String> value = new ArrayList<String>();
		Elements elements = doc.select(getCss());
		if (CollectionUtils.isNotEmpty(elements)) {
			for (Element element : elements) {// 每一个外层元素块
				String[] valueArr = new String[queryArr.length];
				for (int i = 0; i < queryArr.length; i++) {// 每一个选择器
					String childQuery = queryArr[i];
					String childAttr = attrArr[i];
					Elements childElements = element.select(childQuery);

					if (ATTR_TYPE_TEXT.equals(childAttr)) {
						valueArr[i] = JsoupUtils.text(childElements);
					} else if (ATTR_TYPE_HTML.equals(childAttr)) {
						valueArr[i] = JsoupUtils.html(childElements);
					} else if (ATTR_TYPE_OWN_TEXT.equals(childAttr)) {
						valueArr[i] = JsoupUtils.ownText(childElements);
					} else {
						List<String> list = JsoupUtils.attrs(childElements, childAttr);
						if (list != null && !list.isEmpty()) {
							valueArr[i] = list.get(0);
						} else {
							valueArr[i] = "";
						}
					}
				}

				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < valueArr.length; i++) {
					if (i == 0) {
						sb.append(valueArr[i]);
					} else {
						sb.append(RESULT_FIELDS_SEPARATOR).append(valueArr[i]);
						;
					}
				}

				value.add(sb.toString());
			}
		}

		setOutput(context, value);
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getCsses() {
		return csses;
	}

	public void setCsses(String csses) {
		this.csses = csses;
	}

	public String getAttrs() {
		return attrs;
	}

	public void setAttrs(String attrs) {
		this.attrs = attrs;
	}
}