package com.haitao55.spider.crawler.utils;

import java.lang.management.ManagementFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 功能：进程工具类
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午5:09:44
 * @version 1.0
 */
public class ProcessUtils {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	private static String PROCESS_ID = null;
	static {
		try {
			String name = ManagementFactory.getRuntimeMXBean().getName();
			PROCESS_ID = StringUtils.substringBefore(name, "@");
		} catch (Exception e) {
			logger.error("init pid error", e);
			PROCESS_ID = "unknown";
		}
	}

	/**
	 * 返回当前java进程的process id
	 * 
	 * @return
	 */
	public static String getPid() {
		return PROCESS_ID;
	}
}