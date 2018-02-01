package com.haitao55.spider.crawler.core.callable.custom.lookfantastic;

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
public class LookFantasticHandler {
	
	public static final int nThreads = 3;
	private ExecutorService service;
	
	public LookFantasticHandler(){
		service = Executors.newFixedThreadPool(nThreads);
	}
	
	/*private static class Holder {
		public static final AmazonPriceStockHandler handler = new AmazonPriceStockHandler();
	}
	
	public static AmazonPriceStockHandler getInstance(){
		return Holder.handler;
	}*/

	public Map<String,LookFantasticSkuBean> process(List<Url> skuUrls){
		Map<String,LookFantasticSkuBean> result = new HashMap<String,LookFantasticSkuBean>(); 
		if(skuUrls == null || skuUrls.size() == 0){
			return result;
		}
		List<Callable<LookFantasticSkuBean>> calls = new ArrayList<Callable<LookFantasticSkuBean>>();
		for(Url url: skuUrls){
			calls.add(new LookFantasticCallable(url));
		}
		try {
			List<Future<LookFantasticSkuBean>> futures = service.invokeAll(calls);
			for(Future<LookFantasticSkuBean> f : futures){
				try {
					LookFantasticSkuBean skuBean = f.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
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
