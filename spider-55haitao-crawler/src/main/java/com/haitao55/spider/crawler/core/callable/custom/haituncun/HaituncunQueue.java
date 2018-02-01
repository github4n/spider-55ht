package com.haitao55.spider.crawler.core.callable.custom.haituncun;

import java.util.concurrent.ArrayBlockingQueue;

import com.haitao55.spider.common.gson.bean.HaiTunCunRetBody;

/** 
 * @Description: 读取csv文件与发送消息之间的缓冲队列
 * @author: zhoushuo
 * @date: 2017年3月3日 上午11:41:10  
 */
public class HaituncunQueue extends ArrayBlockingQueue<HaiTunCunRetBody>{

	private static final long serialVersionUID = 1L;

	/**
	 * @param capacity
	 */
	private HaituncunQueue(int capacity) {
		super(capacity);
	}

	private static class HaituncunHolder {
		private static HaituncunQueue instance = new HaituncunQueue(1000);
	}
	
	public static HaituncunQueue getInstance(){
		return HaituncunHolder.instance;
	}

}
