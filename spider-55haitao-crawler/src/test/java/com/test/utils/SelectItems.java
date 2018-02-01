package com.test.utils;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.crawler.utils.HttpUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月16日 下午3:01:12  
 */
public class SelectItems {
	private static final String URL = "http://www.rebeccaminkoff.com/handbags/love-collection?limit=all";
	private static final int TIME_OUT = 30*1000;
	private static final int RETRY = 1;
	
	public static int getItemCount(String url, String css){
		String content = HttpUtils.get(url, TIME_OUT, RETRY, null);
		if(StringUtils.isNotBlank(content)){
			Document document = Jsoup.parse(content, url);
			Elements elements = document.select(css);
			for(Element e : elements){
				Singleton.INSTANCE.getSet().add(e.absUrl("href"));
			}
			return elements.size();
		}
		return 0;
	}
}
