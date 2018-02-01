package com.haitao55.spider.crawler.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.common.cache.ExchangeRateCache;

/**
 * 汇率相关工具类 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月30日 下午4:00:28
 * @version 1.0
 */
public class ExchangeRateUtils {
	// 汇率调用接口地址
	private static final String url = "http://mallapi.55haitao.com/minishop-api/exchange_rate_list";

	/**
	 * 调用接口进行汇率获取 cache
	 */
	public static void exchangeRate() {
		String content = HttpUtils.get(url);
		if (StringUtils.isNotBlank(content)) {
			JSONObject parseObject = JSONObject.parseObject(content);
			// 不同货币相关汇率 jsonarray
			JSONArray exchangeRateJSONArray = parseObject.getJSONArray("data");
			if (CollectionUtils.isNotEmpty(exchangeRateJSONArray)) {
				for (Object object : exchangeRateJSONArray) {
					JSONObject exchangeRateJSONObject = (JSONObject) object;
					String skey = exchangeRateJSONObject.getString("skey");
					String cache_key = StringUtils.substringBefore(skey, "2CNY");
					float svalue = exchangeRateJSONObject.getFloatValue("svalue");
					ExchangeRateCache.getInstance().put(cache_key, svalue);
				}
			}
		}
	}

	/**
	 * 根据price具体值进行汇率上浮 不改变缓存中到汇率值，值相应上浮price值
	 * 
	 * @param price
	 *            价格
	 * @return
	 */
	public static float rateUp(float price) {
		float updaterate = 1.0f;
		if(price>13000){
			updaterate = 1.02f;
		}else if(price>5000){
			updaterate = 1.03f;
		}else if(price>3000){
			updaterate = 1.04f;
		}else{
			updaterate = 1.05f;
		}
		return price*updaterate;
	}
	
	public static void main(String[] args) {
		float rateUp = ExchangeRateUtils.rateUp(12000.01f);
		System.out.println(rateUp);
	}
}
