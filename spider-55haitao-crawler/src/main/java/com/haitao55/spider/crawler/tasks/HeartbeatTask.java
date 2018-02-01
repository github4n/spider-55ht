package com.haitao55.spider.crawler.tasks;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.thrift.HeartbeatModel;
import com.haitao55.spider.common.thrift.ThriftService.Client;
import com.haitao55.spider.crawler.thrift.pool.ThriftConnectionProvider;
import com.haitao55.spider.crawler.utils.AddressUtils;
import com.haitao55.spider.crawler.utils.ConfigKeys;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.ProcessUtils;
import com.haitao55.spider.crawler.utils.SpringUtils;

/**
 * 
 * 功能：定时心跳线程任务
 * 
 * @author Arthur.Liu
 * @time 2016年8月2日 下午1:44:04
 * @version 1.0
 */
public class HeartbeatTask implements DaemonTask {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	// 定时向Controller端发送心跳信息的时间间隔，默认1分钟，可以被外部配置覆盖
	private int heartbeatInterval = 1 * 60 * 1000;

	private ThriftConnectionProvider thriftConnectionProvider;

	@Override
	public void run() {
		while (true) {
			Client client = this.thriftConnectionProvider.getObject();

			try {
				HeartbeatModel beat = new HeartbeatModel();
				beat.setTime(System.currentTimeMillis());
				beat.setIp(AddressUtils.getIP());
				beat.setProcId(ProcessUtils.getPid());
				beat.setThreadCount(
						Integer.parseInt(SpringUtils.getProperty(ConfigKeys.CRAWLER_PROPERTY_WORKER_COUNT)));

				client.heartbeat(beat);

				logger.info("Pulse a heartbeat to controller via thrift::{}", beat);
			} catch (TException e1) {
				this.thriftConnectionProvider.invalidateObject(client);
				client = null;
				logger.error("Error occurred while pulse a heartbeat to controller via thrift!::{}", e1);
			} finally {
				this.thriftConnectionProvider.returnObject(client);// 及时释放连接对象
			}

			this.sleep();
		}
	}

	private void sleep() {
		try {
			Thread.sleep(heartbeatInterval);
		} catch (InterruptedException e) {
		}
	}

	public int getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(int heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public ThriftConnectionProvider getThriftConnectionProvider() {
		return thriftConnectionProvider;
	}

	public void setThriftConnectionProvider(ThriftConnectionProvider thriftConnectionProvider) {
		this.thriftConnectionProvider = thriftConnectionProvider;
	}
}