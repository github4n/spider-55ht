package com.haitao55.spider.crawler.core.callable.custom.columbia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * spacenk 多线程请求发送handler
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月30日 下午5:20:03
* @version 1.0
 */
public class ColumbiaHandler {
	private int threads = 3;
	private ExecutorService executorService;
	public ColumbiaHandler(){
		executorService = Executors.newFixedThreadPool(threads); 
	}
	public void process(List<String> requestUrlList, JSONArray skuJSONArray, Context context) {
		if(CollectionUtils.isEmpty(requestUrlList)){
			return ;
		}
		List<Callable<JSONArray>> calls = new ArrayList<Callable<JSONArray>>();
		
		for (String request_url : requestUrlList) {
			calls.add(new ColumbiaCallable(request_url,context));
		}
		try {
			List<Future<JSONArray>> futures = executorService.invokeAll(calls);
			if(CollectionUtils.isNotEmpty(futures)){
				for (Future<JSONArray> future : futures) {
					JSONArray jsonArray = future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(CollectionUtils.isNotEmpty(jsonArray)){
						skuJSONArray.addAll(jsonArray);
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}finally{
			executorService.shutdownNow();
		}
	}
	
}
