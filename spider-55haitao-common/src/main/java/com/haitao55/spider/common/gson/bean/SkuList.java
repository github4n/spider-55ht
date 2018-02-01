package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
/**
 * 
  * @ClassName: SkuList
  * @Description: sku 列表
  * @author songsong.xu
  * @date 2016年10月17日 下午6:58:40
  *
 */
public class SkuList implements Serializable{

	private static final long serialVersionUID = 1391101526121666390L;
	private String SkuId;
	private Map<String,String> Selections;
	private boolean Default;
	private Site Site;
	private ProdUrl ProdUrl;
	private Title Title;
	private Price Price;
	private Stock Stock;
	private Brand Brand;
	private List<String> BreadCrumb;
	private List<String> Category;
	private SKUImages SKUImages;
	private Map<String, Object> Properties;
	private Map<String, Object> FeatureList;
	private Map<String, Object> Description;
	public String getSkuId() {
		return SkuId;
	}
	public void setSkuId(String skuId) {
		SkuId = skuId;
	}
	public Map<String, String> getSelections() {
		return Selections;
	}
	public void setSelections(Map<String, String> selections) {
		Selections = selections;
	}
	public boolean isDefault() {
		return Default;
	}
	public void setDefault(boolean default1) {
		Default = default1;
	}
	public Site getSite() {
		return Site;
	}
	public void setSite(Site site) {
		Site = site;
	}
	public ProdUrl getProdUrl() {
		return ProdUrl;
	}
	public void setProdUrl(ProdUrl prodUrl) {
		ProdUrl = prodUrl;
	}
	public Title getTitle() {
		return Title;
	}
	public void setTitle(Title title) {
		Title = title;
	}
	public Price getPrice() {
		return Price;
	}
	public void setPrice(Price price) {
		Price = price;
	}
	public Stock getStock() {
		return Stock;
	}
	public void setStock(Stock stock) {
		Stock = stock;
	}
	public Brand getBrand() {
		return Brand;
	}
	public void setBrand(Brand brand) {
		Brand = brand;
	}
	public List<String> getBreadCrumb() {
		return BreadCrumb;
	}
	public void setBreadCrumb(List<String> breadCrumb) {
		BreadCrumb = breadCrumb;
	}
	public List<String> getCategory() {
		return Category;
	}
	public void setCategory(List<String> category) {
		Category = category;
	}
	public SKUImages getSKUImages() {
		return SKUImages;
	}
	public void setSKUImages(SKUImages sKUImages) {
		SKUImages = sKUImages;
	}
	public Map<String, Object> getProperties() {
		return Properties;
	}
	public void setProperties(Map<String, Object> properties) {
		Properties = properties;
	}
	public Map<String, Object> getFeatureList() {
		return FeatureList;
	}
	public void setFeatureList(Map<String, Object> featureList) {
		FeatureList = featureList;
	}
	public Map<String, Object> getDescription() {
		return Description;
	}
	public void setDescription(Map<String, Object> description) {
		Description = description;
	}
	@Override
	public String toString() {
		return "SkuList [SkuId=" + SkuId + ", Selections=" + Selections
				+ ", Default=" + Default + ", Site=" + Site + ", ProdUrl="
				+ ProdUrl + ", Title=" + Title + ", Price=" + Price
				+ ", Stock=" + Stock + ", Brand=" + Brand + ", BreadCrumb="
				+ BreadCrumb + ", Category=" + Category + ", SKUImages="
				+ SKUImages + ", Properties=" + Properties + ", FeatureList="
				+ FeatureList + ", Description=" + Description + "]";
	}
}
