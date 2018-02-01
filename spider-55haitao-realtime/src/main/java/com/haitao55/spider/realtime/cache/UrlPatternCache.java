package com.haitao55.spider.realtime.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 
 * Title: Description: url 对应 Pattern, Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2016年11月23日 下午12:06:12
 * @version 1.0
 */
public class UrlPatternCache extends ConcurrentHashMap<String, Pattern> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UrlPatternCache() {
	}

	// static inner class
	private static class UrlPatternCacheHolder {
		private static UrlPatternCache urlPattern = new UrlPatternCache();
	}

	public static UrlPatternCache getInstance() {
		return UrlPatternCacheHolder.urlPattern;
	}

}
