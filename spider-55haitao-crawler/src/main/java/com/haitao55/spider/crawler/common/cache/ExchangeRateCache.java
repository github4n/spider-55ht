package com.haitao55.spider.crawler.common.cache;

import java.util.concurrent.ConcurrentHashMap;
/**
 * 汇率  缓存类   
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月29日 下午3:48:49
* @version 1.0
 */
public class ExchangeRateCache extends ConcurrentHashMap<String, Float> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4971111669910337516L;

	private ExchangeRateCache(){
		
	}
	
	private static class ExchangeRateHold{
		private static final ExchangeRateCache exchangeRateCache = new ExchangeRateCache();
	}

	public static ExchangeRateCache getInstance(){
		return ExchangeRateHold.exchangeRateCache;
	}
}
