package com.haitao55.spider.chart.service;

import java.util.List;

import com.haitao55.spider.chart.entity.RealTimeSection;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年5月31日 下午4:50:39  
 */
public interface RealTimeSectionService {
	int save(RealTimeSection record);
	int saveList(List<RealTimeSection> list);
	List<RealTimeSection> getByCondition(RealTimeSection record);
	List<RealTimeSection> getByDuring(String start, String end);
	/**
	 * 清理一个月以前的数据，防止数据太多
	 * @return
	 */
	int cleanData();
}
