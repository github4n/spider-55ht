package com.haitao55.spider.data.service.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * 功能：在本地缓存一份所有任务的配置信息，定时更新
 * 
 * @author denghuan
 * @time 2017年4月19日 下午2:48:58
 * @version 1.0
 */
public class WebSiteTaskCache extends ConcurrentHashMap<String,String> {
	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = 8864914715131655370L;

	private static class Holder {// 使用这种方式实现单例类，就最安全了
		public static WebSiteTaskCache cache = new WebSiteTaskCache();
	}

	private WebSiteTaskCache() {

	}

	public static WebSiteTaskCache getInstance() {
		return Holder.cache;
	}
}