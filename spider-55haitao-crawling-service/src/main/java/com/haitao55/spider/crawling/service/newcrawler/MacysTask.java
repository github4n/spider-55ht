package com.haitao55.spider.crawling.service.newcrawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年5月4日 下午6:06:28  
 */
public class MacysTask extends BaseNewItemCrawler implements Runnable{

	@Override
	public void run() {
		
		List<String> hotUrls = new ArrayList<>();
		//新品链接暂未找全，后续补充
		String baseUrl = "https://www.macys.com/shop/womens-clothing/new-womens-clothing/Pageindex,Productsperpage,Sortby/1,All,NEW_ITEMS?id=68514";
		String itemUrlCss = "div#browse_womens_default_product ul>li div.fullColorOverlayOff>a";
		
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
