package com.haitao55.spider.crawler.core.callable.custom.colehaan;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.haitao55.spider.crawler.utils.HttpUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年12月26日 下午3:25:08  
 */
public class ColeHaanCall implements Callable<Map<String,Document>>{
	
	private String url;
	
	public ColeHaanCall(String url) {
		this.url = url;
	}

	@Override
	public Map<String, Document> call() throws Exception {
		Map<String, Document> result = new HashMap<>();
		String content = HttpUtils.get(url, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES, null);
		Document document = Jsoup.parse(content, url);
		result.put(url, document);
		return result;
	}
}
