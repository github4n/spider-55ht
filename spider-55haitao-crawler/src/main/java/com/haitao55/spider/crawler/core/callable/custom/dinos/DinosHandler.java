package com.haitao55.spider.crawler.core.callable.custom.dinos;

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
/**
 * 
* Title:
* Description: dinos sku handle
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月16日 下午4:43:16
* @version 1.0
 */
public class DinosHandler {
	
	public static final int nThreads = 3;
	private ExecutorService service;
	
	public DinosHandler(){
		service = Executors.newFixedThreadPool(nThreads);
	}
	
	public JSONArray process(JSONArray requestJsonArray,Url url){
		JSONArray skuJsonArray=new JSONArray();
		if( null == requestJsonArray  || requestJsonArray.size() == 0){
			return skuJsonArray;
		}
		List<Callable<JSONArray>> calls = new ArrayList<Callable<JSONArray>>();
		for(Object object: requestJsonArray){
			JSONObject jsonObject=(JSONObject)object;
			calls.add(new DinosCallable(jsonObject,url));
		}
		try {
			List<Future<JSONArray>> futures = service.invokeAll(calls);
			for(Future<JSONArray> f : futures){
				try {
					JSONArray skuArray = f.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(skuArray != null){
						skuJsonArray.addAll(skuArray);
					}
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					
					e.printStackTrace();
				}
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			service.shutdownNow();
		}
		return skuJsonArray;
	}

}
