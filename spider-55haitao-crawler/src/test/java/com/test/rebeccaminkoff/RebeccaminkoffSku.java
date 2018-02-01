package com.test.rebeccaminkoff;

import java.util.List;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月22日 上午10:54:59  
 */
public class RebeccaminkoffSku {
	private String skuid;
	private String colorId;
	private String colorName;
	private String sizeId;
	private String sizeName;
	private float originalPrice;
	private float salePrice;
	private String unit;
	private int status;
	private int inventory;
	private List<String> imgs;
	public String getSkuid() {
		return skuid;
	}
	public String getColorId() {
		return colorId;
	}
	public String getColorName() {
		return colorName;
	}
	public String getSizeId() {
		return sizeId;
	}
	public String getSizeName() {
		return sizeName;
	}
	public float getOriginalPrice() {
		return originalPrice;
	}
	public float getSalePrice() {
		return salePrice;
	}
	public int getStatus() {
		return status;
	}
	public int getInventory() {
		return inventory;
	}
	public void setSkuid(String skuid) {
		this.skuid = skuid;
	}
	public void setColorId(String colorId) {
		this.colorId = colorId;
	}
	public void setColorName(String colorName) {
		this.colorName = colorName;
	}
	public void setSizeId(String sizeId) {
		this.sizeId = sizeId;
	}
	public void setSizeName(String sizeName) {
		this.sizeName = sizeName;
	}
	public void setOriginalPrice(float originalPrice) {
		this.originalPrice = originalPrice;
	}
	public void setSalePrice(float salePrice) {
		this.salePrice = salePrice;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public void setInventory(int inventory) {
		this.inventory = inventory;
	}
	public List<String> getImgs() {
		return imgs;
	}
	public void setImgs(List<String> imgs) {
		this.imgs = imgs;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
}
