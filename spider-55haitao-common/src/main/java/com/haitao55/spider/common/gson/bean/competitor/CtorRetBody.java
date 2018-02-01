package com.haitao55.spider.common.gson.bean.competitor;

import java.util.List;
import java.util.Map;

import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.Site;

/**
 * 竞品网站 数据json节点
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月21日 上午11:10:56
* @version 1.0
 */
public class CtorRetBody {
	private String DOCID;
	private Site Site;
	private CtorProdUrl CtorProdUrl;
	private Tag Tag;
	private Mall Mall;
	private Brand Brand;
	private CtorTitle CtorTitle;
	private PromoCode PromoCode;
	private List<String> Category;
	private LImageList Image;
	private Map<String, Object> FeatureList;
	
	public CtorRetBody() {
		super();
	}

	public CtorRetBody(String dOCID, com.haitao55.spider.common.gson.bean.Site site,
			com.haitao55.spider.common.gson.bean.competitor.CtorProdUrl ctorProdUrl,
			com.haitao55.spider.common.gson.bean.competitor.Tag tag,
			com.haitao55.spider.common.gson.bean.competitor.Mall mall, com.haitao55.spider.common.gson.bean.Brand brand,
			com.haitao55.spider.common.gson.bean.competitor.CtorTitle ctorTitle,
			com.haitao55.spider.common.gson.bean.competitor.PromoCode promoCode, List<String> category,
			LImageList image, Map<String, Object> featureList) {
		super();
		DOCID = dOCID;
		Site = site;
		CtorProdUrl = ctorProdUrl;
		Tag = tag;
		Mall = mall;
		Brand = brand;
		CtorTitle = ctorTitle;
		PromoCode = promoCode;
		Category = category;
		Image = image;
		FeatureList = featureList;
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
	public CtorProdUrl getCtorProdUrl() {
		return CtorProdUrl;
	}
	public void setCtorProdUrl(CtorProdUrl ctorProdUrl) {
		CtorProdUrl = ctorProdUrl;
	}
	public Tag getTag() {
		return Tag;
	}
	public void setTag(Tag tag) {
		Tag = tag;
	}
	public Mall getMall() {
		return Mall;
	}
	public void setMall(Mall mall) {
		Mall = mall;
	}
	public Brand getBrand() {
		return Brand;
	}
	public void setBrand(Brand brand) {
		Brand = brand;
	}
	public CtorTitle getCtorTitle() {
		return CtorTitle;
	}
	public void setCtorTitle(CtorTitle ctorTitle) {
		CtorTitle = ctorTitle;
	}
	public PromoCode getPromoCode() {
		return PromoCode;
	}
	public void setPromoCode(PromoCode promoCode) {
		PromoCode = promoCode;
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
	
	public Map<String, Object> getFeatureList() {
		return FeatureList;
	}

	public void setFeatureList(Map<String, Object> featureList) {
		FeatureList = featureList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Brand == null) ? 0 : Brand.hashCode());
		result = prime * result + ((Category == null) ? 0 : Category.hashCode());
		result = prime * result + ((CtorProdUrl == null) ? 0 : CtorProdUrl.hashCode());
		result = prime * result + ((CtorTitle == null) ? 0 : CtorTitle.hashCode());
		result = prime * result + ((DOCID == null) ? 0 : DOCID.hashCode());
		result = prime * result + ((Image == null) ? 0 : Image.hashCode());
		result = prime * result + ((Mall == null) ? 0 : Mall.hashCode());
		result = prime * result + ((PromoCode == null) ? 0 : PromoCode.hashCode());
		result = prime * result + ((Site == null) ? 0 : Site.hashCode());
		result = prime * result + ((Tag == null) ? 0 : Tag.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CtorRetBody other = (CtorRetBody) obj;
		if (Brand == null) {
			if (other.Brand != null)
				return false;
		} else if (!Brand.equals(other.Brand))
			return false;
		if (Category == null) {
			if (other.Category != null)
				return false;
		} else if (!Category.equals(other.Category))
			return false;
		if (CtorProdUrl == null) {
			if (other.CtorProdUrl != null)
				return false;
		} else if (!CtorProdUrl.equals(other.CtorProdUrl))
			return false;
		if (CtorTitle == null) {
			if (other.CtorTitle != null)
				return false;
		} else if (!CtorTitle.equals(other.CtorTitle))
			return false;
		if (DOCID == null) {
			if (other.DOCID != null)
				return false;
		} else if (!DOCID.equals(other.DOCID))
			return false;
		if (Image == null) {
			if (other.Image != null)
				return false;
		} else if (!Image.equals(other.Image))
			return false;
		if (Mall == null) {
			if (other.Mall != null)
				return false;
		} else if (!Mall.equals(other.Mall))
			return false;
		if (PromoCode == null) {
			if (other.PromoCode != null)
				return false;
		} else if (!PromoCode.equals(other.PromoCode))
			return false;
		if (Site == null) {
			if (other.Site != null)
				return false;
		} else if (!Site.equals(other.Site))
			return false;
		if (Tag == null) {
			if (other.Tag != null)
				return false;
		} else if (!Tag.equals(other.Tag))
			return false;
		return true;
	}
	public String parseTo() {
		return JsonUtils.bean2json(this);
	}

	public static CtorRetBody buildFrom(String json) {
		return JsonUtils.json2bean(json, CtorRetBody.class);
	}

	@Override
	public String toString() {
		return "CtorRetBody [DOCID=" + DOCID + ", Site=" + Site + ", CtorProdUrl=" + CtorProdUrl + ", Tag=" + Tag
				+ ", Mall=" + Mall + ", Brand=" + Brand + ", CtorTitle=" + CtorTitle + ", PromoCode=" + PromoCode
				+ ", Category=" + Category + ", Image=" + Image + ", FeatureList=" + FeatureList + "]";
	}
	
	
	
}