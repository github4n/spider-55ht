package com.haitao55.spider.crawler.core.callable.custom.shoebuy;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;

/**
 * shoebuy 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月5日 下午3:56:15
 * @version 1.0
 */
public class Shoebuy extends AbstractSelect {
	private static final String SKU_PROPERTY_NONE = "None";
	private static final String DOMAIN = "www.shoebuy.com";
	private static final String SEX_WOMEN = "female";
	private static final String SEX_MEN = "male";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);

		Document doc = this.getDocument(context);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// prodcutId
		String productId = StringUtils.substringBetween(content, "productId : '", "',");
		// spu stock_status
		int spu_stock_status = 0;
		// brand
		JSONObject brandJSONObject = new JSONObject();
		// unit
		JSONObject unitJSONObject = new JSONObject();
		// category
		List<String> categoryList = new ArrayList<String>();
		// images
		JSONObject imageJSONObject = new JSONObject();
		// default color
		JSONObject defaultColorJSONObject = new JSONObject();
		// gender
		String gender = StringUtils.substringBetween(content, "product_gender:\"", "\",");
		gender = getSex(gender);
		// 获取sku信息
		// 首先封装skujsonarray
		JSONArray skuJSONArray = new JSONArray();
		sku_jsonarray_package(doc, content, skuJSONArray, brandJSONObject, unitJSONObject, categoryList,
				imageJSONObject, defaultColorJSONObject);

		// image 验证商品图片是否存在
		new ShoebuyHandler().process(imageJSONObject, context);

		// sku iterator
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

				// stock
				int stock_status = skuJSONObject.getIntValue("stock_status");
				int stock_number = 0;
				if (stock_status > 0) {
					spu_stock_status = 1;
				}

				// style_id
				String style_id = color;

				// selections
				List<Selection> selections = new ArrayList<Selection>();
				String size = skuJSONObject.getString("size");
				String width = skuJSONObject.getString("width");
				if (StringUtils.isNotBlank(size) && !StringUtils.equalsIgnoreCase(SKU_PROPERTY_NONE, size)) {
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("Size");
					selection.setSelect_value(size);
					selections.add(selection);
				}
				if (StringUtils.isNotBlank(width) && !StringUtils.equalsIgnoreCase(SKU_PROPERTY_NONE, width)) {
					Selection selection = new Selection();
					selection.setSelect_id(1);
					selection.setSelect_name("Width");
					selection.setSelect_value(width);
					selections.add(selection);
				}

				// orign_price
				float orign_price = skuJSONObject.getFloatValue("orign_price");
				float sale_price = skuJSONObject.getFloatValue("sale_price");
				// lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
				lselectlist.setPrice_unit(unitJSONObject.getString("unit"));
				lselectlist.setStock_status(stock_status);
				lselectlist.setStock_number(stock_number);
				lselectlist.setStyle_id(style_id);
				lselectlist.setSelections(selections);

				// l_selection_list
				l_selection_list.add(lselectlist);
				if (StringUtils.containsIgnoreCase(defaultColorJSONObject.getString("color"), color)) {
					int save = (int) ((1 - (sale_price / orign_price)) * 100);
					retBody.setPrice(new Price(orign_price, save, sale_price, unitJSONObject.getString("unit")));
				}

				// sku product id
				String colorId = skuJSONObject.getString("colorId");
				// style
				if (!styleSet.contains(color)) {
					// stylelist
					LStyleList lStyleList = new LStyleList();
					// images
					@SuppressWarnings("unchecked")
					List<Image> images = (List<Image>) imageJSONObject.get(colorId);
					if (StringUtils.containsIgnoreCase(defaultColorJSONObject.getString("color"), color)) {
						lStyleList.setDisplay(true);
					}
					context.getUrl().getImages().put(skuId, images);

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
		retBody.setStock(new Stock(spu_stock_status));

		// title
		String title = StringUtils.EMPTY;
		Elements titleElements = doc.select("h1 span[itemprop=name]");
		if (CollectionUtils.isNotEmpty(titleElements)) {
			title = titleElements.text();
		}
		retBody.setTitle(new Title(title, "", "", ""));

		// brand
		retBody.setBrand(new Brand(brandJSONObject.getString("brand"), "", "", ""));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(productId.concat(DOMAIN));
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(DOMAIN));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

		// category breadcrumb
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		cats.addAll(categoryList);
		breads.addAll(categoryList);
		breads.add(brandJSONObject.getString("brand"));
		retBody.setCategory(cats);
		retBody.setBreadCrumb(breads);
		// description
		desc_package(doc, retBody);

		// properties
		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", gender);
		retBody.setProperties(propMap);
		setOutput(context, retBody);
	}

	private static void sku_jsonarray_package(Document doc, String content, JSONArray skuJSONArray,
			JSONObject brandJSONObject, JSONObject unitJSONObject, List<String> categoryList,
			JSONObject imageJSONObject, JSONObject defaultColorJSONObject) {
		String product_data = StringUtils.substringBetween(content, "productCollection.addProductObject(", ");");
		if (StringUtils.isNotBlank(product_data)) {
			product_data = StringUtils.trim(StringUtils.substringAfter(product_data, ","));
		}
		JSONObject productJSONObject = JSONObject.parseObject(product_data);

		// default color
		String default_color = productJSONObject.getString("lgDefaultColor");
		defaultColorJSONObject.put("color", default_color);

		// brand
		JSONObject brandJSONObjectTemp = productJSONObject.getJSONObject("brand");
		String brand = brandJSONObjectTemp.getString("name");
		brandJSONObject.put("brand", brand);

		// unit
		String unitTemp = productJSONObject.getString("currency");
		String unit = getCurrencyValue(unitTemp);
		unitJSONObject.put("unit", unit);

		// category
		String subCategory = productJSONObject.getString("subCategory");
		String category = productJSONObject.getString("category");
		categoryList.add(subCategory);
		categoryList.add(category);

		// base image
		String image_base = productJSONObject.getString("imgbase");

		// sku 相关属性
		JSONArray sizesJSONArray = productJSONObject.getJSONArray("sizes");
		JSONArray widthsJSONArray = productJSONObject.getJSONArray("widths");
		JSONArray colorsJSONArray = productJSONObject.getJSONArray("colors");
		JSONArray skusJSONArray = productJSONObject.getJSONArray("skus");

		/*
		 * skusJSONArray 外层先是size，然后每个size再有width,然后 width里再有color，
		 * 遍历skusJSONArray时严格按照sizesJSONArray，widthsJSONArray，
		 * colorsJSONArray顺序来进行具体对应
		 **/
		if (CollectionUtils.isNotEmpty(skusJSONArray)) {
			// 按照size顺序循环遍历
			for (int i = 0; i < skusJSONArray.size(); i++) {
				JSONArray skuJSONArrayTemp = skusJSONArray.getJSONArray(i);
				if (CollectionUtils.isNotEmpty(skuJSONArrayTemp)) {
					// width 顺序遍历
					for (int j = 0; j < skuJSONArrayTemp.size(); j++) {
						JSONArray skuJSONArrayTemp2 = skuJSONArrayTemp.getJSONArray(j);
						if (CollectionUtils.isNotEmpty(skuJSONArrayTemp2)) {
							// color顺序遍历
							for (int k = 0; k < skuJSONArrayTemp2.size(); k++) {
								JSONObject skuJSONObject = new JSONObject();

								// sku stock 为 0 既代表没有库存，此时需要手动生成skuId
								int sku_stock_number_flag = skuJSONArrayTemp2.getIntValue(k);
								// stock
								int stock_status = 0;
								if (sku_stock_number_flag > 0) {
									stock_status = 1;
								}

								// size
								String size = StringUtils.EMPTY;
								JSONArray sizeJSONArray = sizesJSONArray.getJSONArray(i);
								if (CollectionUtils.isNotEmpty(sizeJSONArray)) {
									size = sizeJSONArray.getString(0);
								}
								// width
								String width = StringUtils.EMPTY;
								JSONArray widthJSONArray = widthsJSONArray.getJSONArray(j);
								if (CollectionUtils.isNotEmpty(widthJSONArray)) {
									width = widthJSONArray.getString(0);
								}
								// color
								JSONObject colorJSONObject = colorsJSONArray.getJSONObject(k);
								String color = StringUtils.EMPTY;
								// colorId 作为imageJSONObject 的key，用于准确找到颜色对应图片
								String colorId = StringUtils.EMPTY;
								// price
								float orign_price = 0f;
								float sale_price = 0f;
								// images
								List<Image> images = new ArrayList<Image>();
								if (MapUtils.isNotEmpty(colorJSONObject)) {
									color = colorJSONObject.getString("name");
									colorId = colorJSONObject.getString("subcode");
									sale_price = colorJSONObject.getFloatValue("price");
									orign_price = colorJSONObject.getFloatValue("retailPrice");
									if (0f == orign_price || orign_price < sale_price) {
										orign_price = sale_price;
									}
									sale_price = sale_price * 0.01f;
									orign_price = orign_price * 0.01f;
									// images
									String image_url_temp = colorJSONObject.getString("jbURL");
									String image_preffix = StringUtils.substringBefore(image_url_temp, "/jb");
									image_url_temp = StringUtils.substringAfter(image_url_temp, "jb");
									image_url_temp = image_base + image_preffix + image_url_temp;
									if (!StringUtils.contains(image_url_temp, "http")) {
										image_url_temp = "http://www.shoebuy.com" + image_url_temp;
									}
									JSONArray imagesJSONArray = colorJSONObject.getJSONArray("multiImages");
									if (CollectionUtils.isNotEmpty(imagesJSONArray)) {
										for (Object object : imagesJSONArray) {
											int index = (int) object;
											String image_url_temp_index = StringUtils.replacePattern(image_url_temp,
													".jpg", index + ".jpg");

											images.add(new Image(image_url_temp_index));
										}
									} else {
										images.add(new Image(image_url_temp));
									}
								}

								// imageJSONObject 封装
								imageJSONObject.put(colorId, images);

								// 用作sku goods_id
								String skuId = color + size + width;

								// skujsonObject 封装 相关数据
								skuJSONObject.put("skuId", skuId);
								skuJSONObject.put("colorId", colorId);
								skuJSONObject.put("color", color);
								skuJSONObject.put("size", size);
								skuJSONObject.put("width", width);
								skuJSONObject.put("orign_price", orign_price);
								skuJSONObject.put("sale_price", sale_price);
								skuJSONObject.put("stock_status", stock_status);
								skuJSONObject.put("skuId", skuId);

								// skusJSONArray 加入skuJSONObject
								skuJSONArray.add(skuJSONObject);
							}
						}
					}
				}
			}
		}

	}

	/***
	 * 描述 封装
	 * 
	 * @param desc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = doc.select("span[itemprop=description],span[itemprop=description] ul li");
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
