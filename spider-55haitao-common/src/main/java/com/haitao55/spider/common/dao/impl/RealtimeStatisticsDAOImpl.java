package com.haitao55.spider.common.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.haitao55.spider.common.dao.RealtimeStatisticsDAO;
import com.haitao55.spider.common.dao.impl.mysql.RealtimeStatisticsMapper;
import com.haitao55.spider.common.dos.RealtimeStatisticsDO;

/**
 * RealtimeStatisticsDAO 实现类
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月19日 下午2:49:51
* @version 1.0
 */
@Repository("realtimeStatisticsDAO")
public class RealtimeStatisticsDAOImpl implements RealtimeStatisticsDAO {
	@Autowired
	private RealtimeStatisticsMapper realtimeStatisticsMapper;

	@Override
	public RealtimeStatisticsDO findByPrimaryKey(Long taskId, String realtimeTime) {
		return realtimeStatisticsMapper.findByPrimaryKey(taskId,realtimeTime);
	}

}
