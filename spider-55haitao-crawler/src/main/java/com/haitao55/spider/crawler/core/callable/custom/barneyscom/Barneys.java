package com.haitao55.spider.crawler.core.callable.custom.barneyscom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
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
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * 
* Title: barneys详情数据封装
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月15日 上午11:29:22
* @version 1.0
 */
public class Barneys extends AbstractSelect{
	private static final String vps = "asdl.55haitao.com/pool?pool=9998";
	private static final String stockFLag = "In Stock";
	private static final String domain = "www.barneys.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		long start = System.currentTimeMillis();
		String url = context.getCurrentUrl().toString();
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("Accept-Encoding", "gzip, deflate, sdch");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		headers.put("Accept", "textml,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		 String content = HttpUtils.crawler_package(context, headers);
		Document doc = JsoupUtils.parse(content);
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();
		
		String productData = StringUtils.substringBetween(content, "digitalData = ", ";");
		if(StringUtils.isBlank(productData)){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"barneys.com itemUrl:" + context.getUrl().toString() + "  this item is offline....");
		}
		
		JSONObject productJSONObject = JSONObject.parseObject(productData);
		
		
		//productId
		String default_sku_id = StringUtils.EMPTY;
		default_sku_id = default_sku_id_package(productJSONObject);
		
		//spu price
		boolean spu_price_flag = false;
		
		//spu stock status
		int spu_stock_status = 0;
		
		//unit
		String unit = StringUtils.EMPTY;
		Elements unitElements = doc.select("span[itemprop=priceCurrency]");
		if(CollectionUtils.isNotEmpty(unitElements)){
			unit = unitElements.attr("content");
		}
		
		//brand
		String brand = StringUtils.EMPTY;
		brand = brand_package(productJSONObject);
		//title
		String title = StringUtils.EMPTY;
		title = title_package(productJSONObject);
		
		//category
		String category = StringUtils.EMPTY;
		category = category_package(productJSONObject);
		
		//颜色对应图片封装
		JSONObject imageJSONObject = new JSONObject();
		color_image_package(doc,productJSONObject,imageJSONObject);
		
		
		//switch_image JSONObject
		JSONObject switchImageJSONObject = new JSONObject();
		
		//封装数据,验证是否存在多个color
		List<JSONObject> params = new ArrayList<JSONObject>();
		params_package(doc,params,url,switchImageJSONObject);
		
		//sku json数据
		JSONArray skuJSONArray = new JSONArray();
		skuJSONArray_package(doc,productJSONObject,skuJSONArray);
		
		//handle 多个color时，发送请求
		new BarneysHandler().process(params,skuJSONArray,imageJSONObject,url,context);
		
