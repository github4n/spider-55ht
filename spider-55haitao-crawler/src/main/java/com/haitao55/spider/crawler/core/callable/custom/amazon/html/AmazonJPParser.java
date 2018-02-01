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


/**
 * 日本亚马逊参数解析
 * @author denghuan
 *
 */
public class AmazonJPParser extends DefaultAmazonParser {

	public AmazonJPParser(Map<String, SkuBean> skuResult) {
		super(skuResult);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, Object> properties(Context context) {
		String gender = StringUtils.EMPTY;
		// pps 俩中方式获取
		Map<String, Object> propMap = new HashMap<String, Object>();
		Document document = context.getCurrentDoc().getDoc();
		Elements es = document.select("div#wayfinding-breadcrumbs_feature_div > ul > li > span > a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				String cat = e.text();
				gender = getSex(cat);
			}
		}
		es = document.select("span#productTitle");
		String title = getText(es);
		if(StringUtils.isBlank(gender)){//性别
			gender = getSex(title);
		}
		propMap.put("s_gender", gender);
		
		es = document.select(".pdTab tr");
		if (es != null && es.size() > 0) {
			for (Element e : es) {
				String key = e.select("td.label").text();
				String value = e.select("td.value").text();
				if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
					propMap.put(key, value);
				}
			}
		}
	
		if (CollectionUtils.isEmpty(es)) {
			es = document.select("tr td.bucket .content ul li");
			es.removeClass(".content ul.qpUL");
			if(CollectionUtils.isNotEmpty(es)){
				for (Element e : es) {
					String key = StringUtils.trim(StringUtils.substringBefore(e.text(), ":"));
					String value = StringUtils.trim(StringUtils.substringAfter(e.text(), ":"));
					if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
						propMap.put(key, value);
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
