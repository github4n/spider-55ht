package com.haitao55.spider.crawler.core.callable.custom.swarovski;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
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
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * swarovski 详情页封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年1月12日 下午2:51:02
 * @version 1.0
 */
public class Swarovski extends AbstractSelect {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String stock_status_flag = "in stock";
	private static final String domain = "www.swarovski.com";
	private static final String image_preffix = "https://www.swarovski.com";
	private static String SWAR_API = "https://www.swarovski.com/Web_US/en/product?VariationSelected=true&SKU=";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";

	@Override
	public void invoke(Context context) throws Exception {
		try {
		String url = context.getCurrentUrl().toString();
		
//		String content = this.getInputString(context);
//		Document doc = this.getDocument(context);
		
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		Document doc = Jsoup.parse(content);
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// default color
		String default_color = StringUtils.EMPTY;

		// brand
		String brand = "swarovski";

		// title
		String title = StringUtils.EMPTY;
		Elements elements = doc.select("h1[itemprop=name]");
		if (CollectionUtils.isNotEmpty(elements)) {
			title = elements.get(0).text();
		}

		// productId
		String productId = StringUtils.substringBetween(content, "'id': '", "',");

		// 多个 color，链接封装
		List<JSONObject> params = new ArrayList<JSONObject>();

		// first
		JSONObject firstSkuJsonObject = new JSONObject();
		// skuJsonObject 封装
		firstSkuJsonObject_package(content, doc, firstSkuJsonObject,context);

		// 请求参数封装
		default_color = request_params_package(params, doc, firstSkuJsonObject);
		if (StringUtils.isNotBlank(default_color) && !StringUtils.equalsIgnoreCase("One Color", default_color)) {
			default_color = default_color + productId;
		}
		// skuJsonArray
		JSONArray skuJsonArray = new JSONArray();
		// 当前链接json数据
		skuJsonArray.add(firstSkuJsonObject);

		skuJsonArray = new SwarovskiHandler().process(params, context.getUrl(), skuJsonArray);

		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();

		// style
		JSONObject styleJsonObject = new JSONObject();

		// sku iteator
		if (null != skuJsonArray && skuJsonArray.size() > 0) {
			for (Object object : skuJsonArray) {
				JSONObject jsonObject = (JSONObject) object;
				Map<String,Integer> sizes = (Map<String,Integer>)jsonObject.get("sizes");
				if(MapUtils.isNotEmpty(sizes)){
					Set<String> set = sizes.keySet();
					Iterator<String> it = set.iterator();
					while(it.hasNext()){
						String sizeVal = it.next();
						setLselectionList(jsonObject,sizeVal,l_selection_list,styleJsonObject,
								default_color,retBody,sizes);
					}
				}else{
					setLselectionList(jsonObject,null,l_selection_list,styleJsonObject,
							default_color,retBody,null);
				}
				
			}
			if (null != styleJsonObject && styleJsonObject.size() > 0) {
				for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
					String styleidAndSkuId = entry.getKey();
					JSONObject jsonObject = (JSONObject) entry.getValue();
					// stylelist
					LStyleList lStyleList = new LStyleList();
					// skuId
					String skuId = jsonObject.getString("skuId");
					// color
					String color = jsonObject.getString("color");
					String switch_img = jsonObject.getString("switch_image");
					if (!StringUtils.contains(switch_img, "http") && StringUtils.isNotBlank(switch_img)) {
						switch_img = image_preffix.concat(switch_img);
					}
					if (StringUtils.equalsIgnoreCase(default_color, styleidAndSkuId)) {
						lStyleList.setDisplay(true);
					}
					String style_id = color;
					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(style_id);

					@SuppressWarnings("unchecked")
					List<Image> pics = (List<Image>) jsonObject.get("images");

					context.getUrl().getImages().put(skuId, pics);

					// l_style_list
					l_style_list.add(lStyleList);
				}
			}
		}

		// sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		int spuStock = 0;
		if(l_selection_list != null 
				&& l_selection_list.size() > 0){
			for(LSelectionList ll : l_selection_list){
				int sku_stock = ll.getStock_status();
				if (sku_stock == 1) {
					spuStock = 1;
					break;
				}
				if (sku_stock == 2){
					spuStock = 2;
				}
			}
		}
		
		retBody.setStock(new Stock(spuStock));
		
		retBody.setSku(sku);

		// title
		retBody.setTitle(new Title(title, "", "", ""));

		// brand
		retBody.setBrand(new Brand(brand, "", "", ""));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(productId.concat(domain));
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

		// category breadcrumb
		category_package(brand, retBody, doc);

		// description
		desc_package(doc, retBody);

		// properties
		properties_package(retBody);

		setOutput(context, retBody);
		} catch(Exception e){
			logger.error("crawler error and url is {}", context.getCurrentUrl());
		}
	}

	private static String crawler_result(Url url, String path)
			throws ClientProtocolException, HttpException, IOException {
		String proxyRegionId = url.getTask().getProxyRegionId();
		String content = StringUtils.EMPTY;
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(30000).url(path).proxy(false).resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(30000).url(path).proxy(true).proxyAddress(proxyAddress)
					.proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

	private void setLselectionList(JSONObject jsonObject, String sizeVal, List<LSelectionList> l_selection_list,
			JSONObject styleJsonObject, String default_color, RetBody retBody, Map<String, Integer> sizes) {
		// selectlist
		LSelectionList lselectlist = new LSelectionList();

		// skuId
		String skuId = jsonObject.getString("skuId");
		int stock_status = jsonObject.getIntValue("stock_status");
		int stock_number = 0;
		// stock
		if (MapUtils.isNotEmpty(sizes)) {
			stock_status = sizes.get(sizeVal);
		}

		// style_id
		String style_id = jsonObject.getString("color");

		// price
		float orign_price = jsonObject.getFloatValue("orign_price");
		float sale_price = jsonObject.getFloatValue("sale_price");
		String unit = jsonObject.getString("unit");
		int save = jsonObject.getIntValue("save");

		// selections
		List<Selection> selections = new ArrayList<Selection>();
		if (StringUtils.isNotBlank(sizeVal)) {
			Selection selection = new Selection();
			selection.setSelect_name("size");
			selection.setSelect_value(sizeVal);
			selections.add(selection);
		}

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

		if (StringUtils.equalsIgnoreCase(default_color, style_id)) {
			retBody.setPrice(new Price(orign_price, save, sale_price, unit));
		}

		// style json
		if (StringUtils.containsIgnoreCase(style_id, "One Color")) {
			styleJsonObject.put(style_id, jsonObject);
		} else {
			styleJsonObject.put(style_id + skuId, jsonObject);
		}
	}

	/**
	 * 封装 当前链接的一些数据
	 * 
	 * @param content
	 * @param doc
	 * @param firstSkuJsonObject
	 * @throws IOException
	 * @throws HttpException
	 * @throws ClientProtocolException
	 */
	private static void firstSkuJsonObject_package(String content, Document doc, JSONObject firstSkuJsonObject,
			Context context) throws ClientProtocolException, HttpException, IOException {
		// skuId
		String skuId = StringUtils.substringBetween(content, "'id': '", "',");
		// stock status flag
		String stock_status_flag_temp = StringUtils.substringBetween(content,
				"<meta property=\"og:availability\" content=\"", "\" />");
		String notAvailable = doc.select(".product-not-available p").text();
		boolean flag = false;
		if (StringUtils.isNotBlank(notAvailable)) {
			if (StringUtils.containsIgnoreCase(notAvailable, "not available")) {
				flag = true;
			}
		}
		int stock_status = 1;
		if (!StringUtils.equalsIgnoreCase(stock_status_flag, stock_status_flag_temp) || flag) {
			stock_status = 0;
		}
		// unit
		String unit = StringUtils.substringBetween(content, "'currencyCode':'", "',");
		// sale_price
		String sale_price = StringUtils.substringBetween(content, "'price': '", "'");
		// orign_price
		String orign_price = StringUtils.substringBetween(content, "'metric1': '", "',");

		String save = StringUtils.EMPTY;
		sale_price = sale_price.replaceAll("[ ,]", "");
		orign_price = orign_price.replaceAll("[ ,]", "");

		if (StringUtils.isBlank(orign_price)) {
			orign_price = sale_price;
		}
		if (StringUtils.isBlank(sale_price) || StringUtils.equals(sale_price, "0.00")) {
			sale_price = orign_price;
		}
		if (StringUtils.isBlank(orign_price) || Float.valueOf(orign_price) < Float.valueOf(sale_price)) {
			orign_price = sale_price;
		}
		if (StringUtils.isBlank(save)) {
			save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100) + "";// discount
		}

		// images
		Elements elements = doc.select("ul.thumbnails.clearfix li a img");
		List<Image> list = new ArrayList<Image>();
		if (CollectionUtils.isNotEmpty(elements)) {
			for (Element element : elements) {
				String image_url = element.attr("data-elevatezoomlargeimg");
				String image_url_src = element.attr("src");
				if (StringUtils.isNotBlank(image_url)) {
					list.add(new Image(image_url));
				}
				if (StringUtils.isBlank(image_url)) {
					if (StringUtils.isNotBlank(image_url_src)
							&& !StringUtils.containsIgnoreCase(image_url_src, "http")) {
						list.add(new Image(image_preffix + image_url_src));
					}
				}
			}
		}

		Map<String, Integer> sizeMap = new HashMap<>();
		Elements es = doc.select("select#variation option");
		for (Element e : es) {
			String value = e.text();
			String skuValue = e.attr("value");
			if (StringUtils.containsIgnoreCase(value, "Select size")) {
				continue;
			}
			String sizeHtml = crawler_result(context.getUrl(), SWAR_API + skuValue);
			int size_stock_status = 1;
			if (StringUtils.isNotBlank(sizeHtml)) {
				Document sizeDoc = JsoupUtils.parse(sizeHtml);
				String sizeNotAvailable = sizeDoc.select(".product-not-available p").text();
				boolean size_flag = false;
				if (StringUtils.isNotBlank(sizeNotAvailable)) {
					if (StringUtils.containsIgnoreCase(sizeNotAvailable, "not available")) {
						size_flag = true;
					}
				}
				// stock status flag
				String size_stock_status_flag_temp = StringUtils.substringBetween(sizeHtml,
						"<meta property=\"og:availability\" content=\"", "\" />");
				if (!StringUtils.equalsIgnoreCase(stock_status_flag, size_stock_status_flag_temp) || size_flag) {
					size_stock_status = 0;
				}

			}
			sizeMap.put(value, size_stock_status);
		}
		// return jsonObject
		firstSkuJsonObject.put("skuId", skuId);
		firstSkuJsonObject.put("stock_status", stock_status);
		firstSkuJsonObject.put("sale_price", sale_price);
		firstSkuJsonObject.put("orign_price", orign_price);
		firstSkuJsonObject.put("save", save);
		firstSkuJsonObject.put("unit", unit);
		firstSkuJsonObject.put("images", list);
		firstSkuJsonObject.put("sizes", sizeMap);
	}

	/**
	 * 请求参数封装
	 * 
	 * @param params
	 * @param doc
	 * @param firstSkuJsonObject
	 */
	private static String request_params_package(List<JSONObject> params, Document doc, JSONObject firstSkuJsonObject) {
		String default_color = StringUtils.EMPTY;
		Elements elements = doc.select("div.variants ul.clearfix li");
		int count = 0;
		if (CollectionUtils.isNotEmpty(elements)) {
			for (Element element : elements) {
				count++;
				JSONObject paramJsonObject = new JSONObject();
				Elements aElements = element.select("a");
				Elements imgElemets = aElements.select("img");
				// 每个颜色对应url
				String param = aElements.attr("abs:href");
				// switch image url
				String switch_image_url = imgElemets.attr("src");

				// color
				String color = imgElemets.attr("alt");
				String active = element.attr("class");
				if (StringUtils.isNotBlank(active)) {
					default_color = color;

					// 当前链接 封装skuJsonObject, 避免线程中再次发送请求，产生消耗
					firstSkuJsonObject.put("color", color + count);
					firstSkuJsonObject.put("switch_image", switch_image_url);

					continue;
				}

				paramJsonObject.put("url", param);
				paramJsonObject.put("switch_image", switch_image_url);
				paramJsonObject.put("color", color + count);

				// add
				params.add(paramJsonObject);
			}
		}
		// 单color
		else {
			String color = "One Color";
			firstSkuJsonObject.put("color", color);
			firstSkuJsonObject.put("switch_image", "");
			default_color = color;
		}
		return default_color;
	}

	/**
	 * category breadcrumbs 封装
	 * 
	 * @param category
	 * @param brand
	 * @param retBody
	 * @param doc
	 */
	private static void category_package(String brand, RetBody retBody, Document doc) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements cateElements = doc.select("div.breadcrumbs span a span");
		for (Element element : cateElements) {
			String cat = StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(element.text()));
			if (StringUtils.isNotBlank(cat)) {
				cats.add(cat);
				breads.add(cat);
			}
		}
		// cats
		retBody.setCategory(cats);

		// BreadCrumb
		breads.add(brand);
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
		// desc trans doc
		Elements es = doc.select("span[itemprop=description] p, ul.desc-features li");
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
	 * @param gender
	 * @param retBody
	 */
	private static void properties_package(RetBody retBody) {
		Map<String, Object> propMap = new HashMap<String, Object>();
		String gender = getSex(retBody.getTitle().getEn());
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

	public static void main(String[] args) throws Exception {
		String str = "https://www.swarovski.com/Web_US/en/5201117/product/Slake_Aqua_Dot_Bracelet  .html";
		System.out.println(str.replaceAll("  ", ""));

		// Swarovski s = new Swarovski();
		// Context context = new Context();
		// context.setUrl(new
		// Url("https://www.swarovski.com/Web_US/en/5199716/product/Parallele_Pierced_Earrings.html"));
		// context.setCurrentUrl("https://www.swarovski.com/Web_US/en/5199716/product/Parallele_Pierced_Earrings.html");
		// s.invoke(context);
	}
}
