package com.haitao55.spider.crawler.core.callable.custom.perfumania;

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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * perfumania 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月8日 上午10:12:12
 * @version 1.0
 */
public class Perfumania extends AbstractSelect{
	private static final String DOMAIN = "www.perfumania.com";
	private static final String OUTOFSTOCK = "unavailable";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		long start = System.currentTimeMillis();
		String url = context.getCurrentUrl().toString();
//		String content = this.getInputString(context);
		Document doc = this.getDocument(context);

		Elements productElements = doc.select("div.product-detail-form");
		if (CollectionUtils.isNotEmpty(productElements)) {
			RetBody retBody = new RetBody();
			Sku sku = new Sku();

			// spu stock status
			int spu_stock_status = 0;

			// product id
			String productId = StringUtils.EMPTY;
			Elements productIdElements = productElements.select("input[name=productId]");
			if (CollectionUtils.isNotEmpty(productIdElements)) {
				productId = productIdElements.attr("value");
			}

			// spu price
			// sale price
			String spu_sale_price = StringUtils.EMPTY;
			Elements spuSalePriceElements = productElements.select("span.sale-price");
			if (CollectionUtils.isNotEmpty(spuSalePriceElements)) {
				spu_sale_price = spuSalePriceElements.text();
			}
			// orign price
			String spu_orign_price = StringUtils.EMPTY;
			Elements spuOrignPriceElements = productElements.select("span.retail-price");
			if (CollectionUtils.isNotEmpty(spuOrignPriceElements)) {
				spu_orign_price = spuOrignPriceElements.text();
			}

			// images
			List<Image> images = new ArrayList<Image>();
			Elements imageElements = doc.select("div.product-photo div img");
			if (CollectionUtils.isNotEmpty(imageElements)) {
				for (Element element : imageElements) {
					String image_url = element.attr("src");
					if (!StringUtils.contains(image_url, "http")) {
						image_url = "https://www.perfumania.com" + image_url;
					}
					images.add(new Image(image_url));
				}
			}
			
			//brand
			String brand = StringUtils.EMPTY;
			Elements brandElements = doc.select("div.product-detail h4");
			if(CollectionUtils.isNotEmpty(brandElements)){
				brand = brandElements.text();
			}
			//title
			String title = StringUtils.EMPTY;
			Elements titleElements = doc.select("div.product-detail h1.product-detail");
			if(CollectionUtils.isNotEmpty(titleElements)){
				title = titleElements.text();
			}

			// sku jsonarray package
			JSONArray skuJSONArray = new JSONArray();
			sku_jsonarray_package(skuJSONArray, productElements, doc, spu_orign_price, spu_sale_price);

			// sku iterator
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
					// spu stock status
					if (stock_status > 0) {
						spu_stock_status = 1;
					}

					// price
					JSONObject priceJSONObject = new JSONObject();
					String sale_price_temp = skuJsonObejct.getString("sale_price");
					String orign_price_temp = skuJsonObejct.getString("orign_price");
					price_json_package(priceJSONObject, sale_price_temp, orign_price_temp);
					float orign_price = priceJSONObject.getFloatValue("orign_price");
					float sale_price = priceJSONObject.getFloatValue("sale_price");
					String unit = priceJSONObject.getString("unit");

					String sku_size = skuJsonObejct.getString("size");

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
					lselectlist.setStyle_id("default");
					lselectlist.setSelections(selections);

					// l_selection_list
					l_selection_list.add(lselectlist);

					// style json
					if (StringUtils.equalsIgnoreCase(sale_price_temp, spu_sale_price)
							&& StringUtils.equalsIgnoreCase(orign_price_temp, spu_orign_price)) {
						int save = priceJSONObject.getIntValue("save");
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
						styleJsonObject.put(skuId, skuJsonObejct);
					}
				}
				// stylelist 封装
				if (null != styleJsonObject && styleJsonObject.size() > 0) {
					for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
						String skuId = entry.getKey();
						// stylelist
						LStyleList lStyleList = new LStyleList();
						String switch_img = StringUtils.EMPTY;

						lStyleList.setDisplay(true);

						// stylelist
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(switch_img);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id("default");
						lStyleList.setStyle_cate_name("Color");
						lStyleList.setStyle_name("default");

						// images
						context.getUrl().getImages().put(skuId, images);
						// l_style_list
						l_style_list.add(lStyleList);
					}
				}
			}
			// 单品
			else {
				// images
//				for (Image image : images) {
//					System.out.println(image.getOriginalUrl());
//				}
				// price
				JSONObject priceJSONObject = new JSONObject();
				price_json_package(priceJSONObject, spu_sale_price, spu_orign_price);
				retBody.setPrice(
						new Price(priceJSONObject.getFloatValue("orign_price"), priceJSONObject.getIntValue("save"),
								priceJSONObject.getFloatValue("sale_price"), priceJSONObject.getString("unit")));
				//stock
				Elements stockElements = doc.select("div.buttons button.button.unavailable-message");
				if (CollectionUtils.isNotEmpty(stockElements)) {
					String stockFlag = stockElements.attr("style");
					if (!StringUtils.containsIgnoreCase(stockFlag, "inline")) {
						spu_stock_status = 1;
					}
				}
				context.getUrl().getImages().put(productId, images);
			}
			
			// sku
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			retBody.setSku(sku);
			// stock
			retBody.setStock(new Stock(spu_stock_status));
			// brand
			retBody.setBrand(new Brand(brand, "", "", ""));
			// title
			retBody.setTitle(new Title(title, "", "", ""));
			// full doc info
			String docid = SpiderStringUtil.md5Encode(DOMAIN+productId);
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(DOMAIN));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
			
			//category breadcrumb
			category_package(doc,brand ,retBody);
			
			// description
		    desc_package(doc,retBody);
		    
		    //properties
			properties_package(retBody);
			long end = System.currentTimeMillis();
			System.out.println(end-start);
			setOutput(context, retBody);
		}

	}

	/**
	 * price 封装
	 * 
	 * @param priceJSONObject
	 * @param sale_price_temp
	 * @param orign_price_temp
	 */
	private static void price_json_package(JSONObject priceJSONObject, String sale_price_temp,
			String orign_price_temp) {
		String unit = getCurrencyValue(sale_price_temp);// 得到货币代码
		String save = StringUtils.EMPTY;
		sale_price_temp = sale_price_temp.replaceAll("[$,]", "");
		orign_price_temp = orign_price_temp.replaceAll("[$,]", "");
		if (StringUtils.isBlank(orign_price_temp)) {
			orign_price_temp = sale_price_temp;
		}
		if (StringUtils.isBlank(sale_price_temp)) {
			sale_price_temp = orign_price_temp;
		}
		if (StringUtils.isBlank(orign_price_temp) || Float.valueOf(orign_price_temp) < Float.valueOf(sale_price_temp)) {
			orign_price_temp = sale_price_temp;
		}

		if (StringUtils.isBlank(save)) {
			save = Math.round((1 - Float.valueOf(sale_price_temp) / Float.valueOf(orign_price_temp)) * 100) + "";// discount
		}
		priceJSONObject.put("sale_price", sale_price_temp);
		priceJSONObject.put("orign_price", orign_price_temp);
		priceJSONObject.put("unit", unit);
		priceJSONObject.put("save", save);
	}

	/**
	 * skujsonarray 封装
	 * 
	 * @param skuJSONArray
	 * @param productElements
	 * @param doc
	 * @param spu_sale_price
	 * @param spu_orign_price
	 */
	private static void sku_jsonarray_package(JSONArray skuJSONArray, Elements productElements, Document doc,
			String spu_orign_price, String spu_sale_price) {
		// size elements
		Elements sizeElements = productElements.select("div.form-fields ul li select option");
		if (CollectionUtils.isNotEmpty(sizeElements)) {
			for (Element element : sizeElements) {
				JSONObject jsonObject = new JSONObject();
				String orign_price = element.attr("data-retail-price");
				String sale_price = element.attr("data-sale-price");
				String stockFlag = element.attr("class");
				int stock_status = 1;
				if (StringUtils.equalsIgnoreCase(OUTOFSTOCK, stockFlag)) {
					stock_status = 0;
				}
				String size = element.text();
				String skuId = element.text();
				jsonObject.put("orign_price", orign_price);
				jsonObject.put("sale_price", sale_price);
				jsonObject.put("stock_status", stock_status);
				jsonObject.put("size", size);
				jsonObject.put("skuId", skuId);
				skuJSONArray.add(jsonObject);
			}
		}
		// 单size或者无size
		else {
			sizeElements = productElements.select("div.form-fields ul li span.single-attr-value");
			Elements stockElements = doc.select("div.buttons button.button unavailable-message");
			int stock_status = 1;
			if (CollectionUtils.isNotEmpty(stockElements)) {
				String stockFlag = stockElements.attr("style");
				if (StringUtils.containsIgnoreCase(stockFlag, "inline")) {
					stock_status = 0;
				}
			}
			if (CollectionUtils.isNotEmpty(sizeElements)) {
				JSONObject jsonObject = new JSONObject();
				String size = sizeElements.text();
				String skuId = sizeElements.text();
				jsonObject.put("orign_price", spu_orign_price);
				jsonObject.put("sale_price", spu_sale_price);
				jsonObject.put("stock_status", stock_status);
				jsonObject.put("size", size);
				jsonObject.put("skuId", skuId);
				skuJSONArray.add(jsonObject);
			}
			// 单品 无sku else
		}
	}
	
	/**
	 * category breadcrumbs  封装
	 * @param doc 
	 * @param brand
	 * @param retBody 
	 */
	private static void category_package(Document doc , String brand, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements categoryElements = doc.select("div#breadcrumbs- ul li:not(:first-child)");
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (Element elements : categoryElements) {
				String cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(elements.text()));
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

	
	/***
	 * 描述　　封装
	 * @param doc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		//desc trans doc
		Elements es = doc.select("div#details-tab p");
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
}
