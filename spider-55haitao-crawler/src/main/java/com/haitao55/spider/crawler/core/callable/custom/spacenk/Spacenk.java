package com.haitao55.spider.crawler.core.callable.custom.spacenk;

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
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * spacenk 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月30日 下午3:44:44
 * @version 1.0
 */
public class Spacenk extends AbstractSelect{
	private static final String SERVICE_URL_COLOR = "http://www.spacenk.com/on/demandware.store/Sites-spacenkgb-Site/en_GB/Product-Variation?pid={}&dwvar_{}_color=()&Quantity=1&format=ajax&productlistid=undefined";
	private static final String SERVICE_URL_SIZE = "http://www.spacenk.com/on/demandware.store/Sites-spacenkgb-Site/en_GB/Product-Variation?pid={}&dwvar_{}_size=()&Quantity=1&format=ajax&productlistid=undefined";
	private static final String DOMAIN = "www.spacenk.com";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		// String url =
		// "http://www.spacenk.com/uk/en_GB/brands/h/hourglass/girl-lip-stylo-MUK300026678.html?dwvar_MUK300026678_color=UK300026677";
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);

		Document doc = this.getDocument(context);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// spu stock status
		int spu_stock_status = 0;
		// productId
		String productId = StringUtils.EMPTY;
		// skuId
		String default_skuId = StringUtils.EMPTY;
		// title
		String title = StringUtils.EMPTY;
		// bradn
		String brand = StringUtils.EMPTY;
		// uint
		String unit = StringUtils.EMPTY;

		String product_data = StringUtils
				.trim(StringUtils.substringBetween(content, "window.digitalData.product =", "];"));
		if (StringUtils.isNotBlank(product_data)) {
			product_data = StringUtils.substringAfter(product_data, "[");
		}
		if (StringUtils.isBlank(product_data)) {
			product_data = StringUtils.trim(StringUtils.substringBetween(content, "window.digitalData.product =", ";"));
		}
		JSONObject productJSONObject = JSONObject.parseObject(product_data);
		if (MapUtils.isNotEmpty(productJSONObject)) {
			productId = productJSONObject.getString("productID");
			default_skuId = productJSONObject.getString("sku");
			title = productJSONObject.getString("productName");
			brand = productJSONObject.getString("manufacturer");
			unit = productJSONObject.getString("currency");
		}

		// 图片jsonobject
		JSONObject imageJSONObject = new JSONObject();
		// sku jsonarray
		JSONArray skuJSONArray = new JSONArray();
		// 获取color元素 拼接多color 对应请求参数列表
		List<String> requestUrlList = new ArrayList<String>();
		requestUrlAndImageJSONObjectPackage(content, requestUrlList, productId);
		new SpacenkHandler().process(requestUrlList, skuJSONArray,imageJSONObject, context);
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
					spu_stock_status = 1;
				}

				// style_id
				String style_id = color;
				if (StringUtils.isBlank(style_id)) {
					style_id = "default";
					color = "default";//没有颜色时
				}

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
				float orign_price = skuJSONObject.getFloatValue("orign_price");
				float sale_price = skuJSONObject.getFloatValue("sale_price");
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

				if (StringUtils.containsIgnoreCase(default_skuId, skuId)) {
					int save = (int) ((1 - (sale_price / orign_price)) * 100);
					retBody.setPrice(new Price(orign_price, save, sale_price, unit));
				}
				// style
				if (!styleSet.contains(color)) {
					// stylelist
					LStyleList lStyleList = new LStyleList();
					// images
					@SuppressWarnings("unchecked")
					List<Image> images = (List<Image>) imageJSONObject.get(color);
					if (StringUtils.containsIgnoreCase(default_skuId, skuId)) {
						lStyleList.setDisplay(true);
					} else if (StringUtils.containsIgnoreCase(color, "default")) {
						lStyleList.setDisplay(true);
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
		retBody.setStock(new Stock(spu_stock_status));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(DOMAIN));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		// brand
		retBody.setBrand(new Brand(brand, "", "", ""));

		// title
		retBody.setTitle(new Title(title, "", "", ""));
		category_package(brand, content, retBody);
		// properties
		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", "");
		retBody.setProperties(propMap);
		// description
		desc_package(doc, retBody);

		setOutput(context, retBody);
	}

	/**
	 * 请求参数和image数据封装
	 * 
	 * @param content
	 * @param request_url
	 * @param productId
	 */
	private static void requestUrlAndImageJSONObjectPackage(String content, List<String> requestUrlList,
			String productId) {
		Document doc = JsoupUtils.parse(content);
		Elements colorElements = doc.select("div.value.size-selector ul#va-color li:not(:first-child)");
		if (CollectionUtils.isNotEmpty(colorElements)) {
			for (Element element : colorElements) {
				requestUrlListPackage(element, requestUrlList, productId);
			}
		} else {
			Elements sizeElements = doc.select("div.value.size-selector ul#va-size li:not(:first-child)");
			if (CollectionUtils.isNotEmpty(sizeElements)) {
				for (Element element : sizeElements) {
					requestUrlListPackage(element, requestUrlList, productId);
				}
			}
		}
	}

	private static void requestUrlListPackage(Element element, List<String> requestUrlList, String productId) {
		String color_url = element.attr("value");
//		String image_url = element.attr("data-lgimg");
//		if (StringUtils.contains(image_url, "{")) {
//			JSONObject imageUrlJSONObject = JSONObject.parseObject(image_url);
//			if (MapUtils.isNotEmpty(imageUrlJSONObject)) {
//				image_url = imageUrlJSONObject.getString("url");
//			}
//		}
//		Elements colorNameElements = element.select("span.variation-select-label");
//		String color = CollectionUtils.isEmpty(colorNameElements) ? StringUtils.EMPTY : colorNameElements.text();
		String request_url_temp = StringUtils.EMPTY;

		// 这里到color_id 可能指颜色 也可能是size
		String color_id = StringUtils.substringAfter(color_url, "_color=");

		// 网站特殊性 可能是color 或者size 对应的SERVICE_URL 不同
		if (StringUtils.isBlank(color_id)) {
			color_id = StringUtils.substringAfter(color_url, "_size=");
			request_url_temp = StringUtils.replacePattern(SERVICE_URL_SIZE, "\\{\\}", productId);
		} else {
			request_url_temp = StringUtils.replacePattern(SERVICE_URL_COLOR, "\\{\\}", productId);
		}

		if (StringUtils.contains(color_id, "&")) {
			color_id = StringUtils.substringBefore(color_id, "&");
		}
		//
		request_url_temp = StringUtils.replacePattern(request_url_temp, "\\(\\)", color_id);
		// 颜色对应请求参数封装
		requestUrlList.add(request_url_temp);
		if (StringUtils.isBlank(color_id)) {
			System.out.println("is null");
		}
	}

	/**
	 * 分类封装
	 * 
	 * @param brand
	 * @param content
	 * @param retBody
	 */
	private static void category_package(String brand, String content, RetBody retBody) {
		String category_data = StringUtils.substringBetween(content, "window.digitalData.page.breadcrumb = ", ";");
		JSONArray categoryJSONArray = JSONArray.parseArray(category_data);
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(categoryJSONArray)) {
			for (Object object : categoryJSONArray) {
				String cate = (String) object;
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
		Elements es = doc.select("div.accordion-wrapper div >p");
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
