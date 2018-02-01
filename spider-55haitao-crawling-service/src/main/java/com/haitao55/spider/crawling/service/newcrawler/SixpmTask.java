package com.haitao55.spider.crawling.service.newcrawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年5月4日 下午5:59:21  
 */
public class SixpmTask extends BaseNewItemCrawler implements Runnable{

	@Override
	public void run() {
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

}
