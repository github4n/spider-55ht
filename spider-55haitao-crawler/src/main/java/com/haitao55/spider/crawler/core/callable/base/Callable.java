package com.haitao55.spider.crawler.core.callable.base;

import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * 功能：callalbe 接口
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:09:20
 * @version 1.0
 */
public interface Callable {
	/**
	 * Callable插件业务功能处理方法；每个插件实际所处理的功能就在这个方法的实现中完成；通过这个统一接口，插件可以无限扩展
	 * 
	 * @param context
	 *            Callable插件上下文,主要是存放抓取和解析的key-value数据
	 * @throws Exception
	 *             抓取和解析过程中产生的异常对象
	 */
	public void invoke(Context context) throws Exception;
	
	
	public void init() throws Exception;
	
	
	public void destroy() throws Exception;
	
}