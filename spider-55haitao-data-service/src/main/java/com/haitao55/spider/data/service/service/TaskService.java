package com.haitao55.spider.data.service.service;

import java.util.List;
import com.haitao55.spider.data.service.view.TaskView;

/**
 * 
 * 功能：任务管理service接口
 * 
 * @author denghuan
 * @time 2017年4月19日 上午10:59:56
 * @version 1.0
 */
public interface TaskService{

	/**
	 * 查询获取所有任务对象
	 * 
	 * @return
	 */
	public List<TaskView> queryAllTasks(int page,int pageSize);

}