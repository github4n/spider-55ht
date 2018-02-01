package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import org.jsoup.nodes.Document;

import com.haitao55.spider.crawler.utils.JsoupUtils;

public class ResultContent {
	
	private String skuId;
	private String skuUrl;
	private String content;
	private Document document;
	
	public String getSkuId() {
		return skuId;
	}
	public void setSkuId(String skuId) {
		this.skuId = skuId;
	}
	public String getSkuUrl() {
		return skuUrl;
	}
	public void setSkuUrl(String skuUrl) {
		this.skuUrl = skuUrl;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Document getDocument() {
		if(null == document){
			document = JsoupUtils.parse(content);
			setDocument(document);
		}
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
	

}
