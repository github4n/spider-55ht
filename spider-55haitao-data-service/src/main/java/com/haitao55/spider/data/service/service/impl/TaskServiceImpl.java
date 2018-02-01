package com.haitao55.spider.data.service.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.haitao55.spider.data.service.dao.TaskDAO;
import com.haitao55.spider.data.service.dos.TaskDO;
import com.haitao55.spider.data.service.service.TaskService;
import com.haitao55.spider.data.service.utils.ConvertPageInstance;
import com.haitao55.spider.data.service.view.TaskView;

/**
 * 
 * 功能：任务管理service接口实现类
 * 
 * @author denghuan
 * @time 2017年4月19日 上午11:03:00
 * @version 1.0
 */
@Service("taskService")
public class TaskServiceImpl  implements TaskService {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

	private TaskDAO taskDAO;
	

	@Autowired
	public TaskDAO getTaskDAO() {
		return taskDAO;
	}

	public void setTaskDAO(TaskDAO taskDAO) {
		this.taskDAO = taskDAO;
	}

	private TaskView convertDO2View(TaskDO taskDO) {
		TaskView taskView = new TaskView();
		taskView.setId(taskDO.getId());
		taskView.setName(taskDO.getName());
		taskView.setDescription(taskDO.getDescription());
		taskView.setDomain(taskDO.getDomain());
		taskView.setInitUrl(taskDO.getInitUrl());
		taskView.setType(taskDO.getType().toString());
		taskView.setStatus(taskDO.getStatus().toString());
		taskView.setPeriod(taskDO.getPeriod());
		taskView.setWinStart(taskDO.getWinStart());
		taskView.setWinEnd(taskDO.getWinEnd());
		taskView.setCreateTime(taskDO.getCreateTime());
		taskView.setUpdateTime(taskDO.getUpdateTime());
		taskView.setMaster(taskDO.getMaster());
		taskView.setUpdateOnly(taskDO.getUpdateOnly().toString());
		taskView.setConfig(taskDO.getConfig());
		taskView.setRatio(taskDO.getRatio());
		taskView.setWeight(taskDO.getWeight());
		taskView.setSiteRegion(taskDO.getSiteRegion());
		taskView.setProxyRegionId(taskDO.getProxyRegionId());
		taskView.setPretreatConfig(taskDO.getPretreatConfig());
		return taskView;
	}
	
	@Override
	public List<TaskView> queryAllTasks(int page, int pageSize) {
		PageHelper.startPage(page, pageSize);
		List<TaskDO> allTaskDOs = this.taskDAO.getAllTasks();
		Page<TaskDO> p = (Page<TaskDO>) allTaskDOs;
		Page<TaskView> pv = new Page<TaskView>();
		ConvertPageInstance.convert(p, pv);
		if (allTaskDOs != null) {
			for (TaskDO taskDO : allTaskDOs) {
				TaskView taskView = this.convertDO2View(taskDO);
				pv.add(taskView);
			}
		}
		
		return pv;
	}
}