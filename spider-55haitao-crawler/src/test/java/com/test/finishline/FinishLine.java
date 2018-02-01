/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: FinishLine.java 
 * @Prject: spider-55haitao-crawler
 * @Package: com.haitao55.spider.crawler.core.callable.custom 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年10月17日 下午2:54:35 
 * @version: V1.0   
 */
package com.test.finishline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;

/** 
 * @ClassName: FinishLine 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年10月17日 下午2:54:35  
 */
public class FinishLine extends AbstractCallable{
	private final String HOST = "www.finishline.com";
	/* (non Javadoc) 
	 * @Title: invoke
	 * @Description: TODO
	 * @param context
	 * @throws Exception 
	 * @see com.haitao55.spider.crawler.core.callable.base.Callable#invoke(com.haitao55.spider.crawler.core.callable.context.Context) 
	 */
	@Override
	public void invoke(Context context) throws Exception {
		Map<String,Object> map = context.getAll();
		String content = super.getInputString(context);
		RetBody retBody = new RetBody();
		String docId = map.get("DOCID").toString();
		String title = map.get("Title").toString();
		String nowPrice = map.get("nowPrice").toString().trim().replace(" ", "");
		String currency = StringUtils.substring(nowPrice, 0, 1);
		String price_unit = Currency.codeOf(currency).name();
		nowPrice = StringUtils.substring(nowPrice, 1);
		String wasPrice = map.get("wasPrice").toString().trim().replace(" ", "");
		wasPrice = StringUtils.substring(wasPrice, 1);
		String fullPrice = map.get("fullPrice").toString().trim().replace(" ", "");
		fullPrice = StringUtils.substring(fullPrice, 1);
		
		float nowPrice_f = 0f;
		float wasPrice_f = 0f;
		float fullPrice_f = 0f;
		if(StringUtils.isNotBlank(nowPrice))
			nowPrice_f = Float.valueOf(nowPrice);
		if(StringUtils.isNotBlank(wasPrice))
			wasPrice_f = Float.valueOf(wasPrice);
		if(StringUtils.isNotBlank(fullPrice))
			fullPrice_f = Float.valueOf(fullPrice);
		retBody.setDOCID(docId);
		retBody.setTitle(new Title(title, ""));
		retBody.setPrice(new Price(wasPrice_f==0?fullPrice_f:wasPrice_f,
				(int)(nowPrice_f==0?fullPrice_f:nowPrice_f),
				nowPrice_f==0?fullPrice_f:nowPrice_f, price_unit));
		retBody.setSite(new Site(HOST));
		String url = context.getCurrentUrl();
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		String brand = "";
		if (StringUtils.isNotBlank(content)) {
			brand = StringUtils.substringBetween(content, "FL.setup.brand = \"", "\";");
		}
		retBody.setBrand(new Brand(brand, ""));
		
		Document document = Jsoup.parse(content);
		Elements es = document.select("ul.breadcrumbs a");
		List<String> breadCrumb = new ArrayList<>();
		for(Element e : es){
			breadCrumb.add(e.text());
		}
		retBody.setBreadCrumb(breadCrumb);
		if(breadCrumb.size()>0)
			breadCrumb.remove(0);
		retBody.setCategory(breadCrumb);
		Map<String,Object> featureList = new HashMap<>();
		Map<String,Object> description = new HashMap<>();
		Elements fes = document.select("div#productDescription > ul > li");
		if(fes!=null && fes.size()>0){
			int count = 1;
			for(Element e : fes){
				featureList.put("feature-"+count, e.text());
				count++;
			}
		}
		Elements des = document.select("div#productDescription > p");
		if(des!=null && des.size()>0){
			String desc = "";
			for(Element e : des){
				if(StringUtils.isNotBlank(e.text()) && !"FEATURES:".equals(e.text().trim())){
					desc += e.text();
				}
			}
			description.put("en", desc);
		}
		retBody.setFeatureList(featureList);
		retBody.setDescription(description);
		setOutput(context, retBody.parseTo());
	}
}
