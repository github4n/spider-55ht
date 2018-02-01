package com.haitao55.spider.crawler.core.callable.custom.ralphlauren;

/**
 * @Description: 拉尔夫劳伦网站sku
 * @author: zhoushuo
 * @date: 2016年11月8日 下午3:09:24
 */
public class RalphlaurenSku {
	private String productId;
	private String skuId;
	private String sizeDesc;
	private String sizeId;
	private String colorId;
	private String colorDesc;
	private String price;
	private String avail;
	private int quantityOnHand;//现有数量
	private String releaseDate;//发布时间
	private String jdaStyle;
	private int status;
	private String switch_img;
	private String default_color;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getSkuId() {
		return skuId;
	}

	public void setSkuId(String skuId) {
		this.skuId = skuId;
	}

	public String getSizeDesc() {
		return sizeDesc;
	}

	public void setSizeDesc(String sizeDesc) {
		this.sizeDesc = sizeDesc;
	}

	public String getSizeId() {
		return sizeId;
	}

	public void setSizeId(String sizeId) {
		this.sizeId = sizeId;
	}

	public String getColorId() {
		return colorId;
	}

	public void setColorId(String colorId) {
		this.colorId = colorId;
	}

	public String getColorDesc() {
		return colorDesc;
	}

	public void setColorDesc(String colorDesc) {
		this.colorDesc = colorDesc;
	}

	public String getAvail() {
		return avail;
	}

	public void setAvail(String avail) {
		this.avail = avail;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public int getQuantityOnHand() {
		return quantityOnHand;
	}

	public void setQuantityOnHand(int quantityOnHand) {
		this.quantityOnHand = quantityOnHand;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getJdaStyle() {
		return jdaStyle;
	}

	public void setJdaStyle(String jdaStyle) {
		this.jdaStyle = jdaStyle;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getSwitch_img() {
		return switch_img;
	}

	public void setSwitch_img(String switch_img) {
		this.switch_img = switch_img;
	}

	public String getDefault_color() {
		return default_color;
	}

	public void setDefault_color(String default_color) {
		this.default_color = default_color;
	}

}
