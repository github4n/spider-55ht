package com.haitao55.spider.chart.service;

import java.util.List;

import com.haitao55.spider.chart.entity.FullData;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年6月1日 上午11:01:37  
 */
public interface FullDataService {
	int save(FullData record);
	int saveList(List<FullData> list);
	List<FullData> getByCondition(FullData condition);
}
