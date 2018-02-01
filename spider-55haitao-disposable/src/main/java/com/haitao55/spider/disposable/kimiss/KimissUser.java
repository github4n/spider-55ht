package com.haitao55.spider.disposable.kimiss;

/**
 * 封装马甲帐号，　图片地址
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年12月20日 下午2:28:40
* @version 1.0
 */
public class KimissUser {
	private String name;
	private String src;
    private String cdn;
    
	public KimissUser() {
		super();
	}
	public KimissUser(String name, String src, String cdn) {
		super();
		this.name = name;
		this.src = src;
		this.cdn = cdn;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getCdn() {
		return cdn;
	}
	public void setCdn(String cdn) {
		this.cdn = cdn;
	}
}
