/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: BlueFlySize.java 
 * @Prject: spider-55haitao-crawler
 * @Package: com.test.bluefly 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年10月31日 下午5:22:59 
 * @version: V1.0   
 */
package com.test.bluefly;

/** 
 * @ClassName: BlueFlySize 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年10月31日 下午5:22:59  
 */
public class BlueFlySize {
	private String id;
	private String goodId;
	private String name;
	private int inventory;
	private float original_price;
	private float sale_price;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGoodId() {
		return goodId;
	}
	public void setGoodId(String goodId) {
		this.goodId = goodId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getInventory() {
		return inventory;
	}
	public void setInventory(int inventory) {
		this.inventory = inventory;
	}
	public float getOriginal_price() {
		return original_price;
	}
	public void setOriginal_price(float original_price) {
		this.original_price = original_price;
	}
	public float getSale_price() {
		return sale_price;
	}
	public void setSale_price(float sale_price) {
		this.sale_price = sale_price;
	}
	
}
