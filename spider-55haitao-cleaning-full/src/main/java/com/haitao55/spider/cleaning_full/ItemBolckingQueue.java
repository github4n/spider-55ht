package com.haitao55.spider.cleaning_full;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 堵塞队列->单列模式
 * @author denghuan
 *
 */
public class ItemBolckingQueue {

	private static BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);

	private ItemBolckingQueue() {
	}

	public static BlockingQueue<String> getInstance() {
		return queue;
	}
}