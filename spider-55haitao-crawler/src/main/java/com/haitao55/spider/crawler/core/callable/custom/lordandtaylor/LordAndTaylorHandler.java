package com.haitao55.spider.crawler.core.callable.custom.lordandtaylor;

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
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class LordAndTaylorHandler {
	private static final int threadCount= 3;
	
	private ExecutorService executorService;
	
	public LordAndTaylorHandler() {
		executorService = Executors.newFixedThreadPool(threadCount);
	}
	
	public JSONObject process(JSONObject styleJsonObject , List<String> image_suffix_list , Url url) {
		JSONObject image_result_json = new JSONObject();
		if(null==image_suffix_list || image_suffix_list.size()==0){
			return image_result_json;
		}
		
		List<Callable<JSONObject>> calls = new ArrayList<Callable<JSONObject>>();
		
		if(null != styleJsonObject  && styleJsonObject.size()>0){
			for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
				calls.add(new LordAndTaylorCallaber(entry,image_result_json,image_suffix_list,url));
			}
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
		return image_result_json;
		
	}

}
