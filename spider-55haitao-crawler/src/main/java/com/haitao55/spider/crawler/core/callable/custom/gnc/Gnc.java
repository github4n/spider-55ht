package com.haitao55.spider.crawler.core.callable.custom.gnc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;


public class Gnc extends AbstractSelect{

	private static final String domain = "www.gnc.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		String content = crawlerUrl(context,url);
		
		RetBody rebody = new RetBody();
		
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			String title = doc.select("h1.product-name").text();
			String salePrice = doc.select(".product-content-container .product-price span.price-sales").text();
			String origPrice = doc.select(".product-content-container .product-price span.price-standard").text();
			if(StringUtils.isBlank(salePrice)){
				salePrice = doc.select(".product-content-container .product-price span.price-saleprice").text();
			}
			
			
			String unit = StringUtils.substringBetween(content, "currency\":\"", "\"");
			String productId = StringUtils.substringBetween(content, "TurnToItemSku = \"", "\"");
			
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand("Gnc", ""));
			
			if(StringUtils.isNotBlank(salePrice)){
				salePrice = salePrice.replaceAll("[$ ]", "");
			}
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[$ ]", "");
			
			List<Image> spuImages = new ArrayList<Image>();
			Elements elements = doc.select("img.productthumbnail");
			if(CollectionUtils.isNotEmpty(elements)){
				for (Element element : elements) {
					String json_image = element.attr("data-lgimg");
					if(StringUtils.isNotBlank(json_image)){
						String image_url = StringUtils.substringBetween(json_image, "url\":\"", "\",");
						if(StringUtils.isNotBlank(image_url)){
							image_url = image_url.replace(" ", "%20");
						}
						spuImages.add(new Image(image_url));
					}
				}
			}
			
			if(CollectionUtils.isEmpty(spuImages)){
				String src = doc.select("img.primary-image").attr("src");
				if(StringUtils.isNotBlank(src)){
					src = src.replace(" ", "%20");
					spuImages.add(new Image(src));
				}
			}
			
			String defaultColor = StringUtils.EMPTY;
			
