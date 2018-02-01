package com.haitao55.spider.crawler.core.callable.custom.rebeccaminkoff;

import java.util.List;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月21日 下午7:15:45  
 */
public class Color {
	private String id;
	private String value;
	private String switchImg;
	private List<String> skuids;
	private List<String> imgs;
	public String getId() {
		return id;
	}
	public String getValue() {
		return value;
	}
	public List<String> getSkuids() {
		return skuids;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setSkuids(List<String> skuids) {
		this.skuids = skuids;
	}
	public String getSwitchImg() {
		return switchImg;
	}
	public void setSwitchImg(String switchImg) {
		this.switchImg = switchImg;
	}
	public List<String> getImgs() {
		return imgs;
	}
	public void setImgs(List<String> imgs) {
		this.imgs = imgs;
	}
	
}
