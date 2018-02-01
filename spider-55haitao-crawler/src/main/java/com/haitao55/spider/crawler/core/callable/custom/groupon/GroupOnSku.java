package com.haitao55.spider.crawler.core.callable.custom.groupon;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年12月7日 下午3:40:47  
 */
public class GroupOnSku {
	private String skuid;
	private String color;
	private String size;
	private String unit;
	private float originalPrice;
	private float salePrice;
	private int stock;
	private int stock_status;
	public String getSkuid() {
		return skuid;
	}
	public String getColor() {
		return color;
	}
	public String getSize() {
		return size;
	}
	public String getUnit() {
		return unit;
	}
	public int getStock() {
		return stock;
	}
	public int getStock_status() {
		return stock_status;
	}
	public void setSkuid(String skuid) {
		this.skuid = skuid;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	public void setStock_status(int stock_status) {
		this.stock_status = stock_status;
	}
	public float getOriginalPrice() {
		return originalPrice;
	}
	public float getSalePrice() {
		return salePrice;
	}
	public void setOriginalPrice(float originalPrice) {
		this.originalPrice = originalPrice;
	}
	public void setSalePrice(float salePrice) {
		this.salePrice = salePrice;
	}
	
}
