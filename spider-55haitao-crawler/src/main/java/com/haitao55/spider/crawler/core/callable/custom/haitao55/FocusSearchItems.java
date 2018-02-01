package com.haitao55.spider.crawler.core.callable.custom.haitao55;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.common.service.impl.RedisService;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.SpringUtils;

public class FocusSearchItems extends AbstractSelect {
	private int length;
	private static final String SEARCH_KEY = "search";
	private static final String SERVICE_URL = "http://10.25.169.237/spider-55haitao-realtime/realtime-crawler/pricing.action";
//	 private static final String SERVICE_URL =
//	 "http://118.178.57.197:8888/spider-55haitao-realtime/realtime-crawler/pricing.action";
	private long CRAWLING_WAIT_TIME = 60 * 1000;// 在线抓取最多等待时间
	// 预核价标识
	private static final String PRERELEASE_REALTIME = "prelease";
	private static final Map<String, String> post = new HashMap<String, String>();
	private static RedisService redisService;
	static {
		redisService = SpringUtils.getBean("redisService");
	}

	@Override
	public void invoke(Context context) throws Exception {
		String url = StringUtils.EMPTY;
		for (int i = 0; i < length; i++) {
			url = redisService.lpop(SEARCH_KEY);
			post.put("url", url);
			post.put("request_from", PRERELEASE_REALTIME);
			post.put("timeout", CRAWLING_WAIT_TIME + "");
			HttpClientUtil.post(SERVICE_URL, post);
			Thread.sleep(800);
		}
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
}
