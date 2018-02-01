package com.haitao55.spider.crawler.common.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.haitao55.spider.crawler.core.model.Task;

/**
 * 
 * 功能：在本地缓存一份所有任务的配置信息，定时更新
 * 
 * @author Arthur.Liu
 * @time 2016年8月2日 下午2:48:58
 * @version 1.0
 */
public class TaskCache extends ConcurrentHashMap<Long, Task> {
	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = 8864914715131655370L;

	private static class Holder {// 使用这种方式实现单例类，就最安全了
		public static TaskCache cache = new TaskCache();
	}

	private TaskCache() {

	}

	public static TaskCache getInstance() {
		return Holder.cache;
	}
}