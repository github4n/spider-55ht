package com.haitao55.spider.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商品库索引缓存，单列
 * @author denghuan
 *
 */
public class ItemDatabaseIndexCache extends ConcurrentHashMap<String, Boolean>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private static ItemDatabaseIndexCache instance = new ItemDatabaseIndexCache();
	
	private ItemDatabaseIndexCache() {}
	
	public static ItemDatabaseIndexCache getInstance() {
		return instance;
	}
}
