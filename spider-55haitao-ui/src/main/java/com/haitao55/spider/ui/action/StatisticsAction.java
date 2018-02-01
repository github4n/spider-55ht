/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: StatisticsAction.java 
 * @Prject: spider-55haitao-ui
 * @Package: com.haitao55.spider.ui.action 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月7日 上午11:41:45 
 * @version: V1.0   
 */
package com.haitao55.spider.ui.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.haitao55.spider.ui.service.StatisticsService;
import com.haitao55.spider.ui.view.StatisticsView;

/** 
 * @ClassName: StatisticsAction 
 * @Description: 统计管理Controller
 * @author: zhoushuo
 * @date: 2016年9月7日 上午11:41:45  
 */
@Controller
@RequestMapping("/statistics")
public class StatisticsAction extends BaseAction{
	
	@Autowired
	private StatisticsService statisticsService;
	
	@RequestMapping("getLastUpdateStatistics")
	public String getLastUpdateStatistics(HttpServletRequest request,HttpServletResponse response,Model model){
		List<StatisticsView> listView = this.statisticsService.getAllLatestTaskStatistics(getPage(request), getPageSize(request));
		model.addAttribute("pageInfo", new PageInfo<StatisticsView>(listView));
		return "statistics/statistics-home";
	}
	
	@RequestMapping("showDetailStatistics")
	public String showDetailStatistics(HttpServletRequest request,HttpServletResponse response,Model model, String taskId){
		List<StatisticsView> list = this.statisticsService.getAllStatisticsByTaskId(getPage(request), getPageSize(request), taskId);
		model.addAttribute("pageInfo", new PageInfo<StatisticsView>(list));
		model.addAttribute("taskId", taskId);
		return "statistics/statistics-history";
	}
	
	@RequestMapping("showStatisticsView")
	public String showStatisticsView(String taskId, Long startTime, Model model){
		model.addAttribute("statisticsView", this.statisticsService.findByPrimaryKey(taskId, startTime));
		return "statistics/statistics-view";
	}
}
