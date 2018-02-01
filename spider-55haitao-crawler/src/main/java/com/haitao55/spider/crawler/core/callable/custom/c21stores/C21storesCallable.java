package com.haitao55.spider.crawler.core.callable.custom.c21stores;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.HttpUtils;

class C21storesCallable implements Callable<Map<String,String>> {
	private String skuId;
	private Url skuUrl;
	

	public C21storesCallable(String skuId, Url skuUrl) {
		this.skuId=skuId;
		this.skuUrl=skuUrl;
	}


	@Override
	public Map<String, String> call() {
		Map<String, String> result=new HashMap<String, String>();
		try{
//			String content = HttpUtils.get(skuUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES, false);
			String content = HttpUtils.get(skuUrl.getValue());
			result.put(skuId, content);
		}catch(HttpException e){
			e.printStackTrace();
		} catch(Exception e1){
			e1.printStackTrace();
		}
		return result;
	}
	
}

