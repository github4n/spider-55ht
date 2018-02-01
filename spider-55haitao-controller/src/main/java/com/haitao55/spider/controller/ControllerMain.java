package com.haitao55.spider.controller;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 
 * 功能：Controller模块启动类
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 下午5:38:51
 * @version 1.0
 */
public class ControllerMain {

	static {
		PropertyConfigurator.configureAndWatch("config/log4j.properties");// 定时重新加载log4j配置文件
	}

	private static final Logger logger = LoggerFactory.getLogger("system");

	private static final String BEANS_FILE = "config/applicationContext-controller-beans.xml";

	public static void main(String... args) throws Exception {
		Thread.sleep(1000 * 10);// for debug only

		logger.info("##########Start Controller Main...##########");
		@SuppressWarnings("resource")
		final FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(BEANS_FILE);
		context.registerShutdownHook();
		logger.info("##########Start Controller Main Successfully!##########");
	}
}