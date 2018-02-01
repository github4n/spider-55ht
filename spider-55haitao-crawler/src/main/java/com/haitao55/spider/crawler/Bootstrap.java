package com.haitao55.spider.crawler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.tasks.DaemonTask;
import com.haitao55.spider.crawler.tasks.Worker;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：系统实际启动类
 * 
 * @author Arthur.Liu
 * @time 2016年8月2日 下午1:35:29
 * @version 1.0
 */
public class Bootstrap {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	// 工作线程数量
	private int workerCount;

	// 工作线程实例
	private Worker worker;

	// 后台线程，如爬虫节点心跳线程/任务配置信息定时拉取线程等
	private List<DaemonTask> tasks;

	/**
	 * 业务系统启动方法
	 */
	public void start() {
		logger.info("##########Start Bootstrap!##########");

		try {
			logger.info("Waiting for spring context to start completely..........");
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
		}

		for (int i = 0; i < workerCount; i++) {
			Thread t = new Thread(worker);
			t.setName("crawler-worker-thread-" + i);
			t.start();
			logger.info(t.getName() + " started!");
		}

		for (DaemonTask task : tasks) {
			Thread t = new Thread(task);
			t.setName(task.getClass().getName());
			t.start();
			logger.info(t.getName() + " started!");
		}

		logger.info("##########Start Bootstrap Successfully!##########");
	}

	/**
	 * 系统退出时收尾方法，主要做些资源释放类型的工作
	 */
	public void destroy() {
		logger.info("##########Destroy Bootstrap!##########");

		logger.info("workerCount::{}", (workerCount + 2));

		logger.info("##########Destroy Bootstrap Successfully!##########");
	}

	public int getWorkerCount() {
		return workerCount;
	}

	public void setWorkerCount(int workerCount) {
		this.workerCount = workerCount;
	}

	public Worker getWorker() {
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	public List<DaemonTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<DaemonTask> tasks) {
		this.tasks = tasks;
	}
}