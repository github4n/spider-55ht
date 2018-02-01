package com.haitao55.spider.crawler.core.model.xobject;

import org.jsoup.nodes.Document;

public class XDocument {

    private String url;

    private Document doc;

    public XDocument(String url, Document doc) {
        this.url = url;
        this.doc = doc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }
    @Override
    public String toString() {
    	return doc.toString();
    }
}