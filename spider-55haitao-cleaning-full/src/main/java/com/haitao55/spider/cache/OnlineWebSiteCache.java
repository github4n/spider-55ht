package com.haitao55.spider.cache;


import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * 功能：在本地缓存一份所有任务的配置信息，定时更新
 * 
 * @author denghuan
 * @time 2017年8月14日 下午2:48:58
 * @version 1.0
 */
public class OnlineWebSiteCache extends HashMap<String,String> {
	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = 8864914715131655370L;

	private static class Holder {// 使用这种方式实现单例类，就最安全了
		public static OnlineWebSiteCache cache = new OnlineWebSiteCache();
	}

	private OnlineWebSiteCache() {}

	public static OnlineWebSiteCache getInstance() {
		return Holder.cache;
	}
}