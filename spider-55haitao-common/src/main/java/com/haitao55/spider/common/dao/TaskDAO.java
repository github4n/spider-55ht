package com.haitao55.spider.common.dao;

import java.util.List;
import java.util.Map;

import com.haitao55.spider.common.dos.TaskDO;

/**
 * 
 * 功能：任务管理DAO接口
 * 
 * @author Arthur.Liu
 * @time 2016年7月4日 下午5:42:05
 * @version 1.0
 */
public interface TaskDAO {
	/** updateStatus方法要用到的Map类型参数的key:taskId */
	public static final String UPDATE_STATUS_PARAMS_TASKID = "taskId";
	/** updateStatus方法要用到的Map类型参数的key:newStatus */
	public static final String UPDATE_STATUS_PARAMS_NEWSTATUS = "newStatus";
	/** updateStatus方法要用到的Map类型参数的key:oldStatus */
	public static final String UPDATE_STATUS_PARAMS_OLDSTATUS = "oldStatus";

	/**
	 * 根据ID获取任务实体对象
	 * 
	 * @param id
	 * @return
	 */
	public TaskDO getTaskById(String id);

	/**
	 * 插入一条task记录
	 * 
	 * @param taskDO
	 */
	public void insert(TaskDO taskDO);

	/**
	 * 更新一条task记录
	 * 
	 * @param taskDO
	 */
	public void update(TaskDO taskDO);

	/**
	 * 更新任务记录的状态字段值
	 * 
	 * @param params
	 *            多个参数的封装
	 */
	public void updateStatus(Map<String, String> params);

	/**
	 * 获取所有任务记录
	 * 
	 * @return
	 */
	public List<TaskDO> getAllTasks();

	/**
	 * 根据任务状态,查询任务记录
	 * 
	 * @param status
	 *            任务状态
	 * @return 符合任务状态条件的任务记录列表
	 */
	public List<TaskDO> queryByStatus(String status);

	/**
	 * 根据任务类型,查询任务记录
	 * 
	 * @param Type
	 *            任务类型
	 * @return 符合任务类型条件的任务记录列表
	 */
	public List<TaskDO> queryByType(String type);

	/**
	 * 任务状态转换
	 * @param setParams
	 */
	public void switchStatus(Map<String, String> setParams);
}