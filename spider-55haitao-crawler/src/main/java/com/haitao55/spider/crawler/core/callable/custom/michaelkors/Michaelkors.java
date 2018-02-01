package com.haitao55.spider.crawler.core.callable.custom.michaelkors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * michaelkors 详情页封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2016年12月19日 下午5:22:47
 * @version 1.0
 */
public class Michaelkors extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.michaelkors.com";
	private String MICHAELKORS_API = "https://www.michaelkors.com/server/productinventory?productList=";
	private static final String IMAGE_PRIFER = "https://michaelkors.scene7.com/is/image/";
	private static final String IMAGE_SUFFER = "?wid=558&hei=748";
	private static final String MK_API = "http://10.128.0.6/mk/get.action?url=";

	public void invoke(Context context) {
		String url = context.getUrl().getValue();
		String content = StringUtils.EMPTY;
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
			content = luminatiHttpClient.request(url, getIndexHeaders());
			context.setHtmlPageSource(content);
		}else{
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(url, getIndexHeaders());
		}
		Document doc = JsoupUtils.parse(content);
		if (StringUtils.isNotBlank(content)) {
			RetBody retBody = new RetBody();

			Sku sku = new Sku();

			// productId
			String productId = StringUtils.EMPTY;
			// spu stock status
			int spu_stock_status = 0;
			// default color code
			String default_color_code = StringUtils.substringAfter(url, "color=");
			// title
			String title = StringUtils.EMPTY;
			// brand
			String brand = StringUtils.EMPTY;
			// desc
			String desc = StringUtils.EMPTY;
			// gender
			String gender = StringUtils.EMPTY;
			// unit
			String unit = Currency.USD.name();

			String parentCategoryName = StringUtils.EMPTY;
			Map<String, String> hashMap = new HashMap<>();

			// product data
			String data = StringUtils.substringBetween(content, "rawJson\":", ",\"schemaOrg\":");
			// JSONObject parseObject = JSONObject.parseObject(data + "}}");
			// JSONObject pdpJSONObject = parseObject.getJSONObject("pdp");
			if (null != data) {
				JSONObject productJSONObject = JSONObject.parseObject(data);
				// JSONObject productJSONObject =
				// pdpJSONObject.getJSONObject("rawJson");

				// sku
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				// style
				Set<String> styleSet = new HashSet<String>();
				// color image jsonobject key:styleNumber_colorcode
				// value:color
				if (null != productJSONObject && !productJSONObject.isEmpty()) {
					productId = productJSONObject.getString("identifier");
					String rsSkus = "";
					if (isRunInRealTime) {
						LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
						rsSkus = luminatiHttpClient.request(MICHAELKORS_API + productId, getStockHeaders(url));
						if (StringUtils.isNotBlank(rsSkus)) {
							logger.info("cralwer mk :::::   url :{} , rsSkus : {}", url, rsSkus);
						}
					}else{
						LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
						rsSkus = luminatiHttpClient.request(MICHAELKORS_API + productId, getStockHeaders(url));
						if (StringUtils.isNotBlank(rsSkus)) {
							logger.info("cralwer mk :::::   url :{} , rsSkus : {}", url, rsSkus);
						}
					}
					if (StringUtils.isNotBlank(rsSkus)) {
						JSONObject jsonObject = JSONObject.parseObject(rsSkus);
						JSONObject productJsonObject = jsonObject.getJSONObject("product");
						if (productJsonObject != null) {
							JSONArray jsonArray = productJsonObject.getJSONArray("SKUs");
							for (int i = 0; i < jsonArray.size(); i++) {
								JSONObject instockJson = jsonArray.getJSONObject(i);
								String pId = instockJson.getString("identifier");
								String stockNumber = StringUtils.substringBetween(instockJson.toString(),
										"stockLevel\":", ",");
								if (StringUtils.isNotBlank(pId) && StringUtils.isNotBlank(stockNumber)) {
									hashMap.put(pId, stockNumber);
								}
							}
						}
					}

					brand = productJSONObject.getString("brand");
					desc = productJSONObject.getString("description");
					parentCategoryName = productJSONObject.getString("parentCategoryName");
					title = productJSONObject.getString("displayName");
					gender = productJSONObject.getString("guideType");
					JSONArray skuJSONArray = productJSONObject.getJSONArray("SKUs");
					if (null != skuJSONArray && skuJSONArray.size() > 0) {
						for (Object object : skuJSONArray) {
							JSONObject skuJSONObject = (JSONObject) object;
							// selectlist
							LSelectionList lselectlist = new LSelectionList();
							String skuId = skuJSONObject.getString("identifier");
							String styleNumber = skuJSONObject.getString("styleNumber");
							String color = StringUtils.EMPTY;
							String color_code = StringUtils.EMPTY;
							String size = StringUtils.EMPTY;
							String swicth_img = StringUtils.EMPTY;
							JSONObject forSizeAnsColorJSONObject = skuJSONObject.getJSONObject("variant_values");
							if (null != forSizeAnsColorJSONObject && !forSizeAnsColorJSONObject.isEmpty()) {
								JSONObject sizeJSONObject = forSizeAnsColorJSONObject.getJSONObject("size");
								JSONObject colorJSONObject = forSizeAnsColorJSONObject.getJSONObject("color");
								size = sizeJSONObject.getString("name");
								color = colorJSONObject.getString("name");
								color_code = colorJSONObject.getString("colorCode");
								swicth_img = colorJSONObject.getString("swatchImageUrl");
							}
							// stock
							int stock_status = 0;
							String stockNum = hashMap.get(skuId);
							if (StringUtils.isNotBlank(stockNum) && Integer.parseInt(stockNum) > 0) {
								spu_stock_status = 1;
								stock_status = 1;
							}
							
							if(stock_status == 0){
								continue;
							}
							
							List<Image> imageList = new ArrayList<>();
							JSONObject mediaJSONObject = skuJSONObject.getJSONObject("media");
							JSONArray jsonArray = mediaJSONObject.getJSONArray("images");
							for (int i = 0; i < jsonArray.size(); i++) {
								String imageKey = jsonArray.getString(i);
								if (StringUtils.isNotBlank(imageKey)) {
									imageList.add(new Image(IMAGE_PRIFER + imageKey + IMAGE_SUFFER));
								}
							}
							context.getUrl().getImages().put(skuId, imageList);
							// price
							float sale_price = 0;
							float orign_price = 0;
							JSONObject priceJSONObject = skuJSONObject.getJSONObject("prices");
							if (null != priceJSONObject && !priceJSONObject.isEmpty()) {
								orign_price = priceJSONObject.getFloatValue("listPrice");
								sale_price = priceJSONObject.getFloatValue("salePrice");
								if (orign_price < sale_price) {
									orign_price = sale_price;
								}
							}

							if (StringUtils.equals(default_color_code, color_code)
									|| StringUtils.isBlank(default_color_code)) {
								int save = Math.round((1 - sale_price / orign_price) * 100);
								retBody.setPrice(new Price(orign_price, save, sale_price, unit));
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

							// lselectlist
							lselectlist.setGoods_id(skuId);
							lselectlist.setOrig_price(orign_price);
							lselectlist.setSale_price(sale_price);
							lselectlist.setPrice_unit(unit);
							lselectlist.setStock_status(stock_status);
							lselectlist.setStyle_id(color);
							lselectlist.setSelections(selections);

							// l_selection_list
							l_selection_list.add(lselectlist);
							if (!styleSet.contains(color)) {
								// stylelist
								LStyleList lStyleList = new LStyleList();
								if (StringUtils.equals(default_color_code, color_code)) {
									lStyleList.setDisplay(true);
								}
								// stylelist
								lStyleList.setGood_id(skuId);
								lStyleList.setStyle_switch_img(swicth_img);
								lStyleList.setStyle_cate_id(0);
								lStyleList.setStyle_id(color);
								lStyleList.setStyle_cate_name("Color");
								lStyleList.setStyle_name(color);
								l_style_list.add(lStyleList);
								styleSet.add(color);
							}
						}
					}
				}

				sku.setL_selection_list(l_selection_list);
				sku.setL_style_list(l_style_list);

				retBody.setSku(sku);

				// stock
				retBody.setStock(new Stock(spu_stock_status));

				// brand
				retBody.setBrand(new Brand(brand, "", "", ""));

				retBody.setTitle(new Title(title, "", "", ""));

				// full doc info
				String docid = SpiderStringUtil.md5Encode(domain + productId);
				String url_no = SpiderStringUtil.md5Encode(url);
				retBody.setDOCID(docid);
				retBody.setSite(new Site(domain));
				retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

				// category breadcrumb
				// category_package(doc, retBody, brand, productId,context);
				List<String> cats = new ArrayList<String>();
				List<String> breads = new ArrayList<String>();

				if (StringUtils.isNotBlank(parentCategoryName)) {
					cats.add(parentCategoryName);
					cats.add(title);
					breads.add(parentCategoryName);
					breads.add(title);
				} else {
					cats.add(title);
					breads.add(title);
				}
				retBody.setCategory(cats);
				retBody.setBreadCrumb(breads);

				// description
				desc_package(desc, doc, retBody);

				// properties
				Map<String, Object> propMap = new HashMap<String, Object>();
				propMap.put("s_gender", gender);
				retBody.setProperties(propMap);
				setOutput(context, retBody);
				//System.out.print(retBody.parseTo());
			}
		}

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
		featureMap.put("feature-1", desc);
		retBody.setFeatureList(featureMap);
		descMap.put("en", desc);
		retBody.setDescription(descMap);
	}

	private static Map<String, Object> getIndexHeaders() {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Accept-Language", "en-US,en;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
		headers.put("Host", "www.michaelkors.com");
		headers.put("cookie","userPrefLanguage=en_US; cookieLanguage=en_US");
		headers.put("If-None-Match", "W/\"1fe9e4-Tn8oUWDqDGDd9RAYaAf0NczuPDk\"");
		return headers;
	}

	private static Map<String, Object> getStockHeaders(String referer) {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		headers.put("Accept", "application/json, text/plain, */*");
		headers.put("Accept-Language", "en-US,en;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.michaelkors.com");
		headers.put("Referer", referer);
		headers.put("cookie","userPrefLanguage=en_US; cookieLanguage=en_US");
		return headers;
	}

	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		Michaelkors mk = new Michaelkors();
		Context con = new Context();
		con.setRunInRealTime(true);
		con.setUrl(new Url(
				"https://www.michaelkors.com/jet-set-travel-saffiano-leather-card-holder/_/R-US_32T4STVF2L?color=0461"));
		mk.invoke(con);
	}
}
