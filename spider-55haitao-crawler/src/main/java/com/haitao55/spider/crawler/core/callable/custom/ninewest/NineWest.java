package com.haitao55.spider.crawler.core.callable.custom.ninewest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * ninewest 详情封装 
 * Title: 
 * Description: 
 * Company: 55海淘
 * @author zhaoxl
 * @date 2017年3月2日 下午4:20:01
 * @version 1.0
 */
public class NineWest extends AbstractSelect{
	private static final String DOMAIN = "www.ninewest.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();

		String content = this.getInputString(context);

		Pattern p = Pattern.compile("THIS PRODUCT IS SOLD OUT");
		Matcher matcher = p.matcher(content);
		if (matcher.find()) {
			throw new ParseException(CrawlerExceptionCode.OFFLINE, "ninewest.com itemUrl:" + url + " not found..");
		}

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		Document doc = this.getDocument(context);

		// spu stock status
		int spu_stock_status = 0;

		// default colorid
		String default_color_id = StringUtils.substringAfter(url, "variantColor");

		boolean spu_price_flag = false;

		// productId
		String productId = StringUtils.EMPTY;
		Elements productIdElements = doc.select("input#productID");
		if (CollectionUtils.isEmpty(productIdElements)) {
			productId = StringUtils.substringBetween(content, "product_id\":[\"", "\"]");
		} else {
			productId = productIdElements.attr("value");
		}

		// images jsonObject
		// 图片从json获取对应key 的图片地址
		JSONObject imageJSONObject = new JSONObject();
		image_order_package(doc, imageJSONObject);

		// swicth_img jsonobject
		JSONObject switchImgJSONObject = new JSONObject();
		// color
		JSONArray colorJSONArray = new JSONArray();

		color_switch_img_package(productId, doc, switchImgJSONObject, colorJSONArray);

		// sku jsonArray
		JSONArray skuJSONArray = new JSONArray();
		new NineWestHandler().process(imageJSONObject, colorJSONArray, skuJSONArray, context);

		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();

		// style
		JSONObject styleJsonObject = new JSONObject();

		// iteartor skuarray
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
				float sale_price = skuJsonObejct.getFloatValue("sale_price");
				float orign_price = skuJsonObejct.getFloatValue("orign_price");
				String unit = skuJsonObejct.getString("unit");

				String sku_color = skuJsonObejct.getString("color");
				String sku_color_id = skuJsonObejct.getString("colorId");
				String sku_size = skuJsonObejct.getString("size");
				String width = skuJsonObejct.getString("width");
//				String sku_size_id = skuJsonObejct.getString("size");
				// spu price
				if (!spu_price_flag) {
					if (StringUtils.containsIgnoreCase(default_color_id, sku_color_id)) {
						int save = skuJsonObejct.getIntValue("save");
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
						spu_price_flag = true;
					}
				}

				// selections
				List<Selection> selections = new ArrayList<Selection>();
				if (StringUtils.isNotBlank(sku_size)) {
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("Size");
					selection.setSelect_value(sku_size);
					selections.add(selection);
				}
				if (StringUtils.isNotBlank(width)) {
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("Width");
					selection.setSelect_value(width);
					selections.add(selection);
				}

				// lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
				lselectlist.setPrice_unit(unit);
				lselectlist.setStock_status(stock_status);
				lselectlist.setStock_number(stock_number);
				lselectlist.setStyle_id(sku_color);
				lselectlist.setSelections(selections);

				// l_selection_list
				l_selection_list.add(lselectlist);

