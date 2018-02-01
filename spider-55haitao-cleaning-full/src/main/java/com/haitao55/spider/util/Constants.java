package com.haitao55.spider.util;

/**
 * 
 * 功能：系统常量；商品全量
 * 
 * @author denghuan
 * @time 2017年4月26日 下午5:49:35
 * @version 1.0
 */
public interface Constants {

	/**
	 * 统计下架数量字段名称前缀:商品offline
	 */
	public static final String CLEANING_FULL_ITEM_OFFLINE_FIELD_PREFIX = "offline_";
	
	/**
	 * 统计所有下架商品
	 */
	public static final String CLEANING_FULL_ITEM_ALL_OFFLINE_FIELD_PREFIX = "all_offline_";
	
	/**
	 * 统计商品stock = 0 ,字段名称前缀:商品总数
	 */
	public static final String CLEANING_FULL_ITEM_STOCK_ZERO_FIELD_PREFIX = "outstock_";
	
	/**
	 * 统计商品stock = 0 ,字段名称前缀:商品总数
	 */
	public static final String CLEANING_FULL_ITEM_ALL_STOCK_ZERO_FIELD_PREFIX = "all_outstock_";
	
	/**
	 * 统计在线数量字段名称前缀:商品online
	 */
	public static final String CLEANING_FULL_ITEM_ONLINE_FIELD_PREFIX = "online_";
	
	/**
	 * 统计所有在售商品
	 */
	public static final String CLEANING_FULL_ITEM_ALL_ONLINE_FIELD_PREFIX = "all_online_";
	
	
	/**
	 * 统计所有在售商品_sku_数量
	 */
	public static final String ITEM_ALL_ONLINE_SKU_SIZE_FIELD_PREFIX = "item_sku_size_";
	
	
	public static final String ITEM_ALL_ONLINE_SKU_SIZE= "item_all_online_sku_size";
	
	/**
	 * 统计总数量字段名称前缀:商品总数
	 */
	public static final String CLEANING_FULL_ITEM_TOTAL_FIELD_PREFIX = "total_";
	
	/**
	 * 分割附
	 */
	public static final String SEPARATOR_INFO_FIELDS = "##";
	
	/**
	 * 字符编码
	 */
	public static final String CHARSET_UTF8 = "utf-8";
	
	/**
	 * 导出excel名称
	 */
	public static final String EXCEL_NAME = "商家全量商品数量统计-";
	
	/**
	 * 导出文件名后缀
	 */
	public static final String EXPORT_EXCEL_FILE_SUFFIX = ".xlsx";
	
	/**
	 * 日志名称：全量日志
	 */
	public static final String LOGGER_NAME_CLEANING_FULL = "cleaning_full";
	
	/**
	 * 全量输出文件,后缀格式
	 */
	public static final String CLEANING_FULL_FILE_SUFFIX = ".json";
	
	
	/**
	 * 输出的文件名称
	 */
	public static final String OUT_PUT_TOTAL_SIZE_FILE_NAME = "total_file_size.json";
	
	
	//上架商家配置文件Key
	public static final String ONLINE_WEB_SITE_KEY = "cleaning.full.online.webSite";
	
}
