package com.haitao55.spider.common.dao.impl.mysql;

import java.util.List;
import java.util.Map;

import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.util.MyMapper;

/**
 * 
 * 功能：任务管理模块的DAO接口的MyBatis实现类
 * 
 * @author Arthur.Liu
 * @time 2016年7月3日 下午5:18:48
 * @version 1.0
 */
public interface TaskMapper extends MyMapper<TaskDO>{

	TaskDO getTaskById(String id);

	int insert(TaskDO taskDO);

	void update(TaskDO taskDO);

	void updateStatus(Map<String, String> params);

	List<TaskDO> queryByStatus(String status);

	List<TaskDO> getAllTasks();

	List<TaskDO> queryByType(String type);
	
	void switchStatus(Map<String, String> params);

}