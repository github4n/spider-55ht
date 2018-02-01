package com.haitao55.spider.crawler.core.callable.custom.michaelkors;

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

/**
 * 处理　Michaelkors　多颜色　发请求
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年12月21日 上午10:25:00
* @version 1.0
 */
public class MichaelkorsHandler {
	private static final int threadCount= 3;
	
	private ExecutorService executorService;
	
	public MichaelkorsHandler() {
		executorService = Executors.newFixedThreadPool(threadCount);
	}
	
	public void process(JSONObject imageJSONObject,JSONObject colorImageParamJSONObject,Context context) {
		if(null==colorImageParamJSONObject || colorImageParamJSONObject.isEmpty()){
			return ;
		}
		
		List<Callable<JSONObject>> calls =new ArrayList<Callable<JSONObject>>();
		
		for (Map.Entry<String,Object> entry : colorImageParamJSONObject.entrySet()) {
			String key = entry.getKey();
			String value = (String) entry.getValue();
			calls.add(new MichaelkorsCallable(imageJSONObject , key , value , context));
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
