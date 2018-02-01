package com.haitao55.spider.crawler.core.callable.custom.c21stores;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * 
* Title:
* Description: c21stores sku handle
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月11日 上午10:43:44
* @version 1.0
 */
public class C21storesHandler {
	
	public static final int nThreads = 3;
	private ExecutorService service;
	
	public C21storesHandler(){
		service = Executors.newFixedThreadPool(nThreads);
	}
	
	/*private static class Holder {
		public static final AmazonPriceStockHandler handler = new AmazonPriceStockHandler();
	}
	
	public static AmazonPriceStockHandler getInstance(){
		return Holder.handler;
	}*/

	public Map<String,String>  process(Map<String,Url> skuUrls, Map<String, List<String>> colorImages, Map<String, Map<String, Object>> colorPrice, Map<String, String> colorIdSkuId, List<Map<String, Object>> skuList,List<String> colorList){
		Map<String,String> result = new HashMap<String,String>(); 
		if(skuUrls == null || skuUrls.size() == 0){
			return result;
		}
		List<Callable<Map<String,String>>> calls = new ArrayList<Callable<Map<String,String>>>();
		for(Map.Entry<String, Url> skuIdUrl: skuUrls.entrySet()){
			String skuId = skuIdUrl.getKey();
			Url url = skuIdUrl.getValue();
			calls.add(new C21storesCallable(skuId,url));
		}
		try {
			List<Future<Map<String,String>>> futures = service.invokeAll(calls);
			for(Future<Map<String,String>> f : futures){
				try {
					Map<String,String> map = f.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(map != null){
						String color=StringUtils.EMPTY;
						String skuContent=StringUtils.EMPTY;
						for(Map.Entry<String, String> entry:map.entrySet()){
							color=entry.getKey();
							skuContent=entry.getValue();
						}
						Document skuDoc = Jsoup.parse(skuContent);
						Elements imageElements = skuDoc.select("img.product-details__alternate-image-button-image");
						if(CollectionUtils.isEmpty(imageElements)){
							imageElements=skuDoc.select("a.product-details__primary-image-button img");
						}
						if(CollectionUtils.isNotEmpty(imageElements)){
							C21storesUtils.colorImagesPackage(colorImages,imageElements,color);
						}
						C21storesUtils.colorPricePackage(colorPrice,skuDoc,color);
						C21storesUtils.colorIdSkuIdPackage(colorIdSkuId,skuDoc,color,colorList);
						Elements skuelements = skuDoc.select("div.size-options ul li");
						if(CollectionUtils.isNotEmpty(skuelements)){
							for (Element element : skuelements) {
								C21storesUtils.skuPackage(element,skuList,color);
							}
						}
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
