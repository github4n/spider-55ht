package com.haitao55.spider.crawling.service.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.common.utils.JsoupUtils;

/** 
 * @Description: 通过核价接口抓取新上架的商品数据
 * @author: zhoushuo
 * @date: 2017年5月3日 下午5:19:47  
 */
@Component
public class NewCrawler {
	
	private static final Logger logger = LoggerFactory.getLogger("newCrawler");
	
	@Value("#{configProperties['realtime.address']}")
	private String REALTIME_API;
	
	private static int DEFAULT_TIME_OUT = 3000;
	private static int DEFAULT_RETRY = 3;
	
	//6pm新品抓取
	public void sixpmNewCrawler(){
		List<String> linkUrls = new ArrayList<>();
		List<String> hotUrls = new ArrayList<>();
		String baseUrl = "http://www.6pm.com";
		String linkUrlCss = "div.header-sub-nav div.lists ul li>h5>a";
		String itemUrlCss = "div#searchResults a";
		
		//1.获取新品第一级url
		this.getUrls(baseUrl, linkUrlCss, linkUrls);
		
		//2.根据第一级url迭代详情url
		if(CollectionUtils.isEmpty(linkUrls)){
			logger.error("linkUrls is empty. and url is {}", baseUrl);
			return;
		}
		
		logger.info("本次收集到{}条LinkUrl", linkUrls.size());
		
		linkUrls.forEach(url -> this.getUrls(url, itemUrlCss, hotUrls));
		
		logger.info("本次收集到{}条详情Url", hotUrls.size());
		
		//3.通过实时核价接口爬取
		hotUrls.forEach(url -> {
			this.executeRealTime(url);
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
	}
	
	//zappos新品抓取
	public void zapposNewCrawler(){
		List<String> linkUrls = new ArrayList<>();
		List<String> hotUrls = new ArrayList<>();
		String baseUrl = "http://www.zappos.com/new-arrivals";
		String linkUrlCss = "div.catNav h4.z-hd-beanie>a";
		String itemUrlCss = "div#searchResults a";
		
		//1.获取新品第一级url
		this.getUrls(baseUrl, linkUrlCss, linkUrls);
		
		//2.根据第一级url迭代详情url
		if(CollectionUtils.isEmpty(linkUrls)){
			logger.error("linkUrls is empty. and url is {}", baseUrl);
			return;
		}
		
		logger.info("本次收集到{}条LinkUrl", linkUrls.size());
		
		linkUrls.forEach(url -> this.getUrls(url, itemUrlCss, hotUrls));
		
		logger.info("本次收集到{}条详情Url", hotUrls.size());
		
		//3.通过实时核价接口爬取
		hotUrls.forEach(url -> {
			this.executeRealTime(url);
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
	}
	
	private List<String> getUrls(String url, String css, List<String> urls){
		String content = null;
		try {
			content = Crawler.create().timeOut(DEFAULT_TIME_OUT).retry(DEFAULT_RETRY).url(url).resultAsString();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("http request get error by url {}", url);
			return urls;
		}
		Document doc = JsoupUtils.parse(content, url);
		Elements elements = doc.select(css);
		if(CollectionUtils.isNotEmpty(elements)){
			for(Element e : elements){
				urls.add(e.absUrl("href"));
			}
		}
		return urls;
	}
	
	private void executeRealTime(String url){
	    logger.info("===================start==================");
		Map<String, String> params = new HashMap<>();
		params.put("url", url);
		params.put("request_from", "firstpageenhance");
		long start = System.currentTimeMillis();
		String result = HttpClientUtil.post(REALTIME_API, params);
		long end = System.currentTimeMillis();
		logger.info("url:{},time:{}ms,result:{}",url, (end-start), result);
		logger.info("====================end===================");
	}
	
	

	public static void main(String[] args){
		NewCrawler test = new NewCrawler();
		test.zapposNewCrawler();
	}
}
