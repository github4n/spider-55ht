package com.haitao55.spider.crawler.core.callable.custom.urbanoutfitters;

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
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * urbanoutfitters 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月6日 下午5:29:03
 * @version 1.0
 */
public class Urbanoutfitters extends AbstractSelect {
	private static final String DOMAIN = "www.urbanoutfitters.com";
	private static final String IMAGE_URL_TEMP = "http://images.urbanoutfitters.com/is/image/UrbanOutfitters/{}?$xlarge$&hei=900&fit=constrain";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		String content = HttpUtils.crawler_package(context, getHeaders());

		Document doc = JsoupUtils.parse(content);

		// productData
		String productData = StringUtils.substringBetween(content, "window.productData = ", "}};");
		if (StringUtils.isNotBlank(productData)) {
			productData = productData + "}}";
		}

		if (StringUtils.isNotBlank(productData)) {

			JSONObject productDataJSONObject = JSONObject.parseObject(productData);

			JSONObject productJSONObject = productDataJSONObject.getJSONObject("product");
			// productId
			String productId = productJSONObject.getString("styleNumber");
			// spu stock status
			int spu_stock_status = 0;
			// default color code
			String defaultColorCode = StringUtils.substringAfter(url, "color=");
			if (StringUtils.contains(defaultColorCode, "&")) {
				defaultColorCode = StringUtils.substringBefore(defaultColorCode, "&");
			}
			// title
			String title = StringUtils.substringBetween(productJSONObject.toString(), "displayName\":\"", "\",");
			if(StringUtils.isBlank(title)){
				Elements titleElements = doc.select("span[itemprop=name]");
				if(CollectionUtils.isNotEmpty(titleElements)){
					title = titleElements.text();
				}
			}
			// brand
			String brand = StringUtils.substringBetween(content, "productMeta\": {\"brandSlug\": \"", "\",");
			// unit
			String unit = productJSONObject.getString("currencyCode");

			// imageJSONObject
			JSONObject imageJSONObject = new JSONObject();
			// switchImageJSONObject
			JSONObject switchImageJSONObject = new JSONObject();

			// sku jsonarray
			JSONArray skuJSONArray = getSkuJSONArray(productJSONObject, imageJSONObject, switchImageJSONObject,
					productId);
			// sku iterator
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
					// colorId
					String colorId = skuJSONObject.getString("colorCode");

					// stock
					int stock_status = skuJSONObject.getIntValue("stockStatus");
					int stock_number = 0;
					if (stock_status > 0) {
						spu_stock_status = 1;
					}
					// style_id
					String style_id = color;

					// selections
					List<Selection> selections = new ArrayList<Selection>();
					String size = skuJSONObject.getString("size");
					if (StringUtils.isNotBlank(size)) {
						Selection selection = new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name("Size");
						selection.setSelect_value(size);
						selections.add(selection);
					}

					// price
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
					if (StringUtils.containsIgnoreCase(defaultColorCode, colorId)) {
						int save = (int) ((1 - (sale_price / orign_price)) * 100);
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
					}

					// style
					if (!styleSet.contains(color)) {
						// stylelist
						LStyleList lStyleList = new LStyleList();
						// images
						@SuppressWarnings("unchecked")
						List<Image> images = (List<Image>) imageJSONObject.get(colorId);
						if (StringUtils.containsIgnoreCase(defaultColorCode, colorId)) {
							lStyleList.setDisplay(true);
						}
						context.getUrl().getImages().put(skuId, images);

						String switch_img = switchImageJSONObject.getString(colorId);

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
			retBody.setTitle(new Title(title, "", "", ""));

			// brand
			if (StringUtils.isBlank(brand)) {
				brand = "urbanoutfitters";
			}
			retBody.setBrand(new Brand(brand, "", "", ""));

			// full doc info
			String docid = SpiderStringUtil.md5Encode(productId.concat(DOMAIN));
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(DOMAIN));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

			// category
			category_package(doc, brand, retBody);
			// properties
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			retBody.setProperties(propMap);

			// description
			desc_package(doc, retBody);

			setOutput(context, retBody);
		}

	}

	/**
	 * 封装skujsonarray
	 * 
	 * @param productJSONObject
	 * @param imageJSONObject
	 * @param switchImageJSONObject
	 * @param productId
	 * @return
	 */
	private static JSONArray getSkuJSONArray(JSONObject productJSONObject, JSONObject imageJSONObject,
			JSONObject switchImageJSONObject, String productId) {
		JSONArray skuJSONArray = new JSONArray();
		JSONObject skuInfoJSONObject = productJSONObject.getJSONObject("skuInfo");
		if (MapUtils.isNotEmpty(skuInfoJSONObject)) {
			JSONObject primarySliceJSONObject = skuInfoJSONObject.getJSONObject("primarySlice");
			if (MapUtils.isNotEmpty(primarySliceJSONObject)) {
				JSONArray sliceItemsJSONArray = primarySliceJSONObject.getJSONArray("sliceItems");
				if (CollectionUtils.isNotEmpty(sliceItemsJSONArray)) {
					for (Object object : sliceItemsJSONArray) {
						JSONObject jsonObject = (JSONObject) object;
						String color = jsonObject.getString("displayName");
						String colorCode = jsonObject.getString("code");
						// switchimage
						String switchImageUrl = jsonObject.getString("swatchUrl");
						switchImageJSONObject.put(colorCode, switchImageUrl);
						// images
						imageJSONObjectPackage(jsonObject, imageJSONObject, productId, colorCode);

						// 封装 skujsonarray
						JSONArray includedSkusJSONArray = jsonObject.getJSONArray("includedSkus");
						if (CollectionUtils.isNotEmpty(includedSkusJSONArray)) {
							for (Object object2 : includedSkusJSONArray) {
								JSONObject jsonObject2 = (JSONObject) object2;
								String skuId = jsonObject2.getString("skuId");
								String sizeId = jsonObject2.getString("sizeId");
								String size = jsonObject2.getString("size");
								String orignPrice = jsonObject2.getString("listPrice");
								String salePrice = jsonObject2.getString("salePrice");
								int stockStatus = 0;
								int stockLevel = jsonObject2.getIntValue("stockLevel");
								if (stockLevel > 0) {
									stockStatus = 1;
								}

								JSONObject skuJSONObject = new JSONObject();
								skuJSONObject.put("skuId", skuId);
								skuJSONObject.put("color", color);
								skuJSONObject.put("colorCode", colorCode);
								skuJSONObject.put("sizeId", sizeId);
								skuJSONObject.put("size", size);
								skuJSONObject.put("orignPrice", orignPrice);
								skuJSONObject.put("salePrice", salePrice);
								skuJSONObject.put("stockStatus", stockStatus);

								skuJSONArray.add(skuJSONObject);
							}
						}

					}
				}
			}
		}
		return skuJSONArray;
	}

	/**
	 * 封装 imageJSONObject
	 * 
	 * @param jsonObject
	 * @param imageJSONObject
	 * @param colorCode
	 * @param productId
	 */
	private static void imageJSONObjectPackage(JSONObject jsonObject, JSONObject imageJSONObject, String productId,
			String colorCode) {
		JSONArray jsonArray = jsonObject.getJSONArray("images");
		if (CollectionUtils.isNotEmpty(jsonArray)) {
			List<Image> images = new ArrayList<Image>();
			for (Object object : jsonArray) {
				String imageFlag = (String) object;
				String imageParam = getImageParam(productId, colorCode, imageFlag);
				String imageUrl = StringUtils.replacePattern(IMAGE_URL_TEMP, "\\{\\}", imageParam);
				images.add(new Image(imageUrl));
			}
			imageJSONObject.put(colorCode, images);
		}
	}

	private static String getImageParam(String productId, String colorCode, String imageFlag) {
		return productId + "_" + colorCode + "_" + imageFlag;
	}

	/**
	 * category breadcrumbs 封装
	 * 
	 * @param doc
	 * @param brand
	 * @param title
	 * @param retBody
	 */
	private static void category_package(Document doc, String brand, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements elements = doc.select("ol.c-breadcrumb__ol.u-clearfix li");
		if (CollectionUtils.isNotEmpty(elements)) {
			for (Element element : elements) {
				String string = element.text();
				cats.add(string);
				breads.add(string);
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
	 * @param desc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// Elements es = doc.select("div.product-description.child>p
		// ,div.product-description.child>div>p,div.product-description.child
		// p>br");
		Elements es = doc.select("div#product_description__panel div.u-global-p p");
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
	
	private static Map<String, Object> getHeaders() {
		final Map<String, Object> headers = new HashMap<String, Object>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2581158727487646435L;

			{
				put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				put("Accept-Encoding", "gzip, deflate, sdch");
				put("Accept-Language", "zh-CN,zh;q=0.8");
				put("Cache-Control", "max-age=0");
				put("Connection", "keep-alive");
				// put("Cookie",
				// "__gads=ID=6317950096b83888:T=1487555926:S=ALNI_MbSXcNaILJorNUxthhWYhDRx6JjtQ;
				// PHPSESSID=66f143f821916269de8a42b564b3b7aa;
				// _ga=GA1.2.421631721.1487555928; _gat=1; rip=D;
				// Hm_lvt_aa1bd5db226a1bae87a0ffc02cee3d7b=1487555927;
				// Hm_lpvt_aa1bd5db226a1bae87a0ffc02cee3d7b=1487675306;
				// OX_plg=pm");
				put("User-Agent",
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/55.0.2883.87 Chrome/55.0.2883.87 Safari/537.36");
			}
		};
		return headers;
	}
}
