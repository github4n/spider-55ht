package com.haitao55.spider.crawler.core.callable.custom.colehaan;

import java.util.List;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年12月21日 下午6:02:28  
 */
public class ColorImgs {
	private String color;
	private List<String> imgs;
	private String switch_img;
	public String getColor() {
		return color;
	}
	public List<String> getImgs() {
		return imgs;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public void setImgs(List<String> imgs) {
		this.imgs = imgs;
	}
	public String getSwitch_img() {
		return switch_img;
	}
	public void setSwitch_img(String switch_img) {
		this.switch_img = switch_img;
	}
	
}
