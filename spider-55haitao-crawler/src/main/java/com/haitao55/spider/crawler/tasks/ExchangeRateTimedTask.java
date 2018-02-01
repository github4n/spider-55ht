package com.haitao55.spider.crawler.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.utils.ExchangeRateUtils;

/**
 * 汇率定时任务
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月29日 下午3:32:28
* @version 1.0
 */
public class ExchangeRateTimedTask {
	private static final Logger logger = LoggerFactory.getLogger(ExchangeRateTimedTask.class);

	/**
	 * 定时进行汇率接口访问，cache缓存起来  用作业务使用
	 */
	public void reloadExchangeRate() {
		logger.info("ExchangeRateTimedTask reloadExchangeRate 汇率定时执行");
		//获取相关汇率数据
		ExchangeRateUtils.exchangeRate();
	}
}