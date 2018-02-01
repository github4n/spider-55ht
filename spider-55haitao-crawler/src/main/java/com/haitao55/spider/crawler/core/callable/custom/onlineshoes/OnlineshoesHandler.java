package com.haitao55.spider.crawler.core.callable.custom.onlineshoes;


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
* Description: Onlineshoes sku handle
* Company: 55海淘
* @author denghuan 
* @date 2016年12月26日 下午4:43:16
* @version 1.0
 */
public class OnlineshoesHandler {
	
	public static final int nThreads = 3;
	private ExecutorService service;
	
	public OnlineshoesHandler(){
		service = Executors.newFixedThreadPool(nThreads);
	}
	
	public JSONArray process(List<String> urlList,Url url){
		JSONArray skuJsonArray=new JSONArray();
		if(urlList == null || urlList.size() == 0){
			return skuJsonArray;
		}
		List<Callable<JSONObject>> calls = new ArrayList<Callable<JSONObject>>();
		for(String skuUrl: urlList){
			calls.add(new OnlineshoesCallable(skuUrl,url));
		}
		try {
			List<Future<JSONObject>> futures = service.invokeAll(calls);
			for(Future<JSONObject> f : futures){
				try {
					JSONObject skuBean = f.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(skuBean != null){
						skuJsonArray.add(skuBean);
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
