package com.haitao55.spider.crawler.core.callable.custom.ninewest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class NineWestHandler {
	private static final int threadCount= 3;
	
	private ExecutorService executorService;
	
	public NineWestHandler() {
		executorService = Executors.newFixedThreadPool(threadCount);
	}
	
	public void process(JSONObject imageJSONObject, JSONArray colorJSONArray , JSONArray skuJSONArray, Context context) {
		if(null == colorJSONArray || colorJSONArray.size() == 0){
			return ;
		}
		
		List<Callable<JSONArray>> calls = new ArrayList<Callable<JSONArray>>();
		
		for (Object object : colorJSONArray) {
			JSONObject jsonObject = (JSONObject)object;
			calls.add(new NineWestCallaber(jsonObject ,imageJSONObject, context));
		}
		
		try {
			List<Future<JSONArray>> futures = executorService.invokeAll(calls);
			try {
				for (Future<JSONArray> future : futures) {
					JSONArray jsonArray = future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(null != jsonArray && jsonArray.size() > 0){
						skuJSONArray.addAll(jsonArray);
					}
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
