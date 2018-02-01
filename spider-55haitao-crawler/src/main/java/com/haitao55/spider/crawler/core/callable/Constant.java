package com.haitao55.spider.crawler.core.callable;

import com.haitao55.spider.crawler.core.callable.base.Callable;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * 功能：向context中写入一个常量
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:37:11
 * @version 1.0
 */
public class Constant implements Callable {

	// 写入context的key
	private String key;

	// 写入context的value
	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void invoke(Context context) throws Exception {
		context.put(getKey(), getValue());
	}

	@Override
	public void init() throws Exception {
		
	}

	@Override
	public void destroy() throws Exception {
		
	}
}