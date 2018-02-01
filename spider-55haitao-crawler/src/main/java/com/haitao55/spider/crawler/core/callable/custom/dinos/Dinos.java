package com.haitao55.spider.crawler.core.callable.custom.dinos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * dinos 详情页封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2016年12月13日 下午4:20:23
 * @version 1.0
 */
public class Dinos extends AbstractSelect {
	private static final String SEX_WOMEN = "女";
	private static final String SEX_MEN = "男";
	private static final String domain = "www.dinos.co.jp";

	@SuppressWarnings("serial")
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		Document doc = this.getDocument(context);

		Pattern pattern = Pattern.compile("ご指定の商品が見つかりませんでした");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"www.dinos.co.jp itemUrl:" + context.getUrl().toString() + " not found..");
		}
		pattern = Pattern.compile("この商品は販売期間を終了しております");
		 matcher = pattern.matcher(content);
		 if (matcher.find()) {
			 throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"www.dinos.co.jp itemUrl:" + context.getUrl().toString() + " not found..");
		 }

		//dinos　特殊商品　　没法处理　http://www.dinos.co.jp/p/1132204399/   http://www.dinos.co.jp/p/1905600206/
		Elements priceElements = doc.select("span[itemprop=price]");
		if(CollectionUtils.isNotEmpty(priceElements)){
			String price=priceElements.text();
			if(StringUtils.contains(price, "よりどり3点")){
				//手动下架处理，不收录
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"www.dinos.co.jp itemUrl:" + context.getUrl().toString() + " not found..");
			}
			if(StringUtils.contains(price, "よりどり2点")){
				//手动下架处理，不收录
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"www.dinos.co.jp itemUrl:" + context.getUrl().toString() + " not found..");
			}
		}
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// stock status
		int stock_status = 1;

		// 默认sku 标识
		boolean default_sku_flag = false;
		// spu 库存标识
		boolean spu_has_stock = false;
		// sku 是否只有size
		boolean only_size = false;
		// sku color,size 是否都是下拉框形式
		boolean is_color_size_select = false;
		// 单品页 标识
		boolean single_item_flag = false;

		List<Image> pics = new ArrayList<Image>();

		String default_skuId = StringUtils.EMPTY;

		String moshbg = StringUtils.substringBetween(content, "var strMoshbg           = \"", "\";");

		String brand_name = StringUtils.substringBetween(content, "var strGoods_Nm         = \"", "\";");

		String title = brand_name;

		Elements elements = doc.select("div#ancSpec + table.mod_defTable.itemD_table tbody tr ");

		// price
		JSONObject priceJson = new JSONObject();
		if (CollectionUtils.isNotEmpty(elements)) {// td:nth-child(3)
			// price JSONObject 封装
			price_josonObject(priceJson, elements,doc,brand_name);

		}

		// image
		JSONObject imageJson = new JSONObject();
		image_package(imageJson, doc, pics);

		elements = doc.select("div.dctSelectBox.color p#pdispiro");
		String cate_name1 = StringUtils.EMPTY;

		cate_name1 = cate_name_package(elements, cate_name1);

		// size 分类
		elements = doc.select("div.dctSelectBox.size p#pdisp");
		String cate_name2 = StringUtils.EMPTY;

		cate_name2 = cate_name_package(elements, cate_name2);

		// 请求json封装
		JSONArray requestJsonArray = new JSONArray();
		// 商品下拉框 http://www.dinos.co.jp/p/1367900245/
		Elements brandElements = doc.select("div.dctCartFormBox select[name=SEGOODSNM] option:not(:first-child)");

		// size
		Elements sizeElements = doc.select("div.dctSelectBox.size div.chip li");

		// color 是下拉框 http://www.dinos.co.jp/p/1275504178/
		Elements color_select_elements = doc.select("div.dctSelectBox.color select.defSize option:not(:first-child)");
		// size 是下拉框 http://www.dinos.co.jp/p/1275504178/
		Elements size_select_elements = doc.select("div.dctSelectBox.size select.defSize option:not(:first-child)");

		/** sku 相关请求参数封装 start */
		elements = doc.select("div.dctSelectBox.color div.chip li");
		if (CollectionUtils.isNotEmpty(elements) && CollectionUtils.isEmpty(brandElements) && CollectionUtils.isNotEmpty(sizeElements)) {
			for (Element element : elements) {
				// requestJsonArray 封装
				color_size_request_array(requestJsonArray, element, cate_name1, cate_name2, brand_name, moshbg, false,null);
			}
		}
		
		// 只有color
		else if (CollectionUtils.isNotEmpty(elements) && CollectionUtils.isEmpty(sizeElements)
				&& CollectionUtils.isEmpty(brandElements)) {
			for (Element element : elements) {
				// requestJsonArray 封装
				color_size_request_array(requestJsonArray, element, cate_name1, cate_name2, brand_name, moshbg, false,"color");
			}
		}
		
		// 没有ｃｏｌｏｒ 只有ｓｉｚｅ
		else if (CollectionUtils.isEmpty(elements) && CollectionUtils.isEmpty(brandElements) && CollectionUtils.isNotEmpty(sizeElements)) {

			only_size = true;
			// requestJsonArray 封装
			color_size_request_array(requestJsonArray, null, cate_name1, cate_name2, brand_name, moshbg, false,null);
		}
		// 商品 颜色组合
		else if (CollectionUtils.isNotEmpty(elements) && CollectionUtils.isNotEmpty(brandElements)) {
			Elements cateElements = doc.select("div#ancCart section div section div h3");
			if (CollectionUtils.isNotEmpty(cateElements)) {
				cate_name2 = cateElements.get(0).text();
			}
			for (Element element : elements) {

				// 每个颜色 对应的image标识
				String color_image = element.attr("data-dpv-select-clr-name");
				String param = element.attr("title");
				Elements imgElements = element.select("img");
				String switch_image = replace_imgurl(imgElements.attr("src"));
				Elements paramElements = element.select("input[type=radio]");
				String  color_value = paramElements.attr("value");

				for (Element brandElement : brandElements) {
					// 商品 和颜色 请求参数封装
					brand_color_request_array(requestJsonArray, brandElement, color_image, color_value, switch_image,param,
							cate_name1, cate_name2, content, "brand_color");
				}
			}
		}

		// 商品 size组合　http://www.dinos.co.jp/p/1330801387/
		else if (CollectionUtils.isNotEmpty(sizeElements) && CollectionUtils.isNotEmpty(brandElements)) {
			Elements cateElements = doc.select("div#ancCart section div section div h3");
			if (CollectionUtils.isNotEmpty(cateElements)) {
				cate_name1 = cateElements.get(0).text();
				cate_name2 = cateElements.get(1).text();
			}
				// 每个颜色 对应的image标识
			// requestJsonArray 封装
			for (Element brandElement : brandElements) {
				String value = brandElement.text();
				// 商品 请求参数封装
				brand_color_request_array(requestJsonArray, brandElement, "", value, "","", cate_name1, cate_name2, content,
						"brand_size");
			}
		}
		
		//商品下拉　　与　颜色下拉　http://www.dinos.co.jp/p/1293900351/
		else if (CollectionUtils.isNotEmpty(color_select_elements) && CollectionUtils.isNotEmpty(brandElements)) {
			Elements cateElements = doc.select("div#ancCart section div section div h3");
			if (CollectionUtils.isNotEmpty(cateElements)) {
				cate_name1 = cateElements.get(0).text();
				cate_name2 = cateElements.get(1).text();
			}
			
			//商品颜色　都是下拉框
			for (Element element : color_select_elements) {

				// 每个颜色 对应的image标识
				String param = element.attr("value");
				String color_value = element.text();
				for (Element brandElement : brandElements) {
					// 商品 和颜色 下拉框形式　请求参数封装
					brand_color_request_array(requestJsonArray, brandElement, "", color_value, "",param,
							cate_name1, cate_name2, content, "brand_color_select");
				}
			}
		}
		
		// 没有ｃｏｌｏｒ 只有brand 下拉 http://www.dinos.co.jp/p/1366400071/
		else if (CollectionUtils.isEmpty(elements)   && CollectionUtils.isEmpty(sizeElements) && CollectionUtils.isNotEmpty(brandElements)&&CollectionUtils.isEmpty(color_select_elements)
				&& CollectionUtils.isEmpty(size_select_elements)) {
			only_size = true;

			Elements cateElements = doc.select("div#ancCart section div section div h3");
			if (CollectionUtils.isNotEmpty(cateElements)) {
				cate_name2 = cateElements.get(0).text();
			}

			// requestJsonArray 封装
			for (Element brandElement : brandElements) {
				// 商品 请求参数封装
				brand_color_request_array(requestJsonArray, brandElement, "", "", "", "",cate_name1, cate_name2, content,
						"brand");
			}
		}
		//size color 下拉
		else if (CollectionUtils.isNotEmpty(color_select_elements)
				&& CollectionUtils.isNotEmpty(size_select_elements)) {

			is_color_size_select = true;

			Elements cateElements = doc.select("div#ancCart section div section div h3");
			if (CollectionUtils.isNotEmpty(cateElements)) {
				cate_name1 = cateElements.get(0).text();
				cate_name2 = cateElements.get(1).text();
			}

			for (Element element : color_select_elements) {
				// requestJsonArray 封装
				color_size_request_array(requestJsonArray, element, cate_name1, cate_name2, brand_name, moshbg, true,null);
			}
		}
		
		// 只有color　下拉
		else if (CollectionUtils.isNotEmpty(color_select_elements)
				&& CollectionUtils.isEmpty(size_select_elements)) {

			is_color_size_select = true;

			Elements cateElements = doc.select("div#ancCart section div section div h3");
			if (CollectionUtils.isNotEmpty(cateElements)) {
				cate_name1 = cateElements.get(0).text();
			}

			for (Element element : color_select_elements) {
				// requestJsonArray 封装
				color_size_request_array(requestJsonArray, element, cate_name1, "", brand_name, moshbg, true,"color_select");
			}
		}
		
		// 单品页
		else {

			single_item_flag = true;

			String single_item = "http://www.dinos.co.jp/defaultMall/sitemap/XHRGetZaikoInfo.jsp?CATNO=900&GOODS_NO=&MOSHBG="
					+ moshbg + "&CLS1CD=&CLS2CD=&DATEFLG=1";
			String sigle_item_result=StringUtils.EMPTY;
			String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
			 if(StringUtils.isBlank(proxyRegionId)){
				 sigle_item_result = Crawler.create().timeOut(15000).url(single_item).proxy(false).resultAsString();
			 }else{
				 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
						 true);
				 String proxyAddress=proxy.getIp();
				 int proxyPort=proxy.getPort();
				 sigle_item_result = Crawler.create().timeOut(15000).url(single_item).proxy(true).proxyAddress(proxyAddress)
						 .proxyPort(proxyPort).resultAsString();
			 }
			if (StringUtils.isNotBlank(sigle_item_result)) {
				JSONObject responseJson = JSONObject.parseObject(sigle_item_result);
				responseJson = responseJson.getJSONObject("Result");
				JSONArray responseArray = responseJson.getJSONArray("data");
				JSONObject resultJson = responseArray.getJSONObject(0);
				String stock = resultJson.getString("zaiko");
				if (!"在庫あり".equals(stock)) {
					stock_status = 0;
				}
			}
		}

		/** sku 相关请求参数封装 end */

		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();

		if (!single_item_flag) {
			// style json object
			JSONObject styleJson = new JSONObject();

			JSONArray skuArray = new DinosHandler().process(requestJsonArray, context.getUrl());
			if (null != skuArray && skuArray.size() > 0) {
				for (Object skuData : skuArray) {
					JSONObject jsonObject = (JSONObject) skuData;
					LSelectionList lselectlist = new LSelectionList();
					String color_value = jsonObject.getString("color");
					String size_value = jsonObject.getString("valuec2");
					String skuId = StringUtils.EMPTY;
					String style_id = StringUtils.EMPTY;

					String brandName = jsonObject.getString("brand_name");
					brandName = StringUtils.replacePattern(brandName, " ","");
					JSONObject priceData = priceJson.getJSONObject(brandName);

					Float orignPrice = priceData.getFloat("origPrice");
					Float salePrice = priceData.getFloat("salePrice");
					if (null == orignPrice) {
						orignPrice = salePrice;
					}

					String unit = priceData.getString("unit");

					int save = Math.round((1 - Float.valueOf(salePrice) / Float.valueOf(orignPrice)) * 100);

					int sku_stock_status = 0;
					if (jsonObject.getIntValue("limited_cnt2") > 0) {
						sku_stock_status = 1;
						// 只要有一个ｓｋｕ 有库存， spu库存就!=0
						spu_has_stock = true;
					}
					if (!spu_has_stock) {
						stock_status = 0;
					}

					List<Selection> selections = new ArrayList<Selection>();

					if (!only_size) {
						if(StringUtils.isBlank(size_value)){
							size_value=StringUtils.EMPTY;
						}
						StringBuffer buffer = new StringBuffer();
						if(StringUtils.isNotBlank(color_value)){
							buffer.append(color_value);
						}
						if(StringUtils.isNotBlank(color_value) && StringUtils.isNotBlank(size_value)){
							buffer.append("&");
						}
						if(StringUtils.isNotBlank(size_value)){
							buffer.append(size_value);
						}
						skuId = buffer.toString();
						// style_id
						style_id = color_value;

						// selections
						if (StringUtils.isNotBlank(jsonObject.getString("namec2"))) {
							Selection selection = new Selection();
							selection.setSelect_id(0);
							selection.setSelect_name(jsonObject.getString("cate_name2"));
							selection.setSelect_value(jsonObject.getString("valuec2"));
							selections.add(selection);
						}
					} else {
						skuId = size_value;
						// style_id
						style_id = jsonObject.getString("valuec2");
					}

					// selectlist
					lselectlist.setStyle_id(style_id);

					lselectlist.setGoods_id(skuId);
					lselectlist.setOrig_price(orignPrice);
					lselectlist.setPrice_unit(unit);
					lselectlist.setSale_price(salePrice);
					// lookfantastic not find number
					lselectlist.setStock_number(0);
					lselectlist.setStock_status(sku_stock_status);

					lselectlist.setSelections(selections);
					l_selection_list.add(lselectlist);

					if (!default_sku_flag) {
						retBody.setPrice(new Price(orignPrice, save, salePrice, unit));
						default_sku_flag = true;
						default_skuId = color_value;
					}

					// style json
					styleJson.put(color_value, jsonObject);

				}

				// style
				for (Map.Entry<String, Object> styleData : styleJson.entrySet()) {

					String style_id = styleData.getKey();

					JSONObject jsonObject = (JSONObject) styleData.getValue();

					// stylelist
					LStyleList lStyleList = new LStyleList();

					String color_value = jsonObject.getString("color");
					String size_value = jsonObject.getString("valuec2");
					
					String skuId = StringUtils.EMPTY;
					
					if (!only_size) {
						if(StringUtils.isBlank(size_value)){
							size_value=StringUtils.EMPTY;
						}
						StringBuffer buffer = new StringBuffer();
						if(StringUtils.isNotBlank(color_value)){
							buffer.append(color_value);
						}
						if(StringUtils.isNotBlank(color_value) && StringUtils.isNotBlank(size_value)){
							buffer.append("&");
						}
						if(StringUtils.isNotBlank(size_value)){
							buffer.append(size_value);
						}
						skuId = buffer.toString();
					} else {
						skuId = size_value;
					}
					
					lStyleList.setGood_id(skuId);

					String switch_img = jsonObject.getString("switch_img");

					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name(jsonObject.getString("cate_name1"));
					lStyleList.setStyle_name(style_id);

					if (StringUtils.equals(default_skuId, style_id)) {
						lStyleList.setDisplay(true);
					}

					if (only_size) {
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_cate_name("color");
						lStyleList.setStyle_id("default");
						lStyleList.setStyle_name("default");
						lStyleList.setStyle_switch_img("");

						context.getUrl().getImages().put(moshbg, pics);

						return;
					}

					l_style_list.add(lStyleList);

					if (is_color_size_select) {
						context.getUrl().getImages().put(skuId, pics);
					}else{
						String imageUrl = imageJson.getString(jsonObject.getString("color_image"));
						if(StringUtils.isNotBlank(imageUrl)){
							context.getUrl().getImages().put(skuId, new ArrayList<Image>() {
								{
									add(new Image(imageUrl));
								}
							});
						}else{
							context.getUrl().getImages().put(skuId, pics);
						}
					}

				}
			}

		}
		// single item
		else {
			// price
			brand_name = StringEscapeUtils.unescapeHtml(StringUtils.replacePattern(brand_name, " ",""));
			JSONObject priceData = priceJson.getJSONObject(brand_name);

			Float orignPrice = priceData.getFloat("origPrice");
			Float salePrice = priceData.getFloat("salePrice");
			if (null == orignPrice) {
				orignPrice = salePrice;
			}

			String unit = priceData.getString("unit");

			int save = Math.round((1 - Float.valueOf(salePrice) / Float.valueOf(orignPrice)) * 100);
			retBody.setPrice(new Price(orignPrice, save, salePrice, unit));

			context.getUrl().getImages().put(moshbg, pics);
		}

		// sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		retBody.setSku(sku);

		// stock
		retBody.setStock(new Stock(stock_status));

		// 面包屑 分类 封装
		category_breadcrumbs_package(retBody, doc,brand_name);

		// properties 封装
		properties_package(retBody, doc);

		// desc 封装
		desc_package(retBody, doc);

		// full doc info
		String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl().toString());
		String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl().toString());
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(context.getCurrentUrl().toString(), System.currentTimeMillis(), url_no));
		retBody.setTitle(new Title("", "", title, ""));

		// brand
		retBody.setBrand(new Brand("", "", brand_name, ""));

		setOutput(context, retBody);
	}

	/**
	 * desc 封装
	 * 
	 * @param retBody
	 * @param doc
	 */
	private static void desc_package(RetBody retBody, Document doc) {
		// description
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		Elements es = doc.select("div.mod_blockB.itemD_itemGuide");
		StringBuilder sb = new StringBuilder();
		if (es != null && es.size() > 0) {
			int count = 1;
			for (Element e : es) {
				featureMap.put("feature-" + count, e.text());
				count++;
				sb.append(e.text());
			}
		}
		retBody.setFeatureList(featureMap);
		descMap.put("en", sb.toString());
		retBody.setDescription(descMap);
	}

	/**
	 * properties 封装
	 * 
	 * @param retBody
	 * @param doc
	 */
	private static void properties_package(RetBody retBody, Document doc) {
		String gender_content = StringUtils.EMPTY;
		String category = StringUtils.EMPTY;
		Elements es = doc.select("div.mod_voiceArea.endBdr");
		if (CollectionUtils.isNotEmpty(es)) {
			gender_content = es.text();
		}
		es = doc.select("div[itemprop=category] li:not(:first-child)");
		if (CollectionUtils.isNotEmpty(es)) {
			category = es.text();
		}
		String gender = getSex(gender_content);

		Map<String, Object> propMap = new HashMap<String, Object>();
		List<List<Object>> propattr = new ArrayList<List<Object>>();
		if(StringUtils.isBlank(gender)){
			gender = getSex(category);
		}
		propMap.put("s_gender", gender);
		es = doc.select("table#itemtable");
		if (es != null && es.size() > 0) {
			for (Element e : es) {
				List<Object> proList = new ArrayList<Object>();
				List<List<String>> list = new ArrayList<List<String>>();
				String keyValue = StringUtils.EMPTY;
				Elements key = e.select("span.thTxt");
				if (CollectionUtils.isNotEmpty(key)) {
					keyValue = key.get(0).text();
				}
				Elements valueList = e.select("tbody tr td");
				if (CollectionUtils.isNotEmpty(valueList)) {
					String[] split = valueList.text().split("■");
					for (String element : split) {
						String prokey = StringUtils.trim(StringUtils.substringBefore(element, "："));
						String value = StringUtils.trim(StringUtils.substringAfter(element, "："));
						if (StringUtils.isNotBlank(prokey) && StringUtils.isNotBlank(value)) {
							List<String> tempList = new ArrayList<String>();
							tempList.add(prokey);
							tempList.add(value);
							list.add(tempList);
						}
					}
				}
				proList.add(keyValue);
				proList.add(list);
				propattr.add(proList);
			}
		}
		propMap.put("attr", propattr);
		retBody.setProperties(propMap);
	}

	/**
	 * category breadcrumb 封装
	 * 
	 * @param retBody
	 * @param doc
	 * @param brand_name 
	 */
	private static void category_breadcrumbs_package(RetBody retBody, Document doc, String brand_name) {
		// Category
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements elements = doc.select("div[itemprop=category] li:not(:first-child)");
		if (elements != null && elements.size() > 0) {
			for (Element c : elements) {
				String cat = Native2AsciiUtils.ascii2Native(c.text());
				cats.add(cat);
				breads.add(cat);
			}
		}
		retBody.setCategory(cats);
		// BreadCrumb
		 breads.add(brand_name);
		retBody.setBreadCrumb(breads);
	}

	/**
	 * cate_name 封装
	 * 
	 * @param elements
	 * @param cate_name
	 */
	private static String cate_name_package(Elements elements, String cate_name) {
		if (CollectionUtils.isNotEmpty(elements)) {
			cate_name = elements.get(0).text();
			cate_name = StringUtils.substringBefore(cate_name, "：");
		}
		return cate_name;
	}

	/**
	 * image json 封装
	 * 
	 * @param imageJson
	 * @param doc
	 * @param pics
	 */
	private static void image_package(JSONObject imageJson, Document doc, List<Image> pics) {
		Elements elements = doc.select("div#dpvThumb ul li");
		if (CollectionUtils.isNotEmpty(elements)) {
			for (Element element : elements) {

				String imageUrl = element.attr("data-dpv-expand-url");
				imageUrl = replace_imgurl(imageUrl);

				String color_image = element.attr("data-dpv-thumb-clr-name");

				pics.add(new Image(imageUrl));

				imageJson.put(color_image, imageUrl);
			}
		}
	}

	/**
	 * price 封装 dinos price从页面下表格中获取
	 * 
	 * @param priceJson
	 * @param elements
	 * @param brand_name 
	 * @param doc 
	 */
	private static void price_josonObject(JSONObject priceJson, Elements elements, Document doc, String brand_name) {
		if (CollectionUtils.isEmpty(elements)) {
			Elements priceElements = doc.select("span.pLarge");
			String salePrice = priceElements.text();
			salePrice = StringUtils.substringBefore(salePrice, "（税込");
			Elements transElements = doc.select("span.icnDeliver + span");
			String transPrice = transElements.text();
			brand_name = StringEscapeUtils.unescapeHtml(StringUtils.replacePattern(brand_name, " ",""));
			set_price_to_json(priceJson, salePrice, transPrice, brand_name);
		} else {
			for (Element element : elements) {

				String brandName = element.select("td:nth-child(2)").text();
				String salePrice = element.select("td:nth-child(3)").text();
				String transPrice = element.select("td:nth-child(4)").text();
				salePrice = StringUtils.substringBefore(salePrice, "（税込");
				transPrice = StringUtils.substringBetween(transPrice, "￥", "税込");
				brandName = StringUtils.replacePattern(brandName, " ","");
				set_price_to_json(priceJson, salePrice, transPrice, brandName);

			}

		}
	}

	/***
	 * price 封装到json 供后面使用
	 * 
	 * @param priceJson
	 * @param salePrice
	 * @param transPrice
	 * @param brandName
	 */
	private static void set_price_to_json(JSONObject priceJson, String salePrice, String transPrice, String brandName) {
		JSONObject price = new JSONObject();
		salePrice = "J".concat(salePrice);

		String unit = StringUtils.EMPTY;
		unit = getCurrencyValue(salePrice);

		if (StringUtils.isNotBlank(salePrice)) {
			salePrice = salePrice.replaceAll("[J¥￥, ]", "");
		}
		if (StringUtils.isNotBlank(transPrice)) {
			transPrice = transPrice.replaceAll("[J¥￥, ]", "");
		}
		price.put("salePrice", Float.valueOf(salePrice) + Float.valueOf(null == transPrice ? "0" : transPrice));
		price.put("unit", unit);
		priceJson.put(brandName, price);
	}

	/**
	 * bran color 请求参数封装
	 * 
	 * @param requestJsonArray
	 * @param brandElement
	 * @param color_image
	 * @param color_value
	 * @param switch_image
	 * @param param
	 * @param cate_name1
	 * @param cate_name2
	 * @param content
	 * @param category
	 */
	private static void brand_color_request_array(JSONArray requestJsonArray, Element brandElement, String color_image,
			String color_value, String switch_image, String param, String cate_name1, String cate_name2, String content,
			String category) {
		JSONObject requestJson = new JSONObject();

		requestJson.put("color_image", color_image);
		requestJson.put("switch_image", switch_image);
		if(StringUtils.equals(category, "brand_color_select")){
			
			requestJson.put("param", param);
		}else{
			requestJson.put("param", "");// 不需要
		}
		requestJson.put("color_value", color_value);
		requestJson.put("cate_name1", cate_name1);
		requestJson.put("cate_name2", cate_name2);

		String value = brandElement.attr("value");
		String brand_name_own = brandElement.text();
		String moshbg_own = StringUtils.substringBetween(content, "objMoshbg[\"" + value + "\"] = \"", "\";");

		requestJson.put("brand_name", brand_name_own);
		requestJson.put("moshbg", moshbg_own);
		requestJson.put("goods_no", value);
		// 标识 是 商品 颜色 组合
		requestJson.put("category", category);

		requestJsonArray.add(requestJson);
	}

	/**
	 * 颜色 size 请求 request array 封装
	 * 
	 * @param requestJsonArray
	 * @param element
	 * @param cate_name1
	 * @param cate_name2
	 * @param brand_name
	 * @param moshbg
	 * @param is_color_size_select
	 *            颜色 ｓｉｚｅ 样式是否都是下拉框
	 * @param category 
	 */
	private static void color_size_request_array(JSONArray requestJsonArray, Element element, String cate_name1,
			String cate_name2, String brand_name, String moshbg, boolean is_color_size_select, String category) {
		String color_image = StringUtils.EMPTY;
		String color_value = StringUtils.EMPTY;
		String switch_image = StringUtils.EMPTY;
		String param = StringUtils.EMPTY;
		JSONObject requestJson = new JSONObject();
		// 每个颜色 对应的image标识
		if (null != element && !is_color_size_select) {
			color_image = element.attr("data-dpv-select-clr-name");
			color_value = element.attr("title");
			Elements imgElements = element.select("img");
			switch_image = replace_imgurl(imgElements.attr("src"));
			Elements paramElements = element.select("input[type=radio]");
			param = paramElements.attr("value");
		} else if (null != element && is_color_size_select) {
			color_value = element.text();
			param = element.attr("value");
		}

		requestJson.put("color_image", color_image);
		requestJson.put("switch_image", switch_image);
		requestJson.put("param", param);
		requestJson.put("color_value", color_value);
		requestJson.put("cate_name1", cate_name1);
		requestJson.put("cate_name2", cate_name2);
		requestJson.put("brand_name", brand_name);
		requestJson.put("moshbg", moshbg);
		requestJson.put("category", category);
		requestJsonArray.add(requestJson);
	}

	/**
	 * img src
	 * //a248.e.akamai.net/f/248/14050/1h/www.dinos.co.jp/defaultMall/images../.
	 * ./defaultMall/images/ht5/common/color/A.png
	 * -->http://a248.e.akamai.net/f/248/14050/1h/www.dinos.co.jp/defaultMall/
	 * images../../defaultMall/images/ht5/common/color/A.png
	 * 
	 * @param img
	 * @return
	 */
	private static String replace_imgurl(String img) {
		String url = StringUtils.EMPTY;
		if (StringUtils.isBlank(img)) {
			return url;
		}
		if (!StringUtils.containsIgnoreCase(img, "http:")) {
			url = "http:".concat(img);
		}
		return url;
	}

	/**
	 * get 货币
	 * 
	 * @param val
	 * @return
	 */
	private static String getCurrencyValue(String val) {
		String currency = StringUtils.substring(val, 0, 2);
		currency = currency.replaceAll("¥", "￥");
		String unit = StringUtils.EMPTY;
		if (StringUtils.isBlank(currency)) {
			return unit;
		}
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