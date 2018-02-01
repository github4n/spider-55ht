package com.haitao55.spider.chart.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.haitao55.spider.chart.dao.RealTimeCountDao;
import com.haitao55.spider.chart.entity.RealTimeCount;
import com.haitao55.spider.chart.service.RealTimeCountService;

import tk.mybatis.mapper.entity.Condition;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年5月31日 下午2:00:14  
 */
@Service("realTimeCountService")
public class RealTimeCountServiceImpl implements RealTimeCountService{

	@Autowired
	private RealTimeCountDao realTimeCountDao;

	@Override
	public int save(RealTimeCount record) {
		return realTimeCountDao.insert(record);
	}

	@Override
	public List<RealTimeCount> getAll() {
		return realTimeCountDao.selectAll();
	}

	@Override
	public List<RealTimeCount> getCurrentDayDate() {
		Condition condition = new Condition(RealTimeCount.class);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			//获取当天数据
//			condition.createCriteria().andBetween("currentTime", sdf.parse(DateFormatUtils.format(new Date(), "yyyy-MM-dd 00:00:00")), sdf.parse(DateFormatUtils.format(new Date(), "yyyy-MM-dd 23:59:59")));
			//后来需求改为获取前24小时的数据
			condition.createCriteria().andBetween("currentTime", new Date(new Date().getTime()-1000*60*60*24), new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return realTimeCountDao.selectByExample(condition);
	}
	
	@Override
	public List<RealTimeCount> getNewestRecord() {
		Condition condition = new Condition(RealTimeCount.class);
		condition.createCriteria().andCondition("execute_time = (SELECT MAX(execute_time) FROM statistics_realtime_times)");
		return realTimeCountDao.selectByExample(condition);
	}
	
	@Override
	public List<RealTimeCount> getByTime(Date startDate, Date endTime) {
		Condition condition = new Condition(RealTimeCount.class);
		condition.createCriteria().andBetween("currentTime", startDate, endTime);
		return realTimeCountDao.selectByExample(condition);
	}

}
