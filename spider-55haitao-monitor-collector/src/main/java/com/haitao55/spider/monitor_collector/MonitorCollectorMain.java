package com.haitao55.spider.monitor_collector;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.haitao55.spider.common.utils.Constants;

/**
 * 
 * 功能：爬虫日志收集主类
 * 
 * @author Arthur.Liu
 * @time 2016年11月25日 下午8:25:02
 * @version 1.0
 */
public class MonitorCollectorMain {

	static {
		PropertyConfigurator.configure("config/log4j.properties");
	}

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	private static final String SPRING_CONTEXT_FILE = "config/applicationContext-monitor-collector-beans.xml";

	public static void main(String... args) throws InterruptedException {
		Thread.sleep(10 * 1000);// for debug only

		logger.info("##########Start Monitor Collector Main...##########");
		@SuppressWarnings("resource")
		final FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(SPRING_CONTEXT_FILE);
		context.registerShutdownHook();
		logger.info("##########Start Monitor Collector Main Successfully!##########");
	}
}