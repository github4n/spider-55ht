/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HttpTest.java 
 * @Prject: spider-55haitao-crawler
 * @Package: com.test.pm6 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月29日 下午2:36:04 
 * @version: V1.0   
 */
package com.test.escentual;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.utils.HttpUtils;

/** 
 * @ClassName: HttpTest 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年9月29日 下午2:36:04  
 */
public class HttpTest {
	
	public static void main(String[] args) throws IOException {
//		Proxy proxy = new Proxy();
//		proxy.setIp("10.47.90.213");
//		proxy.setPort(3128);
		String content = HttpUtils.get("http://www.escentual.com/fragrance/all-fragrance/yslmonparis001/", 30000, 1, null);
		Document document = Jsoup.parse(content);
//		FileWriter fw = new FileWriter("/home/zhoushuo/test1");
//		fw.write(content);
//		fw.flush();
//		fw.close();
		float nowPrice = 0f;
		float oldPrice = 0f;
		String unit = "";
		String stock = "";
		Elements es = document.select("table#super-product-table tbody>tr");
		for(Element e : es){
			String style_switch_img = e.child(0).getElementsByTag("img").get(0).absUrl("src");
			String size = e.child(1).ownText();
			String skuid = e.child(1).getElementsByAttributeValue("class", "product-code").get(0).ownText().replace("Item#", "").trim();
			Elements tempNowPrice = e.child(2).getElementsByAttributeValue("class", "special-price");
			if(tempNowPrice != null && tempNowPrice.size()>0){
				tempNowPrice = tempNowPrice.get(0).getElementsByAttributeValue("class", "price");
				if(tempNowPrice != null && tempNowPrice.size()>0){
					Map<String, Object> map = formatPrice(tempNowPrice.get(0).ownText().trim());
					nowPrice = (float) map.get("price");
					unit = map.get("unit").toString();
					System.out.println(unit);
				}
			}
			Elements tempOldPrice = e.child(2).getElementsByAttributeValue("class", "old-price");
			if(tempOldPrice != null && tempOldPrice.size()>0){
				tempOldPrice = tempOldPrice.get(0).getElementsByAttributeValue("class", "price");
				if(tempOldPrice != null && tempOldPrice.size()>0){
					Map<String, Object> map = formatPrice(tempOldPrice.get(0).ownText().trim());
					oldPrice = (float) map.get("price");
					unit = map.get("unit").toString();
					System.out.println(unit);
				}
			}
			Elements stocks = e.child(4).getElementsByAttributeValue("class", "stock-status-main");
			if(stocks!=null && stocks.size()>0){
				stock = stocks.get(0).ownText().trim();
			}
			System.out.println(e.text());
			System.out.println(style_switch_img);
			System.out.println(size);
			System.out.println(skuid);
			System.out.println(nowPrice);
			System.out.println(oldPrice);
			System.out.println(stock);
		}
//		Elements eps = document.select("table#super-product-table tbody>tr:nth-child(1) td:nth-child(5) span.stock-status-main");
//		for(Element e : eps){
//			System.out.println(e.text());
//		}
		
//		String res = HttpUtils.get("http://118.178.57.197:8080/spider-55haitao-ui/httpproxy/get.action?url=http://www.finishline.com");
//		String res = HttpUtils.get("http://118.178.57.197:8080/spider-55haitao-ui/httpproxy/get.action?url='https://www.amazon.de/Running-Bekleidung/b/ref=sn_gfs_co_sport_508387031_1?ie=UTF8&node=508387031&pf_rd_p=692336507&pf_rd_r=0GEF3RQRN49FEYM4DMWQ&pf_rd_s=sport-subnav-flyout-content-8&pf_rd_t=SubnavFlyout'");
//		String res = HttpUtils.get("http://118.178.57.197:8080/spider-55haitao-ui/httpproxy/get.action?url=http://www.cnblogs.com");
//		String res = HttpUtils.get("http://localhost:8080/spider-55haitao-ui/httpproxy/get.action?url=http://www.sina.com.cn/");
//		System.out.println(res);
		
	}
	
	private static Map<String, Object> formatPrice(String tempPrice){
		Map<String, Object> map = new HashMap<>();
		String currency = StringUtils.substring(tempPrice, 0, 1);
		String unit = Currency.codeOf(currency).name();
		tempPrice = StringUtils.substring(tempPrice, 1);
		map.put("unit", unit);
		try {
			float price = Float.parseFloat(tempPrice);
			map.put("price", price);
		} catch (NumberFormatException e) {
//			logger.error("Format Price Error.", e);
		}
		return map;
	}
}
