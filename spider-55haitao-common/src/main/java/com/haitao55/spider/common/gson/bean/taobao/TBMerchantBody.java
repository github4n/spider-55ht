package com.haitao55.spider.common.gson.bean.taobao;

import com.haitao55.spider.common.gson.JsonUtils;

/**
 * 商家店铺信息字段
 * @author denghuan
 *
 */
public class TBMerchantBody {

	private String DOCID;
	private String url;//店铺Url
	private String searchKeyWord;//店铺Url
	private String shopName;//店铺名
	private String wangWangName;
	private String sellerCredit;//卖家信用
	private String sellerGrade;//卖家等级
	private String buyersCredit;//买家信用
	private String goodRate;//好评率
	private String salesVolume;//销量
	private int successCount;//成功数
	private int failCount;//失败数
	private String column ;//比列
	
	public TBMerchantBody() {
		super();
	}

	
	public TBMerchantBody(String dOCID, String url, String searchKeyWord, String shopName, String wangWangName,
			String sellerCredit, String sellerGrade, String buyersCredit, String goodRate, String salesVolume,
			int successCount, int failCount, String column) {
		super();
		DOCID = dOCID;
		this.url = url;
		this.searchKeyWord = searchKeyWord;
		this.shopName = shopName;
		this.wangWangName = wangWangName;
		this.sellerCredit = sellerCredit;
		this.sellerGrade = sellerGrade;
		this.buyersCredit = buyersCredit;
		this.goodRate = goodRate;
		this.salesVolume = salesVolume;
		this.successCount = successCount;
		this.failCount = failCount;
		this.column = column;
	}


	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public int getFailCount() {
		return failCount;
	}

	public void setFailCount(int failCount) {
		this.failCount = failCount;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getSearchKeyWord() {
		return searchKeyWord;
	}

	public void setSearchKeyWord(String searchKeyWord) {
		this.searchKeyWord = searchKeyWord;
	}

	public String getDOCID() {
		return DOCID;
	}
	public void setDOCID(String dOCID) {
		DOCID = dOCID;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	public String getWangWangName() {
		return wangWangName;
	}
	public void setWangWangName(String wangWangName) {
		this.wangWangName = wangWangName;
	}
	public String getSellerCredit() {
		return sellerCredit;
	}
	public void setSellerCredit(String sellerCredit) {
		this.sellerCredit = sellerCredit;
	}
	public String getSellerGrade() {
		return sellerGrade;
	}
	public void setSellerGrade(String sellerGrade) {
		this.sellerGrade = sellerGrade;
	}
	
	public String getBuyersCredit() {
		return buyersCredit;
	}

	public void setBuyersCredit(String buyersCredit) {
		this.buyersCredit = buyersCredit;
	}

	public String getGoodRate() {
		return goodRate;
	}
	public void setGoodRate(String goodRate) {
		this.goodRate = goodRate;
	}
	
	public String getSalesVolume() {
		return salesVolume;
	}

	public void setSalesVolume(String salesVolume) {
		this.salesVolume = salesVolume;
	}

	public String parseTo() {
		return JsonUtils.bean2json(this);
	}

	public static TBMerchantBody buildFrom(String json) {
		return JsonUtils.json2bean(json, TBMerchantBody.class);
	}
}
