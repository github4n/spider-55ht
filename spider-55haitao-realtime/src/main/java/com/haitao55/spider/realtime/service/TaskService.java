package com.haitao55.spider.realtime.service;

import java.util.Map;

import com.haitao55.spider.common.dos.TaskDO;

/**
 * 
 * 功能：实时抓取(RealtimeCrawler)模块范围内使用的任务服务接口类
 * 
 * @author Arthur.Liu
 * @time 2016年9月7日 下午5:40:31
 * @version 1.0
 */
public interface TaskService {

	/**
	 * 获取所有任务数据信息
	 * 
	 * @return
	 */
	public Map<Long, TaskDO> getAllTasks();
}