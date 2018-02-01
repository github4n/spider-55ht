package com.haitao55.spider.crawling.service.newcrawler;

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
import org.springframework.stereotype.Component;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.common.utils.JsoupUtils;

/**
 * @Description:
 * @author: zhoushuo
 * @date: 2017年5月4日 下午6:02:12
 */
@Component
public class BaseNewItemCrawler {
	
	protected static final Logger logger = LoggerFactory.getLogger("newCrawler");

//	@Value("#{configProperties['realtime.address']}")
	private String REALTIME_API = "http://114.55.10.105/spider-55haitao-realtime/realtime-crawler/pricing.action";

	protected static int DEFAULT_TIME_OUT = 3000;
	protected static int DEFAULT_RETRY = 3;

	protected List<String> getUrls(String url, String css, List<String> urls) {
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
		if (CollectionUtils.isNotEmpty(elements)) {
			for (Element e : elements) {
				urls.add(e.absUrl("href"));
			}
		}
		return urls;
	}

	protected void executeRealTime(String url) {
		logger.info("===================start==================");
		Map<String, String> params = new HashMap<>();
		params.put("url", url);
		params.put("request_from", "firstpageenhance");
		long start = System.currentTimeMillis();
		String result = HttpClientUtil.post(REALTIME_API, params);
		long end = System.currentTimeMillis();
		logger.info("url:{},time:{}ms,result:{}", url, (end - start), result);
		logger.info("====================end===================");
	}
}