				// style json
				styleJsonObject.put(sku_color, skuJsonObejct);

			}
		}
		// stylelist 封装
		if (null != styleJsonObject && styleJsonObject.size() > 0) {
			for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
				String color = entry.getKey();
				JSONObject jsonObject = (JSONObject) entry.getValue();
				// stylelist
				LStyleList lStyleList = new LStyleList();
				// skuId
				String skuId = jsonObject.getString("skuId");
				String sku_color_id = jsonObject.getString("colorId");
				String switch_img = jsonObject
						.getString(StringUtils.lowerCase(StringUtils.replacePattern(sku_color_id, "[ ]", "")));

				if (StringUtils.containsIgnoreCase(default_color_id, sku_color_id)) {
					lStyleList.setDisplay(true);
				}

				// stylelist
				lStyleList.setGood_id(skuId);
				lStyleList.setStyle_switch_img(switch_img);
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_id(color);
				lStyleList.setStyle_cate_name("Color");
				lStyleList.setStyle_name(color);

				// images
				@SuppressWarnings("unchecked")
				List<Image> pics = (List<Image>) jsonObject.get("images");
				context.getUrl().getImages().put(skuId, pics);
				// l_style_list
				l_style_list.add(lStyleList);
			}
		}

		// sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		retBody.setSku(sku);

		// stock
		retBody.setStock(new Stock(spu_stock_status));
		// brand
		String brand = "nine west";
		retBody.setBrand(new Brand(brand, "", "", ""));

		// title
		String title = StringUtils.EMPTY;
		Elements titleElements = doc.select("h1#nameHolder");
		if (CollectionUtils.isNotEmpty(titleElements)) {
			title = titleElements.text();
		}
		retBody.setTitle(new Title(title, "", "", ""));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(DOMAIN));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

		String gender = category_package(doc, brand, title, retBody);

		properties_package(gender, retBody);

		desc_package(doc, retBody);

		setOutput(context, retBody);
	}

	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = doc.select("div#descDetailItems span , div#descContentDesc");
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
		descMap.put("en", doc.select(".descContent").text());
		retBody.setDescription(descMap);
	}

	private static void properties_package(String gender, RetBody retBody) {
		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getTitle().getEn());
		}
		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getCategory().toString());
		}

		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", gender);
		retBody.setProperties(propMap);
	}

	private static String category_package(Document doc, String brand, String title, RetBody retBody) {
		String gender = StringUtils.EMPTY;
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements categoryElemnts = doc.select("div#productCrumbs a:not(:first-child)");
		if (CollectionUtils.isNotEmpty(categoryElemnts)) {
			for (Element element : categoryElemnts) {
				String src = element.attr("href");
				gender = getSex(src);
				String cat = StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(element.text()));
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

		return gender;
	}

	/**
	 * 封装color 和 颜色对应的swicth_img
	 * 
	 * @param productId
	 * @param doc
	 * @param switchImgJSONObject
	 * @param colorJSONArray
	 */
	private static void color_switch_img_package(String productId, Document doc, JSONObject switchImgJSONObject,
			JSONArray colorJSONArray) {
		Elements colorElements = doc.select("div#colorChips span img");
		if (CollectionUtils.isNotEmpty(colorElements)) {
			for (Element element : colorElements) {
				JSONObject jsonObject = new JSONObject();
				String colorId = element.attr("option-value");
				String color = element.attr("alt");
				String switch_img = element.attr("src");
				switchImgJSONObject.put(StringUtils.lowerCase(StringUtils.replacePattern(colorId, "[ ]", "")),
						switch_img);
				jsonObject.put("colorId", colorId);
				jsonObject.put("color", color);
				jsonObject.put("productId", productId);

				// add
				colorJSONArray.add(jsonObject);
			}
		}
	}

	/**
	 * 封装图片key， 方便确认需要取json中的那些图片数据
	 * 
	 * @param doc
	 * @param imageJSONObject
	 */
	private static void image_order_package(Document doc, JSONObject imageJSONObject) {
		Elements imageOrderElements = doc.select("div#viewsMenu .imageTypeSelector");
		if (CollectionUtils.isNotEmpty(imageOrderElements)) {
			List<String> imageOrderList = new ArrayList<String>();
			for (Element element : imageOrderElements) {
				String image_flag = element.attr("id");
				String image_order = getImageOrder(image_flag);
				imageOrderList.add(image_order);
			}
			imageJSONObject.put("image_order", imageOrderList);
		}
	}

	private static String getImageOrder(String image_flag) {
		String image_order = StringUtils.EMPTY;
		switch (image_flag) {
		case "view_Right":
			image_order = "rightZoom";
			break;
		case "view_Main":
			image_order = "zoom";
			break;
		case "view_Top":
			image_order = "topZoom";
			break;
		case "view_Bottom":
			image_order = "bottomZoom";
			break;
		case "view_Back":
			image_order = "backZoom";
			break;
		case "view_Front":
			image_order = "frontZoom";
			break;
		default:
			break;
		}
		return image_order;
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
