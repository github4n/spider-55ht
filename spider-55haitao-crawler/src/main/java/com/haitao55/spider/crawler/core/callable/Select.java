package com.haitao55.spider.crawler.core.callable;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * <p>
 * 功能：通过css选择器,提取页面上的元素内容；
 * </p>
 * <p>
 * 这个select选择器,是用来选取普通数据元素的；如果需要选取urls,可以使用SelectUrls及SelectPages；
 * </p>
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 上午11:53:58
 * @version 1.0
 * @see SelectUrls
 * @see SelectPages
 */
public class Select extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String ATTR_TYPE_TEXT = "text";
	private static final String ATTR_TYPE_HTML = "html";
	private static final String ATTR_TYPE_OWN_TEXT = "own_text";
	private static final String ATTR_TYPE_CONCAT_TEXT = "concat_text";

	// css选择器
	private String css;

	// 使用css选择器进行选择后,取元素的什么属性；默认是text
	private String attr = ATTR_TYPE_TEXT;

	// 需要使用到拼接字符， 遍历elements可能会用到
	private String concatChar;

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public String getConcatChar() {
		return concatChar;
	}

	public void setConcatChar(String concatChar) {
		this.concatChar = concatChar;
	}

	@Override
	public void invoke(Context context) throws Exception {
		Document doc = super.getDocument(context);
		Elements elements = doc.select(getCss());
		Object value = null;
		if (CollectionUtils.isNotEmpty(elements)) {
			String attr = getAttr();
			if (ATTR_TYPE_TEXT.equals(attr)) {
				value = JsoupUtils.text(elements);
			} else if (ATTR_TYPE_HTML.equals(attr)) {
				value = JsoupUtils.html(elements);
			} else if (ATTR_TYPE_OWN_TEXT.equals(attr)) {
				value = JsoupUtils.ownText(elements);
			} else if (ATTR_TYPE_CONCAT_TEXT.equals(attr)) {
				value = JsoupUtils.concatText(elements, concatChar);
			} else {
				value = JsoupUtils.attrs(elements, attr);
			}
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), getCss());
		}

		setOutput(context, value);
	}
}