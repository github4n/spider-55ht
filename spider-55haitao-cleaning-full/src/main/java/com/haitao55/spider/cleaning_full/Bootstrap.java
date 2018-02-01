package com.haitao55.spider.cleaning_full;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.dao.ItemDAO;
import com.haitao55.spider.service.CounterService;
import com.haitao55.spider.util.Constants;

/**
 * 
 * 功能：全量数据清理(CleaningFull)模块程序执行入口类
 * 
 * @author Arthur.Liu
 * @time 2016年9月21日 下午6:17:19
 * @version 1.0
 */
public class Bootstrap {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CLEANING_FULL);
	/**
	 * 线程池属性键值对
	 */
	private Map<String, String> threadPoolProps;
	private ItemDAO itemDAO;
	private String ignoringTaskIds;
	
	private String outputRootPath;
    
	private String outputRootFileName;
	
	private CounterService counterService;
	
	private String emailAddress;
	private String emailPassword;
	private String emailReceiver;
	
	private String excelFilePath;
	
	private String serverAddress;
	
	public void init() {
		logger.info("Bootstrap.init() start....");

		Watcher watcher = new Watcher(this.threadPoolProps, this.itemDAO, this.ignoringTaskIds, outputRootPath,this.outputRootFileName,this.counterService
				,this.emailAddress,this.emailPassword,this.emailReceiver,this.excelFilePath,this.serverAddress);
		watcher.start();

		logger.info("Bootstrap.init() start successfully!");
	}

	public Map<String, String> getThreadPoolProps() {
		return threadPoolProps;
	}

	public void setThreadPoolProps(Map<String, String> threadPoolProps) {
		this.threadPoolProps = threadPoolProps;
	}

	public ItemDAO getItemDAO() {
		return itemDAO;
	}

	public void setItemDAO(ItemDAO itemDAO) {
		this.itemDAO = itemDAO;
	}

	public String getIgnoringTaskIds() {
		return ignoringTaskIds;
	}

	public void setIgnoringTaskIds(String ignoringTaskIds) {
		this.ignoringTaskIds = ignoringTaskIds;
	}

	public String getOutputRootPath() {
		return outputRootPath;
	}

	public void setOutputRootPath(String outputRootPath) {
		this.outputRootPath = outputRootPath;
	}

	public String getOutputRootFileName() {
		return outputRootFileName;
	}

	public void setOutputRootFileName(String outputRootFileName) {
		this.outputRootFileName = outputRootFileName;
	}

	public CounterService getCounterService() {
		return counterService;
	}

	public void setCounterService(CounterService counterService) {
		this.counterService = counterService;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getEmailPassword() {
		return emailPassword;
	}

	public void setEmailPassword(String emailPassword) {
		this.emailPassword = emailPassword;
	}

	public String getEmailReceiver() {
		return emailReceiver;
	}

	public void setEmailReceiver(String emailReceiver) {
		this.emailReceiver = emailReceiver;
	}

	public String getExcelFilePath() {
		return excelFilePath;
	}

	public void setExcelFilePath(String excelFilePath) {
		this.excelFilePath = excelFilePath;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

}