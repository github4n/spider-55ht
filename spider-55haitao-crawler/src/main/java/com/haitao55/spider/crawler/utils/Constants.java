package com.haitao55.spider.crawler.utils;

/**
 * 
 * 功能：系统常量；爬虫程序范围内的系统常量
 * 
 * @author Arthur.Liu
 * @time 2016年8月3日 下午5:49:35
 * @version 1.0
 */
public interface Constants {
	/**
	 * 日志名称：监控日志
	 */
	public static final String LOGGER_NAME_MONITOR = "monitor";
	/**
	 * 日志名称：系统日志
	 */
	public static final String LOGGER_NAME_SYSTEM = "system";
	/**
	 * 日志名称：爬取日志
	 */
	public static final String LOGGER_NAME_CRAWLER = "crawler";
	/**
	 * 日志名称：解析日志
	 */
	public static final String LOGGER_NAME_PARSER = "parser";
	/**
	 * 日志名称：远程日志
	 */
	public static final String LOGGER_NAME_REMOTE = "remote";
	/**
	 * 日志名称：输出日志
	 */
	public static final String LOGGER_NAME_OUTPUT = "output";
	/**
	 * 日志名称：速度控制日志
	 */
	public static final String LOGGER_NAME_TEMPO = "tempo";
	
	public static final String LOGGER_NAME_ALLITEMS = "allitems";
	
	public static final String LOGGER_NAME_AMAZON = "amazon";

	/**
	 * 监控日志字段名称前缀:错误
	 */
	public static final String CRAWLER_MONITOR_FIELD_PREFIX_ERROR = "error.";
	/**
	 * 监控日志字段名称前缀:成功
	 */
	public static final String CRAWLER_MONITOR_FIELD_PREFIX_SUCCESS = "success.";
	
	
	public static final String IS_SELF = "1";
	
	public static final String IS_NOT_SELF = "0";
	
	
	public static final String REDIS_IS_OR_NOT_SELF_SUFFIX = ".self";
	
	public static final String REDIS_PARENT_CHILD_RELATIONS_SUFFIX = ".relations";
	
}