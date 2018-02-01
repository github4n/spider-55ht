package com.haitao55.spider.common.gson.bean.taobao;


import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.LImageList;

/**
 * 淘宝代购网站 数据json节点
* Title:
* Description:
* Company: 55海淘
* @author denghuan 
* @date 2017年3月8日 上午11:10:56
* @version 1.0
 */
public class TBRetBody {
	private String DOCID;
	private String productUrl;
	private String brand;//品牌
	private String title;//标题
	private String salePrice;//商品价格
	private String origPrice;//商品原价
	private String referencePrice;//国内参考价
	private String directLink;//直达链接
	private String weight;//重量
	private String internationalTransferFee;//国际转运费
	private String overseasLogisticsFee;//境外物流费用
	private String logisticsSpeed;//物流速度
	private String productFrom;//商品来自
	private String productMall;//商城简介
	private String description;
	private String category;
	private LImageList Image;
	private TBType type;
	
	public TBRetBody() {
		super();
	}

	public TBRetBody(String dOCID, String productUrl, String brand, String title, String salePrice, String origPrice,
			String referencePrice, String directLink, String weight, String internationalTransferFee,
			String overseasLogisticsFee, String logisticsSpeed, String productFrom, String productMall,
			String description, String category, LImageList image,TBType type) {
		super();
		DOCID = dOCID;
		this.productUrl = productUrl;
		this.brand = brand;
		this.title = title;
		this.salePrice = salePrice;
		this.origPrice = origPrice;
		this.referencePrice = referencePrice;
		this.directLink = directLink;
		this.weight = weight;
		this.internationalTransferFee = internationalTransferFee;
		this.overseasLogisticsFee = overseasLogisticsFee;
		this.logisticsSpeed = logisticsSpeed;
		this.productFrom = productFrom;
		this.productMall = productMall;
		this.description = description;
		this.category = category;
		Image = image;
		this.type = type;
	}

	public TBType getType() {
		return type;
	}

	public void setType(TBType type) {
		this.type = type;
	}

	public String getDOCID() {
		return DOCID;
	}
	public void setDOCID(String dOCID) {
		DOCID = dOCID;
	}

	public String getProductUrl() {
		return productUrl;
	}

	public void setProductUrl(String productUrl) {
		this.productUrl = productUrl;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

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

	public String getReferencePrice() {
		return referencePrice;
	}

	public void setReferencePrice(String referencePrice) {
		this.referencePrice = referencePrice;
	}

	public String getDirectLink() {
		return directLink;
	}

	public void setDirectLink(String directLink) {
		this.directLink = directLink;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getInternationalTransferFee() {
		return internationalTransferFee;
	}

	public void setInternationalTransferFee(String internationalTransferFee) {
		this.internationalTransferFee = internationalTransferFee;
	}

	public String getOverseasLogisticsFee() {
		return overseasLogisticsFee;
	}

	public void setOverseasLogisticsFee(String overseasLogisticsFee) {
		this.overseasLogisticsFee = overseasLogisticsFee;
	}

	public String getLogisticsSpeed() {
		return logisticsSpeed;
	}

	public void setLogisticsSpeed(String logisticsSpeed) {
		this.logisticsSpeed = logisticsSpeed;
	}

	public String getProductFrom() {
		return productFrom;
	}

	public void setProductFrom(String productFrom) {
		this.productFrom = productFrom;
	}

	public String getProductMall() {
		return productMall;
	}

	public void setProductMall(String productMall) {
		this.productMall = productMall;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public LImageList getImage() {
		return Image;
	}

	public void setImage(LImageList image) {
		Image = image;
	}
	
	public String parseTo() {
		return JsonUtils.bean2json(this);
	}

	public static TBRetBody buildFrom(String json) {
		return JsonUtils.json2bean(json, TBRetBody.class);
	}

}