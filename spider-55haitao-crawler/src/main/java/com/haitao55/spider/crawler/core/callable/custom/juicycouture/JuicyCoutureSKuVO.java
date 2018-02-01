package com.haitao55.spider.crawler.core.callable.custom.juicycouture;

public class JuicyCoutureSKuVO {

	private String salePrice;
	private String origPrice;
	private String price;//这个Price有的时候做为saleprice,目标官网
	public String getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(String salePrice) {
		this.salePrice = salePrice;
	}
	public String getOrigPrice() {
		return origPrice;
	}
	public void setOrigPrice(String origPrice) {
		this.origPrice = origPrice;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	
	
}
