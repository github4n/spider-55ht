package com.haitao55.spider.common.gson.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.haitao55.spider.common.gson.JsonUtils;

/**
 * 
 * @ClassName: RetBody
 * @Description: json中的body内容节点
 * @author songsong.xu
 * @date 2016年9月20日 下午2:29:18
 *
 */
public class RetBody {

	private String DOCID;
	private Site Site;
	private ProdUrl ProdUrl;
	private Title Title;
	private Price Price;
	private Stock Stock;
	private Brand Brand;
	private List<String> BreadCrumb;
	private List<String> Category;
	private LImageList Image;
	private Map<String, Object> Properties;
	private Map<String, Object> FeatureList;
	private Map<String, Object> Description;
	private Sku Sku;

	public RetBody() {
		super();
	}

	public RetBody(String dOCID, com.haitao55.spider.common.gson.bean.Site site,
			com.haitao55.spider.common.gson.bean.ProdUrl prodUrl, com.haitao55.spider.common.gson.bean.Title title,
			com.haitao55.spider.common.gson.bean.Price price, com.haitao55.spider.common.gson.bean.Stock stock,
			com.haitao55.spider.common.gson.bean.Brand brand, List<String> breadCrumb, List<String> category,
			LImageList image, Map<String, Object> properties, Map<String, Object> featureList,
			Map<String, Object> description, com.haitao55.spider.common.gson.bean.Sku sku) {
		super();
		DOCID = dOCID;
		Site = site;
		ProdUrl = prodUrl;
		Title = title;
		Price = price;
		Stock = stock;
		Brand = brand;
		BreadCrumb = breadCrumb;
		Category = category;
		Image = image;
		Properties = properties;
		FeatureList = featureList;
		Description = description;
		Sku = sku;
	}

	public String getDOCID() {
		return DOCID;
	}

	public void setDOCID(String dOCID) {
		DOCID = dOCID;
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

	public LImageList getImage() {
		return Image;
	}

	public void setImage(LImageList image) {
		Image = image;
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

	public Sku getSku() {
		return Sku;
	}

	public void setSku(Sku sku) {
		Sku = sku;
	}

	@Override
	public String toString() {
		return "CrawlerJSONResult [DOCID=" + DOCID + ", Site=" + Site + ", ProdUrl=" + ProdUrl + ", Title=" + Title
				+ ", Price=" + Price + ", Stock=" + Stock + ", Brand=" + Brand + ", BreadCrumb=" + BreadCrumb
				+ ", Category=" + Category + ", Image=" + Image + ", Properties=" + Properties + ", FeatureList="
				+ FeatureList + ", Description=" + Description + ", Sku=" + Sku + "]";
	}

	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		list.add("c");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("s_weight", "8 oz");
		map.put("s_gender", "Womens");
		map.put("a", 1);
		map.put("b", 11.11);

		// System.out.println(JsonUtils.bean2json(new
		// CrawlerJSONResult("a1111",new Site("www.6pm.com"),new
		// ProdUrl("http://www.6pm.com/ivanka-trump-kayden-4-black-patent",
		// 1446379863, 847538),list,map)));
	}

	public String parseTo() {
		return JsonUtils.bean2json(this);
	}

	public static RetBody buildFrom(String json) {
		return JsonUtils.json2bean(json, RetBody.class);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		RetBody other = (RetBody) obj;
		
		boolean priceFlag = this.getPrice().equals(other.getPrice());
		
		boolean titleFlag = this.getTitle().equals(other.getTitle());
		
		boolean statusFlag = this.getStock().equals(other.getStock());
		
		if(priceFlag && titleFlag && statusFlag){
			return this.getSku().equals(other.getSku());
		}else{
			return false;
		}
		
	}		
}