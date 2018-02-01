package com.haitao55.spider.service.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.haitao55.spider.service.CounterService;
import com.haitao55.spider.util.DomainCounterCache;

/**
 * 计算器servie接口实现类
 * @author denghuan
 * @time 2017年4月26日 下午3:18:03
 *
 */
public class CounterServiceImpl implements CounterService{
	
	public void incField(String field){
		DomainCounterCache counterCache = DomainCounterCache.getInstance();
		if (!counterCache.containsKey(field)) {
			counterCache.put(field, new AtomicInteger(0));
		}

		counterCache.get(field).incrementAndGet();
	}

	@Override
	public void addAndGet(String field, int count) {
		DomainCounterCache counterCache = DomainCounterCache.getInstance();
		if (!counterCache.containsKey(field)) {
			counterCache.put(field, new AtomicInteger(0));
		}
		counterCache.get(field).addAndGet(count);
	}
}
