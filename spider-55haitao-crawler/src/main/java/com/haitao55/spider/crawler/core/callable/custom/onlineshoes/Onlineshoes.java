package com.haitao55.spider.crawler.core.callable.custom.onlineshoes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;

public class Onlineshoes extends AbstractSelect{

	private static final String domain = "www.onlineshoes.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		//String content = this.getInputString(context);
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		 if(StringUtils.isBlank(proxyRegionId)){
			 content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue()).proxy(false).resultAsString();
		 }else{
			 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
					 true);
			 String proxyAddress=proxy.getIp();
			 int proxyPort=proxy.getPort();
			 content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).proxy(true).method(HttpMethod.GET.getValue()).proxyAddress(proxyAddress)
					 .proxyPort(proxyPort).resultAsString();
		 }
		
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document  doc = Jsoup.parse(content);
			String error = doc.select(".notFound h1.head").text();
			if(StringUtils.isNotBlank(error) &&
					StringUtils.containsIgnoreCase(error, "We swear we have")){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
			}
			String brand = StringUtils.substringBetween(content, "brand-name-link\">", "<");
			String title = doc.select(".product-name span").get(0).text();
			String gender = StringUtils.substringBetween(content, "Gender', '", "'");
			String spuStyleId = doc.select("span").attr("data-original-color");
			
			List<String> urlList = new ArrayList<>();
			urlList.add(context.getCurrentUrl());
			Elements es  = doc.select("ul.small-block-grid-6 li a");
			if(es != null  && es.size() > 0){
				for(Element e : es){
					String url = e.attr("data-url");
					if(StringUtils.isNotBlank(url) && 
							!StringUtils.containsIgnoreCase(url, domain)){
						urlList.add("http://"+domain + url);
					}
				}
			}
			
			String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			JSONArray skuArray = new OnlineshoesHandler().process(urlList, context.getUrl());
			
			if(skuArray != null && skuArray.size() > 0 ){
				for (Object object : skuArray) {
					JSONObject jsonObject = (JSONObject) object;
					LStyleList lStyleList = new LStyleList();
					Object sizeObject = jsonObject.get("size");
					String skuId = jsonObject.getString("skuId");
					float orignPrice = jsonObject.getFloatValue("origPrice");
					float salePrice = jsonObject.getFloatValue("salePrice");
					String unit = jsonObject.getString("unit");
					int save = jsonObject.getIntValue("save");
					int instock = jsonObject.getIntValue("instock");
					String styleId = jsonObject.getString("styleId");
					
					@SuppressWarnings({ "unchecked", "rawtypes" })
					List<String> sizes = (List)sizeObject;
					if(sizes != null && sizes.size() > 0){
						for(String value : sizes){// size变化
							LSelectionList lselectlist = new LSelectionList();
							lselectlist.setGoods_id(skuId);
							lselectlist.setStyle_id(styleId);
							lselectlist.setOrig_price(orignPrice);
							lselectlist.setPrice_unit(unit);
							lselectlist.setSale_price(salePrice);
							lselectlist.setStock_status(instock);
							List<Selection> selections = new ArrayList<Selection>();
							Selection selection = new Selection();
							selection.setSelect_name("size");
							selection.setSelect_value(value);
							selections.add(selection);
							lselectlist.setSelections(selections);
							l_selection_list.add(lselectlist);
						}
					}else{
						LSelectionList lselectlist = new LSelectionList();
						lselectlist.setGoods_id(skuId);
						lselectlist.setStyle_id(styleId);
						lselectlist.setOrig_price(orignPrice);
						lselectlist.setPrice_unit(unit);
						lselectlist.setSale_price(salePrice);
						lselectlist.setStock_status(instock);
						List<Selection> selections = new ArrayList<Selection>();
						lselectlist.setSelections(selections);
						l_selection_list.add(lselectlist);
					}
					
					if(StringUtils.isNotBlank(spuStyleId) && spuStyleId.equals(styleId)){
						lStyleList.setDisplay(true);
						rebody.setPrice(new Price(orignPrice, 
								save, salePrice, unit));
					}
					
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_id(styleId);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(styleId);
					lStyleList.setStyle_switch_img("");
					Object picsObject = jsonObject.get("pics");
					@SuppressWarnings({ "unchecked", "rawtypes" })
					List<Image> pics=(List)picsObject;
					context.getUrl().getImages().put(skuId, pics);
					l_style_list.add(lStyleList);
				}
			}
			
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			
			int spuStock = 0;
			if(l_selection_list != null 
					&& l_selection_list.size() > 0){
				for(LSelectionList ll : l_selection_list){
					int sku_stock = ll.getStock_status();
					if (sku_stock == 1) {
						spuStock = 1;
						break;
					}
					if (sku_stock == 2){
						spuStock = 2;
					}
				}
			}else{
				
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements cates = doc.select(".breadcrumb a");
			for(int i = 2;i < cates.size()-1; i++){
				String  cat = cates.get(i).text();
				if(StringUtils.isNotBlank(cat)){
					cats.add(cat);
					breads.add(cat);
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Map<String, Object> propMap = new HashMap<String, Object>();
			if(StringUtils.isNotBlank(gender)){
				propMap.put("s_gender", gender);
			}else{
				propMap.put("s_gender", "");
			}
			String description = doc.select("#detailsDescription").text();
			String detailList= doc.select("ul.detail-list").text();
			Elements featureEs= doc.select("ul.detail-list li span");
			int count = 0;
			 for(Element e : featureEs){
				 String feature = e.text();
				 if(StringUtils.isNotBlank(feature)){
					 count ++;
					 featureMap.put("feature-"+count, feature);
				 }
			 }
			 
			rebody.setProperties(propMap);
			rebody.setFeatureList(featureMap);
			descMap.put("en", description+detailList);
			rebody.setDescription(descMap);
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}

}
