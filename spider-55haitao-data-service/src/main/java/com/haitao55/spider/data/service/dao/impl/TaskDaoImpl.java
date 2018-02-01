package com.haitao55.spider.data.service.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.haitao55.spider.data.service.dao.TaskDAO;
import com.haitao55.spider.data.service.dao.impl.mysql.TaskMapper;
import com.haitao55.spider.data.service.dos.TaskDO;

@Repository("taskDAO")
public class TaskDaoImpl implements TaskDAO{
	
	@Autowired
	private TaskMapper taskMapper;
	

	@Override
	public List<TaskDO> getAllTasks() {
		return taskMapper.getAllTasks();
	}

}
