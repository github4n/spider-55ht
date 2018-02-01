package com.haitao55.spider.realtime.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 需要调用realtime service进行核价 urlpattern
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月13日 上午11:06:56
* @version 1.0
 */
public class RealtimeServiceUrlPatternCache extends ConcurrentHashMap<String, Pattern> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private RealtimeServiceUrlPatternCache() {
	}

	// static inner class
	private static class UrlPatternCacheHolder {
		private static RealtimeServiceUrlPatternCache urlPattern = new RealtimeServiceUrlPatternCache();
	}

	public static RealtimeServiceUrlPatternCache getInstance() {
		return UrlPatternCacheHolder.urlPattern;
	}

}
