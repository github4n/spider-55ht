package com.haitao55.spider.common.dao.impl.mysql;

import org.apache.ibatis.annotations.Param;

import com.haitao55.spider.common.dos.RealtimeStatisticsDO;
import com.haitao55.spider.common.util.MyMapper;
/**
 * 实时核价统计　mapper
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月19日 下午4:09:56
* @version 1.0
 */
public interface RealtimeStatisticsMapper extends MyMapper<RealtimeStatisticsDO> {

	/**
	 * 根据taskId 和 realtimeTime查询记录
	 * @param taskId
	 * @param realtimeTime
	 * @return
	 */
	RealtimeStatisticsDO findByPrimaryKey(@Param(value="taskId")Long taskId, @Param(value="realtimeTime")String realtimeTime);

}
