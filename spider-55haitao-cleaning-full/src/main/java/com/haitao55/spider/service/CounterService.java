package com.haitao55.spider.service;

public interface CounterService {
	
	/**
	 * 计数，每次累加1
	 * 
	 * @param field
	 */
	public void incField(String field);
	
	/**
	 * 计数,数量累加
	 * @param field
	 */
	public void addAndGet(String field,int count);
	
}
