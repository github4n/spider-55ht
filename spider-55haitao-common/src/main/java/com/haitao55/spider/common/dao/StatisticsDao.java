/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: StatisticsDao.java 
 * @Prject: spider-55haitao-common
 * @Package: com.haitao55.spider.common.dao 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月7日 下午2:26:10 
 * @version: V1.0   
 */
package com.haitao55.spider.common.dao;

import java.util.List;
import java.util.Map;

import com.haitao55.spider.common.dos.StatisticsDO;

/** 
 * @ClassName: StatisticsDao 
 * @Description: 统计管理Dao层接口
 * @author: zhoushuo
 * @date: 2016年9月7日 下午2:26:10  
 */
public interface StatisticsDao {
	/**
	 * 按主键查询用到的参数：taskId
	 */
	public static final String COMPOSITE_KEYS_TASKID = "taskId";
	
	/**
	 * 按主键查询用到的参数：startTime
	 */
	public static final String COMPOSITE_KEYS_STARTTIME = "startTime";
	
	/**
	 * @Title: selectLatestStatistics 
	 * @Description: 以任务ID分组，查询开始时间最新的集合
	 * @return: List<StatisticsDO>
	 */
	public List<StatisticsDO> selectLatestStatistics();
	
	/**
	 * 
	 * @Title: insert 
	 * @Description: 保存一条统计记录
	 * @param statisticsDO
	 * @return: int
	 */
	public int insert(StatisticsDO statisticsDO);
	
	/**
	 * 
	 * @Title: selectStatisticsByTaskId 
	 * @Description: 查询指定任务ID的统计记录
	 * @param taskId
	 * @return: List<StatisticsDO>
	 */
	public List<StatisticsDO> selectStatisticsByTaskId(String taskId);
	/**
	 * 
	 * @Title: selectStatisticsByPK 
	 * @Description: 按主键查询，返回唯一结果，注意这里是联合主键
	 * @param params
	 * @return: StatisticsDO
	 */
	public StatisticsDO selectStatisticsByPK(Map<String,String> params);
	
	/**
	 * 
	 * @Title: deleteByTaskId 
	 * @Description: 根据taskId删除相关统计记录
	 * @param taskId
	 * @return: void
	 */
	public void deleteByTaskId(String taskId);
	
	/**
	 * 修改  任务对应统计信息 最近时间  记录
	 * @param statisticsDO
	 */
	public void update(StatisticsDO statisticsDO);
}
