/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: StatisticsMapper.java 
 * @Prject: spider-55haitao-common
 * @Package: com.haitao55.spider.common.dao.impl.mysql 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月7日 下午3:15:16 
 * @version: V1.0   
 */
package com.haitao55.spider.common.dao.impl.mysql;

import java.util.List;
import java.util.Map;

import com.haitao55.spider.common.dos.StatisticsDO;
import com.haitao55.spider.common.util.MyMapper;

/** 
 * @ClassName: StatisticsMapper 
 * @Description: 统计管理模块的DAO接口的MyBatis实现类
 * @author: zhoushuo
 * @date: 2016年9月7日 下午3:15:16  
 */
public interface StatisticsMapper extends MyMapper<StatisticsDO>{
	List<StatisticsDO> selectLatestStatistics();
	int insert(StatisticsDO statisticsDO);
	List<StatisticsDO> selectByTaskId(String taskId);
	StatisticsDO selectByTaskIdAndStartTime(Map<String, String> params);
	void deleteByTaskId(String taskId);
	void update(StatisticsDO statisticsDO);
	StatisticsDO selectLatestStatisticsByTaskId(StatisticsDO statisticsDO);
}
