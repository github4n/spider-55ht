package com.haitao55.spider.crawler.core.callable.custom.shoebuy;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
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
public class ShoebuyHandler {
	private int threads = 3;
	private ExecutorService executorService;
	public ShoebuyHandler(){
		executorService = Executors.newFixedThreadPool(threads); 
	}
	public void process(JSONObject imageJSONObject, Context context) {
		if(MapUtils.isEmpty(imageJSONObject)){
			return ;
		}
		List<Callable<JSONObject>> calls = new ArrayList<Callable<JSONObject>>();
		
		for (Map.Entry<String,Object> entry : imageJSONObject.entrySet()) {
			String key = entry.getKey();
			List<Image> value = (List<Image>) entry.getValue();
			calls.add(new ShoebuyCallable(key,value,context,imageJSONObject));
		}
		try {
			List<Future<JSONObject>> futures = executorService.invokeAll(calls);
			if(CollectionUtils.isNotEmpty(futures)){
				for (Future<JSONObject> future : futures) {
					future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
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
