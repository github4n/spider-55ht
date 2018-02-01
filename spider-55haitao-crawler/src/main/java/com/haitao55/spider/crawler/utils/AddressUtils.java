package com.haitao55.spider.crawler.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 功能：操作机器IP/HostName的工具类
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午5:08:17
 * @version 1.0
 */
public class AddressUtils {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	private static String IP = null;
	static {
		try {
			IP = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			logger.error("Cann't get local IP address!");
			IP = "unknown";
		}
	}

	public static String getIP() {
		return IP;
	}
}