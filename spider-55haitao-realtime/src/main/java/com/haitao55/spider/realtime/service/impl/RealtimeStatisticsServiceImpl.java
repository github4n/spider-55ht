package com.haitao55.spider.realtime.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.haitao55.spider.common.dao.RealtimeStatisticsDAO;
import com.haitao55.spider.common.dos.RealtimeStatisticsDO;
import com.haitao55.spider.common.service.impl.BaseService;
import com.haitao55.spider.realtime.service.RealtimeStatisticsService;

/**
 * RealtimeStatistics Service 实现类
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月19日 下午2:23:55
* @version 1.0
 */
@Service("realtimeStatisticsService")
public class RealtimeStatisticsServiceImpl extends BaseService<RealtimeStatisticsDO> implements RealtimeStatisticsService {
	
	@Autowired
	private RealtimeStatisticsDAO realtimeStatisticsDAO;
	
	/**
	 * save or update
	 */
	@Override
	public void saveOrUpdate(RealtimeStatisticsDO realtimeStatisticsDO) {
		String realtimeTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		realtimeStatisticsDO.setRealtimeTime(realtimeTime);
		RealtimeStatisticsDO selectByKey = realtimeStatisticsDAO.findByPrimaryKey(realtimeStatisticsDO.getTaskId(),realtimeTime);
		if(null == selectByKey){
			this.save(realtimeStatisticsDO);
		}else{
			int crawler = selectByKey.getCrawler();
			if(0 != realtimeStatisticsDO.getCrawler()){
				crawler = crawler +1;
			}
			int mongo = selectByKey.getMongo();
			if(0 != realtimeStatisticsDO.getMongo()){
				mongo = mongo+1;
			}
			int redis = selectByKey.getRedis();
			if(0 != realtimeStatisticsDO.getRedis()){
				redis = redis+1;
			}
			int exception = selectByKey.getException();
			if(0 != realtimeStatisticsDO.getException()){
				exception = exception+1;
			}
			selectByKey.setCrawler(crawler);
			selectByKey.setMongo(mongo);
			selectByKey.setRedis(redis);
			selectByKey.setException(exception);
			this.updateNotNull(selectByKey);
		}
	}
	
}
