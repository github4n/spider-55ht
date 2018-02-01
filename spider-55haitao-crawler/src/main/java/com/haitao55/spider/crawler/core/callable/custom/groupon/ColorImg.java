package com.haitao55.spider.crawler.core.callable.custom.groupon;

import java.util.List;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年12月8日 上午11:33:43  
 */
public class ColorImg {
	private String color;
	private List<String> imgs;
	private String baseUrl;
	public String getColor() {
		return color;
	}
	public List<String> getImgs() {
		return imgs;
	}
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public void setImgs(List<String> imgs) {
		this.imgs = imgs;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
}
