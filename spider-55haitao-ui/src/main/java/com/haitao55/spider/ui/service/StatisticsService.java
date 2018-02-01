/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: StatisticsService.java 
 * @Prject: spider-55haitao-ui
 * @Package: com.haitao55.spider.ui.service 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月7日 下午4:30:41 
 * @version: V1.0   
 */
package com.haitao55.spider.ui.service;

import java.util.List;

import com.haitao55.spider.common.dos.StatisticsDO;
import com.haitao55.spider.common.service.IService;
import com.haitao55.spider.ui.view.StatisticsView;

/** 
 * @ClassName: StatisticsService 
 * @Description: 统计管理模块Service层接口
 * @author: zhoushuo
 * @date: 2016年9月7日 下午4:30:41  
 */
public interface StatisticsService extends IService<StatisticsDO>{
	public void createStatistics(String taskId);
	public List<StatisticsView> getAllLatestTaskStatistics(int pageNum, int pageSize);
	public List<StatisticsView> getAllStatisticsByTaskId(int pageNum, int pageSize, String taskId);
	/** 
	 * @Title: findById 
	 * @Description: TODO
	 * @param taskId
	 * @return
	 * @return: StatisticsView
	 */
	public StatisticsView findByPrimaryKey(String taskId, Long startTime);
}
