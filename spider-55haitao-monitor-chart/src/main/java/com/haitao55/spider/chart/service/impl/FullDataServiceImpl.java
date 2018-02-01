package com.haitao55.spider.chart.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.haitao55.spider.chart.dao.FullDataDao;
import com.haitao55.spider.chart.entity.FullData;
import com.haitao55.spider.chart.service.FullDataService;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年6月1日 上午11:04:36  
 */
@Service("fullDataService")
public class FullDataServiceImpl implements FullDataService{
	
	@Autowired
	private FullDataDao dao;

	@Override
	public int save(FullData record) {
		return dao.insert(record);
	}

	@Override
	public int saveList(List<FullData> list) {
		return dao.insertList(list);
	}

	@Override
	public List<FullData> getByCondition(FullData condition) {
		return dao.select(condition);
	}

}
