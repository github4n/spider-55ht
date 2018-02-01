package com.haitao55.spider.crawler.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * 功能：jsoup 工具类
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午4:53:40
 * @version 1.0
 */
public class JsoupUtils {

    public static String html(Elements elements) {
        return elements.outerHtml();
    }

    public static Document parse(String html) {
        return parse(html, null);
    }

    public static Document parse(String html, String baseUri) {
        Document doc = Jsoup.parse(html);
        if (baseUri != null) {
            doc.setBaseUri(baseUri);
        }
        return doc;
    }

    public static String text(Element element) {
        return element.text();
    }

    public static String text(Elements elements) {
        return elements.text();
    }

    public static String ownText(Elements elements) {
        StringBuilder sb = new StringBuilder();
        for (Element element : elements) {
            String ownText = element.ownText();
            sb.append(ownText);
        }
        return sb.toString();
    }

    public static String attr(Element element, String attr) {
        if (element == null) {
            return StringUtils.EMPTY;
        }
        // @see abs:attr
        // if ("href".equalsIgnoreCase(attr)) {
        // return element.absUrl("href");
        // } else if ("src".equalsIgnoreCase(attr)) {
        // return element.absUrl("src");
        // }
        return element.attr(attr);// 看这个方法的注释，怎么获取绝对URL（String url = a.attr("abs:href");）
    }

    public static String attr(Elements elements, String attr) {
        if (CollectionUtils.isEmpty(elements)) {
            return StringUtils.EMPTY;
        }
        return attr(elements.get(0), attr);
    }

    public static List<String> attrs(Elements elements, String attr) {
        if (CollectionUtils.isEmpty(elements)) {
            return Collections.emptyList();
        }

        List<String> ret = new ArrayList<String>();
        for (Element element : elements) {
            ret.add(attr(element, attr));
        }
        return ret;
    }

	public static Object concatText(Elements elements, String concatChar) {
		 StringBuilder sb = new StringBuilder();
	        for (Element element : elements) {
	            String ownText = element.text();
	            sb.append(ownText).append(concatChar);
	        }
	        return sb.toString();
	}
}