/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: StatisticsServiceImpl.java 
 * @Prject: spider-55haitao-ui
 * @Package: com.haitao55.spider.ui.service.impl 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月7日 下午4:39:46 
 * @version: V1.0   
 */
package com.haitao55.spider.ui.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.haitao55.spider.common.dao.StatisticsDao;
import com.haitao55.spider.common.dao.TaskDAO;
import com.haitao55.spider.common.dos.StatisticsDO;
import com.haitao55.spider.common.service.impl.BaseService;
import com.haitao55.spider.ui.common.util.ConvertPageInstance;
import com.haitao55.spider.ui.service.StatisticsService;
import com.haitao55.spider.ui.view.StatisticsView;

/** 
 * @ClassName: StatisticsServiceImpl 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年9月7日 下午4:39:46  
 */
@Service("statisticsService")
public class StatisticsServiceImpl extends BaseService<StatisticsDO> implements StatisticsService {

	@Autowired
	private StatisticsDao statisticsDao;
	
	@Autowired
	private TaskDAO taskDao;
	/* (non Javadoc) 
	 * @Title: save
	 * @Description: TODO
	 * @param statisticsView 
	 * @see com.haitao55.spider.ui.service.StatisticsService#save(com.haitao55.spider.ui.view.StatisticsView) 
	 */
	@Override
	public void createStatistics(String taskId) {
		statisticsDao.insert(new StatisticsDO(){{
			setTaskId(Long.parseLong(taskId));
			setStartTime(new Date().getTime());
			setFailedCount(0);
			setHandleCount(0);
			setOfflineCount(0);
			setSuccessCount(0);
			setTotalCount(0);
		}});
	}
	/* (non Javadoc) 
	 * @Title: getAllLatestTaskStatistics
	 * @Description: TODO
	 * @return 
	 * @see com.haitao55.spider.ui.service.StatisticsService#getAllLatestTaskStatistics() 
	 */
	@Override
	public List<StatisticsView> getAllLatestTaskStatistics(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		return doList2PageView(statisticsDao.selectLatestStatistics());
	}
	
	private StatisticsView convertView2DO(StatisticsDO sdo){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StatisticsView view = new StatisticsView();
		view.setEndTime(sdo.getEndTime()==null?"":sdf.format(new Date(sdo.getEndTime())));
		view.setFailedCount(sdo.getFailedCount());
		view.setOfflineCount(sdo.getOfflineCount());
		view.setAccurateStartTime(sdo.getStartTime());
		view.setStartTime(sdf.format(new Date(sdo.getStartTime())));
		view.setSuccessCount(sdo.getSuccessCount());
		view.setTaskId(sdo.getTaskId().toString());
		view.setTotalCount(sdo.getTotalCount());
		view.setTaskName(taskDao.getTaskById(sdo.getTaskId().toString()).getName());
		return view;
	}
	/* (non Javadoc) 
	 * @Title: getAllStatisticsByTaskId
	 * @Description: TODO
	 * @param taskId
	 * @return 
	 * @see com.haitao55.spider.ui.service.StatisticsService#getAllStatisticsByTaskId(java.lang.String) 
	 */
	@Override
	public List<StatisticsView> getAllStatisticsByTaskId(int pageNum, int pageSize, String taskId) {
		PageHelper.startPage(pageNum, pageSize);
		return doList2PageView(statisticsDao.selectStatisticsByTaskId(taskId));
	}
	
	private Page<StatisticsView> doList2PageView(List<StatisticsDO> list){
		Page<StatisticsDO> page = (Page<StatisticsDO>) list;
		Page<StatisticsView> pageView = new Page<>();
		ConvertPageInstance.convert(page, pageView);
		if(list != null) {
			for(StatisticsDO sdo : list){
				pageView.add(this.convertView2DO(sdo));
			}
		}
		return pageView;
	}
	/* (non Javadoc) 
	 * @Title: findById
	 * @Description: TODO
	 * @param taskId
	 * @return 
	 * @see com.haitao55.spider.ui.service.StatisticsService#findById(java.lang.String) 
	 */
	@Override
	public StatisticsView findByPrimaryKey(String taskId, Long startTime) {
		@SuppressWarnings("serial")
		Map<String, String> params = new HashMap<String, String>(){
			{
				put(StatisticsDao.COMPOSITE_KEYS_TASKID, taskId);
				put(StatisticsDao.COMPOSITE_KEYS_STARTTIME, startTime.toString());
			}
		};
		StatisticsDO sdo = this.statisticsDao.selectStatisticsByPK(params);
		return this.convertView2DO(sdo);
	}
}
