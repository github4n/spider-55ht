package com.test.sportdirect;

import java.util.List;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年5月25日 下午3:59:27  
 */
public class Style {
	private String color;
	private String switch_img;
	private String goodId;
	private List<String> imgs;
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
	public String getGoodId() {
		return goodId;
	}
	public void setGoodId(String goodId) {
		this.goodId = goodId;
	}
	
}
