package com.haitao55.spider.disposable.kimiss;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.haitao55.spider.crawler.utils.HttpUtils;

import main.java.com.UpYun;
/**
 * 
* Title:
* Description: 
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月16日 下午4:43:16
* @version 1.0
 */
public class KimissHandler {
	
	public static final int nThreads = 3;
	private ExecutorService service;
	
	public KimissHandler(){
		service = Executors.newFixedThreadPool(nThreads);
	}
	

	public void process( List<String> urlList, UpYun upyun) {
		if( null == urlList  || urlList.size() == 0){
			return ;
		}
		List<Callable<List<KimissUser>>> calls = new ArrayList<Callable<List<KimissUser>>>();
		for(String url: urlList){
			calls.add(new KimissCallable(url,upyun));
		}
		try {
			List<Future<List<KimissUser>>> futures = service.invokeAll(calls);
			for(Future<List<KimissUser>> f : futures){
				try {
					f.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			service.shutdownNow();
		}
	}

}
