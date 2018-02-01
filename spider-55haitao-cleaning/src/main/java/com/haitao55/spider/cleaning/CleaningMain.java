package com.haitao55.spider.cleaning;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 
 * 功能：数据清洗模块的启动主类
 * 
 * @author Arthur.Liu
 * @time 2016年8月30日 下午5:36:38
 * @version 1.0
 */
public class CleaningMain {
	public static final String running = ".running";
	public static File runningFile = null;

	static {
		PropertyConfigurator.configureAndWatch("config/log4j.properties");// 定时重新加载log4j配置文件
	}

	private static final Logger logger = LoggerFactory.getLogger("system");

	private static final String BEANS_FILE = "config/applicationContext-cleaning-beans.xml";

	public static void main(String... args) throws Exception {
		Thread.sleep(1000 * 10);// for debug only
		createRunningFile();//创建全局任务运行标识
		logger.info("##########Start Cleaning Main...##########");
		@SuppressWarnings("resource")
		final FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(BEANS_FILE);
		context.registerShutdownHook();
		logger.info("##########Start Cleaning Main Successfully!##########");
	}
	
	public static void createRunningFile(){
		URL url = CleaningMain.class.getProtectionDomain().getCodeSource().getLocation();
		File jarFile = new File(url.getFile());
		String parentPath = jarFile.getParent();
		try {
			runningFile = new File(parentPath + File.separator + running);
			if(!runningFile.exists()){
				runningFile.createNewFile();
			}
		} catch (IOException e) {
			logger.error("runningFile error ..", e);
		}
	}
}