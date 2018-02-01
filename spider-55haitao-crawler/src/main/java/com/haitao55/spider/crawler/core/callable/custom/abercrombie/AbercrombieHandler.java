package com.haitao55.spider.crawler.core.callable.custom.abercrombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class AbercrombieHandler {
	private static final int threadCount= 3;
	
	private ExecutorService executorService;
	
	public AbercrombieHandler() {
		executorService = Executors.newFixedThreadPool(threadCount);
	}
	
	public void process(JSONObject imageRequestJSONObject , JSONObject imageJSONObject, Context context, String image_param, boolean singleColor) {
		if(imageRequestJSONObject.isEmpty()){
			return ;
		}
		
		List<Callable<JSONObject>> calls = new ArrayList<Callable<JSONObject>>();
		
		for (Map.Entry<String, Object> entry : imageRequestJSONObject.entrySet()) {
			String color = entry.getKey();
			String image_url = (String) entry.getValue();
			calls.add(new AbercrombieCallaber(color,image_url,imageJSONObject,context,image_param,singleColor));
		}
		
		try {
			List<Future<JSONObject>> futures = executorService.invokeAll(calls);
			try {
				for (Future<JSONObject> future : futures) {
					future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
				}
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			
		}finally {
			executorService.shutdownNow();
		}
		return ;
		
	}

}
