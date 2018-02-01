package com.haitao55.spider.crawler.core.callable.custom.skinstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
public class SkinStoreHandler {
	
	public static final int nThreads = 3;
	private ExecutorService service;
	
	public SkinStoreHandler(){
		service = Executors.newFixedThreadPool(nThreads);
	}
	
	/*private static class Holder {
		public static final AmazonPriceStockHandler handler = new AmazonPriceStockHandler();
	}
	
	public static AmazonPriceStockHandler getInstance(){
		return Holder.handler;
	}*/

	public Map<String,SkinStoreSkuBean> process(List<Url> skuUrls){
		Map<String,SkinStoreSkuBean> result = new HashMap<String,SkinStoreSkuBean>(); 
		if(skuUrls == null || skuUrls.size() == 0){
			return result;
		}
		List<Callable<SkinStoreSkuBean>> calls = new ArrayList<Callable<SkinStoreSkuBean>>();
		for(Url url: skuUrls){
			calls.add(new SkinStoreCallable(url));
		}
		try {
			List<Future<SkinStoreSkuBean>> futures = service.invokeAll(calls);
			for(Future<SkinStoreSkuBean> f : futures){
				try {
					SkinStoreSkuBean skuBean = f.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(skuBean != null){
						result.put(skuBean.getSkuId(), skuBean);
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
		return result;
	}

}
