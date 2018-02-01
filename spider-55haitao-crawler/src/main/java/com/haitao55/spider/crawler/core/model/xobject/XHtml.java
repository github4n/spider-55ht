package com.haitao55.spider.crawler.core.model.xobject;

/**
 * 
 * 功能：保存url以及对应的html<br>
 * 在jsoup解析document的时候，可以方便的设置baseURI，方便获取页面url的绝对值
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 下午2:09:07
 * @version 1.0
 */
public class XHtml {

    private String url;

    private String html;

    public XHtml(String url, String html) {
        this.url = url;
        this.html = html;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    /**
     * 这里只返回html，因为url只是在jsoup parse的时候才会用到<br>
     * 实际有用的信息是html
     */
    public String toString() {
        return html;
    }
}