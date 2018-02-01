package com.haitao55.spider.crawler.core.callable.custom.lastcall;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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

import sun.misc.BASE64Encoder; 


@SuppressWarnings("restriction")
public class LastCall extends AbstractSelect{

	private static final String domain = "www.lastcall.com";
	private static final String BERGDORF_SERVICE_API = "http://www.lastcall.com/product.service?";
	private String IMAGE_URL = "http://lastcall.scene7.com/is/image/lastcall/{id}?&wid=400&height=500";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select(".product-details-source h1.product-name span").text();
			String brand = doc.select(".product-details-source h1.product-name span.product-designer a").text();
			if(StringUtils.isBlank(brand)){
				doc.select(".product-details-source h1.product-name span").remove();
				brand = doc.select(".product-details-source h1.product-name").text();
			}
			
			Map<String,String> imageMap = new HashMap<>();
			Elements es = doc.select("ul#color-pickers li.color-picker");
			for(Element  e : es){
				String image = e.attr("data-sku-img");
				String dataColorName = e.attr("data-color-name");
				if(StringUtils.isNotBlank(image) && 
						StringUtils.isNotBlank(dataColorName)){
					String iId = StringUtils.substringBetween(image, "{\"m*\":\"", "\"");
					imageMap.put(dataColorName, iId);
				}
			}
			
			Elements images =  doc.select(".product-thumbnails ul.list-inline li .alt-img-wrap img");
			List<Image> imageList = new ArrayList<>();
			for(Element  is : images){
				String image = is.attr("data-main-url");
				if(StringUtils.isNotBlank(image) && 
						!StringUtils.containsIgnoreCase(image, "http")){
					imageList.add(new Image("http:"+image));
				}else{
					imageList.add(new Image(image));
				}
			}
			
