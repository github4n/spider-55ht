package com.test.ralphlauren;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.crawler.utils.HttpUtils;

/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: RalphlaurenLinkTest.java 
 * @Prject: spider-55haitao-crawler
 * @Package:  
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年11月3日 上午11:18:49 
 * @version: V1.0   
 */

/** 
 * @ClassName: RalphlaurenLinkTest 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年11月3日 上午11:18:49  
 */
public class RalphlaurenTopLinkTest {
//	private static final String URL = "http://www.ralphlauren.com/home/index.jsp?geos=1";
	private static final String URL = "http://www.ralphlauren.com/family/index.jsp?categoryId=72871326";
	private static final int TIME_OUT = 30*1000;
	private static final int RETRY = 1;
	
	public static void main(String[] args){
		String content = HttpUtils.get(URL, TIME_OUT, RETRY, null);
		if(StringUtils.isNotBlank(content)){
			Document document = Jsoup.parse(content, URL);
//			Elements elements = document.select("ul#global-nav>li>a");
			Elements elements = document.select("ol.products>li.product>div.product-photo a.photo");
			for(Element e : elements){
				System.out.println(e.absUrl("href"));
			}
			System.out.println(elements.size());
		}
	}
}
