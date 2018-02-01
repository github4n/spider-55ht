package com.haitao55.spider.cleaning.writer;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.cleaning.CleaningMain;
import com.haitao55.spider.cleaning.utils.ConsumerKafkaItemBolckingQueue;
import com.haitao55.spider.common.kafka.SpiderKafkaConsumer;

/**
 * 
 * 功能：负责将kafka item信息put到queue中
 * 
 * @author denghuan
 * @time 2017年2月28日 下午7:02:14
 * @version 1.0
 */
public class KafkaItemToQueue extends Thread {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private SpiderKafkaConsumer consumer;

	public KafkaItemToQueue(SpiderKafkaConsumer consumer) {
		this.consumer = consumer;

	}
	
	@Override
	public void run() {
		BlockingQueue<List<String>> queue = ConsumerKafkaItemBolckingQueue.getInstance();
		while(CleaningMain.runningFile.exists()){
			long startTime = System.currentTimeMillis();
			List<String> itemList = this.consumer.fetchData();
			if(CollectionUtils.isNotEmpty(itemList)){
				try {
					queue.put(itemList);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			logger.info("Fetch data from kafka end, current-time:{}", System.currentTimeMillis() - startTime);
		}
	}

}