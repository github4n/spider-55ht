package com.haitao55.spider.common.dos;
/**
 * 
* Title: 接收地区枚举
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年8月30日 上午11:01:38
* @version 1.0
 */
public class ProxyBean {
	private String code;
	private String region;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	@Override
	public String toString() {
		return "ProxyBean [code=" + code + ", region=" + region + "]";
	}
	
}
