package com.haitao55.spider.crawler.common.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.haitao55.spider.common.dos.TaskDO;

/**
 * 用作本地任务缓存
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月16日 下午8:10:49
* @version 1.0
 */
public class TaskDOCache extends ConcurrentHashMap<Long, TaskDO> {
	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = 8864914715131655370L;

	private static class Holder {// 使用这种方式实现单例类，就最安全了
		public static TaskDOCache cache = new TaskDOCache();
	}

	private TaskDOCache() {

	}

	public static TaskDOCache getInstance() {
		return Holder.cache;
	}
}