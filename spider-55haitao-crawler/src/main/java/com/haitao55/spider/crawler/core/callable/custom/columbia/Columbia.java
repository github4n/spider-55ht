package com.haitao55.spider.crawler.core.callable.custom.columbia;

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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * columbia 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月20日 下午2:28:50
 * @version 1.0
 */
public class Columbia extends AbstractSelect{
	private static final String REQUEST_URL_AJAX = "http://www.columbia.com/on/demandware.store/Sites-Columbia_US-Site/en_US/Product-Variation?pid={}&dwvar_{}_variationColor=()&Quantity=1&format=ajax";
	private static final String DOMAIN = "www.columbia.com";
	private static final String BRAND = "columbia";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);
		Document doc = JsoupUtils.parse(content);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		String productData = StringUtils.substringBetween(content, "TagManagerData = ", "};");
		if (StringUtils.isNotBlank(productData)) {
			productData = productData+"}";
			JSONObject productJSONObject = JSONObject.parseObject(productData);
			// productId
			String productId = productJSONObject.getString("ProductID");
			// defaultColorCode
			String defaultColorCode = StringUtils.substringAfter(url, "variationColor=");
			if (StringUtils.contains(defaultColorCode, "&")) {
				defaultColorCode = StringUtils.substringBefore(defaultColorCode, "&");
			}
			// title
			String title = productJSONObject.getString("ProductName");
			// spuStockStatus
			int spuStockStatus = 0;
			boolean hasStock = productJSONObject.getBooleanValue("ProductInStock");
			if (hasStock) {
				spuStockStatus = 1;
			}
			// desc
			String desc = productJSONObject.getString("ProductShortDesc");

			// unit
			String unit = StringUtils.EMPTY;
			Elements uintElements = doc.select("meta[itemprop=priceCurrency]");
			if (CollectionUtils.isNotEmpty(uintElements)) {
				unit = uintElements.attr("content");
			}
			// gender
			String gender = StringUtils.EMPTY;

			// requestUrls 封装对应颜色请求url，异步调用获取sku信息（库存，价格）
			List<String> requestUrls = new ArrayList<String>();

			// color 对应图片封装
			JSONObject imageJSONObject = new JSONObject();
			
			//color switchImage
			JSONObject switchImageJSONObject =  new JSONObject();

			// colorCode 对应 colorName
			JSONObject colorCodeToNameJSONObject = new JSONObject();

			// skujsonarray 封装sku
			JSONArray skuJSONArray = new JSONArray();

			// requesturls imageJSONObject封装
			requestUrlsAndImageJSONObjectPackage(doc, productId, requestUrls, imageJSONObject,switchImageJSONObject,
					colorCodeToNameJSONObject);

			new ColumbiaHandler().process(requestUrls, skuJSONArray, context);

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
					String colorCode = skuJSONObject.getString("colorCode");
					String color = colorCodeToNameJSONObject.getString(colorCode);
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
						if (StringUtils.equals(defaultColorCode, colorCode)) {
							lStyleList.setDisplay(true);
							int save = (int) ((1 - (sale_price / orign_price)) * 100);
							retBody.setPrice(new Price(orign_price, save, sale_price, unit));

							// gender
							gender = getSex(skuJSONObject.getString("gender"));
						}

						@SuppressWarnings("unchecked")
						List<Image> skuImages = (List<Image>) imageJSONObject.get(colorCode);
						
						context.getUrl().getImages().put(skuId, skuImages);

						// switch_img
						String switch_img = switchImageJSONObject.getString(colorCode);

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
			retBody.setBrand(new Brand(BRAND, "", "", ""));
			// title
			retBody.setTitle(new Title(title, "", "", ""));
			category_package(BRAND, title, doc, retBody);
			// properties
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", gender);
			retBody.setProperties(propMap);
			// description
			desc_package(desc, doc, retBody);

			setOutput(context, retBody);
		}
	}

	/**
	 * 封装requestUrls和imageJSONObject（colorCode:list<Image>）
	 * 
	 * @param doc
	 * @param productId
	 * @param requestUrls
	 * @param imageJSONObject
	 * @param colorCodeToNameJSONObject
	 * @param colorCodeToNameJSONObject2 
	 */
	private static void requestUrlsAndImageJSONObjectPackage(Document doc, String productId, List<String> requestUrls,
			JSONObject imageJSONObject, JSONObject switchImageJSONObject, JSONObject colorCodeToNameJSONObject) {
		Elements colorElements = doc.select("ul.swatches.variationcolor li a");
		if (CollectionUtils.isNotEmpty(colorElements)) {
			for (Element element : colorElements) {
				String colorData = element.attr("data-lgimg");
				JSONObject colorJSONObject = JSONObject.parseObject(colorData);
				String colorCode = colorJSONObject.getString("colorcode");
				String colorName = colorJSONObject.getString("colorname");
				String colorImageData = element.attr("data-thumbnails");
				JSONArray colorImageJSONArray = JSONArray.parseArray(colorImageData);
				if (CollectionUtils.isNotEmpty(colorImageJSONArray)) {
					List<Image> images = new ArrayList<Image>();
					for (Object object : colorImageJSONArray) {
						String imageUrl = (String) object;
						images.add(new Image(imageUrl));
					}
					imageJSONObject.put(colorCode, images);
				}

				// 替换成颜色对应请求url
				String requestUrl = StringUtils.replacePattern(REQUEST_URL_AJAX, "\\(\\)", colorCode);
				requestUrl = StringUtils.replacePattern(requestUrl, "\\{\\}", productId);

				requestUrls.add(requestUrl);

				// color Code 对应 colorName
				colorCodeToNameJSONObject.put(colorCode, colorName);
				
				//switch imageurl
				String switchImageVlaue = element.attr("style");
				String switchImageUrl = StringUtils.substringBetween(switchImageVlaue, "(", ")");
				
				switchImageJSONObject.put(colorCode, switchImageUrl);
			}
		}
	}

	/**
	 * category
	 * 
	 * @param brand
	 * @param title
	 * @param doc
	 * @param retBody
	 */
	private static void category_package(String brand, String title, Document doc, RetBody retBody) {
		Elements categoryElements = doc.select("ol.breadcrumb li:not(:first-child) a");
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (int i = 0; i < categoryElements.size() - 1; i++) {
				Element element = categoryElements.get(i);
				String cate = element.text();
				cats.add(cate);
				breads.add(cate);
			}
		}
		cats.add(title);
		breads.add(title);
		breads.add(brand);
		retBody.setCategory(cats);
		retBody.setBreadCrumb(breads);
	}

	/***
	 * 描述 封装
	 * 
	 * @param desc
	 * 
	 * @param doc
	 * @param retBody
	 */
	private static void desc_package(String desc, Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = doc.select("div.pdpDetailsContent.leftPane ul li");
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
		descMap.put("en", desc);
		retBody.setDescription(descMap);
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
