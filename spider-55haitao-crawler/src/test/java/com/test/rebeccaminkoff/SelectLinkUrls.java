package com.test.rebeccaminkoff;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.HttpUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月15日 下午5:08:21  
 */
public class SelectLinkUrls {
	private static final String URL = "http://www.rebeccaminkoff.com/handbags/love-collection?limit=all";
	private static final int TIME_OUT = 30*1000;
	private static final int RETRY = 1;
	
	public static void main(String[] args){
//		Proxy proxy = new Proxy(){{
//			setId(System.currentTimeMillis());
//			setIp("104.196.239.157");
//			setPort(3128);
//			setRegionId("GG");
//			setRegionName("谷歌云");
//		}};
		String content = HttpUtils.get(URL, TIME_OUT, RETRY, null);
		if(StringUtils.isNotBlank(content)){
			Document document = Jsoup.parse(content, URL);
			Elements elements = document.select("div.product-info h2.product-name>a");
			for(Element e : elements){
				System.out.println(e.absUrl("href"));
			}
			System.out.println(elements.size());
		}
	}
}
