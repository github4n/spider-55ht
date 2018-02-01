package com.haitao55.spider.crawler.core.callable.custom.yoox.com;

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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * yoox com 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月21日 上午10:45:42
 * @version 1.0
 */
public class YooxCom extends AbstractSelect{
	private static final String DOMAIN = "www.yoox.com";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);

		if (StringUtils.isNotBlank(content)) {
			Document doc = this.getDocument(context);

			RetBody retBody = new RetBody();
			Sku sku = new Sku();

			// productid
			String productId = StringUtils.substringBetween(content, "tc_vars[\"product_cod8\"] = \"", "\";");
			// gender
			String gender = StringUtils.substringBetween(content, "tc_vars[\"nav_dept\"] = \"", "\";");
			// default color PF
			String default_color_code = StringUtils.substringBetween(content, "tc_vars[\"product_cod10\"] = \"", "\";");
			// brand
			String brand = StringUtils.substringBetween(content, "tc_vars[\"product_brand\"] = \"", "\";");
			// title
			String title = StringUtils.substringBetween(content, "tc_vars[\"product_title\"] = \"", "\";");
			// spu stock status
			int spu_stock_status = 0;

			// price jsonobject
			JSONObject priceJSONObject = new JSONObject();
			price_package(content, priceJSONObject);

			// 截取content 获取商品sku信息
			// sku jsonarray
			JSONArray skuJSONArray = new JSONArray();
			Set<String> colorSet = new HashSet<String>();
			String product_data = StringUtils.substringBetween(content, "jsInit.item.colorSizeJson = ", ";");
			if (StringUtils.isNotBlank(product_data)) {
				JSONObject productJSONObject = JSONObject.parseObject(product_data);
				if (null != productJSONObject && !productJSONObject.isEmpty()) {
					// sku jsonarray 封装
					sku_jsonarray_package(productJSONObject, skuJSONArray, colorSet);
				}
			}

			// image jsonobject
			JSONObject imageJSONObject = new JSONObject();
			image_package(doc, colorSet, imageJSONObject, productId);

			// sku iterator
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			// style
			Set<String> styleSet = new HashSet<String>();
			if (null != skuJSONArray && skuJSONArray.size() > 0) {
				for (Object object : skuJSONArray) {
					JSONObject skuJsonObejct = (JSONObject) object;

					// selectlist
					LSelectionList lselectlist = new LSelectionList();

					// skuId
					String skuId = skuJsonObejct.getString("skuId");

					// color
					String color = skuJsonObejct.getString("color_name");

					// size
					String size = skuJsonObejct.getString("size_name");

					// stock
					int stock_status = skuJsonObejct.getIntValue("stock_status");
					int stock_number = 0;
					if (stock_status > 0) {
						spu_stock_status = 1;
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
					lselectlist.setOrig_price(priceJSONObject.getFloatValue("orign_price"));
					lselectlist.setSale_price(priceJSONObject.getFloatValue("sale_price"));
					lselectlist.setPrice_unit(priceJSONObject.getString("unit"));
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id(style_id);
					lselectlist.setSelections(selections);

					// l_selection_list
					l_selection_list.add(lselectlist);

					// style
					String color_value = skuJsonObejct.getString("color_value");
					if (!styleSet.contains(color_value)) {
						// stylelist
						LStyleList lStyleList = new LStyleList();
						if (StringUtils.equalsIgnoreCase(default_color_code, color_value)) {
							retBody.setPrice(new Price(priceJSONObject.getFloatValue("orign_price"),
									priceJSONObject.getIntValue("save"), priceJSONObject.getFloatValue("sale_price"),
									priceJSONObject.getString("unit")));
							lStyleList.setDisplay(true);
						}
						// switch_img
						String switch_img = StringUtils.EMPTY;

						// image
						@SuppressWarnings("unchecked")
						List<Image> images = (List<Image>) imageJSONObject.get(StringUtils.lowerCase(color_value));
						context.getUrl().getImages().put(skuId, images);
						// stylelist
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(switch_img);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id(style_id);
						lStyleList.setStyle_cate_name("Color");
						lStyleList.setStyle_name(style_id);
						l_style_list.add(lStyleList);

						styleSet.add(color_value);
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
			String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(DOMAIN));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

			// brand
			retBody.setBrand(new Brand(brand, "", "", ""));
			;

			// title
			retBody.setTitle(new Title(title, "", "", ""));

			// category breadcrumb
			category_package(doc, brand, retBody);
			// description
			desc_package(doc, retBody);

			// //properties
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", gender);
			retBody.setProperties(propMap);
			setOutput(context, retBody);
		}

	}

	/**
	 * 解析json 封装skujsonarray
	 * 
	 * @param productJSONObject
	 * @param skuJSONArray
	 * @param colorSet
	 */
	private static void sku_jsonarray_package(JSONObject productJSONObject, JSONArray skuJSONArray,
			Set<String> colorSet) {
		JSONArray colorJSONArray = productJSONObject.getJSONArray("Colors");
		JSONArray sizesJSONArray = productJSONObject.getJSONArray("Sizes");
		// color_size 对应库存数量
		JSONObject qtyJSONObject = productJSONObject.getJSONObject("Qty");

		// iterator colorJSONArray
		if (null != colorJSONArray && colorJSONArray.size() > 0) {
			for (Object object : colorJSONArray) {
				JSONObject colorJSONObject = (JSONObject) object;
				String color_code = colorJSONObject.getString("Color");
				String color_id = colorJSONObject.getString("ColorId");
				String color_name = colorJSONObject.getString("Name");
				if(StringUtils.equalsIgnoreCase("(-)", color_name)){
					color_name = "One Color";
				}
				String color_value = colorJSONObject.getString("Cod10");
				// colorset add
				colorSet.add(color_value);
				// 每个颜色对应size
				JSONArray sizeJSONArray = colorJSONObject.getJSONArray("Sizes");
				if (null != sizeJSONArray && sizeJSONArray.size() > 0) {
					for (Object object2 : sizeJSONArray) {
						JSONObject skuJSONObject = new JSONObject();

						String size_id = object2.toString();
						String size_name = StringUtils.EMPTY;
						// 由size_id 遍历得到size_name
						if (null != sizesJSONArray && sizesJSONArray.size() > 0) {
							for (Object object3 : sizesJSONArray) {
								JSONObject sizeJSONObject = (JSONObject) object3;
								String sizeID = sizeJSONObject.getString("Id");
								if (StringUtils.equalsIgnoreCase(size_id, sizeID)) {
									size_name = sizeJSONObject.getString("Name");
								}
							}
						}
						if(StringUtils.equalsIgnoreCase("--", size_name)){
							size_name = "One Size";
						}

						// 拼接参数 获取库存数量
						String qty_param = color_value + "_" + size_id;
						int stock_status = 0;
						int qty = qtyJSONObject.getIntValue(qty_param);
						if (qty > 0) {
							stock_status = 1;
						}

						// skuJSONObject put
						skuJSONObject.put("skuId", qty_param);
						skuJSONObject.put("color_code", color_code);
						skuJSONObject.put("color_id", color_id);
						skuJSONObject.put("color_name", color_name);
						skuJSONObject.put("color_value", color_value);
						skuJSONObject.put("size_id", size_id);
						skuJSONObject.put("size_name", size_name);
						skuJSONObject.put("stock_status", stock_status);

						skuJSONArray.add(skuJSONObject);
					}
				}

			}
		}
	}

	/**
	 * price jsonobject 封装
	 * 
	 * @param content
	 * @param priceJSONObject
	 */
	private static void price_package(String content, JSONObject priceJSONObject) {
		String priceData = StringUtils.substringBetween(content, "jsInit.item.price = ", ";");
		if (StringUtils.isNotBlank(priceData)) {
			@SuppressWarnings("static-access")
			JSONObject jsonObject = priceJSONObject.parseObject(priceData);
			String orign_price = jsonObject.getString("full");
			String sale_price = jsonObject.getString("discounted");
			String unit = StringUtils.substringBetween(content, "tc_vars[\"nav_currency\"] = \"", "\";");
			String save = StringUtils.EMPTY;
			sale_price = sale_price.replaceAll("[¥￥$,  ]", "");
			orign_price = orign_price.replaceAll("[¥￥$,  ]", "");

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
			priceJSONObject.put("sale_price", sale_price);
			priceJSONObject.put("orign_price", orign_price);
			priceJSONObject.put("save", save);
			priceJSONObject.put("unit", unit);
		}
	}

	/***
	 * image jsonobject package
	 * 
	 * @param doc
	 * @param colorSet
	 * @param imageJSONObject
	 * @param productId
	 */
	private static void image_package(Document doc, Set<String> colorSet, JSONObject imageJSONObject,
			String productId) {
		Elements imageElements = doc.select("ul#itemThumbs li img");
		if (CollectionUtils.isEmpty(imageElements)) {
			imageElements = doc.select("div#openZoom img");
		}
		if (null != colorSet && colorSet.size() > 0) {
			for (String color : colorSet) {
				List<Image> images = new ArrayList<Image>();
				if (CollectionUtils.isNotEmpty(imageElements)) {
					for (Element element : imageElements) {
						String image_url = element.attr("src");
						if (StringUtils.contains(image_url, "_9")) {
							image_url = StringUtils.replacePattern(image_url,
									StringUtils.substringBetween(image_url, productId, "_9") + "_9",
									StringUtils.substringAfter(color, productId) + "_14");
						} else if (StringUtils.contains(image_url, "_12")) {
							image_url = StringUtils.replacePattern(image_url,
									StringUtils.substringBetween(image_url, productId, "_12") + "_12",
									StringUtils.substringAfter(color, productId) + "_14");

						}
						images.add(new Image(image_url));
					}
				}
				imageJSONObject.put(StringUtils.lowerCase(color), images);
			}
		}
	}

	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = doc.select("ul.item-info-content.text-size-default.font-sans.text-secondary li span");
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

	private static void category_package(Document doc, String brand, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements categoryElements = doc.select("ul#breadcrumbs li:not(:first-child) a");
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (Element elements : categoryElements) {
				String cat = StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(elements.text()));
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
}
