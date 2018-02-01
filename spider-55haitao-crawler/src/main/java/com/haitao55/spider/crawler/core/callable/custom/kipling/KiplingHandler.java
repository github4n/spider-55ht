package com.haitao55.spider.crawler.core.callable.custom.kipling;

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

public class KiplingHandler {
	private static final int threadCount= 5;
	
	private ExecutorService executorService;
	
	public KiplingHandler() {
		executorService = Executors.newFixedThreadPool(threadCount);
	}
	
	public void process(List<String> list,JSONArray skuJsonArray, String productId, Context context) {
		if(null==list || list.size()==0){
			return ;
		}
		
		List<Callable<JSONObject>> calls = new ArrayList<Callable<JSONObject>>();
		
		for (String param : list) {
			calls.add(new KiplingCallaber(param,productId,context));
		}
		
		try {
			List<Future<JSONObject>> futures = executorService.invokeAll(calls);
			try {
				for (Future<JSONObject> future : futures) {
					JSONObject json = future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(null != json){
						skuJsonArray.add(json);
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
