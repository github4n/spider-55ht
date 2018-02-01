package com.haitao55.spider.cleaning_full;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.haitao55.spider.util.Constants;

/**
 * 
 * 功能：全量数据清理(CleaningFull)模块程序执行主入口
 * 
 * @author Arthur.Liu
 * @time 2016年9月21日 下午6:05:51
 * @version 1.0
 */
public class CleaningFullMain {

	static {
		PropertyConfigurator.configureAndWatch("config/log4j.properties");// 定时重新加载log4j配置文件
	}

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CLEANING_FULL);

	private static final String BEANS_FILE = "config/applicationContext-cleaning-full-beans.xml";

	public static void main(String... args) throws Exception {
		Thread.sleep(1000 * 10);// for debug only

		logger.info("##########Start Cleaning Main...##########");
		@SuppressWarnings("resource")
		final FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(BEANS_FILE);
		context.registerShutdownHook();
		logger.info("##########Start Cleaning Main Successfully!##########");
	}
}