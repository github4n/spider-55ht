package com.haitao55.spider.crawler.core.callable.custom.amazon.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.haitao55.spider.crawler.utils.SpringUtils;

public class ImportMain {
	
	private static final String SPRING_CONTEXT_FILE = "config/applicationContext-crawler-beans.xml";
	private static final int CONSUME_THREADS = 10;

	public static void main(String[] args) {
		
		SpringUtils.initFileSystemXmlApplicationContext(SPRING_CONTEXT_FILE);
		
		CountDownLatch countDown = new CountDownLatch(1);
		ProxyPool pool = new ProxyPool();
		
		HTQueue<String> queue = new HTQueue<String>();
		Thread p = new Thread(new Producer(queue,countDown));
		p.start();
		
		ExecutorService service = Executors.newFixedThreadPool(CONSUME_THREADS);
		List<Consumer> list = new ArrayList<Consumer>();
		for(int i = 0; i < CONSUME_THREADS; i++){
			Consumer consumer = new Consumer(queue,pool);
			list.add(consumer);
			service.submit(consumer);
		}
		try {
			countDown.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for(Consumer consumer : list){
			consumer.stop();
		}
		
	}
}
