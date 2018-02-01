package com.haitao55.spider.crawler.core.callable.custom.amazon.imp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.haitao55.spider.crawler.utils.SpringUtils;

public class Producer implements Runnable{
	
	private HTQueue<String> queue;
	private AtomicLong count = new AtomicLong(0);
	private CountDownLatch countDown;
	private MongoTemplate mongoTemplate = SpringUtils.getBean("mongoTemplate");
	
	public  Producer(HTQueue<String> queue,CountDownLatch countDown){
		this.queue = queue;
		this.countDown = countDown;
	}
	
	@Override
	public void run() {
	     try{
	    	 ///data/amazon_dump.20161222.json
	    	 BufferedReader br =  new BufferedReader(new InputStreamReader(new FileInputStream("/data/amazon_dump.20161222.json")));
	    	 BufferedWriter bw =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/data/amazon_dump_item.txt", true)));
	    	 String line = null;
		     while ((line = br.readLine()) != null) {
		    	 if(StringUtils.isBlank(line)){
		    		 continue;
		    	 }
		    	 queue.add(line);
		    	 String url = StringUtils.substringBetween(line, "\"url\":\"", "\",");
		    	 if(StringUtils.contains(url, "http://")){
		    		 url = StringUtils.replace(url, "http://", "https://");
		    	 }
		    	 StringBuilder sb = new StringBuilder();
				 sb.append(url).append("\n");
				 bw.write(sb.toString());
				 bw.flush();
		    		 
				 if (count.incrementAndGet() % 1000 == 0) {
		                System.out.println("Producer process : " + count.get());
		         }
		     }
		     countDown.countDown();
	     }catch(Throwable e){
	    	 e.printStackTrace();
	     }
	     System.out.println(Thread.currentThread().getName() +"=============stoped============");
	}

}
