package com.haitao55.spider.chart.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.haitao55.spider.chart.service.RealTimeSectionService;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年6月2日 下午2:59:42  
 */
@Component
public class CleanData {
	
	private static final Logger logger = LoggerFactory.getLogger(CleanData.class);
	
	@Autowired
	private RealTimeSectionService realTimeSectionService;
	
	public void deleteDataBeforeOneMonth(){
		logger.info("=========开始清理一个月以前的数据===========");
		logger.info("已删除{}条旧数据，清理成功！",realTimeSectionService.cleanData());
	}
}
