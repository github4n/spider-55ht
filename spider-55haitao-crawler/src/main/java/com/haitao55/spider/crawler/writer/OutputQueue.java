package com.haitao55.spider.crawler.writer;

import java.util.concurrent.ArrayBlockingQueue;

import com.haitao55.spider.crawler.core.model.OutputObject;

/**
 * 
 * @author jerome
 *
 * @param <T>a
 */
public class OutputQueue extends ArrayBlockingQueue<OutputObject> {

	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private static class Holder {
		private static OutputQueue instance = new OutputQueue(1000);
	}

	private OutputQueue(int capacity) {
		super(capacity);
	}

	public static OutputQueue getInstance() {
		return Holder.instance;
	}
}