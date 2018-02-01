package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;
/**
 * 
* Title:
* Description: amazonde  德亚json封装
* Company: 55海淘
* @author zhaoxl 
* @date 2016年10月9日 下午8:14:35
* @version 1.0
 */
public class CopyAmazon_de extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.amazon.de";
	private static final String UNIT = "EUR";
	private static final String NICHT_NACH = "nicht nach";
	private static final String KANN_NACH = "kann nach";
	

	@Override
	public void invoke(Context context) throws Exception {
		Document doc = super.getDocument(context);
		RetBody rebody=new RetBody();
		String[] split = context.getCurrentUrl().split("/");
		String productId=split[split.length-1];
		List<String> image=new ArrayList<String>();
		Map<String, Object> propMap = new HashMap<String, Object>();
		
		String isOwn =null;
		String isExistSku =null;
		
		isExistSku = StringUtils.substringBetween(doc.toString(), "var dataToReturn = ", ";");
		if(StringUtils.isEmpty(isExistSku)){
			
		}else{//多个sku
			
		}
		String title=null;
		Elements select=doc.select("span#productTitle");
		if(CollectionUtils.isNotEmpty(select)){
			title=select.get(0).text();
		}
		
		//历史价格
		String orignPrice =null;
		select = doc.select("span.a-text-strike");
		if (CollectionUtils.isNotEmpty(select)) {
			orignPrice = select.get(0).text();
		}
		
		//售价
		String salePrice =null;
		select = doc.select("td[class=a-span12]>span");
		if (CollectionUtils.isNotEmpty(select)) {
			salePrice=select.get(0).text();
		}
		
		//折扣
		String save =null;
		select = doc.select("td.a-span12.a-color-price.a-size-base");
		if (CollectionUtils.isNotEmpty(select)) {
			save=select.get(0).text();
		}
		orignPrice=null==orignPrice?null:priceConvert(orignPrice);
		salePrice=null==salePrice?null:priceConvert(salePrice);
		save=null==save?null:priceConvert(save);
		if(null==orignPrice){
			orignPrice=salePrice;
			save="0";
		}
		rebody.setPrice(new Price(null==orignPrice?null:Float.parseFloat(orignPrice), null==save?null:Integer.parseInt(save), null==salePrice?null:Float.parseFloat(salePrice), Currency.valueOf(UNIT).name()));
		
		//brand
		select = doc.select("a#brand");
		String brandName=null;
		if (CollectionUtils.isNotEmpty(select)) {
			brandName=select.get(0).text();
		}
		rebody.setBrand(new Brand(brandName, ""));
		
		//面包屑
		select=doc.select("ul.a-unordered-list>li>span.a-list-item>a.a-color-tertiary");
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(select)) {
			for (Element element : select) {
				cats.add(element.text());
				breads.add(element.text());
			}
		}
		rebody.setCategory(cats);
		rebody.setCategory(breads);
		
		//图片
		select=doc.select("ul>li span.a-button-text>img");
		if (CollectionUtils.isNotEmpty(select)) {
			List<String> attrs = JsoupUtils.attrs(select, "abs:src");
			for (String url : attrs) {
				image.add(url);
			}
		}
		
		//properties
		propMap.put("s_gender", "");
		select=doc.select("div#detail_bullets_id div.content>ul>li:not(#SalesRank)");
		if(CollectionUtils.isNotEmpty(select)){
			for (Element element : select) {
				String key = StringUtils.trim(StringUtils.substringBefore(element.text(), ":"));
				String value = StringUtils.trim(StringUtils.substringAfter(element.text(), ":"));
				if(key.equals("Durchschnittliche Kundenbewertung")){
					continue;
				}
				if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
					propMap.put(key, value);
				}
			}
		}
		
		if(CollectionUtils.isEmpty(select)){
			//另一种样式获取产品信息
			select=doc.select("div.wrapper.DElocale div:first-of-type > div.section.techD table tr>td");
//			Elements select2 = doc.select("div.wrapper.DElocale div:first-of-type > div.section.techD table tr>td.value");
			if(CollectionUtils.isNotEmpty(select)){
				for (int i=0; i<select.size()-2;i++) {
					if(i<select.size()-2-1){
						if(i%2==0){
							if (StringUtils.isNotBlank(select.get(i).text()) && StringUtils.isNotBlank(select.get(i+1).text())) {
								propMap.put(select.get(i).text(), select.get(i+1).text());
							}
						}
					}
				}
			}
		}
		if(CollectionUtils.isEmpty(select)){
			//另一种获取产品信息方式
			select=doc.select("td.kmd-right-col-container table#technical-details-table td");
			if(CollectionUtils.isNotEmpty(select)){
				for (int i=0; i<select.size();i++) {
					if(i<select.size()-1){
						if(i%2==0){
							Element element = select.get(i);
							Element element2 = select.get(i+1);
							if (StringUtils.isNotBlank(select.get(i).text()) && StringUtils.isNotBlank(select.get(i+1).text())) {
								propMap.put(select.get(i).text(), select.get(i+1).text());
							}
						}
					}
				}
			}
			
		}
		
		rebody.setProperties(propMap);
		
		//stock
		select = doc.select("div#ddmDeliveryMessage");
		String stock =null;
		int stockStatus = 0;
		if (CollectionUtils.isNotEmpty(select)) {
			stock=select.get(0).text();
		}
		if(null!=stock){
			stock=stock.contains(KANN_NACH)?"1":"0";
			stockStatus=Integer.parseInt(stock);
		}else{
			stockStatus=Integer.parseInt("1");
		}
		rebody.setStock(new Stock(stockStatus));
		
		//featureList
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		StringBuilder sb = new StringBuilder();
		select=doc.select("div#productDescription p");
		if (CollectionUtils.isNotEmpty(select)) {
			int count = 1;
			for (Element element : select) {
				featureMap.put("feature-" + count, element.text());
				count++;
				sb.append(element.text());
			}
		}
		rebody.setFeatureList(featureMap);
		descMap.put("en", sb.toString());
		rebody.setDescription(descMap);
		
		//是否自营
		select=doc.select("div#merchant-info");
		if (CollectionUtils.isNotEmpty(select)) {
			isOwn=select.get(0).text();
		}
		
		// full doc info
		String docid = SpiderStringUtil.md5Encode(domain + productId);
		rebody.setDOCID(docid);
		rebody.setSite(new Site(domain));
		rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), docid));
		rebody.setTitle(new Title(title, ""));
		
		
		setOutput(context, rebody.parseTo());
	}
	
	
	private String priceConvert(String price){
		return price.replace("EUR","").replaceAll("\\(.*\\)", "").trim().replace(".", "").replace(",", ".");
	}
	
}
