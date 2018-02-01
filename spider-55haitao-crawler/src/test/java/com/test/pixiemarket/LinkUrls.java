package com.test.pixiemarket;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.crawler.core.callable.SelectAllPages;
import com.haitao55.spider.crawler.utils.HttpUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年4月18日 上午10:54:20  
 */
public class LinkUrls {
	private static final String URL = "http://www.pixiemarket.com";
	private static final int TIME_OUT = 30*1000;
	private static final int RETRY = 1;
	
	public static void main(String[] args) {
		String content = HttpUtils.get(URL, TIME_OUT, RETRY, null);
		if(StringUtils.isNotBlank(content)){
			Document document = Jsoup.parse(content, URL);
			Elements elements = document.select("#header-nav #nav ol.nav-primary>li>a");
			for(Element e : elements){
				System.out.println(e.absUrl("href"));
			}
			System.out.println(elements.size());
		}
	}
}
