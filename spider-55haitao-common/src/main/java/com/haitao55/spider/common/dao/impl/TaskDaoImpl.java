package com.haitao55.spider.common.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.haitao55.spider.common.dao.TaskDAO;
import com.haitao55.spider.common.dao.impl.mysql.TaskMapper;
import com.haitao55.spider.common.dos.TaskDO;

@Repository("taskDAO")
public class TaskDaoImpl implements TaskDAO{
	
	@Autowired
	private TaskMapper taskMapper;
	
	@Override
	public TaskDO getTaskById(String id) {
		return taskMapper.getTaskById(id);
	}

	@Override
	public void insert(TaskDO taskDO) {
		taskMapper.insert(taskDO);
	}

	@Override
	public void update(TaskDO taskDO) {
		taskMapper.update(taskDO);
	}

	@Override
	public void updateStatus(Map<String, String> params) {
		taskMapper.updateStatus(params);
	}

	@Override
	public List<TaskDO> getAllTasks() {
		return taskMapper.getAllTasks();
	}

	@Override
	public List<TaskDO> queryByStatus(String status) {
		return taskMapper.queryByStatus(status);
	}

	@Override
	public List<TaskDO> queryByType(String type) {
		return taskMapper.queryByType(type);
	}

	@Override
	public void switchStatus(Map<String, String> setParams) {
		taskMapper.switchStatus(setParams);
	}

}
