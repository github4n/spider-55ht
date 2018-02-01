package com.haitao55.spider.crawler.tasks;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.thrift.TaskModel;
import com.haitao55.spider.common.thrift.ThriftService.Client;
import com.haitao55.spider.crawler.common.cache.TaskCache;
import com.haitao55.spider.crawler.core.model.Rules;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.service.XmlParseService;
import com.haitao55.spider.crawler.thrift.pool.ThriftConnectionProvider;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：定时任务线程，定时从Controller端拉取任务配置信息
 * 
 * @author Arthur.Liu
 * @time 2016年8月2日 下午1:47:39
 * @version 1.0
 */
public class TaskReloadTask implements DaemonTask {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_REMOTE);

	// 定时从Controller端拉取任务配置信息的时间间隔；默认1分钟，可以被外部配置文件覆盖
	private int taskReloadFetchInterval = 1 * 60 * 1000;

	private ThriftConnectionProvider thriftConnectionProvider;

	private XmlParseService xmlParseService;

	@Override
	public void run() {
		while (true) {
			Client client = this.thriftConnectionProvider.getObject();

			Map<Long, TaskModel> map = null;
			try {
				map = client.fetchHotTasks();
			} catch (TException e1) {
				this.thriftConnectionProvider.invalidateObject(client);
				client = null;
				logger.error("Error occurred while fetch task config entities from controller!{}", e1);
			} finally {
				this.thriftConnectionProvider.returnObject(client);// 及时释放连接对象
			}

			logger.info("Fetch {} hot task config entities from controller!", MapUtils.isEmpty(map) ? 0 : map.size());

			if (MapUtils.isEmpty(map)) {
				this.sleep();
				continue;
			}

			TaskCache localCache = TaskCache.getInstance();
			for (Entry<Long, Task> entry : localCache.entrySet()) {
				if (!map.containsKey(entry.getKey())) {
					localCache.remove(entry.getKey());
				}
			}

			for (Entry<Long, TaskModel> entry : map.entrySet()) {
				Task task = new Task();
				task.setTaskId(entry.getValue().getId());
				task.setTaskName(entry.getValue().getName());
				task.setSiteRegion(entry.getValue().getSiteRegion());
				task.setProxyRegionId(entry.getValue().getProxyRegionId());

				Rules rules = null;
				try {
					rules = this.xmlParseService.parse(entry.getValue().getConfig());
				} catch (Exception e) {
					logger.error("Error occurred while parsing task-content!e:{}", e);
					this.noticeCrawlerConfigError(entry.getValue().getId());
					continue;
				}
				task.setRules(rules);

				localCache.put(task.getTaskId(), task);
			}

			this.sleep();
		}
	}

	/**
	 * 向Controller告知一个TaskConfig配置错误
	 * 
	 * @param taskId
	 */
	private void noticeCrawlerConfigError(long taskId) {
		Client client = this.thriftConnectionProvider.getObject();
		try {
			client.noticeTaskConfigError(taskId);
			throw new TException();
		} catch (TException e) {
			this.thriftConnectionProvider.invalidateObject(client);
			client = null;
			logger.error("Error occurred while notice task error!e:{}", e);
		} finally {
			this.thriftConnectionProvider.returnObject(client);// 及时释放连接对象
		}
	}

	private void sleep() {
		try {
			Thread.sleep(taskReloadFetchInterval);
		} catch (InterruptedException e) {
		}
	}

	public int getTaskReloadFetchInterval() {
		return taskReloadFetchInterval;
	}

	public void setTaskReloadFetchInterval(int taskReloadFetchInterval) {
		this.taskReloadFetchInterval = taskReloadFetchInterval;
	}

	public ThriftConnectionProvider getThriftConnectionProvider() {
		return thriftConnectionProvider;
	}

	public void setThriftConnectionProvider(ThriftConnectionProvider thriftConnectionProvider) {
		this.thriftConnectionProvider = thriftConnectionProvider;
	}

	public XmlParseService getXmlParseService() {
		return xmlParseService;
	}

	public void setXmlParseService(XmlParseService xmlParseService) {
		this.xmlParseService = xmlParseService;
	}
}