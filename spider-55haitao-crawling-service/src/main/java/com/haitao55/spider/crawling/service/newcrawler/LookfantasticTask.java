package com.haitao55.spider.crawling.service.newcrawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年5月4日 下午6:06:28  
 */
public class LookfantasticTask extends BaseNewItemCrawler implements Runnable{

	@Override
	public void run() {
		List<String> linkUrls = new ArrayList<>();
		List<String> hotUrls = new ArrayList<>();
		String baseUrl = "https://www.lookfantastic.com/health-beauty/new/new-in.list?sortOrder=releaseDate";
		String itemUrlCss = "div#divSearchResults a:has(img)";
		
		this.getUrls(baseUrl, itemUrlCss, hotUrls);
		logger.info("本次收集到{}条详情Url", hotUrls.size());
		
		//3.通过实时核价接口爬取
		hotUrls.forEach(url -> {
			this.executeRealTime(url);
			try {
				Thread.sleep(8000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
