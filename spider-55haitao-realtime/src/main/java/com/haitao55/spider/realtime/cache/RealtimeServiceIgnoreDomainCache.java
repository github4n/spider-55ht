package com.haitao55.spider.realtime.cache;

import java.util.LinkedHashSet;
import java.util.regex.Pattern;

/**
 * 
* Title: 
* Description: 实时核价, 加载配置域名,用于访问realtime service接口 实时核价,返回json
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月22日 下午8:46:12
* @version 1.0
 */
public class RealtimeServiceIgnoreDomainCache extends LinkedHashSet<Pattern> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private RealtimeServiceIgnoreDomainCache(){}
	
	//static inner class
	private static class IgnoreDomainCacheHolder{
		private static RealtimeServiceIgnoreDomainCache ignoreDomain=new RealtimeServiceIgnoreDomainCache();
	}

	
	public static RealtimeServiceIgnoreDomainCache getInstance(){
		return IgnoreDomainCacheHolder.ignoreDomain;
	}
}
