package com.test.groupon;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.model.UrlType;
import com.test.utils.GetUrls;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年12月5日 上午11:11:26  
 */
public class GroupOnLinkUrlTest {
	public static void main(String[] args) throws Exception {
//		GetUrls urltest = new GetUrls("https://www.groupon.com/goods", "div#left-nav-categories>ul>li>a", "abs:href", UrlType.LINK.getValue(), 1);
//		List<String> topList = GetUrls.list;
//		for(String url : topList){
//			if(StringUtils.isNotBlank(url)){
//				GetUrls temp = new GetUrls(url, "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
//			}
//		}
		GetUrls temp = new GetUrls("https://www.groupon.com/goods/auto-and-home-improvement", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp2 = new GetUrls("https://www.groupon.com/goods/jewelry-and-watches", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp3 = new GetUrls("https://www.groupon.com/goods/baby-kids-and-toys", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp4 = new GetUrls("https://www.groupon.com/goods/electronics", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp5 = new GetUrls("https://www.groupon.com/occasion/cyber-week", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp6 = new GetUrls("https://www.groupon.com/goods/health-and-beauty", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp7 = new GetUrls("https://www.groupon.com/goods/for-the-home", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp8 = new GetUrls("https://www.groupon.com/goods/sports-and-outdoors", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp9 = new GetUrls("https://www.groupon.com/goods/groceries-household-and-pets", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp10 = new GetUrls("https://www.groupon.com/goods/entertainment-and-media", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp11 = new GetUrls("https://www.groupon.com/goods/mens-clothing-shoes-and-accessories", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
		GetUrls temp12 = new GetUrls("https://www.groupon.com/goods/womens-clothing-shoes-and-accessories", "div#refinement-ui div.refinement-box a", "abs:href", UrlType.LINK.getValue(), 2);
	}
}
