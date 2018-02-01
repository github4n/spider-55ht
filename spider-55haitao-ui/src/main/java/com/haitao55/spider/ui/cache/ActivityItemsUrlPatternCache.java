package com.haitao55.spider.ui.cache;

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
public class ActivityItemsUrlPatternCache extends ConcurrentHashMap<String, Pattern> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ActivityItemsUrlPatternCache() {
	}

	// static inner class
	private static class UrlPatternCacheHolder {
		private static ActivityItemsUrlPatternCache urlPattern = new ActivityItemsUrlPatternCache();
	}

	public static ActivityItemsUrlPatternCache getInstance() {
		return UrlPatternCacheHolder.urlPattern;
	}

}
