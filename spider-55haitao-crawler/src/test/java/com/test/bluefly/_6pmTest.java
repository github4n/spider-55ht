/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: _6pmTest.java 
 * @Prject: spider-55haitao-crawler
 * @Package: com.test.bluefly 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年10月28日 下午3:24:52 
 * @version: V1.0   
 */
package com.test.bluefly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom._6pm._6PM;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/** 
 * @ClassName: _6pmTest 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年10月28日 下午3:24:52  
 */
public class _6pmTest extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.6pm.com";

	public static void main(String[] args) {
		_6pmTest test = new _6pmTest();
		Context context = new Context();
//		context.setCurrentUrl("http://www.bluefly.com/lkbennett-lkbennett-silk-midi-dress/p/392932601");
		context.setCurrentUrl("http://www.6pm.com/petrol-classic-flare-jeans-in-dark-blue-dark-blue");
		try {
			test.invoke(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void invoke(Context context) throws Exception {
		try {
//			String content = super.getInputString(context);
			String content = HttpUtils.get(context.getCurrentUrl(), 30000, 1, null);
			RetBody rebody = new RetBody();
			if (StringUtils.isNotBlank(content)) {
				String code = StringUtils.substringBetween(content, "_p || {}", "</script>");
				String styleId = StringUtils.substringBetween(code, "var styleId = ", ";");
				String productId = StringUtils.substringBetween(code, "var productId = ", ";");
				String title = StringUtils.substringBetween(code, "var productName = \"", "\";");
				// String brandId = StringUtils.substringBetween(code,
				// "var brandId = ", ";");
				String brandName = StringUtils.substringBetween(code, "var brandName = \"", "\";");
				String gender = StringUtils.substringBetween(code, "var productGender = \"", "\";");
				String categores = StringUtils.substringBetween(code, "var zetaCategories = ", ";");
				String stockJSON = StringUtils.substringBetween(code, "var stockJSON = ", ";");
				String dimensions = StringUtils.substringBetween(code, "var dimensions = ", ";");
				// String dimToUnitToValJSON =
				// StringUtils.substringBetween(code,
				// "var dimToUnitToValJSON = ", ";");
				String dimensionIdToNameJson = StringUtils.substringBetween(code, "var dimensionIdToNameJson = ", ";");
				String valueIdToNameJSON = StringUtils.substringBetween(code, "var valueIdToNameJSON = ", ";");
				String colorNames = StringUtils.substringBetween(code, "var colorNames = ", ";");
				String colorPrices = StringUtils.substringBetween(code, "var colorPrices = ", ";");
				String styleIds = StringUtils.substringBetween(code, "var styleIds = ", ";");
				String colorIds = StringUtils.substringBetween(code, "var colorIds = ", ";");

				// pics
				// Set<Image> pics = getPicsByStyleId(code, styleId);
				JSONArray dimension = new JSONArray();
				if (StringUtils.isNotBlank(dimensions)) {
					dimension = JSONArray.parseArray(StringUtils.trim(dimensions));
				}
				// sku key
				JSONObject nameJson = new JSONObject();
				if (StringUtils.isNotBlank(dimensionIdToNameJson)) {
					nameJson = JSONObject.parseObject(StringUtils.trim(dimensionIdToNameJson));
				}
				// sku value
				JSONObject valueJson = new JSONObject();
				if (StringUtils.isNotBlank(valueIdToNameJSON)) {
					valueJson = JSONObject.parseObject(StringUtils.trim(valueIdToNameJSON));
				}
				// sku color id and name
				JSONObject colorIdNames = new JSONObject();
				if (StringUtils.isNotBlank(colorNames)) {
					colorIdNames = JSONObject.parseObject(StringUtils.trim(colorNames));
				}
				// sku color id and prices
				JSONObject colorIdPrices = new JSONObject();
				if (StringUtils.isNotBlank(colorPrices)) {
					colorIdPrices = JSONObject.parseObject(StringUtils.trim(colorPrices));
				}
				// color id and sku id
				JSONObject colorIdSkuId = new JSONObject();
				if (StringUtils.isNotBlank(styleIds)) {
					colorIdSkuId = JSONObject.parseObject(StringUtils.trim(styleIds));
				}
				// sku id and color id
				JSONObject skuIdColorId = new JSONObject();
				if (StringUtils.isNotBlank(colorIds)) {
					skuIdColorId = JSONObject.parseObject(StringUtils.trim(colorIds));
				}
				// selection list
				// JSONObject selectionObj = new JSONObject();
				// JSONArray selection_list = new JSONArray();
				Sku sku = new Sku();
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				Set<String> colors = new HashSet<String>();// all colors
				Map<String, Integer> colorIdStock = new HashMap<String, Integer>();
				if (StringUtils.isNotBlank(stockJSON)) {
					JSONArray stockArr = JSONArray.parseArray(StringUtils.trim(stockJSON));
					if (stockArr != null) {
						for (int i = 0; i < stockArr.size(); i++) {
							// JSONObject sku = new JSONObject();
							LSelectionList lselectlist = new LSelectionList();
							JSONObject obj = stockArr.getJSONObject(i);
							String colorId = obj.getString("color");
							colors.add(colorId);
							int stock_number = obj.getIntValue("onHand");// stock
							Integer st = colorIdStock.get(colorId);
							if (st == null) {
								colorIdStock.put(colorId, stock_number);
							} else {
								colorIdStock.put(colorId, stock_number + st);
							}
							int stock_status = 0;
							if (stock_number > 0) {
								stock_status = 2;
							}
							int skuId = colorIdSkuId.getIntValue(colorId);// skuid
							JSONObject price = colorIdPrices.getJSONObject(colorId);
							float orig_price = price.getFloatValue("wasInt");
							float sale_price = price.getFloatValue("nowInt");
							String currency = StringUtils.substring(price.getString("now"), 0, 1);
							String price_unit = Currency.codeOf(currency).name();
							String colorValue = colorIdNames.getString(colorId);
							List<Selection> selections = new ArrayList<Selection>();
							for (int j = 0; j < dimension.size(); j++) {
								Selection select = new Selection();
								String key = dimension.getString(j);
								String valueId = obj.getString(key);
								String propName = nameJson.getString(key);
								String propValue = StringUtils.EMPTY;
								JSONObject val = valueJson.getJSONObject(valueId);
								if (val != null) {
									propValue = val.getString("value");
								}
								select.setSelect_id(Long.valueOf(valueId));
								select.setSelect_name(propName);
								select.setSelect_value(propValue);
								selections.add(select);
							}
							lselectlist.setSelections(selections);
							lselectlist.setGoods_id(skuId+"");
							lselectlist.setStyle_id(colorValue);
							lselectlist.setOrig_price(orig_price);
							lselectlist.setSale_price(sale_price);
							lselectlist.setPrice_unit(price_unit);
							lselectlist.setStock_number(stock_number);
							lselectlist.setStock_status(stock_status);
							l_selection_list.add(lselectlist);
						}
					}
				}
				sku.setL_selection_list(l_selection_list);
				// style list
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				for (String colorid : colors) {
					String colorname = colorIdNames.getString(colorid);
					int skuId = colorIdSkuId.getIntValue(colorid);
					List<Image> picsPerSku = getPicsByStyleId(content, skuId + "");
//					context.getUrl().getImages().put(skuId + "", picsPerSku);// picture
																				// download
					LStyleList style = new LStyleList();
					style.setStyle_switch_img("");
					style.setStyle_id(colorname);
					style.setStyle_cate_id(0l);
					style.setStyle_cate_name("color");
					style.setStyle_name(colorname);
					// style.setStyle_images(picsPerSku);
					style.setGood_id(skuId+"");
					if (StringUtils.isNotBlank(styleId) && styleId.equals(skuId + "")) {
						style.setDisplay(true);
					}
					l_style_list.add(style);
				}
				sku.setL_style_list(l_style_list);

				// full doc info
				String docid = SpiderStringUtil.md5Encode(domain + productId);
				String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
				rebody.setDOCID(docid);
				rebody.setSite(new Site(domain));
				rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
				rebody.setTitle(new Title(title, ""));

				// price
				String colorId = skuIdColorId.getString(styleId);
				if (StringUtils.isNotBlank(colorId)) {
					JSONObject price = colorIdPrices.getJSONObject(colorId);
					float orig = 0;
					float sale  = 0;
					if(price != null){
						orig = price.getFloatValue("wasInt");
						sale = price.getFloatValue("nowInt");
					}
					if(orig < sale ){
						orig = sale;
					}
					String currency = StringUtils.substring(price.getString("now"), 0, 1);
					String unit = Currency.codeOf(currency).name();
					int save = Math.round((1 - sale / orig) * 100);// discount
					rebody.setPrice(new Price(orig, save, sale, unit));
					
					int stock = colorIdStock.get(colorId);
					int stockStatus = 0;
					if (stock > 0) {
						stockStatus = 2;
					}
					if(sale == 0){
						stockStatus = 0;
					}
					// stock
					rebody.setStock(new Stock(stockStatus));
				}
				// images l_image_list
				// rebody.setImage(new LImageList(pics));
				// brand
				rebody.setBrand(new Brand(brandName, ""));
				// Category
				List<String> cats = new ArrayList<String>();
				List<String> breads = new ArrayList<String>();
				String[] arrCat = StringUtils.substringsBetween(categores, "\": \"", "\"");
				if (arrCat != null && arrCat.length > 0) {
					for (String c : arrCat) {
						String cat = Native2AsciiUtils.ascii2Native(c);
						cats.add(cat);
						breads.add(cat);
					}
				}
				rebody.setCategory(cats);
				// BreadCrumb
				breads.add(brandName);
				rebody.setBreadCrumb(breads);
				// description
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				Document document = Jsoup.parse(content);
				Elements es = document.select("div.description > ul > li");
				StringBuilder sb = new StringBuilder();
				if (es != null && es.size() > 0) {
					int count = 1;
					for (Element e : es) {
						featureMap.put("feature-" + count, e.text());
						count++;
						sb.append(e.text());
					}
				}
				rebody.setFeatureList(featureMap);
				descMap.put("en", sb.toString());
				rebody.setDescription(descMap);

				Map<String, Object> propMap = new HashMap<String, Object>();
				propMap.put("s_gender", gender);
				es = document.select("div.description > ul > li.measurements > ul > li");
				if (es != null && es.size() > 0) {
					for (Element e : es) {
						String key = StringUtils.trim(StringUtils.substringBefore(e.text(), ":"));
						String value = StringUtils.trim(StringUtils.substringAfter(e.text(), ":"));
						if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
							propMap.put(key, value);
						}
					}
				}
				rebody.setProperties(propMap);
				rebody.setSku(sku);
			}
//			setOutput(context, rebody.parseTo());
			System.err.println(rebody.parseTo());
		} catch (Throwable e) {
			e.printStackTrace();
			logger.error("Error while crawling url {} ,exception {}", context.getCurrentUrl(),e);
		}
	}

	public JSONObject put(String key, Object value) {
		JSONObject param = new JSONObject();
		param.put(key, value);
		return param;
	}

	public JSONObject put(String key, String value) {
		JSONObject param = new JSONObject();
		param.put(key, value);
		return param;
	}

	public JSONObject put(String key, int value) {
		JSONObject param = new JSONObject();
		param.put(key, value);
		return param;
	}

	public _6pmTest put(JSONObject obj, String key, String value) {
		obj.put(key, value);
		return this;
	}

	public _6pmTest put(JSONObject obj, String key, long value) {
		obj.put(key, value);
		return this;
	}

	public _6pmTest put(JSONObject obj, String key, float value) {
		obj.put(key, value);
		return this;
	}

	private List<Image> getPicsByStyleId(String content, String skuId) {
		List<Image> pics = new ArrayList<Image>();
		List<String> pic_key = new ArrayList<String>();
		pic_key.add("p");
		pic_key.add("1");
		pic_key.add("2");
		pic_key.add("3");
		pic_key.add("4");
		pic_key.add("5");
		pic_key.add("6");
		for (String key : pic_key) {
			String image2xp = StringUtils.substringBetween(content, "pImgs[" + skuId + "]['2x']['" + key + "'] = '",
					"';");
			if (StringUtils.isNotBlank(image2xp)) {
				Image image = new Image(image2xp);
				pics.add(image);
			}
		}
		return pics;
	}

}
