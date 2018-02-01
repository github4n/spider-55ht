package com.haitao55.spider.ui.view;

/**
 * 
 * 功能：为页面展示用的数据对象
 * 
 * @author denghuan
 * @time 2017年1月17日 下午4:30:28
 * @version 1.0
 */
public class ItemHistoryView {

	private Float salePrice;
	private Float origPrice;
	private String skus;
	private String createTime;
	
	public Float getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(Float salePrice) {
		this.salePrice = salePrice;
	}
	public Float getOrigPrice() {
		return origPrice;
	}
	public void setOrigPrice(Float origPrice) {
		this.origPrice = origPrice;
	}
	public String getSkus() {
		return skus;
	}
	public void setSkus(String skus) {
		this.skus = skus;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	
}
