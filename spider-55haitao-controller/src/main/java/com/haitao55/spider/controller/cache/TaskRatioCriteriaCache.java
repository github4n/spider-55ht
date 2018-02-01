package com.haitao55.spider.controller.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * 功能：这是一个单例类,维护着所有任务的抓取速率控制信息
 * 
 * @author Arthur.Liu
 * @time 2016年7月29日 下午5:22:26
 * @version 1.0
 */
public class TaskRatioCriteriaCache extends ConcurrentHashMap<Long, TaskRatioCriteria> {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private TaskRatioCriteriaCache() {
		// 私有化构造方法
	}

	private static class TaskRatioCriteriaCacheHolder {
		private static TaskRatioCriteriaCache instance = new TaskRatioCriteriaCache();
	}

	public static TaskRatioCriteriaCache getInstance() {
		return TaskRatioCriteriaCacheHolder.instance;
	}
}