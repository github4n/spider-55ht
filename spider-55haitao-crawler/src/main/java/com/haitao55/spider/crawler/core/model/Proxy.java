package com.haitao55.spider.crawler.core.model;

/**
 * 
 * 功能：代理IP实体类
 * 
 * @author Arthur.Liu
 * @time 2016年8月23日 下午2:51:57
 * @version 1.0
 */
public class Proxy {
	private Long id;
	private String regionId;
	private String regionName;
	private String ip;
	private int port;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}