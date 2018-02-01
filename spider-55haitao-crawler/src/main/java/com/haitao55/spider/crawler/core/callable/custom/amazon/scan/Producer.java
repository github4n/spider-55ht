package com.haitao55.spider.crawler.core.callable.custom.amazon.scan;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.haitao55.spider.crawler.core.callable.custom.amazon.imp.HTQueue;
import com.haitao55.spider.crawler.utils.SpringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class Producer implements Runnable {

	private HTQueue<String> queue;
	private AtomicLong count = new AtomicLong(0);
	private CountDownLatch countDown;
	private MongoTemplate mongoTemplate = SpringUtils.getBean("mongoTemplate");

	public Producer(HTQueue<String> queue, CountDownLatch countDown) {
		this.queue = queue;
		this.countDown = countDown;
	}

	@Override
	public void run() {
		try {
			DBObject object = new BasicDBObject();
			object.put("grade", 2);
			DBCursor cursor = mongoTemplate.getCollection("urls1482470886504").find(object);
			cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
			while (cursor.hasNext()) {
				DBObject result = cursor.next();
				String url = result.get("value").toString();
				if (StringUtils.isBlank(url)) {
					continue;
				}
				System.out.println("producer url:"+url);
				queue.add(url);
				if (count.incrementAndGet() % 1000 == 0) {
					System.out.println("Producer process : " + count.get());
				}
			}
			//queue.add("https://www.amazon.com/dp/B004XO28IQ/");
			countDown.countDown();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName()
				+ "=============stoped============");
	}

}
