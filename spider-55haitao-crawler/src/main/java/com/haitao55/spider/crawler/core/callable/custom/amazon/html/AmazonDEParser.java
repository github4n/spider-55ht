package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.crawler.core.callable.context.Context;

public class AmazonDEParser extends DefaultAmazonParser {

	public AmazonDEParser(Map<String, SkuBean> skuResult) {
		super(skuResult);
	}
	
	@Override
	public Map<String, Object> properties(Context context) {
		String gender = StringUtils.EMPTY;
		Document document = context.getCurrentDoc().getDoc();
		Elements es = document.select("div#wayfinding-breadcrumbs_feature_div > ul > li > span > a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				String cat = e.text();
				gender = getSex(cat);
			}
		}
		Map<String, Object> propMap = new HashMap<String, Object>();
		es = document.select("span#productTitle");
		String title = getText(es);
		if(StringUtils.isBlank(gender)){
			gender = getSex(title);
		}
		propMap.put("s_gender", gender);
		
		es=document.select("div#detail_bullets_id div.content>ul>li:not(#SalesRank)");
		if (es != null && es.size() > 0) {
			for (Element e : es) {
				String key = StringUtils.trim(StringUtils.substringBefore(e.text(), ":"));
				String value = StringUtils.trim(StringUtils.substringAfter(e.text(), ":"));
				if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
					propMap.put(key, value);
				}
			}
		}
		if(CollectionUtils.isEmpty(es)){
			//另一种样式获取产品信息
			es=document.select("div.wrapper.DElocale div:first-of-type > div.section.techD table tr>td");
//			Elements select2 = doc.select("div.wrapper.DElocale div:first-of-type > div.section.techD table tr>td.value");
			if(CollectionUtils.isNotEmpty(es)){
				for (int i=0; i<es.size()-2;i++) {
					if(i<es.size()-2-1){
						if(i%2==0){
							if (StringUtils.isNotBlank(es.get(i).text()) && StringUtils.isNotBlank(es.get(i+1).text())) {
								propMap.put(es.get(i).text(), es.get(i+1).text());
							}
						}
					}
				}
			}
		}
		if(CollectionUtils.isEmpty(es)){
			//另一种获取产品信息方式
			es=document.select("td.kmd-right-col-container table#technical-details-table td");
			if(CollectionUtils.isNotEmpty(es)){
				for (int i=0; i<es.size();i++) {
					if(i<es.size()-1){
						if(i%2==0){
							if (StringUtils.isNotBlank(es.get(i).text()) && StringUtils.isNotBlank(es.get(i+1).text())) {
								propMap.put(es.get(i).text(), es.get(i+1).text());
							}
						}
					}
				}
			}
			
		}
		return propMap;
	}

	@Override
	public RetBody retboby(Context context) {
			RetBody ret = new RetBody();
			ret.setDOCID(docID(context));
			ret.setSite(site(context));
			ret.setProdUrl(prodUrl(context));
			ret.setTitle(title(context));
			ret.setPrice(price(context));
			ret.setStock(stock(context));
			ret.setBrand(brand(context));
			ret.setBreadCrumb(breadCrumb(context));
			ret.setCategory(category(context));
			ret.setImage(image(context));
			ret.setProperties(this.properties(context));
			ret.setFeatureList(featureList(context));
			ret.setDescription(description(context));
			ret.setSku(sku(context));
			return ret;
	}
	
}
