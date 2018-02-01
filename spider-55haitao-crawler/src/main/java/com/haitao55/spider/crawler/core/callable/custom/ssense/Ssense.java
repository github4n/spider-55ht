package com.haitao55.spider.crawler.core.callable.custom.ssense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * ssense 网站接入 这家网站比较特殊， 不同颜色作为不同商品，以单独商品形式存在，一般多属性到情况 多为size Title:
 * Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年1月6日 上午11:03:27
 * @version 1.0
 */
public class Ssense extends AbstractSelect {
	private static final String stock_flag = "Sold Out";
	private static final String size_value_split = " -";
	private static final String domain = "www.ssense.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	private static final String DISABLE_KEY = "disabled";

	@Override
	public void invoke(Context context) throws Exception {
		String referer = context.getUrl().getParentUrl();
		String content = StringUtils.EMPTY;
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
			content = luminatiHttpClient.request(context.getCurrentUrl(), getHeaders(null));
		} else {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(context.getCurrentUrl(), getHeaders(referer));
		}

		if (StringUtils.isNotBlank(content)) {
			Document doc = JsoupUtils.parse(content);

			String url = context.getCurrentUrl().toString();

			RetBody retBody = new RetBody();
			Sku sku = new Sku();

			// images
			List<Image> pics = new ArrayList<Image>();

			Elements elements = doc.select("div.product-images-container img");
			if (CollectionUtils.isNotEmpty(elements)) {
				for (Element element : elements) {
					String image_url = element.attr("data-srcset");
					pics.add(new Image(image_url));
				}
			}

			// item info
			elements = doc.select("div.product-description-container");

			// sku id
			// String skuId = StringUtils.EMPTY;

			// title
			String title = StringUtils.EMPTY;

			// gender
			String gender = StringUtils.EMPTY;

			// spu stock
			int spu_stock_status = 0;

			// price
			float sale_price = 0f;
			float orign_price = 0f;
			String unit = StringUtils.EMPTY;
			int save = 0;

			// item info

			Elements priceEs = doc.select(".price-container h3.price span");
			if (priceEs != null && priceEs.size() > 1) {
				String orign_price_temp = priceEs.get(0).text();
				unit = getCurrencyValue(orign_price_temp);
				orign_price_temp = StringUtils.replacePattern(orign_price_temp, "[$ USD]", "");
				orign_price = Float.parseFloat(orign_price_temp);

				String sale_price_temp = priceEs.get(1).text();
				sale_price_temp = StringUtils.replacePattern(sale_price_temp, "[$ USD]", "");
				sale_price = Float.parseFloat(sale_price_temp);
			} else {
				String sale_price_temp = priceEs.text();
				unit = getCurrencyValue(sale_price_temp);
				sale_price_temp = StringUtils.replacePattern(sale_price_temp, "[$ USD]", "");
				sale_price = Float.parseFloat(sale_price_temp);
			}

			String category = StringUtils.substringBetween(content, "product:category\" content=\"", "\"");
			String productId = StringUtils.substringBetween(content, "\"id\":", ",\"name");
			String brand = doc.select(".product-brand a").text();

			if (StringUtils.isBlank(unit)) {
				unit = "USD";
			}

			if (orign_price < sale_price) {
				orign_price = sale_price;
			}
			save = Math.round((1 - sale_price / orign_price) * 100);

			// title
			elements = doc.select("h2.product-name");
			if (CollectionUtils.isNotEmpty(elements)) {
				title = elements.text();
			}

			// gender
			elements = doc.select("div#product-item");
			if (CollectionUtils.isNotEmpty(elements)) {
				gender = elements.attr("data-gender");
			}

			// sku
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			// 给定默认goods_id
			List<String> skuIds = new ArrayList<String>();

			// size 这家网站比较特殊， 不同颜色作为不同商品，以单独商品形式存在，一般多属性到情况 多为size
			JSONArray skuJsonArray = new JSONArray();
			skuJsonArray_package(doc, skuJsonArray);
			if (null != skuJsonArray && skuJsonArray.size() > 0) {
				for (Object object : skuJsonArray) {
					JSONObject jsonObject = (JSONObject) object;

					// selectlist
					LSelectionList lselectlist = new LSelectionList();

					// skuid
					String sku_Id = jsonObject.getString("goods_id");

					// stock
					int stock_status = jsonObject.getIntValue("stock_status");
					int stock_number = 0;
					if (stock_status > 0) {
						spu_stock_status = 1;
						skuIds.add(sku_Id);
					}

					// selections
					String size = jsonObject.getString("size");
					List<Selection> selections = new ArrayList<Selection>();
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("Size");
					selection.setSelect_value(size);
					selections.add(selection);

					// lselectlist
					lselectlist.setGoods_id(sku_Id);
					lselectlist.setOrig_price(orign_price);
					lselectlist.setSale_price(sale_price);
					lselectlist.setPrice_unit(unit);
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id("default");
					lselectlist.setSelections(selections);

					// l_selection_list
					l_selection_list.add(lselectlist);
				}

				// style package
				LStyleList lStyleList = new LStyleList();
				lStyleList.setGood_id(skuIds.get(0));
				lStyleList.setDisplay(true);
				lStyleList.setStyle_cate_name("color");
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_name("default");
				lStyleList.setStyle_id("default");
				lStyleList.setStyle_switch_img("");
				context.getUrl().getImages().put(skuIds.get(0), pics);// picture
				l_style_list.add(lStyleList);
			}

			// sku
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			retBody.setSku(sku);

			// stock
			retBody.setStock(new Stock(spu_stock_status));

			// title
			retBody.setTitle(new Title(title, "", "", ""));

			// brand
			retBody.setBrand(new Brand(brand, "", "", ""));

			// price
			retBody.setPrice(new Price(orign_price, save, sale_price, unit));

			// full doc info

			String docid = StringUtils.EMPTY;
			if (StringUtils.isNotBlank(productId)) {
				docid = SpiderStringUtil.md5Encode(domain.concat(productId));
			} else {
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(domain));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

			// category breadcrumb
			category_package(category, brand, title, gender, retBody);

			// description
			desc_package(content, retBody);

			// properties
			properties_package(gender, retBody);
			setOutput(context, retBody);
		}
	}

	/**
	 * skuJsonArray 封装
	 * 
	 * @param doc
	 * @param skuJsonArray
	 */
	private static void skuJsonArray_package(Document doc, JSONArray skuJsonArray) {
		Elements elements = doc.select("select#size option:not(:first-child)");
		if (CollectionUtils.isNotEmpty(elements)) {

			// sku JSONArray
			for (Element element : elements) {
				JSONObject skuJson = new JSONObject();

				String goods_id = element.attr("value");

				String sku_text = element.text();

				boolean stock_flag_temp = stock_flag_package(element);
				int stock_status = 1;
				if (StringUtils.containsIgnoreCase(sku_text, stock_flag) || stock_flag_temp) {
					stock_status = 0;
				}

				String size = sku_text;
				if (StringUtils.containsIgnoreCase(sku_text, size_value_split)) {
					size = StringUtils.substringBefore(sku_text, size_value_split);
				}

				// String cate_name = "Size";

				skuJson.put("size", size);
				skuJson.put("goods_id", goods_id);
				skuJson.put("stock_status", stock_status);
				// add
				skuJsonArray.add(skuJson);
			}
		}
		// 单size
		else {
			elements = doc.select("div.button.full-width.onesize");
			if (CollectionUtils.isNotEmpty(elements)) {

				String sku_text = elements.text();

				int stock_status = 1;
				if (StringUtils.containsIgnoreCase(sku_text, stock_flag)) {
					stock_status = 0;
				}

				String size = sku_text;
				if (StringUtils.containsIgnoreCase(sku_text, size_value_split)) {
					size = StringUtils.substringBefore(sku_text, size_value_split);
				}
				String goods_id = size;

				JSONObject skuJson = new JSONObject();
				skuJson.put("size", size);
				skuJson.put("goods_id", goods_id);
				skuJson.put("stock_status", stock_status);
				// add
				skuJsonArray.add(skuJson);
			}
		}
	}

	private static boolean stock_flag_package(Element element) {
		boolean stock_flag_temp = false;
		Attributes attributes = element.attributes();
		// if(element instanceof Element){
		// Element ele = (Element)element;
		// attributes = ele.attributes();
		// }else if(element instanceof Elements){
		// Elements ele = (Elements)element;
		// attributes = ele.get(0).attributes();
		// }
		if (null != attributes && attributes.size() > 0) {
			Iterator<Attribute> iterator = attributes.iterator();
			while (iterator.hasNext()) {
				Attribute next = iterator.next();
				if (StringUtils.contains(next.getKey(), DISABLE_KEY)) {
					stock_flag_temp = true;
				}
			}
		}
		return stock_flag_temp;
	}

	/**
	 * category breadcrumbs 封装
	 * 
	 * @param category
	 * @param brand
	 * @param gender
	 * @param title
	 * @param retBody
	 */
	private static void category_package(String category, String brand, String title, String gender, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if (StringUtils.isNotBlank(gender)) {
			cats.add(gender);
			breads.add(gender);
		}
		if (StringUtils.isNotBlank(category)) {
			cats.add(category);
			breads.add(category);
		}
		if (StringUtils.isNotBlank(title)) {
			cats.add(title);
			breads.add(title);
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
	private static void desc_package(String content, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		String description = StringUtils.substringBetween(content, "og:description\" content=\"", "\">");
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(description)) {
			featureMap.put("feature-" + 1, description);
			sb.append(description);
		}
		retBody.setFeatureList(featureMap);
		descMap.put("en", sb.toString());
		retBody.setDescription(descMap);
	}

	/**
	 * properties 封装
	 * 
	 * @param gender
	 * @param retBody
	 */
	private static void properties_package(String gender, RetBody retBody) {
		Map<String, Object> propMap = new HashMap<String, Object>();
		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getTitle().getEn());
		}
		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getCategory().toString());
		}
		propMap.put("s_gender", gender);
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

	private static Map<String, Object> getHeaders(String referer) {
		final Map<String, Object> headers = new HashMap<String, Object>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2581158727487646435L;

			{
				put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
				put("Accept-Encoding", "gzip, deflate, br");
				put("accept-language", "zh-CN,zh;q=0.9");
				// put("upgrade-insecure-requests", 1);
				// put(":authority", "www.ssense.com");
				put("Cache-Control", "max-age=0");
				put("Connection", "keep-alive");
				if(StringUtils.isNotBlank(referer)){
					put("referer", referer);
				}
				put("User-Agent",
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/62.0.3202.89 Chrome/62.0.3202.89 Safari/537.36");
				put("cookie", "ssenseStack6=pureLegacyStack; lang=en_US; country=US;");
			}
		};
		return headers;
	}

	public static void main(String[] args) throws Exception {
		Ssense shan = new Ssense();
		Context context = new Context();
		context.setUrl(new Url(
				"https://www.ssense.com/en-us/women/product/lart-de-lautomobile/white-new-lart-t-shirt/2711508"));
		context.setCurrentUrl(
				"https://www.ssense.com/en-us/women/product/lart-de-lautomobile/white-new-lart-t-shirt/2711508");
		// http://www.yslbeautyus.com/forever-light-creator-cc-primer/890YSL.html
		shan.invoke(context);
	}
}
