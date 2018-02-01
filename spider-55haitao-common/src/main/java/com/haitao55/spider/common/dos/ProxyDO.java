package com.haitao55.spider.common.dos;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * 
* Title: 代理IP模块的数据库表映射类,在service层与ProxyView对象做转换
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年8月30日 上午11:01:38
* @version 1.0
 */
@Table(name="proxy")
public class ProxyDO {
	@Id
	private Long id;
	@Column
	private String regionId;
	@Column
	private String regionName;
	@Column
	private String ip;
	@Column
	private Integer port;

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

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
}