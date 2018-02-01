package com.haitao55.spider.ui.cache;

import java.util.LinkedHashSet;
import java.util.regex.Pattern;

/**
 * 活动商品查询　域名cache　用于进行阿里云　谷歌云地址切换
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月20日 下午3:03:51
* @version 1.0
 */
public class ActivityItemsIgnoreDomainCache extends LinkedHashSet<Pattern> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ActivityItemsIgnoreDomainCache(){}
	
	//static inner class
	private static class IgnoreDomainCacheHolder{
		private static ActivityItemsIgnoreDomainCache ignoreDomain=new ActivityItemsIgnoreDomainCache();
	}

	
	public static ActivityItemsIgnoreDomainCache getInstance(){
		return IgnoreDomainCacheHolder.ignoreDomain;
	}
}
