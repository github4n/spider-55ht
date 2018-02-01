package com.haitao55.spider.crawler.core.callable.custom.everlane;

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
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * everlane 详情数据封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年2月17日 下午2:01:31
 * @version 1.0
 */
public class Everlane extends AbstractSelect{
	private static final String ITEM_API = "https://www.everlane.com/api/v2/product_groups?product_permalink=";
	private static final String INSTOCK = "shippable";
	private static final String DOMAIN = "www.everlane.com";
	private static final String SEX_WOMEN = "female";
	private static final String SEX_MEN = "male";
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();

		String param = StringUtils.substring(url, StringUtils.lastIndexOf(url, "/") + 1);
		String request_url = ITEM_API.concat(param);
		String content = crawler_package(context,request_url);
		context.put(input, content);
		Document doc = this.getDocument(context);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// productId
		String productId = param;

		// gender
		String gender = StringUtils.EMPTY;

		// spu stock
		int spu_stock_status = 0;

		// unit
		String unit = StringUtils.EMPTY;
		Elements unitElements = doc.select("span.product-heading__price-value");
		if (CollectionUtils.isNotEmpty(unitElements)) {
			unit = getCurrencyValue(unitElements.text());
		}
		if (StringUtils.isBlank(unit)) {
			unit = "USD";
		}
		
		//brand
		String brand = "Everlane";
		
