package com.haitao55.spider.common.gson.bean;

import java.util.UUID;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年2月27日 下午7:51:51  
 */
public class HaiTunCunRetBody {
	private String docId;
	private String item;
	private Boolean available;
	private String title;
	private String link;
	private String image;
	private String c_image_400;
	private String category;
	private Double price;
	private Double msrp;
	private String brand;
	private String description;
	
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getItem() {
		return item;
	}
	public Boolean getAvailable() {
		return available;
	}
	public String getTitle() {
		return title;
	}
	public String getLink() {
		return link;
	}
	public String getImage() {
		return image;
	}
	public String getC_image_400() {
		return c_image_400;
	}
	public String getCategory() {
		return category;
	}
	public Double getPrice() {
		return price;
	}
	public Double getMsrp() {
		return msrp;
	}
	public String getBrand() {
		return brand;
	}
	public String getDescription() {
		return description;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public void setAvailable(Boolean available) {
		this.available = available;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public void setC_image_400(String c_image_400) {
		this.c_image_400 = c_image_400;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public void setMsrp(Double msrp) {
		this.msrp = msrp;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
