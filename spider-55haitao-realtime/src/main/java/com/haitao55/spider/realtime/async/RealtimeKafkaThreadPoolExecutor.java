package com.haitao55.spider.realtime.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 发现　kafka 发送消息也比较耗时，　先开线程　　进行消息发送
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月12日 下午6:23:34
* @version 1.0
 */
public class RealtimeKafkaThreadPoolExecutor extends ThreadPoolExecutor {
	private RealtimeKafkaThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
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

		private static RealtimeKafkaThreadPoolExecutor instance = new RealtimeKafkaThreadPoolExecutor(corePoolSize,
				maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	public static RealtimeKafkaThreadPoolExecutor getInstance() {
		return Holder.instance;
	}
}