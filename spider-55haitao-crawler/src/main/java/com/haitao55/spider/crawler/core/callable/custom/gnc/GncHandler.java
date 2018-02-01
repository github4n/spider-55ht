package com.haitao55.spider.crawler.core.callable.custom.gnc;



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

public class GncHandler {
	private static final int threadCount= 3;
	
	private ExecutorService executorService;
	
	public GncHandler() {
		executorService = Executors.newFixedThreadPool(threadCount);
	}
	
	public JSONArray process(List<JSONObject> params, Url url, JSONArray skuJsonArray) {
		if(null==params || params.size()==0){
			return skuJsonArray;
		}
		
		List<Callable<JSONObject>> calls = new ArrayList<Callable<JSONObject>>();
		
		if(null != params  && params.size()>0){
			for (JSONObject jsonObject : params) {
				calls.add(new GncCallaber(jsonObject,url));
			}
		}	
		
		try {
			List<Future<JSONObject>> futures = executorService.invokeAll(calls);
			try {
				for (Future<JSONObject> future : futures) {
					JSONObject resultJsonObject = future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(null!=resultJsonObject){
						skuJsonArray.add(resultJsonObject);
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
		return skuJsonArray;
		
	}

}