		//遍历sku jsonarray
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		
		//style 
		JSONObject styleJsonObject = new JSONObject();
		//　iteartor skuarray
		if(null!=skuJSONArray && skuJSONArray.size()>0){
			for (Object object : skuJSONArray) {
				JSONObject skuJsonObejct=(JSONObject)object;
				
				// selectlist
				LSelectionList lselectlist = new LSelectionList();
				//skuId
				String skuId = skuJsonObejct.getString("sku");
				//stock
				int stock_number=0;
				int stock_status = skuJsonObejct.getIntValue("stock_status");
				if(stock_status > 0){
					spu_stock_status = 1;
				}
				
				
				//price
				float sale_price = skuJsonObejct.getFloatValue("sale_price");
				float orign_price = skuJsonObejct.getFloatValue("orign_price");
				int save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100);
				
				
				
				String color_id = skuJsonObejct.getString("productId");
				//spu price
				if(!spu_price_flag){
					if(default_sku_id.equals(color_id)){
						retBody.setPrice(
								new Price(orign_price, save, sale_price, unit));
						spu_price_flag=true;
					}
				}
				
				String sku_color = skuJsonObejct.getString("color");
				String sku_size = skuJsonObejct.getString("size");
				
				//selections
				List<Selection> selections = new ArrayList<Selection>();
				if(StringUtils.isNotBlank(sku_size)){
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("Size");
					selection.setSelect_value(sku_size);
					selections.add(selection);
				}
				
				//lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
				lselectlist.setPrice_unit(unit);
				lselectlist.setStock_status(stock_status);
				lselectlist.setStock_number(stock_number);
				lselectlist.setStyle_id(sku_color);
				lselectlist.setSelections(selections);
				
				//l_selection_list
				l_selection_list.add(lselectlist);
				
				//style json
				styleJsonObject.put(color_id, skuJsonObejct);
			}
		}
		
		//stylelist 封装
		if(null != styleJsonObject  && styleJsonObject.size()>0){
			for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
				String colorId = entry.getKey();
				JSONObject jsonObject = (JSONObject)entry.getValue();
				// stylelist
				LStyleList lStyleList = new LStyleList();
				//skuId
				String skuId = jsonObject.getString("sku");
				String switch_img=switchImageJSONObject.getString(colorId);
				if(default_sku_id.equals(colorId)){
					lStyleList.setDisplay(true);
				}
				
				//item image upc 
				
				String sku_color=jsonObject.getString("color");
				// stylelist
				lStyleList.setGood_id(skuId);
				lStyleList.setStyle_switch_img(switch_img);
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_id(sku_color);
				lStyleList.setStyle_cate_name("Color");
				lStyleList.setStyle_name(sku_color);
				
				//images
				@SuppressWarnings("unchecked")
				List<Image> pics = (List<Image>)imageJSONObject.get(colorId);
				context.getUrl().getImages().put(skuId, pics);
				//l_style_list
				l_style_list.add(lStyleList);
			}
		}
		
		//sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		
		retBody.setSku(sku);
		
		//stock
		retBody.setStock(new Stock(spu_stock_status));
		
		//brand
		retBody.setBrand(new Brand(brand, "", "", ""));;
		
		//title
		retBody.setTitle(new Title(title, "", "", ""));
		
		
		// full doc info
		String docid = SpiderStringUtil.md5Encode(url);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		
		//category breadcrumb
		category_package(category,brand ,retBody);
		
		// description
	    desc_package(doc,retBody);
	    
	    //properties
		properties_package(retBody);
		
		long end = System.currentTimeMillis();
		System.out.println(end-start);
		setOutput(context, retBody);
	}



	private String crawler_package(Context context) throws ClientProtocolException, HttpException, IOException {
		String vpsIps = Crawler.create().url(vps).timeOut(15000).resultAsString();
		JSONArray vpsJsonArray = null;
		if(StringUtils.isNotBlank(vpsIps)){
			vpsJsonArray = JSONArray.parseArray(vpsIps);
		}
		String proxy = vpsJsonArray.getString(new Random().nextInt(vpsJsonArray.size()));
		String ip = StringUtils.substringBefore(proxy, ":");
		String port = StringUtils.substringAfter(proxy, ":");
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).header(headers).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
//			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
//			String proxyAddress=proxy.getIp();
//			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(ip).proxyPort(Integer.parseInt(port)).resultAsString();
		}
		return content;
	}
	

	/**
	 * 颜色对应图片 json封装
	 * @param doc
	 * @param productJSONObject
	 * @param imageJSONObject
	 */
	private static void color_image_package(Document doc, JSONObject productJSONObject, JSONObject imageJSONObject) {
		String productId = StringUtils.EMPTY;
		JSONObject jsonObject = productJSONObject.getJSONObject("product");
		if(!jsonObject.isEmpty()){
			JSONObject productInfoJSONObject = jsonObject.getJSONObject("StyleInfo");
			productId = productInfoJSONObject.getString("productID");
		}
		
		//images
		List<Image> images = new ArrayList<Image>();
		Elements imagesElements = doc.select("div.atg_store_productImage div a img");
		if(CollectionUtils.isNotEmpty(imagesElements)){
			for (Element element : imagesElements) {
				String image_url = element.attr("src");
				
				//add
				images.add(new Image(image_url));
			}
		}
		//对应颜色 存在对应productId ，作为key
		imageJSONObject.put(productId, images);
		
		
	}


	/**
	 * sku jsonarray 封装
	 * @param doc 
	 * @param productJSONObject
	 * @param skuJSONArray
	 */
	private static void skuJSONArray_package(Document doc, JSONObject productJSONObject, JSONArray skuJSONArray) {
		String productId = StringUtils.EMPTY;
		JSONObject jsonObject = productJSONObject.getJSONObject("product");
		if(!jsonObject.isEmpty()){
			JSONObject productInfoJSONObject = jsonObject.getJSONObject("StyleInfo");
			productId = productInfoJSONObject.getString("productID");
		}
		
		Elements sizeElements = doc.select("span[class=selector] a");
		
		//sku
		JSONArray jsonArray = jsonObject.getJSONArray("SkuInfo");
		if(null != jsonArray && jsonArray.size() > 0){
			for (Object object : jsonArray) {
				JSONObject skuJSONObject = (JSONObject)object;
				JSONObject skuInfoJSONObject = skuJSONObject.getJSONObject("productInfo");
				skuInfoJSONObject.put("productId", productId);
				
				sku_price_package(doc,sizeElements,skuInfoJSONObject);
				
				String invStatus = skuInfoJSONObject.getString("invStatus");
				int stock_status = 0 ;
				if(StringUtils.equalsIgnoreCase(stockFLag, invStatus)){
					stock_status = 1 ;
				}
				skuInfoJSONObject.put("stock_status", stock_status);
				//add
				skuJSONArray.add(skuInfoJSONObject);
			}
		}
	}

	/**
	 * 
	 * @param doc
	 * @param sizeElements
	 * @param skuInfoJSONObject
	 */
	private static void sku_price_package(Document doc, Elements sizeElements, JSONObject skuInfoJSONObject) {
		if(CollectionUtils.isEmpty(sizeElements)){
			Elements priceElements = doc.select("span#fp-data");
			if(CollectionUtils.isEmpty(priceElements)){
				Elements elements = doc.select("div.atg_store_productPrice div.red-discountPrice");
				//没有折扣
				if(CollectionUtils.isEmpty(elements)){
					elements = doc.select("div.atg_store_productPrice");
					String price = elements.text();
					price = StringUtils.replacePattern(price, "[$, ]", "");
					skuInfoJSONObject.put("sale_price", price);
					skuInfoJSONObject.put("orign_price", price);
				}
				
				//存在折扣
				else{
					elements = doc.select("div.atg_store_productPrice");
					Elements salePriceElements = elements.select("div.red-discountPrice");
					Elements orignPriceElements = elements.select("del.red-strike");
					String sale_price = salePriceElements.text();
					String orign_price = orignPriceElements.text();
					sale_price = StringUtils.replacePattern(sale_price, "[$, ]", "");
					orign_price = StringUtils.replacePattern(orign_price, "[$, ]", "");
					skuInfoJSONObject.put("sale_price", sale_price);
					skuInfoJSONObject.put("orign_price", orign_price);
				}
			}else{
				skuInfoJSONObject.put("sale_price", priceElements.attr("data-minprice"));
				skuInfoJSONObject.put("orign_price", priceElements.attr("data-maxprice"));
			}
		}else{
			for (Element element : sizeElements) {
				if(!StringUtils.equalsIgnoreCase(element.text(), skuInfoJSONObject.getString("size"))){
					continue ;
				}
				skuInfoJSONObject.put("sale_price", element.attr("data-sale-price"));
				skuInfoJSONObject.put("orign_price", element.attr("data-list-price"));
			}
		}
	}



	/**
	 * 多个color 请求数据封装
	 * @param doc
	 * @param params
	 * @param url 
	 * @param switchImageJSONObject 
	 */
	private static void params_package(Document doc, List<JSONObject> params, String url, JSONObject switchImageJSONObject) {
		Elements colorElements = doc.select("span.selector.pdp-thumbnail-img.pdp-swatch-scroll a");
		if(CollectionUtils.isNotEmpty(colorElements)){
			for (Element element : colorElements) {
				String colorId = element.attr("data-productid");
				//switch_image
				Elements switchIamgeElements = element.select("img");
				String switch_image = StringUtils.EMPTY;
				if(CollectionUtils.isNotEmpty(switchIamgeElements)){
					switch_image = switchIamgeElements.attr("src");
				}
				switchImageJSONObject.put(colorId, switch_image);
				//排除当前链接对应到默认颜色，默认颜色sku jsonarray 另作处理
				if(StringUtils.containsIgnoreCase(url, colorId)){
					continue ;
				}
				JSONObject jsonObject = new JSONObject();
				String data_picker = element.attr("data-picker");
				
				jsonObject.put("colorId", colorId);
				jsonObject.put("data_picker", data_picker);
				
				//add
				params.add(jsonObject);
			}
		}
	}
	
	
	
	/**
	 * default_sku_id 获取
	 * @param productJSONObject
	 * @return
	 */
	private static String default_sku_id_package(JSONObject productJSONObject) {
		String productId = StringUtils.EMPTY;
		JSONObject jsonObject = productJSONObject.getJSONObject("product");
		if(!jsonObject.isEmpty()){
			JSONObject productInfoJSONObject = jsonObject.getJSONObject("StyleInfo");
			productId = productInfoJSONObject.getString("productID");
		}
		return productId;
	}
	/**
	 * brand 获取
	 * @param productJSONObject
	 * @return
	 */
	private static String brand_package(JSONObject productJSONObject) {
		String brand = StringUtils.EMPTY;
		JSONObject jsonObject = productJSONObject.getJSONObject("product");
		if(!jsonObject.isEmpty()){
			JSONObject productInfoJSONObject = jsonObject.getJSONObject("StyleInfo");
			brand = productInfoJSONObject.getString("brand");
		}
		return brand;
	}
	/**
	 * title 获取
	 * @param productJSONObject
	 * @return
	 */
	private static String title_package(JSONObject productJSONObject) {
		String brand = StringUtils.EMPTY;
		JSONObject jsonObject = productJSONObject.getJSONObject("product");
		if(!jsonObject.isEmpty()){
			JSONObject productInfoJSONObject = jsonObject.getJSONObject("StyleInfo");
			brand = productInfoJSONObject.getString("productName");
		}
		return brand;
	}
	
	/**
	 * category 封装
	 * @param productJSONObject
	 * @return
	 */
	private static String category_package(JSONObject productJSONObject) {
		String category = StringUtils.EMPTY;
		JSONObject pageJSONObject = productJSONObject.getJSONObject("page");
		if(!pageJSONObject.isEmpty()){
			JSONObject categoryJSONObject = pageJSONObject.getJSONObject("category");
			StringBuffer categoryBuffer = new StringBuffer();
			categoryBuffer.append(categoryJSONObject.getString("subCategory1"));
			categoryBuffer.append(",");
			categoryBuffer.append(categoryJSONObject.getString("subCategory2"));
			categoryBuffer.append(",");
			categoryBuffer.append(categoryJSONObject.getString("primaryCategory"));
			category = categoryBuffer.toString();
		}
		return category;
	}
	
	/**
	 * category breadcrumbs  封装
	 * @param category
	 * @param brand
	 * @param retBody 
	 */
	private static void category_package(String category, String brand, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		String[] category_data = StringUtils.split(category, ",");
		if (null != category_data && category_data.length>0) {
			for (String cat : category_data) {
				String str =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(cat));
				if (StringUtils.isNotBlank(str)) {
					cats.add(str);
					breads.add(str);
				}
			}
		}
		retBody.setCategory(cats);
		
		// BreadCrumb
		if(StringUtils.isNotBlank(brand)){
			breads.add(brand);
		}
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
		Elements es = doc.select("div.hidden-xs.hidden-sm div font , div.hidden-xs.hidden-sm div ul li");
		if(CollectionUtils.isEmpty(es)){
			es = doc.select("div.pdpReadMore div , div.pdpReadMore div i");
		}
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
		} 
		else if(StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		} 
		return gender;
	}
}
