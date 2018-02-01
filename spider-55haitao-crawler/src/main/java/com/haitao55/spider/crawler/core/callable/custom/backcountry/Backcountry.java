package com.haitao55.spider.crawler.core.callable.custom.backcountry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * 
 * Title:backcountry 详情页封装 Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年2月10日 上午11:43:14
 * @version 1.0
 */
public class Backcountry extends AbstractSelect {
	private static final String domain = "www.backcountry.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);
		Document doc = this.getDocument(context);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// product data
		String productData = StringUtils.substringBetween(content, ">BC.product =", ";$.publish");
		if (StringUtils.isBlank(productData)) {
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"backcountry.com itemUrl:" + context.getUrl().toString() + " is offline..");
		}

		JSONObject productJSONObject = JSONObject.parseObject(productData);

		// default skuid
		String default_sku_id = StringUtils.substringAfter(url, "skid=");

		// spu stock status
		int spu_stock_status = 0;

		// product id
		String productId = productJSONObject.getString("id");

		// brand
		String brand = productJSONObject.getJSONObject("brand").getString("displayName");

		// title
		String title = productJSONObject.getString("title");

		// category
		String category = productJSONObject.getString("categoriesHierarchy");

		// unit
		String unit = productJSONObject.getString("lowSalePrice");
		if (StringUtils.isBlank(unit)) {
			unit = productJSONObject.getString("lowListPrice");
		}
		unit = getCurrencyValue(unit);

		// images 基础图片信息
		List<Image> images = new ArrayList<Image>();
		Elements imagesElements = doc.select("ul.product-detail-imgs__ul li img");
		if (CollectionUtils.isNotEmpty(imagesElements)) {
			for (int i = 1; i < imagesElements.size(); i++) {
				Element element = imagesElements.get(i);
				String image_url = element.attr("data-large-img");
				if (!StringUtils.contains(image_url, "http")) {
					image_url = "http:" + image_url;
				}
				images.add(new Image(image_url));
			}
		}

		// color image
		JSONObject colorImageJSONObject = new JSONObject();
		Elements colorImageElements = doc.select("ul.ui-flexslider__list.js-flexslider-list li img");
		if (CollectionUtils.isNotEmpty(colorImageElements)) {
			for (int i = 0; i < colorImageElements.size(); i++) {
				Element element = colorImageElements.get(i);
				String image_url = element.attr("data-large-img");
				if (!StringUtils.contains(image_url, "http")) {
					image_url = "http:" + image_url;
				}

				String alt = element.attr("alt");
				colorImageJSONObject.put(alt, image_url);
			}
		}

		// sku json
		JSONObject skuJSONObject = productJSONObject.getJSONObject("skusCollection");

		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();

		// style
		Set<String> styleSet = new HashSet<String>();
		if (!skuJSONObject.isEmpty()) {
			for (Map.Entry<String, Object> entry : skuJSONObject.entrySet()) {
				String skuId = entry.getKey();
				JSONObject jsonObject = (JSONObject) entry.getValue();

				// selectlist
				LSelectionList lselectlist = new LSelectionList();

				// color ,size
				String displayName = jsonObject.getString("displayName");
				String color = StringUtils.substringBefore(displayName, ", ");
				String size = StringUtils.substringAfter(displayName, ", ");

				// stock
				int inventory = jsonObject.getIntValue("inventory");
				int stock_status = 0;
				int stock_number = 0;
				if (inventory > 0) {
					stock_status = 1;
					spu_stock_status = 1;
				}

				JSONObject priceJsonObject = jsonObject.getJSONObject("price");
				// sale price
				float sale_price = priceJsonObject.getFloatValue("low");
				// orign price
				float orign_price = priceJsonObject.getFloatValue("high");
				// save
				int save = priceJsonObject.getIntValue("discount");

				if (StringUtils.containsIgnoreCase(default_sku_id, StringUtils.replacePattern(color, " ", ""))) {
					retBody.setPrice(new Price(orign_price, save, sale_price, unit));
				}

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

				jsonObject.put("skuId", skuId);
				// style
				if (!styleSet.contains(color)) {
					// stylelist
					LStyleList lStyleList = new LStyleList();
					if (StringUtils.containsIgnoreCase(default_sku_id, skuId)) {
						lStyleList.setDisplay(true);
					}

					// switch_img
					String switch_img = jsonObject.getString("color");
					if (!StringUtils.contains(switch_img, "http")) {
						switch_img = "http:" + switch_img;
					}

					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(style_id);

					// images
					List<Image> sku_pics = new ArrayList<Image>();
					for (Map.Entry<String, Object> imageEntry : colorImageJSONObject.entrySet()) {
						String color_flag = imageEntry.getKey();
						String image_url = (String) imageEntry.getValue();
						if (StringUtils.containsIgnoreCase(color_flag, style_id)) {
							sku_pics.add(new Image(image_url));
						}
					}
					sku_pics.addAll(images);

					context.getUrl().getImages().put(skuId, sku_pics);
					// l_style_list
					l_style_list.add(lStyleList);

					styleSet.add(color);
				}
			}
		}

		// sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		retBody.setSku(sku);

		// stock
		retBody.setStock(new Stock(spu_stock_status));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(productId + domain);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

		// brand
		retBody.setBrand(new Brand(brand, "", "", ""));

		// title
		retBody.setTitle(new Title(title, "", "", ""));

		// category breadcrumb
		category_package(doc, brand, category, retBody);

		// description
		desc_package(doc, retBody);

		// properties
		properties_package(doc, retBody);

		setOutput(context, retBody);

	}

	/**
	 * category breadcrumbs 封装
	 * 
	 * @param doc
	 * @param brand
	 * @param category
	 * @param retBody
	 */
	private static void category_package(Document doc, String brand, String category, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		String[] categoryArray = category.split(",");
		if (null != categoryArray && categoryArray.length != 0) {
			for (String string : categoryArray) {
				String cat = StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(string));
				if (StringUtils.isNotBlank(cat)) {
					cats.add(cat);
					breads.add(cat);
				}
			}
		}
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
		Elements es = doc.select("div.ui-product-details__description , ul.product-details-accordion__list li");
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
		String gender = getSex(retBody.getTitle().getEn());
		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getCategory().toString());
		}

		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", gender);
		List<List<Object>> propattr = new ArrayList<List<Object>>();
		List<Object> proList = new ArrayList<Object>();
		List<List<String>> list = new ArrayList<List<String>>();
		String keyValue = StringUtils.EMPTY;
		Elements key = doc.select("h3.product-details-accordion__header");
		if (CollectionUtils.isNotEmpty(key)) {
			Element element = key.get(1);
			if (null != element) {
				keyValue = element.text();
			} else {
				keyValue = key.get(0).text();
			}
		}
		Elements valueList = doc.select("div.table.product-details-accordion__techspecs-container>div");
		if (CollectionUtils.isNotEmpty(valueList)) {
			for (Element element : valueList) {
				List<String> tempList = new ArrayList<String>();
				String prokey = StringUtils.trim(
						element.select("div.td.product-details-accordion__techspec-name.js-techspec-name").text());
				String value = StringUtils.trim(
						element.select("div.td.product-details-accordion__techspec-value.js-techspec-value").text());
				if (StringUtils.isNotBlank(prokey) && StringUtils.isNotBlank(value)) {
					tempList.add(prokey);
					tempList.add(value);
				}
				list.add(tempList);
			}
		}
		proList.add(keyValue);
		proList.add(list);
		propattr.add(proList);
		propMap.put("attr", propattr);

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
