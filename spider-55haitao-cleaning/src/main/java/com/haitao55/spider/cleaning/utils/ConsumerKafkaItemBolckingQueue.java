package com.haitao55.spider.cleaning.utils;


import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 堵塞队列->单列模式
 * @author denghuan
 *
 */
public class ConsumerKafkaItemBolckingQueue {

	private static BlockingQueue<List<String>> queue = new LinkedBlockingQueue<List<String>>(10000);

	private ConsumerKafkaItemBolckingQueue() {
	}

	public static BlockingQueue<List<String>> getInstance() {
		return queue;
	}
}