			if(CollectionUtils.isEmpty(imageList) && 
					MapUtils.isEmpty(imageMap)){
				String image = doc.select("#prod-img .img-wrap img").attr("src");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image));
				}
			}
			
			String salePrice = StringUtils.substringBetween(content, "product_price\":[\"", "\"");
			doc.select(".price-adornments-elim-suites ins.sale-text").remove();
			String origPirce = doc.select(".price-adornments-elim-suites span.item-price").text();
			String unit = StringUtils.substringBetween(content, "order_currency_code\":\"", "\"");
			
			if(StringUtils.isNotBlank(origPirce)){
				origPirce = origPirce.replaceAll("[$,s\\ ]", "");
				origPirce = pattern(origPirce);
			}
			
			if(StringUtils.isNotBlank(salePrice)){
				salePrice = salePrice.replaceAll("[$, ]", "");
			}
			
			if(StringUtils.isBlank(origPirce)){
				origPirce = salePrice;
			}
			
			if(StringUtils.isNotBlank(salePrice)){
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPirce)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(origPirce), 
						save, Float.parseFloat(salePrice), unit));
			}
			
			String productId = StringUtils.substringBetween(content, "product_id\":[\"", "\"");
			
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
			rebody.setBrand(new Brand(brand, ""));
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Map<String,String> styleMap = new HashMap<>();
			boolean display = true;
			if(StringUtils.isNotBlank(productId)){
				String base64 = base64(productId);
				
				String html = getContent(context,BERGDORF_SERVICE_API+base64);
				
				JSONObject jsonObject = JSONObject.parseObject(html);
				String productSizeAndColor = jsonObject.getString("ProductSizeAndColor");
				JSONObject productSizeJsonObject = JSONObject.parseObject(productSizeAndColor);
				String productSizeAndColorJSON = productSizeJsonObject.getString("productSizeAndColorJSON");
				JSONArray jsonArray = JSONArray.parseArray(productSizeAndColorJSON);
				for(int i = 0; i < jsonArray.size(); i++){
					JSONObject skuJsonObject = jsonArray.getJSONObject(i);
					JSONArray skuJsonArray = skuJsonObject.getJSONArray("skus");
					for(int j = 0; j < skuJsonArray.size(); j++){
						JSONObject skusJSON = skuJsonArray.getJSONObject(j);
						LSelectionList lSelectionList = new LSelectionList();
						//String defaultSkuColor = skusJSON.getString("defaultSkuColor");
						//String stockLevel = skusJSON.getString("stockLevel");
						String colorVal = skusJSON.getString("color");
						if(StringUtils.isNotBlank(colorVal) && 
								StringUtils.containsIgnoreCase(colorVal, "?")){
							colorVal = colorVal.substring(0, colorVal.indexOf("?"));
						}
						String sizeVal = skusJSON.getString("size");
						String skuId = skusJSON.getString("sku");
						lSelectionList.setGoods_id(skuId);
						lSelectionList.setOrig_price(Float.parseFloat(origPirce));
						lSelectionList.setSale_price(Float.parseFloat(salePrice));
						lSelectionList.setPrice_unit(unit);
						lSelectionList.setStock_status(1);
						if(StringUtils.isNotBlank(colorVal)){
							lSelectionList.setStyle_id(colorVal);
						}else{
							lSelectionList.setStyle_id("default");
						}
						List<Selection> selections = new ArrayList<>();
						if(StringUtils.isNotBlank(sizeVal)){
							Selection selection = new Selection();
							selection.setSelect_name("size");
							selection.setSelect_value(sizeVal);
							selections.add(selection);
						}
						lSelectionList.setSelections(selections);
						
						if(!styleMap.containsKey(colorVal)){
							LStyleList lStyleList = new LStyleList();
							if(display){
								lStyleList.setDisplay(true);
								display = false;
							}
							lStyleList.setGood_id(skuId);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("color");
							if(StringUtils.isNotBlank(colorVal)){
								lStyleList.setStyle_id(colorVal);
								lStyleList.setStyle_name(colorVal);
							}else{
								lStyleList.setStyle_id("default");
								lStyleList.setStyle_name("default");
							}
							
							lStyleList.setStyle_switch_img("");
							l_style_list.add(lStyleList);
							List<Image> list = new ArrayList<>();
							String imageId = imageMap.get(colorVal);
							if(StringUtils.isNotBlank(imageId)){
								String image = IMAGE_URL.replace("{id}", imageId);
								list.add(new Image(image));
							}
							
							list.addAll(imageList);
							
							context.getUrl().getImages().put(skuId, list);// picture
						}
						styleMap.put(colorVal, colorVal);
						l_selection_list.add(lSelectionList);
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
					rebody.setStock(new Stock(spuStock));
				}
				
				List<String> cats = new ArrayList<String>();
				List<String> breads = new ArrayList<String>();
				Elements cates = doc.select("ul.breadcrumbs li.bcClick a");
				for(Element e : cates){
					String cate = e.text();
					if(StringUtils.isNotBlank(cate)){
						cats.add(cate);
						breads.add(cate);
					}
				}
				
				if(CollectionUtils.isEmpty(cates)){
					cats.add(title);
					breads.add(title);
				}
				
				rebody.setCategory(cats);
				rebody.setBreadCrumb(breads);
				
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				String  description = doc.select(".product-details-info").text();
				Elements featureEs = doc.select(".productCutline ul li");
			    int count = 0;
				if(featureEs != null &&  featureEs.size() > 0){
					for(Element e : featureEs){
						String text = e.text();
						if(StringUtils.isNotBlank(text)){
							 count ++;
							 featureMap.put("feature-"+count, text);
						}
					}
				}
						
						
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
	}
	private  String pattern(String price){
		Pattern pattern = Pattern.compile("(\\d+(.\\d+))");
		Matcher matcher = pattern.matcher(price);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	private String base64(String productId){
		 String data_string = "{\"ProductSizeAndColor\":{\"productIds\":\""+productId+"\"}}";
		 byte[] bt = data_string.getBytes();
		 String base64 = (new BASE64Encoder()).encodeBuffer(bt);
		 return "data=$b64$"+base64.trim()+"&sid=getSizeAndColorData&bid=ProductSizeAndColor&timestamp="+new Date().getTime()+"";
	}
	
	private String getContent(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders(context)).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders(context)).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	
	
	private static Map<String,Object> getHeaders(Context context){
		final Map<String, Object> headers = new HashMap<String, Object>();
		 headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/56.0.2924.76 Chrome/56.0.2924.76 Safari/537.36");
		 headers.put("Accept", "*/*");
		 headers.put("Accept-Encoding", "gzip");
		 headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		 headers.put("Host", "www.lastcall.com");
		 headers.put("X-Requested-With", "XMLHttpRequest");
		 headers.put("X-Distil-Ajax", "zbcfsrxazqfdzesryx");
		 headers.put("Referer", context.getCurrentUrl());
		return headers;
	}

}
