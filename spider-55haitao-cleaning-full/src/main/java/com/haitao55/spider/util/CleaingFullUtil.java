package com.haitao55.spider.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.view.DomainCounter;

/**
 * 全量商品 工具类
 * @author denghuan
 *
 */
public class CleaingFullUtil {

	public static  List<DomainCounter> getDomainCountList(String domainPrefix){
		List<DomainCounter> doMainLIst = new ArrayList<>();
		
		DomainCounterCache counterCache = DomainCounterCache.getInstance();
		Set<Entry<String, AtomicInteger>> set = counterCache.entrySet();
		Iterator<Entry<String, AtomicInteger>> it = set.iterator();
		while(it.hasNext()){
			Entry<String, AtomicInteger> next = it.next();
			String key = next.getKey();
			AtomicInteger value = next.getValue();
			if(StringUtils.isNotBlank(key) && 
					StringUtils.startsWith(key, domainPrefix)){
				String domain = StringUtils.substringAfter(key, domainPrefix);
				doMainLIst.add(new DomainCounter(domain, value.longValue()));
			}
		}
		return doMainLIst;
	}
}
