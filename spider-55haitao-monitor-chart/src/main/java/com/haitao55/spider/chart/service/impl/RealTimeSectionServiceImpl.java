package com.haitao55.spider.chart.service.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.haitao55.spider.chart.dao.RealTimeSectionDao;
import com.haitao55.spider.chart.entity.RealTimeSection;
import com.haitao55.spider.chart.service.RealTimeSectionService;

import tk.mybatis.mapper.entity.Condition;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年5月31日 下午4:52:56  
 */
@Service("realTimeSectionService")
public class RealTimeSectionServiceImpl implements RealTimeSectionService{
	
	@Autowired
	private RealTimeSectionDao dao;

	@Override
	public int save(RealTimeSection record) {
		return dao.insert(record);
	}

	@Override
	public List<RealTimeSection> getByCondition(RealTimeSection record) {
		return dao.select(record);
	}

	@Override
	public int saveList(List<RealTimeSection> list) {
		return dao.insertList(list);
	}

	@Override
	public int cleanData() {
		Condition condition = new Condition(RealTimeSection.class);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);//得到一个月前的时间
		condition.createCriteria().andLessThanOrEqualTo("currentTime", DateFormatUtils.format(cal, "yyyy-MM-dd HH:mm"));
		return dao.deleteByExample(condition);
	}

	@Override
	public List<RealTimeSection> getByDuring(String start, String end) {
		Map<String, String> params = new HashMap<>();
		params.put("startTime", start);
		params.put("endTime", end);
		return dao.selectByTime(params);
	}

}
