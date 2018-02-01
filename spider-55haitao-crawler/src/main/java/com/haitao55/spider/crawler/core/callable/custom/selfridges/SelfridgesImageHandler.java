package com.haitao55.spider.crawler.core.callable.custom.selfridges;

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

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.core.model.Image;
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

public class SelfridgesImageHandler {
	private static String split="&&";
	private static String imageUrlPreffix = "http://images.selfridges.com/is/image//";
	public static final int nThreads = 3;
	private ExecutorService service;
	
	public SelfridgesImageHandler(){
		service = Executors.newFixedThreadPool(nThreads);
	}
	public Map<String, List<Image>> process(List<String> skuImageUrlList) {
		Map<String,List<Image>> map=new HashMap<String,List<Image>>();
		if(null==skuImageUrlList||skuImageUrlList.size()==0){
			return map;
		}
		List<Callable<String>> calls=new ArrayList<Callable<String>>();
		for (String imageurl : skuImageUrlList) {
			calls.add(new SelfridgesCallable(imageurl,split,true));
		}
		try {
			List<Future<String>> futures = service.invokeAll(calls);
			for (Future<String> future : futures) {
				String imageJson=future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
				String skuImageUrl=imageJson.split(split)[1];
				List<Image> colorImageList = new ArrayList<Image>();
				imageJson = StringUtils.substringBetween(imageJson, "(", ",\"\");");
				JSONObject jsonObject = JSONObject.parseObject(imageJson);

				// json封装太深
				String item = StringUtils.EMPTY;
				Object object = jsonObject.get("set");
				jsonObject = (JSONObject) object;
				object = jsonObject.get("item");
				jsonObject = (JSONObject) object;
				object = jsonObject.get("set");
				jsonObject = (JSONObject) object;
				object = jsonObject.get("item");
				try {
					item = StringUtils.substringBetween(object.toString(), "\"item\":", "],").concat("]");
				} catch (NullPointerException e) {
					try {
						JSONArray parseArray = JSONObject.parseArray(object.toString());
						object = parseArray.get(1);
						jsonObject = (JSONObject) object;
						object = jsonObject.get("set");
						jsonObject = (JSONObject) object;
						object = jsonObject.get("item");
						StringBuffer buffer = new StringBuffer();
						buffer.append("[").append(object.toString()).append("]");
						item = buffer.toString();
					} catch (NullPointerException e2) {
						// logger.error("sku image is empty url:{} skuimageUrl
						// :{}",context.getCurrentUrl(),skuImageUrl);
					}
				}
				JSONArray imageArray = JSONArray.parseArray(item);
				if (null != imageArray && imageArray.size() > 0) {
					JSONObject firstImageJson = (JSONObject) imageArray.get(imageArray.size() - 1);
					firstImageJson = (JSONObject) firstImageJson.get("s");
					colorImageList.add(new Image(imageUrlPreffix.concat((String) firstImageJson.get("n")).concat("?$PDP_M_ALL$")));
					for (int i = 0; i < imageArray.size() - 1; i++) {
						JSONObject jbt = (JSONObject) imageArray.get(i);
						jbt = (JSONObject) jbt.get("s");
						String imgUrl = (String) jbt.get("n");
						colorImageList.add(new Image(imageUrlPreffix.concat(imgUrl).concat("?$PDP_M_ALL$")));
					}
					String colorValue = StringUtils.substringBetween(skuImageUrl, "_", "_");
					map.put(colorValue, colorImageList);
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
		return map;
	}
}
