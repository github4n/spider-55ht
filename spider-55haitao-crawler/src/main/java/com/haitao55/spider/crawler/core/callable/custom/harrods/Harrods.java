package com.haitao55.spider.crawler.core.callable.custom.harrods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * harrods 详情封装
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月9日 下午3:55:54
* @version 1.0
 */
public class Harrods extends AbstractSelect{
	private static final String domain = "www.harrods.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	private static final String image_url_flag="https://images.harrods.com/img/{}?$productMain$";
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = crawler_package(context);
//		context.put(input, content);
		Document doc = JsoupUtils.parse(content);
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();
		
		//spu stock status
		int spu_stock_status = 0;
		
		//spu price flag
		boolean spu_price_flag = false;
		
		//productId
		String productId = StringUtils.EMPTY;
		Elements productIdElements = doc.select("span.product_code");
		if(CollectionUtils.isNotEmpty(productIdElements)){
			productId = productIdElements.attr("data-prodid");
		}
		if(StringUtils.isBlank(productId)){
			productId = StringUtils.substringBetween(content, "var prodId = '", "';");
		}
		
		//brand
		String brand = StringUtils.EMPTY;
		Elements brandElements = doc.select("span[class=brand]");
		if(CollectionUtils.isNotEmpty(brandElements)){
			brand = brandElements.text();
		}
		//title
		String title = StringUtils.EMPTY;
		Elements titleElements = doc.select("span[class=productname]");
		if(CollectionUtils.isNotEmpty(titleElements)){
			title = titleElements.text();
		}
		
		//unit
		String unit = StringUtils.EMPTY;
		Elements unitElemets = doc.select("span.prices.price span[itemprop=price]");
		if(CollectionUtils.isEmpty(unitElemets)){
			unitElemets = doc.select("span.prices.price span.was");
		}
		if(CollectionUtils.isNotEmpty(unitElemets)){
			unit = getCurrencyValue(unitElemets.text());
		}
		
