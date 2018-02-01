package com.haitao55.spider.realtime.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 功能：实时核价爬虫范围内使用的线程池类
 * 
 * @author Arthur.Liu
 * @time 2016年9月18日 下午8:19:31
 * @version 1.0
 */
public class RealtimeCrawlerThreadPoolExecutor extends ThreadPoolExecutor {
	private RealtimeCrawlerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	private static class Holder {
		private static final int corePoolSize = 5;
		private static final int maximumPoolSize = 50;
		private static final long keepAliveTime = 10 * 1000;// 毫秒
		private static final TimeUnit unit = TimeUnit.MILLISECONDS;// 毫秒
		private static final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(1000);
		private static final ThreadFactory threadFactory = Executors.defaultThreadFactory();
		private static final RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();

		private static RealtimeCrawlerThreadPoolExecutor instance = new RealtimeCrawlerThreadPoolExecutor(corePoolSize,
				maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	public static RealtimeCrawlerThreadPoolExecutor getInstance() {
		return Holder.instance;
	}
}