//			String attributes = doc.select(".product-variations").attr("data-attributes");
//			if(StringUtils.isNotBlank(attributes)){
//				defaultColor = StringUtils.substringBetween(attributes, "value\":\"", "\"");
//			}
			
			String defaultSizeVal = StringUtils.EMPTY;
			String attributes = doc.select(".product-variations").attr("data-attributes");
			if(StringUtils.isNotBlank(attributes)){
				String value = StringUtils.substringBetween(attributes, "displayValue\":\"", "\"");
				String sizeKey = StringUtils.substringBetween(attributes, "displayName\":\"", "\"");
				if(StringUtils.isNotBlank(sizeKey) && 
						(StringUtils.containsIgnoreCase(sizeKey, "Count") || 
								StringUtils.containsIgnoreCase(sizeKey, "size")) ){
					defaultSizeVal = value;
				}else{
					defaultColor = value;
				}
			}

			String szVal = doc.select(".item-count").text();
			if (StringUtils.isNotBlank(szVal)) {
				szVal = StringUtils.substringAfter(szVal, "Size:");
			}

			List<JSONObject> params = new ArrayList<>();

			Elements sizeEs = doc.select("select#va-count option");
			for (Element e : sizeEs) {
				String colorVal = e.text();
				String skuUrl = e.attr("value");
				if (StringUtils.containsIgnoreCase(colorVal, "Select Count")) {
					continue;
				}
				if (StringUtils.isNotBlank(skuUrl)) {
					JSONObject paramJsonObject = new JSONObject();
					paramJsonObject.put("url", skuUrl + "&format=ajax");
					params.add(paramJsonObject);
				}
			}
			Elements countSize = doc.select("select#va-productDisplayCount option");
			if (CollectionUtils.isEmpty(sizeEs)) {
				for (Element e : countSize) {
					String colorVal = e.text();
					String skuUrl = e.attr("value");
					if (StringUtils.containsIgnoreCase(colorVal, "Select Size") || 
							StringUtils.containsIgnoreCase(colorVal, "Select Count")) {
						continue;
					}
					if (StringUtils.isNotBlank(skuUrl)) {
						JSONObject paramJsonObject = new JSONObject();
						paramJsonObject.put("url", skuUrl + "&format=ajax");
						params.add(paramJsonObject);
					}
				}
			}

			if (CollectionUtils.isEmpty(countSize)) {
				Elements es = doc.select("#va-flavor option");
				for (Element e : es) {
					String colorVal = e.text();
					String skuUrl = e.attr("value");
					if (StringUtils.containsIgnoreCase(colorVal, "Select Flavor")
							|| StringUtils.containsIgnoreCase(colorVal, "Select Size")) {
						continue;
					}
					if (StringUtils.isNotBlank(skuUrl)) {
						JSONObject paramJsonObject = new JSONObject();
						paramJsonObject.put("url", skuUrl + "&format=ajax");
						params.add(paramJsonObject);
					}
				}
			}
		
			
			// skuJsonArray
			JSONArray skuJsonArray = new JSONArray();
			
			skuJsonArray = new GncHandler().process(params, context.getUrl(), skuJsonArray);
			
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			if (skuJsonArray != null) {
				int count = 0;
				for (Object object : skuJsonArray) {
					count ++;
					JSONObject skuJsonObj = (JSONObject) object;
					String color = skuJsonObj.getString("color");
					String sizeVal = skuJsonObj.getString("size");
					String skuId = skuJsonObj.getString("skuId");
					String stock_status = skuJsonObj.getString("stock_status");
					String sale_price = skuJsonObj.getString("sale_price");
					String orign_price = skuJsonObj.getString("orign_price");
					@SuppressWarnings("unchecked")
					List<Image> images = (List<Image>) skuJsonObj.get("images");

					LSelectionList lSelectionList = new LSelectionList();
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setOrig_price(Float.parseFloat(orign_price));
					lSelectionList.setPrice_unit(unit);
					lSelectionList.setSale_price(Float.parseFloat(sale_price));
					lSelectionList.setStock_status(Integer.parseInt(stock_status));
					if(StringUtils.isNotBlank(color)){
						lSelectionList.setStyle_id(color);
					}else{
						lSelectionList.setStyle_id("default"+count);
					}
					List<Selection> selections = new ArrayList<>();
					if (StringUtils.isNotBlank(sizeVal) || 
							StringUtils.isNotBlank(szVal)) {
						Selection selection = new Selection();
						if(StringUtils.isNotBlank(sizeVal)){
							selection.setSelect_value(sizeVal);
						}else{
							selection.setSelect_value(szVal);
						}
						selection.setSelect_name("size");
						
						selections.add(selection);
					}

					lSelectionList.setSelections(selections);
					l_selection_list.add(lSelectionList);

					LStyleList lStyleList = new LStyleList();
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_cate_name("color");
					if(StringUtils.isNotBlank(color)){
						lStyleList.setStyle_id(color);
						lStyleList.setStyle_name(color);
					}else{
						lStyleList.setStyle_id("default"+count);
						lStyleList.setStyle_name("default"+count);
					}
				
					lStyleList.setStyle_switch_img("");
					
					if(StringUtils.isNotBlank(color)){
						if (color.equals(defaultColor)) {
							lStyleList.setDisplay(true);
							int save = Math.round((1 - Float.parseFloat(sale_price) / Float.parseFloat(orign_price)) * 100);// discount
							rebody.setPrice(new Price(Float.parseFloat(orign_price), save, Float.parseFloat(sale_price), unit));
						}
					}else{
						if (defaultSizeVal.equals(sizeVal)) {
							lStyleList.setDisplay(true);
							int save = Math.round((1 - Float.parseFloat(sale_price) / Float.parseFloat(orign_price)) * 100);// discount
							rebody.setPrice(new Price(Float.parseFloat(orign_price), save, Float.parseFloat(sale_price), unit));
						}
					}
			
					l_style_list.add(lStyleList);

					context.getUrl().getImages().put(skuId, images);// picture

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
				String stock_status_flag = doc.select("#add-to-cart").text();
				if(StringUtils.equalsIgnoreCase(stock_status_flag, "Add to Cart")){
					spuStock = 1;
				}
				context.getUrl().getImages().put(productId, spuImages);// picture
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
			}
			
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements breadcrumbEs = doc.select(".breadcrumb a.breadcrumb-element");
			for(Element e : breadcrumbEs){
				String cate = e.text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
				}
			}
			
			if(CollectionUtils.isEmpty(breads)){
				cats.add(title);
				breads.add(title);
			}
			
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select(".product-information .content").text();
			featureMap.put("feature-1", description);
			
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			rebody.setProperties(propMap);
			rebody.setSku(sku);
			
		}
		setOutput(context, rebody);
		
	}
	private String crawlerUrl(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
}