		if(!StringUtils.equals(unit, "USD")){
            throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" Harrods-price-unit-is-not-USD...");
        }
		
		//color　目前通过观察各个种类到商品，化妆品才会有color的sku,一般都是单颜色多size形式
		Elements colorElements = doc.select("div.color_c a");
		//size
		Elements sizeElements = doc.select("select#size option");
		
		//skuJsonArray封装
		JSONArray skuJsonArray = new JSONArray();
		sku_jsonArray_package(doc,skuJsonArray,colorElements,sizeElements);
		
		//skuJsonArray　遍历
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		
		//style 
		JSONObject styleJsonObject = new JSONObject();
		
		if(null != skuJsonArray && skuJsonArray.size() > 0){
			for (Object object : skuJsonArray) {
				JSONObject skuJsonObejct = (JSONObject)object;
				
				// selectlist
				LSelectionList lselectlist = new LSelectionList();
				
				//skuId
				String skuId = skuJsonObejct.getString("skuId");
				
				//color
				String color = skuJsonObejct.getString("color");
				if(StringUtils.isBlank(color)){
					color = "default";
				}
				
				//size
				String size = skuJsonObejct.getString("size");
				
				//stock
				int stock_status =skuJsonObejct.getIntValue("stock_status");
				int stock_number=0;
				if(stock_status > 0 ){
					spu_stock_status =1;
				}
				
				//price
				float sale_price = skuJsonObejct.getFloatValue("sale_price");
				float orign_price = skuJsonObejct.getFloatValue("orign_price");
				
				//style_id
				String style_id = color;
				
				//selections
				List<Selection> selections = new ArrayList<Selection>();
				if(StringUtils.isNotBlank(size)){
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("Size");
					selection.setSelect_value(size);
					selections.add(selection);
				}
				
				//lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
				lselectlist.setPrice_unit(unit);
				lselectlist.setStock_status(stock_status);
				lselectlist.setStock_number(stock_number);
				lselectlist.setStyle_id(style_id);
				lselectlist.setSelections(selections);
				
				//l_selection_list
				l_selection_list.add(lselectlist);
				
				//style json
				styleJsonObject.put(style_id, skuJsonObejct);
			}
			
			//stylelist
			if(!styleJsonObject.isEmpty()){
				for (Map.Entry<String,Object> entry : styleJsonObject.entrySet()) {
					String style_id = entry.getKey();
					
					JSONObject jsonObject = (JSONObject) entry.getValue();
					
					// stylelist
					LStyleList lStyleList = new LStyleList();
					//skuId
					String skuId = jsonObject.getString("skuId");
					
					int save = jsonObject.getIntValue("save");
					if(!spu_price_flag){
						lStyleList.setDisplay(true);
						//price
						float sale_price = jsonObject.getFloatValue("sale_price");
						float orign_price = jsonObject.getFloatValue("orign_price");
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
						spu_price_flag = true;
					}
					
					
					String switch_img=StringUtils.EMPTY;
					//switch_img
					switch_img = jsonObject.getString("switch_img");
					
					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(style_id);
					
					//images
					@SuppressWarnings("unchecked")
					List<Image> sku_pics = (List<Image>) jsonObject.get("imageUlrs");
					
					context.getUrl().getImages().put(skuId, sku_pics);
					
					//l_style_list
					l_style_list.add(lStyleList);
				}
			}
		}
		//单品页
		else{
			//price
			String sale_price = StringUtils.EMPTY;
			String orign_price = StringUtils.EMPTY;
			Elements saleElements = doc.select("span.prices.price span.now");
			if(CollectionUtils.isEmpty(saleElements)){
				saleElements = doc.select("span.prices.price span[itemprop=price]");
			}
			sale_price = saleElements.text();
			sale_price = StringUtils.replacePattern(sale_price, "[now £,]", "");
			Elements orignElements = doc.select("span.prices.price span.was");
			orign_price = orignElements.text();
			orign_price = StringUtils.replacePattern(orign_price, "[now £,]", "");
			String save = StringUtils.EMPTY;
			if (StringUtils.isBlank(orign_price)) {
				orign_price = sale_price;
			}
			if (StringUtils.isBlank(sale_price)) {
				sale_price = orign_price;
			}
			if (StringUtils.isBlank(orign_price)
					|| Float.valueOf(orign_price) < Float.valueOf(sale_price)) {
				orign_price = sale_price;
			}
			if (StringUtils.isBlank(save)) {
				save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100)
						+ "";// discount
			}
			
			retBody.setPrice(new Price(Float.valueOf(orign_price), StringUtils.isBlank(save)?0:Integer.parseInt(save), Float.valueOf(sale_price), unit));
			
			List<Image> imageUlrs = new ArrayList<Image>();
			Elements imagesElements = doc.select("ul.alt_view li a img");
			if(CollectionUtils.isNotEmpty(imagesElements)){
				for (Element element : imagesElements) {
					String image_url = element.attr("src");
					image_url = StringUtils.replacePattern(image_url, "thumbnail", "productMain");
					imageUlrs.add(new Image(image_url));
				}
			}
			context.getUrl().getImages().put(productId, imageUlrs);
			
		}
		
		//sku 
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		
		retBody.setSku(sku);
		
		//stock
		retBody.setStock(new Stock(spu_stock_status));
		
		// full doc info
		String docid = SpiderStringUtil.md5Encode(url);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		
		//brand
		retBody.setBrand(new Brand(brand, "", "", ""));;
		
		//title
		retBody.setTitle(new Title(title, "", "", ""));
		
		//category breadcrumb
		category_package(doc,brand,title,retBody);
		
		// description
	    desc_package(doc,retBody);
	    
	    //properties
		properties_package(doc,retBody);
		
		setOutput(context, retBody);
	}

	/**
	 * sku jsonarry 数据封装
	 * @param doc 
	 * @param skuJsonArray
	 * @param colorElements
	 * @param sizeElements
	 */
	private static void sku_jsonArray_package(Document doc, JSONArray skuJsonArray, Elements colorElements, Elements sizeElements) {
		if(CollectionUtils.isNotEmpty(colorElements)){
			for (Element colorElement : colorElements) {
				JSONObject jsonObject = new JSONObject();
				List<Image> imageUlrs = new ArrayList<Image>();
				int stock_status = 0;
				String color = colorElement.attr("data-cname");
				String size = StringUtils.EMPTY;
				String skuId = colorElement.attr("data-prodid");
				String stock = colorElement.attr("data-quan");
				if(StringUtils.isNotBlank(stock) && Integer.parseInt(stock) > 0){
					stock_status = 1;
				}
				String orign_price = colorElement.attr("data-cwasprice");
				String sale_price = colorElement.attr("data-cprice");
				price_package(orign_price,sale_price,jsonObject);
				String image_url = StringUtils.replacePattern(image_url_flag, "\\{\\}", skuId);
				imageUlrs.add(new Image(image_url));
				String switch_img = colorElement.attr("href");
				
				if(CollectionUtils.isNotEmpty(sizeElements)){
					for (Element sizeElement : sizeElements) {
						JSONObject jsonObjectWithSize = new JSONObject();
						orign_price = sizeElement.attr("data-cwasprice");
						sale_price = sizeElement.attr("data-cprice");
						stock = sizeElement.attr("data-quan");
						if(StringUtils.isNotBlank(stock) && Integer.parseInt(stock) > 0){
							stock_status = 1;
						}
						size = sizeElement.text();
						price_package(orign_price,sale_price,jsonObjectWithSize);
						
						//jsonObject封装
						sku_json_package(color,size,skuId,stock_status,imageUlrs,switch_img,jsonObjectWithSize);
						
						//jsonArray 封装
						skuJsonArray.add(jsonObjectWithSize);
					}
					
				}
				else{
					sku_json_package(color,size,skuId,stock_status,imageUlrs,switch_img,jsonObject);
					//jsonArray 封装
					skuJsonArray.add(jsonObject);
				}
			}
		}else if(CollectionUtils.isNotEmpty(sizeElements)){
			List<Image> imageUlrs = new ArrayList<Image>();
			Elements imagesElements = doc.select("ul.alt_view li a img");
			if(CollectionUtils.isNotEmpty(imagesElements)){
				for (Element element : imagesElements) {
					String image_url = element.attr("src");
					image_url = StringUtils.replacePattern(image_url, "thumbnail", "productMain");
					imageUlrs.add(new Image(image_url));
				}
			}
			for (Element sizeElement : sizeElements) {
				JSONObject jsonObject = new JSONObject();
				String color = StringUtils.EMPTY;
				String size = sizeElement.text();
				String switch_img = StringUtils.EMPTY;
				String skuId = sizeElement.attr("data-pid").concat(sizeElement.attr("value"));
				String stock = sizeElement.attr("data-quan");
				int stock_status = 0;
				if(StringUtils.isNotBlank(stock) && Integer.parseInt(stock) > 0){
					stock_status = 1;
				}
				String orign_price = sizeElement.attr("data-cwasprice");
				String sale_price = sizeElement.attr("data-cprice");
				price_package(orign_price,sale_price,jsonObject);
				
				sku_json_package(color,size,skuId,stock_status,imageUlrs,switch_img,jsonObject);
				//jsonArray 封装
				skuJsonArray.add(jsonObject);
			}
		}
	}
	
	/**
	 * sku json　数据封装
	 * @param color
	 * @param size
	 * @param skuId 
	 * @param stock_status
	 * @param imageUlrs
	 * @param switch_img 
	 * @param jsonObject
	 */
	private static void sku_json_package(String color, String size, String skuId, int stock_status, List<Image> imageUlrs,
			String switch_img, JSONObject jsonObject) {
		jsonObject.put("color", color);
		jsonObject.put("size", size);
		jsonObject.put("skuId", skuId);
		jsonObject.put("stock_status", stock_status);
		jsonObject.put("imageUlrs", imageUlrs);
		jsonObject.put("switch_img", switch_img);
	}

	/**
	 * price　封装
	 * @param orign_price
	 * @param sale_price
	 * @param jsonObject
	 */
	private static void price_package(String orign_price, String sale_price, JSONObject jsonObject) {
		String save = StringUtils.EMPTY;
		if (StringUtils.isBlank(orign_price)) {
			orign_price = sale_price;
		}
		if (StringUtils.isBlank(sale_price)) {
			sale_price = orign_price;
		}
		if (StringUtils.isBlank(orign_price)
				|| Float.valueOf(orign_price) < Float.valueOf(sale_price)) {
			orign_price = sale_price;
		}
		if (StringUtils.isBlank(save)) {
			save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100)
					+ "";// discount
		}
		jsonObject.put("orign_price",orign_price);
		jsonObject.put("sale_price",sale_price);
		jsonObject.put("save",save);
	}
	
	
	/**
	 * category breadcrumbs  封装
	 * @param doc 
	 * @param brand
	 * @param title 
	 * @param retBody 
	 */
	private static void category_package(Document doc , String brand, String title, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements categoryElements = doc.select("ul.breadcrumbs li a:not(:first-child)");
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (Element elements : categoryElements) {
				String cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(elements.text()));
				if (StringUtils.isNotBlank(cat)) {
					cats.add(cat);
					breads.add(cat);
				}
			}
		}
		cats.add(title);
		breads.add(title);
		retBody.setCategory(cats);
		
		// BreadCrumb
		if(StringUtils.isNotBlank(brand)){
			breads.add(brand);
		}
		retBody.setBreadCrumb(breads);
	}

	
	/***
	 * 描述　　封装
	 * @param doc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		//desc trans doc
		Elements es = doc.select("p.description , dl#details ul li");
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
	 * @param doc 
	 * @param retBody
	 */
	private static void properties_package(Document doc, RetBody retBody) {
		String gender = StringUtils.EMPTY;
		Elements categoryElements = doc.select("ul.breadcrumbs li a:not(:first-child)");
		if(CollectionUtils.isNotEmpty(categoryElements)){
			for (Element element : categoryElements) {
				String url = element.attr("href");
				gender = getSex(url);
			}
			
		}
		Map<String, Object> propMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(gender)){
			gender = getSex(retBody.getTitle().getEn());
		}
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
	 * get 货币
	 * 
	 * @param val
	 * @return
	 */
	private static String getCurrencyValue(String val) {
		String currency = StringUtils.substring(val, 0, 1);
		String unit = StringUtils.EMPTY;
		unit = Currency.codeOf(currency).name();
		return unit;

	}
	
	private String crawler_package(Context context) throws ClientProtocolException, HttpException, IOException {
		HashMap<String,Object> headers = new HashMap<String,Object>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate, sdch");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");

		headers.put("Cookie", "Country=GB; Currency=GBP; Region=; TranslationCountry=GB;");
		headers.put("Host", "www.harrods.com");
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/55.0.2883.87 Chrome/55.0.2883.87 Safari/537.36");

		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).header(headers).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
}
