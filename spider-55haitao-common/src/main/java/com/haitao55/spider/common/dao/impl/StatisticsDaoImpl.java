/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: StatisticsDaoImpl.java 
 * @Prject: spider-55haitao-common
 * @Package: com.haitao55.spider.common.dao.impl 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月7日 下午2:58:48 
 * @version: V1.0   
 */
package com.haitao55.spider.common.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.haitao55.spider.common.dao.StatisticsDao;
import com.haitao55.spider.common.dao.impl.mysql.StatisticsMapper;
import com.haitao55.spider.common.dos.StatisticsDO;

/** 
 * @ClassName: StatisticsDaoImpl 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年9月7日 下午2:58:48  
 */
@Repository("StatisticsDao")
public class StatisticsDaoImpl implements StatisticsDao{
	
	@Autowired
	private StatisticsMapper statisticsMapper;

	/* (non Javadoc) 
	 * @Title: insert
	 * @Description: TODO
	 * @param statisticsDO
	 * @return 
	 * @see com.haitao55.spider.common.dao.StatisticsDao#insert(com.haitao55.spider.common.dos.StatisticsDO) 
	 */
	@Override
	public int insert(StatisticsDO statisticsDO) {
		return statisticsMapper.insert(statisticsDO);
	}

	/* (non Javadoc) 
	 * @Title: selectLatestStatistics
	 * @Description: TODO
	 * @return 
	 * @see com.haitao55.spider.common.dao.StatisticsDao#selectLatestStatistics() 
	 */
	@Override
	public List<StatisticsDO> selectLatestStatistics() {
		return statisticsMapper.selectLatestStatistics();
	}

	/* (non Javadoc) 
	 * @Title: selectStatisticsByTaskId
	 * @Description: TODO
	 * @param taskId
	 * @return 
	 * @see com.haitao55.spider.common.dao.StatisticsDao#selectStatisticsByTaskId(java.lang.String) 
	 */
	@Override
	public List<StatisticsDO> selectStatisticsByTaskId(String taskId) {
		return this.statisticsMapper.selectByTaskId(taskId);
	}

	/* (non Javadoc) 
	 * @Title: selectStatisticsByPK
	 * @Description: TODO
	 * @param params
	 * @return 
	 * @see com.haitao55.spider.common.dao.StatisticsDao#selectStatisticsByPK(java.util.Map) 
	 */
	@Override
	public StatisticsDO selectStatisticsByPK(Map<String, String> params) {
		return this.statisticsMapper.selectByTaskIdAndStartTime(params);
	}

	/* (non Javadoc) 
	 * @Title: deleteByTaskId
	 * @Description: TODO
	 * @param taskId 
	 * @see com.haitao55.spider.common.dao.StatisticsDao#deleteByTaskId(java.lang.String) 
	 */
	@Override
	public void deleteByTaskId(String taskId) {
		this.statisticsMapper.deleteByTaskId(taskId);
	}

	
	/**
	 * 在更新时出现了死锁现象, 经研究,现采用主键锁概念,  先查出记录主键,再根据主键索引进行where条件更新
	 */
	@Override
	public void update(StatisticsDO statisticsDO) {
		StatisticsDO statistics=this.statisticsMapper.selectLatestStatisticsByTaskId(statisticsDO);
		if(null==statistics){
			return;
		}
		statisticsDO.setTaskId(statistics.getTaskId());
		statisticsDO.setStartTime(statistics.getStartTime());
		this.statisticsMapper.update(statisticsDO);
	}
}
