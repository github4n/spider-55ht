package com.haitao55.spider.realtime.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.haitao55.spider.common.dao.TaskDAO;
import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.realtime.service.TaskService;

/**
 * 
 * 功能：实时抓取(RealtimeCrawler)模块范围内使用的任务服务接口实现类
 * 
 * @author Arthur.Liu
 * @time 2016年9月7日 下午5:42:22
 * @version 1.0
 */
@Service("taskService")
public class TaskServiceImpl implements TaskService {
	private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

	private TaskDAO taskDAO;

	@Override
	public Map<Long, TaskDO> getAllTasks() {
		Map<Long, TaskDO> rst = new HashMap<Long, TaskDO>();

		List<TaskDO> taskDOList = this.taskDAO.getAllTasks();
		if (CollectionUtils.isEmpty(taskDOList)) {
			logger.warn("got no TaskDO from db!");
			return rst;
		}

		for (TaskDO taskDO : taskDOList) {
			rst.put(taskDO.getId(), taskDO);
		}

		return rst;
	}

	public TaskDAO getTaskDAO() {
		return taskDAO;
	}

	public void setTaskDAO(TaskDAO taskDAO) {
		this.taskDAO = taskDAO;
	}
}