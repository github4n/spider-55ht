package com.haitao55.spider.crawler.core.callable.custom.madewell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * madewell 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月22日 下午1:53:35
 * @version 1.0
 */
public class MadeWell extends AbstractSelect {
	private static final String PRODUCT_DEATIL_AJAX_URL = "https://www.madewell.com/browse2/ajax/product_details_ajax.jsp?sRequestedURL=[]&isFiftyOneContext=false&isProdSellable=true&bRestrictedProduct=false&isIgnoreOutOfStock=false&prodCode=()&color_name={}";
	private static final String DOMAIN = "www.madewell.com";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		long start = System.currentTimeMillis();
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);
		Document doc = this.getDocument(context);

		// productId
		String productId = StringUtils.substringBetween(content, "productID':'", "',");
		if (StringUtils.isNotBlank(productId)) {
			// spu stock_status
			int spu_stock_status = 0;
			// default_color
			String default_color = StringUtils.substringBetween(content, "colorName : '", "',");
			// unit
			String unit = StringUtils.substringBetween(content, "currency':'", "',");
			// sale_price
			String sale_price = StringUtils.substringBetween(content, "currentPrice':'", "',");
			// orign_price
			String orign_price = StringUtils.substringBetween(content, "fullPrice':'", "',");
			if (StringUtils.isBlank(orign_price)) {
				orign_price = sale_price;
			}
			// title
			String title = StringUtils.substringBetween(content, "productShortDesc : '", "',");
			// brand
			String brand = "Madewell";
			// category
			String category = StringUtils.substringBetween(content, "subCategory':'", "',");

			JSONArray skuJSONArray = new JSONArray();
			// 请求接口，封装sku jsonarray
			String sRequestedURL_param = StringUtils.substringBefore(url, "?");
			String prod_code_param = productId;
			String color_name_param = default_color;
			String request_url = getRequestUrl(sRequestedURL_param, prod_code_param, color_name_param);
			String product_detail = StringUtils.EMPTY;
			if (StringUtils.isNotBlank(request_url)) {
				product_detail = crawler_package(context, request_url);
				if (StringUtils.isNotBlank(product_detail)) {
					String product_data = StringUtils.substringBetween(product_detail, "var productDetailsJSON = '",
							"';");
					JSONObject productJSONObject = JSONObject.parseObject(product_data);
					sku_jsonarray_package(productJSONObject, skuJSONArray);
				}
			}

			// 默认颜色副图
			List<Image> otherImages = new ArrayList<Image>();
			Elements otherImagesElemts = doc.select("div.product-detail-main-img div.float-left img");
			if (CollectionUtils.isNotEmpty(otherImagesElemts)) {
				for (Element element : otherImagesElemts) {
					String image_url = element.attr("data-imgurl");
					otherImages.add(new Image(image_url));
				}
			}

			// image jsonObject
			JSONObject imageJSONObject = new JSONObject();
			// switch image jsonobejct
			JSONObject swicthImageJSONObject = new JSONObject();
			imageAndswicthImagePackage(JsoupUtils.parse(product_detail), imageJSONObject, swicthImageJSONObject);

			RetBody retBody = new RetBody();
			Sku sku = new Sku();
			// sku
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			// style
			Set<String> styleSet = new HashSet<String>();
			if (null != skuJSONArray && skuJSONArray.size() > 0) {
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
					lselectlist.setOrig_price(skuJSONObject.getFloatValue("orign_price"));
					lselectlist.setSale_price(skuJSONObject.getFloatValue("sale_price"));
					lselectlist.setPrice_unit(unit);
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id(style_id);
					lselectlist.setSelections(selections);

					// l_selection_list
					l_selection_list.add(lselectlist);

					// spu price
					String color_temp = StringUtils.replacePattern(color, "[ ]", "-");
					if (StringUtils.isBlank(default_color) && stock_status > 0) {
						default_color = color_temp;
					}
					if (StringUtils.containsIgnoreCase(color_temp, default_color)
							&& StringUtils.isNotBlank(default_color)) {
						int save = (int) ((1 - skuJSONObject.getFloatValue("sale_price")
								/ skuJSONObject.getFloatValue("orign_price")) * 100);
						retBody.setPrice(new Price(skuJSONObject.getFloatValue("orign_price"), save,
								skuJSONObject.getFloatValue("sale_price"), unit));
					}

					// style
					if (!styleSet.contains(color)) {
						// stylelist
						LStyleList lStyleList = new LStyleList();
						// color code
						String color_code = skuJSONObject.getString("color_code");
						// images
						String image_url = imageJSONObject.getString(color_code);
						if (StringUtils.isBlank(image_url)) {
							continue;
						}
						List<Image> images = new ArrayList<Image>();
						images.add(new Image(image_url));
						if (StringUtils.containsIgnoreCase(color_temp, default_color)
								&& StringUtils.isNotBlank(default_color)) {
							lStyleList.setDisplay(true);
							// images
							images.addAll(otherImages);
						}
						context.getUrl().getImages().put(skuId, images);

						// switch_img
						String switch_img = swicthImageJSONObject.getString(color_code);
						if (StringUtils.isBlank(switch_img)) {
							switch_img = StringUtils.EMPTY;
						}

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
			// cats
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			cats.add(category);
			breads.add(category);
			cats.add(title);
			breads.add(title);
			cats.add(brand);
			retBody.setCategory(cats);
			retBody.setBreadCrumb(breads);

			// properties
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "women");
			retBody.setProperties(propMap);
			// description
			desc_package(doc, retBody);

			long end = System.currentTimeMillis();
			System.out.println(end - start);
			setOutput(context, retBody);
		}

	}

	/**
	 * skujsonarray 封装
	 * 
	 * @param productJSONObject
	 * @param skuJSONArray
	 */
	private static void sku_jsonarray_package(JSONObject productJSONObject, JSONArray skuJSONArray) {
		JSONArray jsonArray = productJSONObject.getJSONArray("sizeset");
		if (null != jsonArray && jsonArray.size() > 0) {
			for (Object object : jsonArray) {
				JSONObject jsonObject = (JSONObject) object;
				String size = jsonObject.getString("size");
				JSONArray colorJsonArray = jsonObject.getJSONArray("colors");
				if (null != colorJsonArray && colorJsonArray.size() > 0) {
					for (Object object2 : colorJsonArray) {
						JSONObject jsonObject2 = (JSONObject) object2;
						String color_code = jsonObject2.getString("colorlabel");
						float sale_price = jsonObject2.getFloatValue("currentPrice");
						float orign_price = jsonObject2.getFloatValue("fullPrice");
						String color_name = jsonObject2.getString("colordisplayname");
						String skuId = jsonObject2.getString("skuCode");
						int stock_status = 1;
						boolean stock_flag = jsonObject2.getBooleanValue("outofstock");
						if (stock_flag) {
							stock_status = 0;
						}

						// 封装jsonobject
						JSONObject skuJSONObject = new JSONObject();
						skuJSONObject.put("skuId", skuId);
						skuJSONObject.put("color", color_name);
						skuJSONObject.put("color_code", color_code);
						skuJSONObject.put("size", size);
						skuJSONObject.put("sale_price", sale_price);
						skuJSONObject.put("orign_price", orign_price);
						skuJSONObject.put("stock_status", stock_status);

						skuJSONArray.add(skuJSONObject);
					}
				}
			}
		}
	}

	/**
	 * 封装 颜色对应图片 大图 小图
	 * 
	 * @param doc
	 * @param imageJSONObject
	 * @param swicthImageJSONObject
	 */
	private static void imageAndswicthImagePackage(Document doc, JSONObject imageJSONObject,
			JSONObject swicthImageJSONObject) {
		// colors
		Elements colorElements = doc.select("section.color-row div a,section.color-row.last-row div a");
		if (CollectionUtils.isNotEmpty(colorElements)) {
			for (Element element : colorElements) {
				String color_code = element.attr("id");
				Elements imgElements = element.select("img");
				if (CollectionUtils.isNotEmpty(imgElements)) {
					String image_url = imgElements.attr("data-imgurl");
					String swicth_image = imgElements.attr("src");
					swicthImageJSONObject.put(color_code, swicth_image);
					imageJSONObject.put(color_code, image_url);
				}
			}
		}
	}

	/**
	 * 获得请求url
	 * 
	 * @param sRequestedURL_param
	 * @param prod_code_param
	 * @param color_name_param
	 * @return
	 */
	private static String getRequestUrl(String sRequestedURL_param, String prod_code_param, String color_name_param) {
		String replacePattern = StringUtils.EMPTY;
		replacePattern = StringUtils.replacePattern(PRODUCT_DEATIL_AJAX_URL, "\\[\\]", sRequestedURL_param);
		replacePattern = StringUtils.replacePattern(replacePattern, "\\(\\)", prod_code_param);
		replacePattern = StringUtils.replacePattern(replacePattern, "\\{\\}", color_name_param);
		return replacePattern;
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
		Elements es = doc.select("div.product_desc,div.product_desc ul li");
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

	private String crawler_package(Context context, String url)
			throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(15000).url(url).method(HttpMethod.GET.getValue()).resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(15000).url(url).method(HttpMethod.GET.getValue()).proxy(true)
					.proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

}
