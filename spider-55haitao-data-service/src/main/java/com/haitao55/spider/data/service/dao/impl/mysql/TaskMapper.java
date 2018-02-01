package com.haitao55.spider.data.service.dao.impl.mysql;

import java.util.List;

import com.haitao55.spider.data.service.dos.TaskDO;
import com.haitao55.spider.data.service.utils.MyMapper;


/**
 * 
 * 功能：任务管理模块的DAO接口的MyBatis实现类
 * 
 * @author denghuan
 * @time 2017年4月19日 下午5:18:48
 * @version 1.0
 */
public interface TaskMapper extends MyMapper<TaskDO>{

	List<TaskDO> getAllTasks();

}