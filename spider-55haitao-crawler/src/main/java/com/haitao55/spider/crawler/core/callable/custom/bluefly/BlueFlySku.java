/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: BlueFlySku.java 
 * @Prject: spider-55haitao-crawler
 * @Package: com.test.bluefly 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年10月31日 下午5:18:57 
 * @version: V1.0   
 */
package com.haitao55.spider.crawler.core.callable.custom.bluefly;

import java.util.List;

/** 
 * @ClassName: BlueFlySku 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年10月31日 下午5:18:57  
 */
public class BlueFlySku {
	private String goodId;
	private String switchStyleImg;
	private String color;
	private List<String> imgUrls;
	private List<BlueFlySize> sizeList;
	
	public String getGoodId() {
		return goodId;
	}
	public void setGoodId(String goodId) {
		this.goodId = goodId;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public List<BlueFlySize> getSizeList() {
		return sizeList;
	}
	public void setSizeList(List<BlueFlySize> sizeList) {
		this.sizeList = sizeList;
	}
	public String getSwitchStyleImg() {
		return switchStyleImg;
	}
	public void setSwitchStyleImg(String switchStyleImg) {
		this.switchStyleImg = switchStyleImg;
	}
	public List<String> getImgUrls() {
		return imgUrls;
	}
	public void setImgUrls(List<String> imgUrls) {
		this.imgUrls = imgUrls;
	}
	
}
