package com.haitao55.spider.crawler.core.callable.custom.feelunique.cn;

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

public class CNFeeluniqueHandler {
	private static final int threadCount= 10;
	
	private ExecutorService executorService;
	
	public CNFeeluniqueHandler() {
		executorService = Executors.newFixedThreadPool(threadCount);
	}
	
	public JSONArray process(JSONArray skuJSONArray , List<String> productList , Context context) {
		if(null==productList || productList.size()==0){
			return skuJSONArray;
		}
		
		List<Callable<JSONObject>> calls = new ArrayList<Callable<JSONObject>>();
		
		for (String productId : productList) {
			calls.add(new CNFeeluniqueCallaber(productId,context));
		}
		
		try {
			List<Future<JSONObject>> futures = executorService.invokeAll(calls);
			try {
				for (Future<JSONObject> future : futures) {
					JSONObject jsonObject = future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(!jsonObject.isEmpty()){
						skuJSONArray.add(jsonObject);
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
		return skuJSONArray;
		
	}

}
