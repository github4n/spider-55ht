package com.haitao55.spider.realtime.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 针对核价超时的网站进行异步post请求处理，某些网站耗时较长的话可以从mongo取最新数据
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月19日 上午10:31:01
* @version 1.0
 */
public class RealtimeThreadPoolExecutor extends ThreadPoolExecutor {
	private RealtimeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
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

		private static RealtimeThreadPoolExecutor instance = new RealtimeThreadPoolExecutor(corePoolSize,
				maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	public static RealtimeThreadPoolExecutor getInstance() {
		return Holder.instance;
	}
}