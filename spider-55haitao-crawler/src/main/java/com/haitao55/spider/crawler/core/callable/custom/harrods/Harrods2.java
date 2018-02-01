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
import org.jsoup.helper.StringUtil;
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
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * harrods 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年2月9日 下午3:55:54
 * @version 1.0
 */
public class Harrods2 extends AbstractSelect{
	private static final String DOMAIN = "www.harrods.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	private static final String REQUEST_URL_COLOR = "https://www.harrods.com/en-gb/product/colours/{}";
	private static final String REQUEST_URL_SIZE = "https://www.harrods.com/en-gb/product/sizes/{}";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).header(getHeaders()).url(url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		Document doc = JsoupUtils.parse(content);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// spu stock status
		int spu_stock_status = 0;

		// productId
		String productId = StringUtils.EMPTY;
		Elements productIdElements = doc.select("input[name=ProductCode]");
		if (CollectionUtils.isNotEmpty(productIdElements)) {
			productId = productIdElements.attr("value");
		}

		String requestUrl = StringUtils.replacePattern(REQUEST_URL_COLOR, "\\{\\}", productId);
		String otherContent = HttpUtils.get(requestUrl);
		Document otherDoc = JsoupUtils.parse(otherContent);

		// defaultSkuId
		String defaultSkuId = StringUtils.EMPTY;
		Elements skuIdElements = doc.select("div.buying-controls_prodID.js-buying-control-prodID");
		if (CollectionUtils.isNotEmpty(skuIdElements)) {
			defaultSkuId = skuIdElements.text();
		}

		// brand
		StringBuffer brandBuffer = new StringBuffer();
		String brand = StringUtils.EMPTY;
		Elements brandElements = doc.select("a.buying-controls_brand span[itemprop=name]");
		if (CollectionUtils.isNotEmpty(brandElements)) {
			brandBuffer.append(brandElements.text() + " ");
		}
		brand = StringUtils.trim(brandBuffer.toString());
		// title
		String title = StringUtils.EMPTY;
		Elements titleElements = doc.select("span[class=buying-controls_name]");
		if (CollectionUtils.isNotEmpty(titleElements)) {
			title = titleElements.text();
		}

		// unit
		String unit = StringUtils.substringBetween(content, "currency: \"", "\"");
         if(StringUtil.isBlank(unit)){
        	 unit = StringUtils.substringBetween(content, "currency:\"", "\"");
         }
         if(StringUtil.isBlank(unit)){
        	 throw new ParseException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,"itemUrl:"+context.getUrl().toString()+"item unit data not found..");
         }
		// colorList 页面实际展示存在的color列表
		List<String> colorList = new ArrayList<String>();
		colorListPackage(doc, colorList);
		// color 目前通过观察各个种类到商品，化妆品才会有color的sku,一般都是单颜色多size形式
		Elements colorElements = otherDoc.select("select[name=productColour] option");
		if (CollectionUtils.isEmpty(colorElements)) {
			colorElements = otherDoc.select(
					"div.field.buying-controls_option.buying-controls_option--colour.js-buying-controls_option--colour  span");
		}
		// size
		Elements sizeElements = doc.select("select[name=productSize] option");
		// sizeList 页面实际展示size
		List<String> sizeList = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(sizeElements)) {
			sizeListPackage(doc, sizeList);
			String requestSizeUrl = StringUtils.replacePattern(REQUEST_URL_SIZE, "\\{\\}", productId);
			String otherSizeContent = HttpUtils.get(requestSizeUrl);
			Document otherSizeDoc = JsoupUtils.parse(otherSizeContent);
			sizeElements = otherSizeDoc.select("select[name=productSize] option");
		}

		// skuJsonArray封装
		JSONArray skuJsonArray = new JSONArray();
		sku_jsonArray_package(skuJsonArray, colorElements, sizeElements, colorList, sizeList);

		// skuJsonArray 遍历
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();

		// style
		JSONObject styleJsonObject = new JSONObject();

		if (null != skuJsonArray && skuJsonArray.size() > 0) {
			for (Object object : skuJsonArray) {
				JSONObject skuJsonObejct = (JSONObject) object;

				// selectlist
				LSelectionList lselectlist = new LSelectionList();

				// skuId
				String skuId = skuJsonObejct.getString("skuId");

				// color
				String color = skuJsonObejct.getString("color");
				if (StringUtils.isBlank(color)) {
					color = "default";
				}

				// size
				String size = skuJsonObejct.getString("size");

				// stock
				int stock_status = skuJsonObejct.getIntValue("stock_status");
				int stock_number = 0;
				if (stock_status > 0) {
					spu_stock_status = 1;
				}

				// price
				float sale_price = skuJsonObejct.getFloatValue("sale_price");
				float orign_price = skuJsonObejct.getFloatValue("orign_price");

				// style_id
				String style_id = color;

				// selections
				List<Selection> selections = new ArrayList<Selection>();
				if (StringUtils.isNotBlank(size)) {
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("Size");
					selection.setSelect_value(size);
					selections.add(selection);
				}

				// lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
				lselectlist.setPrice_unit(unit);
				lselectlist.setStock_status(stock_status);
				lselectlist.setStock_number(stock_number);
				lselectlist.setStyle_id(style_id);
				lselectlist.setSelections(selections);

				// l_selection_list
				l_selection_list.add(lselectlist);

				// style json
				styleJsonObject.put(style_id, skuJsonObejct);
			}

			// stylelist
			if (!styleJsonObject.isEmpty()) {
				for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
					String style_id = entry.getKey();

					JSONObject jsonObject = (JSONObject) entry.getValue();

					// stylelist
					LStyleList lStyleList = new LStyleList();
					// skuId
					String skuId = jsonObject.getString("skuId");

					int save = jsonObject.getIntValue("save");
					if (StringUtils.containsIgnoreCase(defaultSkuId, skuId)||styleJsonObject.size()==1) {
						lStyleList.setDisplay(true);
						// price
						float sale_price = jsonObject.getFloatValue("sale_price");
						float orign_price = jsonObject.getFloatValue("orign_price");
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
					}

					String switch_img = StringUtils.EMPTY;
					// switch_img
					switch_img = jsonObject.getString("switch_img");

					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(style_id);

					// images
					@SuppressWarnings("unchecked")
					List<Image> sku_pics = (List<Image>) jsonObject.get("imageUlrs");

					context.getUrl().getImages().put(skuId, sku_pics);
					
					// l_style_list
					l_style_list.add(lStyleList);
				}
			}
		}
		
		// sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		retBody.setSku(sku);

		if(CollectionUtils.isEmpty(l_style_list)){
			String salePrice = StringUtils.substringBetween(content, "data-product-option-now-price=\"", "\"");
			String instock = StringUtils.substringBetween(content, "availability\" href=\"", "\"");
			Elements imageEs = doc.select("li.pdp_images-item img");
			List<Image> pics = new ArrayList<Image>();
			for(Element e : imageEs){
				String image = e.attr("src");
				if(StringUtils.isNotBlank(image)){
					pics.add(new Image("https:"+image));
				}
			}
			context.getUrl().getImages().put(productId, pics);// picture
			if(StringUtils.isNotBlank(salePrice)){
				retBody.setPrice(new Price(Float.parseFloat(salePrice), 
						0, Float.parseFloat(salePrice), unit));
			}
			
			if(StringUtils.isNotBlank(instock)){
				spu_stock_status = 1;
			}
		
		}
		
		// stock
		retBody.setStock(new Stock(spu_stock_status));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(productId+DOMAIN);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(DOMAIN));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

		// brand
		retBody.setBrand(new Brand(brand, "", "", ""));

		// title
		retBody.setTitle(new Title(title, "", "", ""));

		// category breadcrumb
		category_package(doc, brand, title, retBody);

		// description
		desc_package(doc, retBody);

		// properties
		properties_package(doc, retBody);

		setOutput(context, retBody);
	}

	/**
	 * 封装页面展示的颜色
	 * 
	 * @param doc
	 * @param colorList
	 */
	private static void colorListPackage(Document doc, List<String> colorList) {
		Elements colorElements = doc.select("select[name=productColour] option");
		if (CollectionUtils.isEmpty(colorElements)) {
			colorElements = doc.select(
					"div.field.buying-controls_option.buying-controls_option--colour.js-buying-controls_option--colour  span");
		}
		for (Element element : colorElements) {
			String color = element.text();
			if (StringUtils.isNotBlank(color)) {
				colorList.add(color);
			}
		}
	}

	/**
	 * 封装页面展示size
	 * 
	 * @param doc
	 * @param sizeList
	 */
	private static void sizeListPackage(Document doc, List<String> sizeList) {
		Elements sizeElements = doc.select("select[name=productSize] option");
		for (Element element : sizeElements) {
			String size = element.text();
			if (StringUtils.isNotBlank(size)) {
				sizeList.add(size);
			}
		}
	}

	/**
	 * sku jsonarry 数据封装
	 * 
	 * @param skuJsonArray
	 * @param colorElements
	 * @param sizeElements
	 * @param colorList
	 * @param sizeList
	 */
	private static void sku_jsonArray_package(JSONArray skuJsonArray, Elements colorElements, Elements sizeElements,
			List<String> colorList, List<String> sizeList) {
		if (CollectionUtils.isNotEmpty(colorElements)) {
			for (Element colorElement : colorElements) {
				JSONObject jsonObject = new JSONObject();
				List<Image> imageUlrs = new ArrayList<Image>();
				int stock_status = 0;
				String color = colorElement.text();
				if (!colorList.contains(color)) {
					continue;
				}
				String size = StringUtils.EMPTY;
				String skuId = colorElement.attr("data-product-option-code");
				String stock = colorElement.attr("data-max-qty");
				if (StringUtils.isNotBlank(stock) && Integer.parseInt(stock) > 0) {
					stock_status = 1;
				}
				String orign_price = colorElement.attr("data-product-option-was-price");
				String sale_price = colorElement.attr("data-product-option-now-price");
				price_package(orign_price, sale_price, jsonObject);
				// images
				String imagesAttr = colorElement.attr("data-gallery-images");
				JSONArray imagesJSONArray = JSONArray.parseArray(imagesAttr);
				imagesPackage(imagesJSONArray, imageUlrs);
				// switchImg
				String switch_img = colorElement.attr("data-product-image-swatch");
				if (!StringUtils.contains(switch_img, "http")) {
					switch_img = "http:" + switch_img;
				}
				if (CollectionUtils.isNotEmpty(sizeElements)) {
					for (Element sizeElement : sizeElements) {
						JSONObject jsonObjectWithSize = new JSONObject();

						size = sizeElement.text();
						if (!sizeList.contains(size)) {
							continue;
						}
						if (StringUtils.isBlank(skuId)) {
							skuId = sizeElement.attr("data-product-option-code");
						}
						orign_price = sizeElement.attr("data-product-option-was-price");
						sale_price = sizeElement.attr("data-product-option-now-price");
						stock = sizeElement.attr("data-max-qty");
						if (StringUtils.isNotBlank(stock) && Integer.parseInt(stock) > 0) {
							stock_status = 1;
						}
						size = sizeElement.text();
						price_package(orign_price, sale_price, jsonObjectWithSize);

						// jsonObject封装
						sku_json_package(color, size, skuId, stock_status, imageUlrs, switch_img, jsonObjectWithSize);

						// jsonArray 封装
						skuJsonArray.add(jsonObjectWithSize);
					}

				} else {
					sku_json_package(color, size, skuId, stock_status, imageUlrs, switch_img, jsonObject);
					// jsonArray 封装
					skuJsonArray.add(jsonObject);
				}
			}
		}
	}

	private static void imagesPackage(JSONArray imagesJSONArray, List<Image> imageUlrs) {
		if (CollectionUtils.isEmpty(imagesJSONArray)) {
			return;
		}
		for (Object object : imagesJSONArray) {
			String imageUrl = (String) object;
			if (StringUtils.isBlank(imageUrl)) {
				continue;
			}
			if (!StringUtils.contains(imageUrl, "http")) {
				imageUrl = "http:" + imageUrl;
			}
			if (!StringUtils.contains(imageUrl, ".jpg")) {
				imageUrl = imageUrl + ".jpg";
			}
			imageUlrs.add(new Image(imageUrl));
		}
	}

	/**
	 * sku json 数据封装
	 * 
	 * @param color
	 * @param size
	 * @param skuId
	 * @param stock_status
	 * @param imageUlrs
	 * @param switch_img
	 * @param jsonObject
	 */
	private static void sku_json_package(String color, String size, String skuId, int stock_status,
			List<Image> imageUlrs, String switch_img, JSONObject jsonObject) {
		jsonObject.put("color", color);
		jsonObject.put("size", size);
		jsonObject.put("skuId", skuId);
		jsonObject.put("stock_status", stock_status);
		jsonObject.put("imageUlrs", imageUlrs);
		jsonObject.put("switch_img", switch_img);
	}

	/**
	 * price 封装
	 * 
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
		if (StringUtils.isBlank(orign_price) || Float.valueOf(orign_price) < Float.valueOf(sale_price)) {
			orign_price = sale_price;
		}
		if (StringUtils.isBlank(save)) {
			save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100) + "";// discount
		}
		jsonObject.put("orign_price", orign_price);
		jsonObject.put("sale_price", sale_price);
		jsonObject.put("save", save);
	}

	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/60.0.3112.113 Chrome/60.0.3112.113 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.harrods.com");
		return headers;
	}
	
	
	/**
	 * category breadcrumbs 封装
	 * 
	 * @param doc
	 * @param brand
	 * @param title
	 * @param retBody
	 */
	private static void category_package(Document doc, String brand, String title, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements categoryElements = doc.select("section.breadcrumb ol li a");
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (Element elements : categoryElements) {
				String cat = StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(elements.text()));
				if (StringUtils.equalsIgnoreCase("Home", cat)) {
					continue;
				}
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
		if (StringUtils.isNotBlank(brand)) {
			breads.add(brand);
		}
		retBody.setBreadCrumb(breads);
	}

	/***
	 * 描述 封装
	 * 
	 * @param doc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = doc.select(
				"div.product-info_content.js-accordion-content ul[class=product-info_list] li , div[itemprop=description] p");
		StringBuilder sb = new StringBuilder();
		if (es != null && es.size() > 0) {
			int count = 1;
			for (Element e : es) {
				String text = e.text();
				if (StringUtils.isNotBlank(text)) {
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
	 * 
	 * @param doc
	 * @param retBody
	 */
	private static void properties_package(Document doc, RetBody retBody) {
		String gender = StringUtils.EMPTY;
		Elements categoryElements = doc.select("section.breadcrumb ol li a");
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (Element element : categoryElements) {
				String url = element.attr("href");
				gender = getSex(url);
			}

		}
		Map<String, Object> propMap = new HashMap<String, Object>();
		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getTitle().getEn());
		}
		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getCategory().toString());
		}
		propMap.put("s_gender", gender);
		retBody.setProperties(propMap);
	}

	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN)) {
			gender = "women";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		}
		return gender;
	}
}
