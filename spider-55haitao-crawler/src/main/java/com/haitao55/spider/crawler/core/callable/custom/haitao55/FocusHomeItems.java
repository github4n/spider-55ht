package com.haitao55.spider.crawler.core.callable.custom.haitao55;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.haitao55.spider.common.service.impl.RedisService;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.SpringUtils;

public class FocusHomeItems extends AbstractSelect {
	private static final String HOME_KEY = "home";
	private static final String SERVICE_URL = "http://10.25.169.237/spider-55haitao-realtime/realtime-crawler/pricing.action";
//	private static final String SERVICE_URL = "http://118.178.57.197:8888/spider-55haitao-realtime/realtime-crawler/pricing.action";
	private long CRAWLING_WAIT_TIME = 60 * 1000;// 在线抓取最多等待时间
	// 预核价标识
	private static final String PRERELEASE_REALTIME = "prelease";
	private static final Map<String,String> post = new HashMap<String,String>();
	private static RedisService redisService;
	static{
		redisService = SpringUtils.getBean("redisService");
	}

	@Override
	public void invoke(Context context) throws Exception {
		Set<String> urls = redisService.smembers(HOME_KEY);
		if(urls.size()>0){
			Iterator<String> iterator = urls.iterator();
			while(iterator.hasNext()){
				post.put("url", iterator.next());
				post.put("request_from", PRERELEASE_REALTIME);
				post.put("timeout", CRAWLING_WAIT_TIME+"");
				HttpClientUtil.post(SERVICE_URL, post);
				Thread.sleep(3000);
			}
		}
	}

}
