package com.haitao55.spider.data.service.dao;

import java.util.List;

import com.haitao55.spider.data.service.dos.TaskDO;


/**
 * 
 * 功能：任务管理DAO接口
 * 
 * @author denghuan
 * @time 2017年4月19日 下午5:42:05
 * @version 1.0
 */
public interface TaskDAO {
	/**
	 * 获取所有任务记录
	 * 
	 * @return
	 */
	public List<TaskDO> getAllTasks();

}