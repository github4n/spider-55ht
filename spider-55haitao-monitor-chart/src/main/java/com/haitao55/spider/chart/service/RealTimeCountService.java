package com.haitao55.spider.chart.service;

import java.util.Date;
import java.util.List;

import com.haitao55.spider.chart.entity.RealTimeCount;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年5月31日 下午1:59:06  
 */
public interface RealTimeCountService {
	int save(RealTimeCount record);
	List<RealTimeCount> getAll();
	List<RealTimeCount> getCurrentDayDate();
	List<RealTimeCount> getNewestRecord();
	List<RealTimeCount> getByTime(Date startDate, Date endTime);
}
