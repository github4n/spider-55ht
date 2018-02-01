package com.haitao55.spider.crawler.core.callable.custom.abercrombie;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * abercrombie 详情数据封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月1日 下午2:11:48
 * @version 1.0
 */
public class Abercrombie extends AbstractSelect{
	private static final String IMAGE_REQUEST_URL = "https://anf.scene7.com/is/image/anf?imageset=%7Banf/anf_()%7D&req=set,json&defaultimage=anf/anf_()_model1&id={}";
	private static final String INSTOCK = "Available";
	private static final String DOMAIN = "www.abercrombie.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_WOMEN2 = "female";
	private static final String SEX_MEN = "men";
	private static final String SEX_MEN2 = "male";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);

		Document doc = JsoupUtils.parse(content);

		Pattern p = Pattern.compile("This item is no longer available");
		Matcher m = p.matcher(content);
		if(m.find()){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"www.abercrombie.com:"+url+" not found..");
		}
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// productId
		String productId = StringUtils.substringBetween(content, "productPrices[", "]");

		// spu stock status
		int spu_stock_status = 0;

		// spu_price_flag
		boolean spu_price_flag = false;

		// title
		String title = StringUtils.EMPTY;
		Elements titleElements = doc.select("h1.product-page-title");
		if (CollectionUtils.isNotEmpty(titleElements)) {
			title = titleElements.text();
		}
		// brand
		String brand = "A&F";

		// default color
		String default_color = StringUtils.EMPTY;
		Elements colorElements = doc.select("meta[itemprop=color]");
		if (CollectionUtils.isEmpty(colorElements)) {
			colorElements = doc.select("h2.product-attrs__shown-in span");
		}
		if (CollectionUtils.isNotEmpty(colorElements)) {
			default_color = colorElements.attr("content");
			if (StringUtils.isBlank(default_color)) {
				default_color = colorElements.text();
			}
		}

		// priceJSONObject
		String priceJSON = StringUtils.substringBetween(content, "productPrices[" + productId + "] = ", ";");
		JSONObject productPricesJSONObject = JSONObject.parseObject(priceJSON);
		JSONObject priceJSONObject = productPricesJSONObject.getJSONObject("items");

		// images
		JSONObject imageJSONObject = new JSONObject();
		// swicth image
		JSONObject swicthJSONObject = new JSONObject();
		imageJSONObject_package(doc, imageJSONObject, swicthJSONObject, default_color, productId, context);

		// sku
		JSONArray skuJSONArray = new JSONArray();
		sku_jsonarray_package(skuJSONArray, doc);

		// sku iteator
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
				JSONObject skuPriceJSONObject = priceJSONObject.getJSONObject(skuId);
				float sale_price = skuPriceJSONObject.getFloatValue("offerPrice");
				float orign_price = skuPriceJSONObject.getFloatValue("listPrice");
				String unit = getCurrencyValue(skuPriceJSONObject.getString("listPriceFmt"));

				String sku_color = skuJsonObejct.getString("color");
				String sku_size = skuJsonObejct.getString("size");
				String sku_length = skuJsonObejct.getString("length");
				// spu price
				if (!spu_price_flag) {
					if (default_color.equalsIgnoreCase(sku_color)) {
						int save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100);
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
				if (StringUtils.isNotBlank(sku_length)) {
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("Length");
					selection.setSelect_value(sku_length);
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
				String switch_img = StringUtils.EMPTY;

				if (default_color.equalsIgnoreCase(color)) {
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
				List<Image> pics = (List<Image>) imageJSONObject.get(StringUtils.lowerCase(color));
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
		retBody.setBrand(new Brand(brand, "", "", ""));

		// title
		retBody.setTitle(new Title(title, "", "", ""));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(DOMAIN+productId);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(DOMAIN));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

		category_package(doc,content, brand, retBody);

		properties_package(content, retBody);

		desc_package(doc, retBody);
		
		setOutput(context, retBody);
	}

	/**
	 * skujsonarray 封装
	 * 
	 * @param skuJSONArray
	 * @param doc
	 */
	private static void sku_jsonarray_package(JSONArray skuJSONArray, Document doc) {
		Elements skuElements = doc.select("select.product-page__skus option");
		if (CollectionUtils.isNotEmpty(skuElements)) {
			for (Element element : skuElements) {
				// sku
				String sku = element.attr("value");
				if (StringUtils.isBlank(sku)) {
					continue;
				}
				JSONObject jsonObject = new JSONObject();
				// stock
				int stock_status = 0;
				String stock_flag = element.attr("data-inventory-status");
				if (StringUtils.endsWithIgnoreCase(INSTOCK, stock_flag)) {
					stock_status = 1;
				}
				// color
				String color = element.attr("data-swatch");
				// size
				String size = element.attr("data-size-primary");
				// length
				String length = element.attr("data-size-secondary");

				jsonObject.put("skuId", sku);
				jsonObject.put("color", color);
				jsonObject.put("size", size);
				jsonObject.put("length", length);
				jsonObject.put("stock_status", stock_status);

				skuJSONArray.add(jsonObject);
			}
		}
	}

	/**
	 * 封装imageJSONObject
	 * 
	 * color 作为imageJSONObject key 时 统一小写
	 * 
	 * @param doc
	 * @param imageJSONObject
	 * @param swicthJSONObject
	 * @param default_color
	 * @param productId
	 * @param context
	 */
	private static void imageJSONObject_package(Document doc, JSONObject imageJSONObject, JSONObject swicthJSONObject,
			String default_color, String productId, Context context) {
		Elements imageElements = doc.select("ul.product-swatches li.product-attrs__attr.product-swatch__attr");
		String image_param = doc.select("input[name=collection]").attr("value");
		String image_flag = image_param + "_0";
		JSONObject imageRequestJSONObject = new JSONObject();
		if (CollectionUtils.isNotEmpty(imageElements)) {
			int i = 0;
			// 商品多个颜色
			for (Element element : imageElements) {
				String color = StringUtils.lowerCase(element.attr("aria-label"));
				i++;
				String image_flag_temp = image_flag + i;

				String request_url = StringUtils.replacePattern(IMAGE_REQUEST_URL, "\\(\\)", image_flag_temp);
				request_url = StringUtils.replacePattern(request_url, "\\{\\}", image_param);

				// imageRequestJSONObject put
				imageRequestJSONObject.put(color, request_url);
			}

			// 多次请求 封装color 对应 图片
			new AbercrombieHandler().process(imageRequestJSONObject, imageJSONObject, context, image_param,false);

		} else {
			// 说明商品只有一个颜色
			String request_url = StringUtils.replacePattern(IMAGE_REQUEST_URL, "\\(\\)", image_flag+1);
			request_url = StringUtils.replacePattern(request_url, "\\{\\}", image_param);

			// imageRequestJSONObject put
			imageRequestJSONObject.put(StringUtils.lowerCase(default_color), request_url);
			
			new AbercrombieHandler().process(imageRequestJSONObject, imageJSONObject, context, image_param,true);
		}
	}

	/**
	 * category breadcrumbs 封装
	 * @param doc 
	 * 
	 * @param content
	 * @param brand
	 * @param retBody
	 */
	private static void category_package(Document doc, String content, String brand, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		String category = StringUtils.substringBetween(content, "CategoryId\":\"", "\",");
		if(StringUtils.isNotBlank(category)){
			String[] categoryArray = category.split("->");
			if (null != categoryArray && categoryArray.length != 0) {
				for (String string : categoryArray) {
					String cat = StringUtils.replacePattern(
							StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(string)), "_", " ");
					if (StringUtils.isNotBlank(cat)) {
						cats.add(cat);
						breads.add(cat);
					}
				}
			}
		}else{
			Elements categoryElemnts = doc.select("div.upper-breadcrumb div.breadcrumbs.l-breadcrumbs a");
			if(CollectionUtils.isNotEmpty(categoryElemnts)){
				for (Element element : categoryElemnts) {
					String cat = StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(element.text()));
					if (StringUtils.isNotBlank(cat)) {
						cats.add(cat);
						breads.add(cat);
					}
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
		Elements es = doc.select(
				"p[itemprop=description] ,div.details__length div, div.fabric-care__container.has-fabric-info.has-care-info h4, div.fabric-care__container.has-fabric-info.has-care-info ul li");
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
	 * @param content
	 * @param retBody
	 */
	private static void properties_package(String content, RetBody retBody) {
		String gender = StringUtils.substringBetween(content, "gender\":\"", "\",");
		gender = getSex(gender);
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

	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN)) {
			gender = "women";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN2)) {
			gender = "women";
		}else if (StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		}else if (StringUtils.containsIgnoreCase(cat, SEX_MEN2)) {
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
		if (StringUtils.isBlank(currency)) {
			return unit;
		}
		unit = Currency.codeOf(currency).name();
		return unit;

	}
}
