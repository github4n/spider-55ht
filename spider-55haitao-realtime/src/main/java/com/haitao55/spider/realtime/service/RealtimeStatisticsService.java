package com.haitao55.spider.realtime.service;

import com.haitao55.spider.common.dos.RealtimeStatisticsDO;
import com.haitao55.spider.common.service.IService;

/**
 * RealtimeStatistics Service 
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月19日 下午2:22:30
* @version 1.0
 */
public interface RealtimeStatisticsService extends IService<RealtimeStatisticsDO> {

	/**
	 * save or update
	 * @param realtimeStatisticsDO
	 */
	void saveOrUpdate(RealtimeStatisticsDO realtimeStatisticsDO);

}
