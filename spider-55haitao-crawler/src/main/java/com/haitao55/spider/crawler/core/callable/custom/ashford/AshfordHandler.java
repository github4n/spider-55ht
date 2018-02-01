package com.haitao55.spider.crawler.core.callable.custom.ashford;

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
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class AshfordHandler {
	private static final int threadCount= 3;
	
	private ExecutorService executorService;
	
	public AshfordHandler() {
		executorService = Executors.newFixedThreadPool(threadCount);
	}
	
	public JSONArray process(String productId, List<String> params , Url url) {
		JSONArray jsonArray = new JSONArray();
		if(null==params || params.size()==0){
			return jsonArray;
		}
		
		List<Callable<JSONObject>> calls = new ArrayList<Callable<JSONObject>>();
		
		for (String param : params) {
			calls.add(new AshfordCallaber(productId ,param , url));
		}
		
		try {
			List<Future<JSONObject>> futures = executorService.invokeAll(calls);
			try {
				for (Future<JSONObject> future : futures) {
					JSONObject futureJsonObject = future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(null!=futureJsonObject && futureJsonObject.size()>0){
						jsonArray.add(futureJsonObject);
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
		return jsonArray;
		
	}

}
