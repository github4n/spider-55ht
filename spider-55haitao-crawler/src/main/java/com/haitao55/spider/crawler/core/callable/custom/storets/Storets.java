package com.haitao55.spider.crawler.core.callable.custom.storets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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

/**
 * storets 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月10日 下午4:55:35
 * @version 1.0
 */
public class Storets extends AbstractSelect {
	private static final String DOMAIN = "www.storets.com";
	private static final String COLOR_KEY = "92";
	private static final String SIZE_KEY = "174";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);

		Document doc = this.getDocument(context);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// productId
		String productId = StringUtils.EMPTY;
		// spu stock status
		int spuStockStatus = 0;
		// unit
		String unit = StringUtils.substringBetween(content, "currency: \"", "\"");
		// boolean
		boolean isSetSpuPrice = false;
		// sku jsonarray
		JSONArray skuJSONArray = new JSONArray();
		// product data
		String productData = StringUtils.substringBetween(content, "new Product.Config(", ");");
		if (StringUtils.isNotBlank(productData)) {
			productId = StringUtils.substringBetween(productData, "productId\":\"", "\",");
			skuJSONArrayPackage(productData, skuJSONArray);
		}

		// images
		List<Image> images = new ArrayList<Image>();
		Elements imagesElements = doc.select("div.product-image.product-image-zoom.inner a");
		if (CollectionUtils.isNotEmpty(imagesElements)) {
			for (Element element : imagesElements) {
				String image_url = element.attr("href");
				images.add(new Image(StringUtils.trim(image_url)));
			}
		}

		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		// style
		Set<String> styleSet = new HashSet<String>();
		if (CollectionUtils.isNotEmpty(skuJSONArray)) {
			for (Object object : skuJSONArray) {
				JSONObject skuJSONObject = (JSONObject) object;
				// selectlist
				LSelectionList lselectlist = new LSelectionList();

				// skuId
				String skuId = skuJSONObject.getString("skuId");

				// color
				String color = skuJSONObject.getString("color");

				// size
				String size = skuJSONObject.getString("size");

				// stock
				int stock_status = skuJSONObject.getIntValue("stock_status");
				int stock_number = 0;
				if (stock_status > 0) {
					spuStockStatus = 1;
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

				// orign_price
				float orign_price = skuJSONObject.getFloatValue("orignPrice");
				float sale_price = skuJSONObject.getFloatValue("salePrice");
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

				// style
				if (!styleSet.contains(color)) {
					// stylelist
					LStyleList lStyleList = new LStyleList();
					// images
					if (!isSetSpuPrice) {
						lStyleList.setDisplay(true);
						int save = (int) ((1 - (sale_price / orign_price)) * 100);
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
						isSetSpuPrice = true;
					}

					context.getUrl().getImages().put(skuId, images);

					// switch_img
					String switch_img = StringUtils.EMPTY;

					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(style_id);

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
		retBody.setStock(new Stock(spuStockStatus));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(DOMAIN));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		// brand
		String brand = "Storets";
		retBody.setBrand(new Brand(brand, "", "", ""));

		// title
		String title = StringUtils.substringBetween(content, " content_name: \"", "\"");
		retBody.setTitle(new Title(title, "", "", ""));
		category_package(brand, doc, retBody);
		// properties
		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", "");
		retBody.setProperties(propMap);
		// description
		desc_package(doc, retBody);

		setOutput(context, retBody);
	}

	/**
	 * 封装 sku jsonarray
	 * 
	 * @param productData
	 * @param skuJSONArray
	 */
	private static void skuJSONArrayPackage(String productData, JSONArray skuJSONArray) {
		JSONObject productDataJSONObject = JSONObject.parseObject(productData);
		if (MapUtils.isNotEmpty(productDataJSONObject)) {
			// spu price
			float spuSalePrice = productDataJSONObject.getFloatValue("basePrice");
			float spuOrignPrice = productDataJSONObject.getFloatValue("oldPrice");

			JSONObject skuPropertyJSONObject = productDataJSONObject.getJSONObject("attributes");
			if (MapUtils.isNotEmpty(skuPropertyJSONObject)) {
				JSONObject colorJSONObject = skuPropertyJSONObject.getJSONObject(COLOR_KEY);
				JSONObject sizeJSONObject = skuPropertyJSONObject.getJSONObject(SIZE_KEY);

				// color
				JSONArray colorJSONArray = colorJSONObject.getJSONArray("options");
				// size
				JSONArray sizeJSONArray = sizeJSONObject.getJSONArray("options");
				// 遍历color
				if (CollectionUtils.isNotEmpty(colorJSONArray)) {
					for (Object object : colorJSONArray) {
						JSONObject colorJSONObjectTemp = (JSONObject) object;
						String colorId = colorJSONObjectTemp.getString("id");
						String color = colorJSONObjectTemp.getString("label");
						// saleprice orignprice 价格与spuprice对应价格相同时为0,所以给到spu价格
						float salePrice = 0 == colorJSONObjectTemp.getFloatValue("price") ? spuSalePrice
								: colorJSONObjectTemp.getFloatValue("price");
						float orignPrice = 0 == colorJSONObjectTemp.getFloatValue("oldPrice") ? spuOrignPrice
								: colorJSONObjectTemp.getFloatValue("oldPrice");
						@SuppressWarnings("unchecked")
						List<String> colorCodes = (List<String>) colorJSONObjectTemp.get("products");
						if (CollectionUtils.isNotEmpty(colorCodes)) {
							// color 存在products 用来对应存在的color size
							for (String code : colorCodes) {
								// 遍历 sizejsonarray 验证color 对应的size
								for (Object object2 : sizeJSONArray) {
									JSONObject sizeJSONObjectTemp = (JSONObject) object2;
									String sizeId = sizeJSONObjectTemp.getString("id");
									String size = sizeJSONObjectTemp.getString("label");
									float sizeSalePrice = 0 == colorJSONObjectTemp.getFloatValue("price") ? spuSalePrice
											: colorJSONObjectTemp.getFloatValue("price");
									float sizeOrignPrice = 0 == colorJSONObjectTemp.getFloatValue("oldPrice")
											? spuOrignPrice : colorJSONObjectTemp.getFloatValue("oldPrice");
									@SuppressWarnings("unchecked")
									List<String> sizeCodes = (List<String>) sizeJSONObjectTemp.get("products");
									if (CollectionUtils.isNotEmpty(sizeCodes) && sizeCodes.contains(code)) {
										JSONObject skuJSONObject = new JSONObject();
										skuJSONObject.put("skuId", colorId + sizeId);
										skuJSONObject.put("color", color);
										skuJSONObject.put("size", size);
										// 存在即有货，不然不会包含
										skuJSONObject.put("stock_status", 1);
										skuJSONObject.put("salePrice", sizeSalePrice);
										skuJSONObject.put("orignPrice", sizeOrignPrice);

										skuJSONArray.add(skuJSONObject);
										continue;
									}
								}
							}
						} else {
							JSONObject skuJSONObject = new JSONObject();
							skuJSONObject.put("skuId", colorId);
							skuJSONObject.put("color", color);
							// 存在即有货
							skuJSONObject.put("stock_status", 1);
							skuJSONObject.put("salePrice", salePrice);
							skuJSONObject.put("orignPrice", orignPrice);

							skuJSONArray.add(skuJSONObject);
						}

					}
				}
			}
		}
	}

	/**
	 * 分类封装
	 * 
	 * @param brand
	 * @param doc
	 * @param retBody
	 */
	private static void category_package(String brand, Document doc, RetBody retBody) {
		Elements categoryElements = doc.select("ul.breadcrumb li:not(:first-child)");
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (Element element : categoryElements) {
				String cate = element.text();
				cats.add(cate);
				breads.add(cate);
			}
		}
		breads.add(brand);
		retBody.setCategory(cats);
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
		Elements es = doc.select("div.std");
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
}
