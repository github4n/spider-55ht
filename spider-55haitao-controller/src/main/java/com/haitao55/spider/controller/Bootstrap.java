package com.haitao55.spider.controller;

import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.controller.service.impl.ControllerService;
import com.haitao55.spider.controller.thread.ControllerServer;

/**
 * 
 * 功能：启动程序
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 下午6:22:31
 * @version 1.0
 */
public class Bootstrap {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private Map<String, String> thriftConfig;
	private ControllerService controllerService;

	public Map<String, String> getThriftConfig() {
		return thriftConfig;
	}

	public void setThriftConfig(Map<String, String> thriftConfig) {
		this.thriftConfig = thriftConfig;
	}

	public ControllerService getControllerService() {
		return controllerService;
	}

	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}

	public void init() {
		this.controllerService.clearHeartbeatDbTable();
		
		logger.info("----------Start Controller Boostrap...----------");
		int port = NumberUtils.toInt(this.thriftConfig.get("port"), 7911);
		int workerNum = NumberUtils.toInt(this.thriftConfig.get("workerNum"), 16);
		int selectorNum = NumberUtils.toInt(this.thriftConfig.get("selectorNum"), 2);
		int acceptQueueSize = NumberUtils.toInt(this.thriftConfig.get("acceptQueueSize"), 10000);
		int readBufSize = NumberUtils.toInt(this.thriftConfig.get("readBufSize"), 1024);
		int clientTimeout = NumberUtils.toInt(this.thriftConfig.get("clientTimeout"), 10);

		ControllerServer controllerServer = new ControllerServer(this.controllerService, port, workerNum, selectorNum,
				acceptQueueSize, readBufSize, clientTimeout);
		controllerServer.start();
		logger.info("----------Start Controller Boostrap Successfully!----------");
	}
}