		// product jsonarray
		if (StringUtils.isNotBlank(content)) {
			JSONObject productJSONObject = JSONObject.parseObject(content);

			// image jsonobject
			JSONObject imageJSONObject = new JSONObject();
			// gender jsonobject
			JSONObject genderJSONObject = new JSONObject();
			// title jsonobject
			JSONObject titleJSONObject = new JSONObject();
			// description jsonobject
			JSONObject descJSONObject = new JSONObject();
			//display name jsonobject
			JSONObject displayJSONObject = new JSONObject();
			

			// sku jsonarray
			JSONArray skuJSONArray = new JSONArray();
			sku_jsonarray_package(productJSONObject, skuJSONArray, imageJSONObject, genderJSONObject, titleJSONObject,
					descJSONObject,displayJSONObject);

			// sku iterator
			// 遍历sku jsonarray
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			// style
			JSONObject styleJsonObject = new JSONObject();
			if (null != skuJSONArray && skuJSONArray.size() > 0) {
				for (Object object : skuJSONArray) {
					JSONObject skuJsonObejct = (JSONObject) object;

					// selectlist
					LSelectionList lselectlist = new LSelectionList();
					// skuId
					String skuId = skuJsonObejct.getString("skuId");
					// stock
					int stock_number = 0;
					int stock_status = skuJsonObejct.getIntValue("stock_status");
					if (stock_status > 0) {
						spu_stock_status = 1;
					}

					// price
					float sale_price = skuJsonObejct.getFloatValue("sale_price");
					float orign_price = skuJsonObejct.getFloatValue("orign_price");

					String color_id = skuJsonObejct.getString("colorId");

					String sku_color = skuJsonObejct.getString("color");
					String sku_size = skuJsonObejct.getString("size");
					//spu price 
					String permalink = skuJsonObejct.getString("permalink");
					if(StringUtils.equalsIgnoreCase(productId, permalink)){
						int save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100);
						// spu price
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
					}

					// selections
					List<Selection> selections = new ArrayList<Selection>();
					if (StringUtils.isNotBlank(sku_size)) {
						Selection selection = new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name("Size");
						selection.setSelect_value(sku_size);
						selections.add(selection);
					}

					// lselectlist
					lselectlist.setGoods_id(skuId);
					lselectlist.setOrig_price(orign_price);
					lselectlist.setSale_price(sale_price);
					lselectlist.setPrice_unit(unit);
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id(sku_color);
					lselectlist.setSelections(selections);

					// l_selection_list
					l_selection_list.add(lselectlist);

					// style json
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
					String skuId = jsonObject.getString("skuId");
					String switch_img=StringUtils.EMPTY;
					
					//item image upc 
					String sku_color=jsonObject.getString("color");
					
					String permalink = jsonObject.getString("permalink");
					if(StringUtils.equalsIgnoreCase(productId, permalink)){
						lStyleList.setDisplay(true);
					}
					
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
			retBody.setTitle(new Title(titleJSONObject.getString("title"), "", "", ""));
			
			
			// full doc info
			String docid = SpiderStringUtil.md5Encode(DOMAIN+productId);
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(DOMAIN));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
			
			gender = genderJSONObject.getString("gender");

			//category breadcrumb
			category_package(gender,brand ,displayJSONObject,retBody);
			
			// description
		    desc_package(descJSONObject,retBody);
		    
		    //properties
			properties_package(gender,retBody);

		}
		setOutput(context, retBody);
	}

	/**
	 * properties 封装
	 * @param gender
	 * @param retBody
	 */
	private static void properties_package(String gender, RetBody retBody) {
		Map<String, Object> propMap = new HashMap<String, Object>();
		gender = getSex(gender);
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
	 * 商品描述封装
	 * @param descJSONObject
	 * @param retBody
	 */
	private static void desc_package(JSONObject descJSONObject, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		StringBuilder sb = new StringBuilder();
		JSONArray jsonArray = descJSONObject.getJSONArray("desc");
		if(null != jsonArray && jsonArray.size() > 0){
			int count = 1;
			for (Object object : jsonArray) {
				String text = (String) object;
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
	 * 面包屑封装
	 * @param gender
	 * @param brand
	 * @param displayJSONObject 
	 * @param retBody
	 */
	private static void category_package(String gender, String brand, JSONObject displayJSONObject, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		String display_name = displayJSONObject.getString("display_name");
		List<String> categoryList = new ArrayList<String>();
		gender = getSex(gender);
		categoryList.add(gender);
		categoryList.add(display_name);
		if (CollectionUtils.isNotEmpty(categoryList)) {
			for (String str : categoryList) {
				String cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(str));
				if (StringUtils.isNotBlank(cat)) {
					cats.add(cat);
					breads.add(cat);
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

	/**
	 * sku jsonarray 封装
	 * 
	 * @param productJSONObject
	 * @param skuJSONArray
	 * @param imageJSONObject
	 * @param titleJSONObject
	 * @param genderJSONObject
	 * @param descJSONObject
	 * @param displayJSONObject 
	 */
	private static void sku_jsonarray_package(JSONObject productJSONObject, JSONArray skuJSONArray,
			JSONObject imageJSONObject, JSONObject genderJSONObject, JSONObject titleJSONObject,
			JSONObject descJSONObject, JSONObject displayJSONObject) {
		if (productJSONObject.isEmpty()) {
			return;
		}
		JSONArray productJSONArray = productJSONObject.getJSONArray("products");
		if (null != productJSONArray && productJSONArray.size() > 0) {
			for (Object object : productJSONArray) {
				JSONObject jsonObject = (JSONObject) object;
				String colorId = jsonObject.getString("id");
				String productId = jsonObject.getString("product_group_id");
				// price
				String sale_price = jsonObject.getString("price");
				String orign_price = jsonObject.getString("traditional_price");
				if (StringUtils.isBlank(orign_price)) {
					orign_price = sale_price;
				}
				String gender = jsonObject.getString("gender");
				genderJSONObject.put("gender", gender);
				// color
				JSONObject colorJSONObject = jsonObject.getJSONObject("color");
				String color = colorJSONObject.getString("name");
				String color_value = colorJSONObject.getString("hex_value");

				// title
				String title = jsonObject.getString("display_name");
				titleJSONObject.put("title", title);
				
				//permalink  用於確認默認sku
				String permalink = jsonObject.getString("permalink");
				
				//display name
				displayJSONObject.put("display_name", jsonObject.getString("display_name"));

				// image
				List<Image> images = new ArrayList<Image>();
				JSONObject imagesJSONObject = jsonObject.getJSONObject("albums");
				JSONArray imagesJSONArray = imagesJSONObject.getJSONArray("square");
				if (null != imagesJSONArray && imagesJSONArray.size() > 0) {
					for (Object object2 : imagesJSONArray) {
						images.add(new Image(object2.toString()));
					}
				}
				imageJSONObject.put(colorId, images);

				// description package
				desc_package(jsonObject, descJSONObject);

				// sku iteator
				JSONArray jsonArray = jsonObject.getJSONArray("variants");
				if (null != jsonArray & jsonArray.size() > 0) {
					for (Object object2 : jsonArray) {
						JSONObject skuJSONObject = (JSONObject) object2;

						// sku jsonobject
						JSONObject jsonObject2 = new JSONObject();

						// size
						String sizeId = skuJSONObject.getString("id");
						String size = skuJSONObject.getString("abbreviated_size");

						// sku
						String skuId = skuJSONObject.getString("sku");

						// stock status
						int stock_status = 0;
						String stock = skuJSONObject.getString("orderable_state");
						if (StringUtils.containsIgnoreCase(INSTOCK, stock)) {
							stock_status = 1;
						}

						jsonObject2.put("productId", productId);
						jsonObject2.put("skuId", skuId);
						jsonObject2.put("colorId", colorId);
						jsonObject2.put("color", color);
						jsonObject2.put("color_value", color_value);
						jsonObject2.put("sizeId", sizeId);
						jsonObject2.put("size", size);
						jsonObject2.put("stock_status", stock_status);
						jsonObject2.put("sale_price", sale_price);
						jsonObject2.put("orign_price", orign_price);
						jsonObject2.put("colorId", colorId);
						jsonObject2.put("permalink", permalink);
						
						// jsonarray add
						skuJSONArray.add(jsonObject2);
					}
				}
			}
		}
	}

	/**
	 * product json 中抽取商品描述信息 进行封装
	 * 
	 * @param jsonObject
	 * @param descJSONObject
	 */
	private static void desc_package(JSONObject jsonObject, JSONObject descJSONObject) {
		JSONObject detailJSONObject = jsonObject.getJSONObject("details");
		if(null == detailJSONObject || detailJSONObject.isEmpty()){
			return ;
		}
		JSONObject fabricJSONObject = detailJSONObject.getJSONObject("fabric");
		JSONArray fitJSONArray = detailJSONObject.getJSONArray("fit");
		JSONArray additionalJSONArray = detailJSONObject.getJSONArray("additional_details");
		String description = detailJSONObject.getString("description");
		JSONObject factoryJSONObject = detailJSONObject.getJSONObject("factory");

			if (!fabricJSONObject.isEmpty()) {
				fitJSONArray.add(factoryJSONObject.getString("type"));
			}
			if (null != additionalJSONArray && additionalJSONArray.size() > 0) {
				fitJSONArray.addAll(additionalJSONArray);
			}
			if (!factoryJSONObject.isEmpty()) {
				String localtion = factoryJSONObject.getString("location");
				String country = factoryJSONObject.getString("country");
				fitJSONArray.add(localtion + "," + country);
			}
			if (StringUtils.isNotBlank(description)) {
				fitJSONArray.add(description);
			}
		// put desc data
		descJSONObject.put("desc", fitJSONArray);
	}

	private String crawler_package(Context context, String request_url) throws ClientProtocolException, HttpException, IOException {
		Map<String,Object> headers = new HashMap<String,Object>();
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(request_url).header(headers).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(request_url).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
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
		if (StringUtils.isBlank(currency)) {
			return unit;
		}
		unit = Currency.codeOf(currency).name();
		return unit;

	}
}
