package com.haitao55.spider.crawler.core.callable.custom.selfridges;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.utils.HttpUtils;
/**
 * 
* Title:
* Description: selfridges item stock handler
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月25日 上午9:22:52
* @version 1.0
 */

public class SelfridgesStockHandler {
	private static String split="&&";
	public static final int nThreads = 3;
	private ExecutorService service;
	
	public SelfridgesStockHandler(){
		service = Executors.newFixedThreadPool(nThreads);
	}
	public JSONArray process(Set<String> skuStockUrlList, boolean onlySizeFlag) {
		JSONArray stockArray=new JSONArray();
		if(null==skuStockUrlList||skuStockUrlList.size()==0){
			return stockArray;
		}
		List<Callable<String>> calls=new ArrayList<Callable<String>>();
		for (String stockurl : skuStockUrlList) {
			calls.add(new SelfridgesCallable(stockurl,split,true));
		}
		try {
			List<Future<String>> futures = service.invokeAll(calls);
			for (Future<String> future : futures) {
				String stockJson=future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
				String skuStockUrl=stockJson.split(split)[1];
				stockJson=stockJson.split(split)[0];
				JSONObject jsonObject = JSONObject.parseObject(stockJson);
				JSONArray skuStockArrayTemp = (JSONArray) jsonObject.get("stocks");
				for (Object object : skuStockArrayTemp) {
					JSONObject stockJsonObject = (JSONObject) object;
					if (!"Colour".equalsIgnoreCase((String) stockJsonObject.get("name")) && !onlySizeFlag) {
						// 封装 stock json color
						stockJsonObject.put("colorName", "Colour");
						stockJsonObject.put("colorValue", replaceFromStockUrl(skuStockUrl));
					}
					stockArray.add(stockJsonObject);
				}
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}finally {
			service.shutdown();
		}
		return stockArray;
	}
	
	/**
	 * 获取 链接中 color
	 * 
	 * @param skuStockUrl
	 * @return
	 */
	private Object replaceFromStockUrl(String skuStockUrl) {
		String temp = StringUtils.EMPTY;
		if (StringUtils.isBlank(skuStockUrl)) {
			return temp;
		}

		temp = StringUtils.substringBetween(skuStockUrl, "attrval=", "&");

		if (StringUtils.containsIgnoreCase(temp, "+")) {
			temp = temp.replaceAll("[+]", " ");
		}
		return temp;
	}

}
