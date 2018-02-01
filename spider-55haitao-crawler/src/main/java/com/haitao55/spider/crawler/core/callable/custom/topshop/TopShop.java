package com.haitao55.spider.crawler.core.callable.custom.topshop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * topshop　详情页封装
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月11日 下午2:23:32
* @version 1.0
 */
public class TopShop extends AbstractSelect {

	private static final String switch_image_preffix = "http://mediaus.topshop.com/wcsstore/TopShopUS/";
	private static final String cate_name_flag = "Size";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	private static final String domain = "www.topshop.com";
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl().toString();
		
		String content = crawler_result(context.getUrl(),url);
		
		context.put(input, content);
		
		Document doc = this.getDocument(context);
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();
		
		Pattern pattern = Pattern.compile("We couldn't find the page you were looking for");
		Matcher matcher = pattern.matcher(content);
		if(matcher.find()){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"topshop.com itemUrl:" + context.getUrl().toString() + " not found..");
		}
		
		//截取有用到json串
		String product_data = StringUtils.substringBetween(content, "var productData = ", "Arcadia");
		//product_data = StringUtils.replacePattern(product_data, " ", "");
		product_data = StringUtils.substringBefore(product_data,"\"description");
		product_data = product_data.concat("}");
		product_data = product_data.replace("\"horizontal\",", "\"\"");
		if(StringUtils.isBlank(product_data)){
			return;
		}
		
		//spu stock
		int spu_stock_status = 0;
		
		JSONObject productJsonObject = JSONObject.parseObject(product_data);
		
		//sku jsonarray
		JSONArray skuJsonArray = productJsonObject.getJSONArray("SKUs");
		
		//color
		String color = StringUtils.substringBetween(content, "\"color\": \"", "\",");
		
		//productId
		String productId = productJsonObject.getString("id");
		
		//title
		String title = productJsonObject.getString("name");
		
		//brand
		String brand = StringUtils.EMPTY;
		
		String temp = StringUtils.substringBetween(content, "ECMC_PROD_CE3_BRANDS_1:\"",	"\",");
		if(StringUtils.isBlank(temp)){
			temp = "Topshop";
		}
		brand = temp;
		
		//switch image 
		JSONArray switchJsonArray = productJsonObject.getJSONArray("colourSwatches");
		//switch_image_url
		String switch_image_url = StringUtils.EMPTY;
		if(null != switchJsonArray && skuJsonArray.size() > 0){
			for (Object object : switchJsonArray) {
				JSONObject jsonObject = (JSONObject)object;
				switch_image_url = jsonObject.getString("colourThumbUrl");
			}
			
		}
		if(!StringUtils.contains(switch_image_url, "http")){
			switch_image_url = switch_image_preffix.concat(switch_image_url);
		}
		
		//images
		List<Image> pics = new ArrayList<Image>();
		JSONObject imageJsonObject = productJsonObject.getJSONObject("imageUrls");
		JSONArray imageJsonArray = imageJsonObject.getJSONArray("Thumbnailimageurls");
		if(null != imageJsonArray && imageJsonArray.size() > 0){
			for (Object object : imageJsonArray) {
				JSONObject jsonObject = (JSONObject)object;
				String image_url = jsonObject.getString("large");
				pics.add(new Image(image_url));
			}
		}
		
		
		//prices
		String salePrice = StringUtils.substringBetween(content, "\"unit_sale_price\": ", ",");
		String orignPrice = StringUtils.substringBetween(content, "\"unit_price\": ", ",");
		String unit = StringUtils.substringBetween(content, "\"currency\": \"", "\",");
		
		String save=StringUtils.EMPTY;
		salePrice = salePrice.replaceAll("[ ,]", "");
		orignPrice = orignPrice.replaceAll("[ ,]", "");

		if (StringUtils.isBlank(orignPrice)) {
			orignPrice = salePrice;
		}
		if (StringUtils.isBlank(salePrice)) {
			salePrice = orignPrice;
		}
		if (StringUtils.isBlank(orignPrice)
				|| Float.valueOf(orignPrice) < Float.valueOf(salePrice)) {
			orignPrice = salePrice;
		}
		if (StringUtils.isBlank(save)) {
			save = Math.round((1 - Float.valueOf(salePrice) / Float.valueOf(orignPrice)) * 100)
					+ "";// discount
		}
		
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		
		//style 
		JSONObject styleJsonObject = new JSONObject();
		
		//sku iteator
		if(null != skuJsonArray && skuJsonArray.size() > 0){
			for (Object object : skuJsonArray) {
				JSONObject jsonObject = (JSONObject)object; 
				
				// selectlist
				LSelectionList lselectlist = new LSelectionList();
				
				//skuId
				String skuId = jsonObject.getString("skuid");
				//stock
				int stock_status = 0;
				int stock_number=0;
				int stock_status_temp = jsonObject.getIntValue("availableinventory");
				if(stock_status_temp > 0){
					stock_status = 1;
					spu_stock_status = 1;
				}
				
				//selections
				List<Selection> selections = new ArrayList<Selection>();
				String cate_name = jsonObject.getString("defining");
				String size = StringUtils.EMPTY;
				if(StringUtils.equals(cate_name_flag, cate_name)){
					size = jsonObject.getString("value");
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name(cate_name);
					selection.setSelect_value(size);
					selections.add(selection);
				}
				
				//lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(Float.valueOf(orignPrice));
				lselectlist.setSale_price(Float.valueOf(salePrice));
				lselectlist.setPrice_unit(unit);
				lselectlist.setStock_status(stock_status);
				lselectlist.setStock_number(stock_number);
				lselectlist.setStyle_id(color);
				lselectlist.setSelections(selections);
				
				//l_selection_list
				l_selection_list.add(lselectlist);
				
				//style json
				if(stock_status > 0){
					styleJsonObject.put(color, jsonObject);
				}
			}
			if(null != styleJsonObject  && styleJsonObject.size()>0){
				for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
					String style_id = entry.getKey();
					JSONObject jsonObject = (JSONObject)entry.getValue();
					// stylelist
					LStyleList lStyleList = new LStyleList();
					//skuId
					String skuId = jsonObject.getString("skuid");
					String switch_img = switch_image_url;
					if(color.equals(style_id)){
						lStyleList.setDisplay(true);
					}
					
					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(style_id);
					
					context.getUrl().getImages().put(skuId, pics);
					//l_style_list
					l_style_list.add(lStyleList);
				}
			}
		}

		//sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		
		retBody.setSku(sku);
		
		//stock
		retBody.setStock(new Stock(spu_stock_status));
		
		//title
		retBody.setTitle(new Title(title, "", "", ""));
		
		//brand
		retBody.setBrand(new Brand(brand, "", "", ""));

		
		// full doc info
		String docid = SpiderStringUtil.md5Encode(productId.concat(domain));
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		
		//category breadcrumb
		category_package(brand ,retBody , doc);
		
		// description
	    desc_package(doc,retBody);
	    
	    //properties
		properties_package(retBody);
		
		setOutput(context, retBody);
	}
	/**
	 * category breadcrumbs  封装
	 * @param category
	 * @param brand
	 * @param retBody 
	 * @param doc 
	 */
	private static void category_package(String brand, RetBody retBody, Document doc) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements cateElements = doc.select("ul#nav_breadcrumb li:not(:first-child) span");
		for (Element element : cateElements) {
			String cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(element.text()));
			if (StringUtils.isNotBlank(cat)) {
				cats.add(cat);
				breads.add(cat);
			}
		}
		//cats
		retBody.setCategory(cats);
		
		// BreadCrumb
		breads.add(brand);
		retBody.setBreadCrumb(breads);
	}

	
	/***
	 * 描述　　封装
	 * @param desc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		//desc trans doc
		Elements es = doc.select("div#productInfo p , div#productInfo b , div#productInfo ul li");
		StringBuilder sb = new StringBuilder();
		if (es != null && es.size() > 0) {
			int count = 1;
			for (Element e : es) {
				String text = e.text();
				if(StringUtils.isNotBlank(text)){
					featureMap.put("feature-" + count, text);
					count++;
					sb.append(text);
				}
			}
		}
		retBody.setFeatureList(featureMap);
		descMap.put("en", sb.toString());
		retBody.setDescription(descMap);
	}
	
	/**
	 * properties 封装
	 * @param gender 
	 * @param retBody
	 */
	private static void properties_package(RetBody retBody) {
		Map<String, Object> propMap = new HashMap<String, Object>();
		String gender = getSex(retBody.getTitle().getEn());
		if(StringUtils.isBlank(gender)){
			gender = getSex(retBody.getCategory().toString());
		}
		propMap.put("s_gender", gender);
		retBody.setProperties(propMap);
	}
	
	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN)){
			gender = "women";
		} else if(StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		} 
		return gender;
	}
	
	/**
	 * 线上爬取
	 * @param url
	 * @param path
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	private String crawler_result(Url url, String path) throws ClientProtocolException, HttpException, IOException {
		String proxyRegionId = url.getTask().getProxyRegionId();
		String content = StringUtils.EMPTY;
		 if(StringUtils.isBlank(proxyRegionId)){
			 content = Crawler.create().timeOut(15000).url(path).header(getHeaders()).proxy(false).resultAsString();
		 }else{
			 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
					 true);
			 String proxyAddress=proxy.getIp();
			 int proxyPort=proxy.getPort();
			 content = Crawler.create().timeOut(15000).url(path).header(getHeaders()).proxy(true).proxyAddress(proxyAddress)
					 .proxyPort(proxyPort).resultAsString();
		 }
		 return content;
	}
	
	private static Map<String,Object> getHeaders(){
		@SuppressWarnings("serial")
		final Map<String,Object> headers = new HashMap<String,Object>(){
			{
				put("Cookie", "userLanguage=en; prefShipCtry=GB; userCountry=United Kingdom; usergeo=USER;");
			}
		};
		return headers;
	}

}
