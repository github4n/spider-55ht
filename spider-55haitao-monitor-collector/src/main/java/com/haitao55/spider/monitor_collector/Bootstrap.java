package com.haitao55.spider.monitor_collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.Constants;

/**
 * 
 * 功能：爬虫日志收集主启动类
 * 
 * @author Arthur.Liu
 * @time 2016年11月25日 下午9:12:38
 * @version 1.0
 */
public class Bootstrap {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);
	
	public void init(){
		logger.info("Bootstrap.init() started....");
	}
	
	public void destroy(){
		logger.info("Bootstrap.destroy() started....");
	}
}