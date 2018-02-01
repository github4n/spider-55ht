package com.haitao55.spider.crawler.core.callable.custom.famousfootwear;

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
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class FamousFootwearHandler {
	private static final int threadCount= 3;
	
	private ExecutorService executorService;
	
	public FamousFootwearHandler() {
		executorService = Executors.newFixedThreadPool(threadCount);
	}
	
	public JSONArray process(List<String> params , Url url) {
		JSONArray jsonArray = new JSONArray();
		if(null==params || params.size()==0){
			return jsonArray;
		}
		
		List<Callable<JSONArray>> calls = new ArrayList<Callable<JSONArray>>();
		
		for (String param : params) {
			calls.add(new FamousFootwearCallaber(param , url));
		}
		
		try {
			List<Future<JSONArray>> futures = executorService.invokeAll(calls);
			try {
				for (Future<JSONArray> future : futures) {
					JSONArray futureJsonArray = future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(null!=futureJsonArray && futureJsonArray.size()>0){
						jsonArray.addAll(futureJsonArray);
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
