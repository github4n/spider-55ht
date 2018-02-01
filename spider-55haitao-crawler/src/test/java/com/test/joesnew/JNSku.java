package com.test.joesnew;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月28日 下午3:35:30  
 */
public class JNSku {
	private String skuId;
	private String size;
	private String width;
	private int inventory;
	private float original_price;
	private float sale_price;
	private int status;
	public String getSkuId() {
		return skuId;
	}
	public String getSize() {
		return size;
	}
	public String getWidth() {
		return width;
	}
	public void setSkuId(String skuId) {
		this.skuId = skuId;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public int getInventory() {
		return inventory;
	}
	public float getOriginal_price() {
		return original_price;
	}
	public float getSale_price() {
		return sale_price;
	}
	public int getStatus() {
		return status;
	}
	public void setInventory(int inventory) {
		this.inventory = inventory;
	}
	public void setOriginal_price(float original_price) {
		this.original_price = original_price;
	}
	public void setSale_price(float sale_price) {
		this.sale_price = sale_price;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
}
