package com.haitao55.spider.crawler.core.callable.custom.beautybay;

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
* Description: lookFantastic sku handle
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月16日 下午4:43:16
* @version 1.0
 */
public class BeautyBayHandler {
	
	public static final int nThreads = 3;
	private ExecutorService service;
	
	public BeautyBayHandler(){
		service = Executors.newFixedThreadPool(nThreads);
	}
	
	/*private static class Holder {
		public static final AmazonPriceStockHandler handler = new AmazonPriceStockHandler();
	}
	
	public static AmazonPriceStockHandler getInstance(){
		return Holder.handler;
	}*/

	public JSONArray process(List<String> skuIdList,Url url,String path){
		JSONArray skuJsonArray=new JSONArray();
		if(skuIdList == null || skuIdList.size() == 0){
			return skuJsonArray;
		}
		List<Callable<JSONObject>> calls = new ArrayList<Callable<JSONObject>>();
		for(String skuId: skuIdList){
			calls.add(new BeautyBayCallable(skuId,url,path));
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
