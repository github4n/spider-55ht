package com.haitao55.spider.crawler;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.SpringUtils;

/**
 * 
 * 功能：Crawler模块启动类
 * 
 * @author Arthur.Liu
 * @time 2016年6月19日 上午10:08:58
 * @version 1.0
 */
public class CrawlerMain {

	static {
		PropertyConfigurator.configure("config/log4j.properties");
	}

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	private static final String SPRING_CONTEXT_FILE = "config/applicationContext-crawler-beans.xml";

	public static void main(String... args) throws Exception {
		Thread.sleep(10 * 1000);// for debug only

		logger.info("##########Start CrawlerMain...!##########");
		SpringUtils.initFileSystemXmlApplicationContext(SPRING_CONTEXT_FILE);
		logger.info("##########Start CrawlerMain Successfully!##########");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("##########Exit CrawlerMain...!##########");
				Bootstrap bootstrap = (Bootstrap) SpringUtils.getBean("bootstrap");
				bootstrap.destroy();
				logger.info("##########Exit CrawlerMain Successfully!##########");
			}
		});

		Thread.currentThread().join();
	}
}