package com.haitao55.spider.crawler.core.callable.base;

import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.xobject.XDocument;
import com.haitao55.spider.crawler.core.model.xobject.XHtml;

/**
 * 
 * 功能：根据css选择器进行页面元素选择的抽象类
 * 
 * @author Arthur.Liu
 * @time 2016年8月19日 下午3:21:53
 * @version 1.0
 */
public abstract class AbstractSelect extends AbstractCallable {
	protected Document getDocument(Context context) {
		Document doc = null;

		Object obj = getInputObject(context);
		if (obj instanceof XDocument) {
			XDocument xdoc = (XDocument) obj;
			doc = xdoc.getDoc();
		} else if (obj instanceof XHtml) {
			XHtml xhtml = (XHtml) obj;
			doc = Jsoup.parse(xhtml.getHtml(), xhtml.getUrl());
		} else if (obj instanceof Document) {
			doc = (Document) obj;
		} else {
			doc = Jsoup.parse(Objects.toString(obj, ""));
		}

		return doc;
	}
}