package com.haitao55.spider.crawler.core.callable.custom.skinstore;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.haitao55.spider.crawler.core.model.Image;

public class SkinStoreSkuBean implements Serializable {

	private static final long serialVersionUID = -4253958376097997690L;

	private String sale;
	private String orig;
	private String unit;
	private String save;
	private String skuId;
	private int stockNum;
	private int stockStatus;
	private String isSelf;
	private String url;

	private String brand;
	private String title;
	private List<String> cats;
	private List<String> breads;
	private Map<String, Object> featureMap;
	private Map<String, Object> descMap;
	private Map<String, Object> propMap;

	private List<Image> imageUrl;

	private String skuRel;

	private String cate_name;

	private String styleId;

	private List<Map<String, String>> selections;

	public SkinStoreSkuBean() {
	}

	public SkinStoreSkuBean(String skuId) {
		super();
		this.skuId = skuId;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getSale() {
		return sale;
	}

	public void setSale(String sale) {
		this.sale = sale;
	}

	public String getOrig() {
		return orig;
	}

	public void setOrig(String orig) {
		this.orig = orig;
	}

	public String getSave() {
		return save;
	}

	public void setSave(String save) {
		this.save = save;
	}

	public int getStockNum() {
		return stockNum;
	}

	public void setStockNum(int stockNum) {
		this.stockNum = stockNum;
	}

	public int getStockStatus() {
		return stockStatus;
	}

	public void setStockStatus(int stockStatus) {
		this.stockStatus = stockStatus;
	}

	public String getSkuId() {
		return skuId;
	}

	public void setSkuId(String skuId) {
		this.skuId = skuId;
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

	public List<String> getCats() {
		return cats;
	}

	public void setCats(List<String> cats) {
		this.cats = cats;
	}

	public List<String> getBreads() {
		return breads;
	}

	public void setBreads(List<String> breads) {
		this.breads = breads;
	}

	public Map<String, Object> getFeatureMap() {
		return featureMap;
	}

	public void setFeatureMap(Map<String, Object> featureMap) {
		this.featureMap = featureMap;
	}

	public Map<String, Object> getDescMap() {
		return descMap;
	}

	public void setDescMap(Map<String, Object> descMap) {
		this.descMap = descMap;
	}

	public Map<String, Object> getPropMap() {
		return propMap;
	}

	public void setPropMap(Map<String, Object> propMap) {
		this.propMap = propMap;
	}

	public String getIsSelf() {
		return isSelf;
	}

	public void setIsSelf(String isSelf) {
		this.isSelf = isSelf;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Image> getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(List<Image> imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getSkuRel() {
		return skuRel;
	}

	public void setSkuRel(String skuRel) {
		this.skuRel = skuRel;
	}

	public String getCate_name() {
		return cate_name;
	}

	public void setCate_name(String cate_name) {
		this.cate_name = cate_name;
	}

	public String getStyleId() {
		return styleId;
	}

	public void setStyleId(String styleId) {
		this.styleId = styleId;
	}

	public List<Map<String, String>> getSelections() {
		return selections;
	}

	public void setSelections(List<Map<String, String>> selections) {
		this.selections = selections;
	}

	@Override
	public String toString() {
		return "SkinStoreSkuBean [sale=" + sale + ", orig=" + orig + ", unit=" + unit + ", save=" + save + ", skuId="
				+ skuId + ", stockNum=" + stockNum + ", stockStatus=" + stockStatus + ", isSelf=" + isSelf + ", url="
				+ url + ", brand=" + brand + ", title=" + title + ", cats=" + cats + ", breads=" + breads
				+ ", featureMap=" + featureMap + ", descMap=" + descMap + ", propMap=" + propMap + ", imageUrl="
				+ imageUrl + ", skuRel=" + skuRel + ", cate_name=" + cate_name + ", styleId=" + styleId + "]";
	}

}
