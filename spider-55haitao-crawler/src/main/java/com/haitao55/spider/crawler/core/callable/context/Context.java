package com.haitao55.spider.crawler.core.callable.context;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlStatus;
import com.haitao55.spider.crawler.core.model.xobject.XDocument;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
 * 功能：Callable Context,在各Callable之间保存/传递/共享数据
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午4:52:26
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class Context {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	@SuppressWarnings("rawtypes")
	private Map data = new ConcurrentHashMap();
	private Url url;
	private OutputObject outputObject;

	private boolean isRunInRealTime = false;
	private String htmlPageSource;
	
	public void init() {
		// nothing
	}

	public String getHtmlPageSource() {
		return htmlPageSource;
	}


	public void setHtmlPageSource(String htmlPageSource) {
		this.htmlPageSource = htmlPageSource;
	}


	public boolean isRunInRealTime() {
		return isRunInRealTime;
	}

	public void setRunInRealTime(boolean isRunInRealTime) {
		this.isRunInRealTime = isRunInRealTime;
	}


	public String getCurrentUrl() {
		return (String) data.get(Keyword.URL.getValue());
	}

	public void setCurrentUrl(String currentUrl) {
		data.put(Keyword.URL.getValue(), currentUrl);
	}

	public String getCurrentHtml() {
		return (String) data.get(Keyword.HTML.getValue());
	}

	public void setCurrentHtml(String currentHtml) {
		data.put(Keyword.HTML.getValue(), currentHtml);
	}

	public XDocument getCurrentDoc() {
		return (XDocument) data.get(Keyword.DOC.getValue());
	}

	public void setCurrentDoc(XDocument currentDoc) {
		data.put(Keyword.DOC.getValue(), currentDoc);
	}

	public Set<Url> getCurrentNewUrls() {
		return (Set<Url>) data.get(Keyword.NEWURLS.getValue());
	}

	public Url getUrl() {
		return url;
	}

	public void setUrl(Url url) {
		this.url = url;
	}

	public OutputObject getOutputObject() {
		return outputObject;
	}

	public void setOutputObject(OutputObject outputObject) {
		this.outputObject = outputObject;
	}

	public Object get(String key) {
		Object obj = data.get(key);
		if (obj == null) {
			obj = internalGet(key);
		}

		return obj;
	}

	private Object internalGet(String key) {
		Keyword keyword = Keyword.codeOf(key);
		if (keyword == null) {
			return null;
		}
		switch (keyword) {
		case URL:
			return getCurrentUrl();
		case HTML:
			initHtml();
			return getCurrentHtml();
		case DOC:
			initDoc();
			return getCurrentDoc();
		case NEWURLS:
			return getCurrentNewUrls();
		default:
			return null;
		}
	}

	private void initHtml() {
		if (getCurrentHtml() == null) {
			setCurrentHtml(HttpUtils.get(getUrl()));
		}
	}

	private void initDoc() {
		if (getCurrentDoc() == null) {
			initHtml();
			Document doc = JsoupUtils.parse(getCurrentHtml(), getCurrentUrl());
			XDocument xdoc = new XDocument(getCurrentUrl(), doc);
			setCurrentDoc(xdoc);
		}
	}

	@SuppressWarnings("deprecation")
	private void internalPut(String key, Object value) {
		Keyword keyword = Keyword.codeOf(key);
		if (keyword == null) {
			return;
		}
		switch (keyword) {
		case URL:
			setCurrentUrl(ObjectUtils.toString(value));
			return;
		case HTML:
			setCurrentHtml(ObjectUtils.toString(value));
			return;
		case DOC:
			XDocument xdoc = new XDocument(getCurrentUrl(), (Document) value);
			setCurrentDoc(xdoc);
			return;
		case NEWURLS:
			setCurrentNewUrls(value);
			return;
		default:
			return;
		}
	}

	/**
	 *
	 * @param obj
	 *            单个String/String数组/String集合/实现了toString方法的数组、集合、对象
	 */
	public void setCurrentNewUrls(Object obj) {
		if (obj == null) {
			return;
		}

		if (obj instanceof Collection) {
			Collection<Object> collection = (Collection<Object>) obj;
			Object[] array = collection.toArray(new Object[collection.size()]);
			addCurrentNewUrls(array);
		} else if (obj.getClass().isArray()) {
			Object[] array = (Object[]) obj;
			addCurrentNewUrls(array);
		} else {
			addCurrentNewUrls(obj);
		}
	}

	@SuppressWarnings("deprecation")
	private void addCurrentNewUrls(Object... newUrls) {
		if (ArrayUtils.isEmpty(newUrls)) {
			return;
		}

		for (Object obj : newUrls) {
			String newUrlValue = ObjectUtils.toString(obj);

			if (StringUtils.isBlank(newUrlValue)) {
				continue;
			}

			String _newurlValue = StringUtils.trim(newUrlValue);
			Url _newurl = new Url();
			_newurl.setTaskId(url.getTaskId());
			_newurl.setValue(_newurlValue);
			_newurl.setUrlStatus(UrlStatus.NEWCOME);// 新迭代出来的Urls
			url.getNewUrls().add(_newurl);
		}
	}

	public void put(String key, Object value) {
		try {
			if (value == null) {
				value = StringUtils.EMPTY;// 避免NullPointerException
			}

			data.put(key, value);
			internalPut(key, value);
		} catch (Exception e) {
			logger.warn("Put key-value into context error!!!key:{};value:{};e:{}", key, StringUtils.EMPTY, e);
		}
	}

	public Map<String, Object> getAll() {
		return data;
	}
}