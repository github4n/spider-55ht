package com.haitao55.spider.crawler.core.callable.custom.sephora;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.haitao55.spider.common.gson.JsonUtils;
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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.macys.Macys;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.exception.ParseException;

/**
 * sephora US 处理器
 * 
 * @author denghuan
 *
 */
public class SephoraUS extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String SEPHORA_API = "http://www.sephora.com/global/json/getSkuJson.jsp?skuId=";
	private static final String domain = "www.sephora.com";
	private static final String SITE = "https://www.sephora.com";

	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getUrl().getValue();
		String referer = context.getUrl().getParentUrl();
		
		String  content = "";
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(url, getHeaders(referer));
			context.setHtmlPageSource(content);
		}else{
			content = crawlerUrl(context, url,referer);
		}
		
		RetBody rebody = new RetBody();

		String isexist = StringUtils.substringBetween(content, "class=\"c-ImageGrid-table\"", "</div>");
		if (StringUtils.isNotBlank(isexist) && StringUtils.containsIgnoreCase(isexist, "We do not currently carry")) {
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"itemUrl:" + context.getUrl().toString() + " not found..");
		}

		if (StringUtils.isNotBlank(content)) {
			List<String> skuList = new ArrayList<String>();
			Pattern p = Pattern.compile("\"sku\":\"(.*?)\",");
			Matcher m = p.matcher(content);
			while (m.find()) {
				skuList.add(m.group(1));
			}

			String productJson = StringUtils.substringBetween(content, "</script><script type=\"application/ld+json\">",
					"</script>");
			if(StringUtils.isBlank(productJson)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"itemUrl:" + context.getUrl().toString() + " not found..");
			}
			
			JSONObject jsonObject = JSONObject.parseObject(productJson);
			String title = jsonObject.getString("name");
			String brand = jsonObject.getString("brand");

			String productId = StringUtils.substringBetween(content, "productId\":\"", "\"");
			String unit = Currency.codeOf("$").name();

			String docid = StringUtils.EMPTY;
			if (StringUtils.isBlank(productId)) {
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			} else {
				docid = SpiderStringUtil.md5Encode(domain + productId);
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));

			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Map<String, Boolean> defalutMap = new HashMap<>();
			Map<String, Boolean> displayMap = new HashMap<>();
			Map<String, String> colorExistMap = new HashMap<>();
			defalutMap.put("defalut", false);
			displayMap.put("display", true);
			int stockStatus = 0;

			if (org.apache.commons.collections.CollectionUtils.isNotEmpty(skuList)) {
				String ids = Joiner.on(",").join(skuList);
				//
				String skuJson =""; 
				if (isRunInRealTime) {
					LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
					skuJson = luminatiHttpClient.request(SEPHORA_API + ids, getHeaders(referer));
				}else{
					skuJson = crawlerUrl(context, SEPHORA_API + ids,referer);
				}
				if (StringUtils.isNotBlank(skuJson)) {
					if (skuList.size() == 1) {
						JsonObject skuDetails = JsonUtils.json2bean(skuJson, JsonObject.class);
						getLSelectList(l_selection_list, l_style_list, skuDetails, unit, context, defalutMap,
								displayMap, colorExistMap);
						if (CollectionUtils.isEmpty(l_selection_list)) {
							String sku_large_images = skuDetails.getAsJsonPrimitive("large_images").getAsString();
							if (StringUtils.isNotBlank(sku_large_images)) {
								List<Image> images = getImageList(sku_large_images);
								context.getUrl().getImages().put(productId, images);// picture
							}

							String is_in_stock = "";
							if (skuDetails.has("is_show_add_to_basket")) {
								is_in_stock = skuDetails.getAsJsonPrimitive("is_show_add_to_basket").getAsString();
							}

							if ("true".equals(is_in_stock)) {
								stockStatus = 1;
							}

							String sku_list_price = skuDetails.getAsJsonPrimitive("list_price").getAsString();
							String sale_price = "";
							if (skuDetails.has("sale_price")) {
								sale_price = skuDetails.getAsJsonPrimitive("sale_price").getAsString();
							}

							String value_price = "";
							if (skuDetails.has("value_price")) {
								value_price = skuDetails.getAsJsonPrimitive("value_price").getAsString();
							}

							if (StringUtils.isBlank(sale_price) && StringUtils.isBlank(value_price)
									&& StringUtils.isBlank(sku_list_price)) {
								throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "skuPrice parse error");
							} else if (StringUtils.isBlank(sale_price) && StringUtils.isBlank(value_price)) {
								rebody.setPrice(new Price(Float.parseFloat(sku_list_price), 0,
										Float.parseFloat(sku_list_price), unit));
							} else if (StringUtils.isNotBlank(value_price) && StringUtils.isBlank(sale_price)
									&& StringUtils.isNotBlank(sku_list_price)) {
								int save = Math.round(
										(1 - Float.parseFloat(sku_list_price) / Float.parseFloat(value_price)) * 100);// discount
								rebody.setPrice(new Price(Float.parseFloat(value_price), save,
										Float.parseFloat(sku_list_price), unit));
							} else if (StringUtils.isBlank(value_price) && StringUtils.isNotBlank(sale_price)
									&& StringUtils.isNotBlank(sku_list_price)) {
								int save = Math.round(
										(1 - Float.parseFloat(sale_price) / Float.parseFloat(sku_list_price)) * 100);// discount
								rebody.setPrice(new Price(Float.parseFloat(sku_list_price), save,
										Float.parseFloat(sale_price), unit));
							}
						}
					} else if (skuList.size() > 1) {
						JsonArray skuDetails = JsonUtils.json2bean(skuJson, JsonArray.class);
						if (skuDetails != null && skuDetails.size() > 0) {
							for (int i = 0; i < skuDetails.size(); i++) {
								JsonObject obj = skuDetails.get(i).getAsJsonObject();
								getLSelectList(l_selection_list, l_style_list, obj, unit, context, defalutMap,
										displayMap, colorExistMap);
							}
						}
					}
				}
			}

			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			int spuStock = 0;
			if (l_selection_list != null && l_selection_list.size() > 0) {
				for (LSelectionList ll : l_selection_list) {
					int sku_stock = ll.getStock_status();
					if (sku_stock == 1) {
						spuStock = 1;
						break;
					}
					if (sku_stock == 2) {
						spuStock = 2;
					}
				}
			} else {
				spuStock = stockStatus;
			}

			if (spuStock == 0) {
				logger.error("##########,stock:{}, url:{}, document:{}", spuStock, context.getUrl().getValue(),
						content);
			}

			rebody.setStock(new Stock(spuStock));

			Document doument = Jsoup.parse(content);
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String gender = StringUtils.EMPTY;
			;
			Elements es = doument.select(".css-1nxwcjq a");
			if (es != null && es.size() > 0) {
				for (Element e : es) {
					String cat = e.text();
					if (StringUtils.containsIgnoreCase(cat, "men")) {
						gender = "men";
					} else if (StringUtils.containsIgnoreCase(cat, "women")) {
						gender = "women";
					}
					if (StringUtils.isNotBlank(cat)) {
						cats.add(cat);
						breads.add(cat);
					}
				}
				rebody.setCategory(cats);
				rebody.setBreadCrumb(breads);
			} else {
				cats.add(title);
				breads.add(title);
				rebody.setCategory(cats);
				rebody.setBreadCrumb(breads);
			}

			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Elements desEs = doument.select(".css-1e532l3");
			String description = StringUtils.EMPTY;
			for (Element e : desEs) {
				description = e.text();
				break;
			}
			StringBuilder sb = new StringBuilder();
			if (StringUtils.isNotBlank(description)) {
				featureMap.put("feature-1", description);
				sb.append(description);
			}
			rebody.setFeatureList(featureMap);
			descMap.put("en", sb.toString());
			rebody.setDescription(descMap);

			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", gender);
			rebody.setProperties(propMap);
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}

	private List<Image> getImageList(String large_images) {
		List<Image> imgList = new ArrayList<Image>();
		if (StringUtils.isNotBlank(large_images)) {
			if (StringUtils.containsIgnoreCase(large_images, " ")) {
				String[] images = large_images.split(" ");
				for (int i = images.length - 1; i >= 0; i--) {
					String image = StringUtils.EMPTY;
					if (!StringUtils.containsIgnoreCase(images[i], domain)) {
						image = SITE + images[i];
					}
					imgList.add(new Image(image));
				}
			} else {
				if (!StringUtils.containsIgnoreCase(large_images, domain)) {
					large_images = SITE + large_images;
				}
				imgList.add(new Image(large_images));
			}
		}
		return imgList;
	}

	private void getLSelectList(List<LSelectionList> l_selection_list, List<LStyleList> l_style_list, JsonObject obj,
			String unit, Context context, Map<String, Boolean> defalutMap, Map<String, Boolean> displayMap,
			Map<String, String> colorExistMap) {
		LSelectionList lSelection = new LSelectionList();
		LStyleList lStyleList = new LStyleList();
		String skuSize = obj.getAsJsonPrimitive("sku_size").getAsString();
		List<Selection> selections = new ArrayList<Selection>();
		String color_value = "";
		if (obj.has("variation_value")) {
			color_value = obj.getAsJsonPrimitive("variation_value").getAsString();
		}
		boolean colorExistFlag = true;
		if (StringUtils.isNotBlank(color_value)) {
			if (StringUtils.isNotBlank(colorExistMap.get(color_value))) {
				colorExistFlag = false;
			}
		}

		String sku_large_images = obj.getAsJsonPrimitive("large_images").getAsString();
		String sku_list_price = obj.getAsJsonPrimitive("list_price").getAsString();
		// String swatch_image =
		// obj.getAsJsonPrimitive("swatch_image").getAsString();
		String sku_number = obj.getAsJsonPrimitive("sku_number").getAsString();

		String sale_price = "";
		if (obj.has("sale_price")) {
			sale_price = obj.getAsJsonPrimitive("sale_price").getAsString();
		}

		String value_price = "";
		if (obj.has("value_price")) {
			value_price = obj.getAsJsonPrimitive("value_price").getAsString();
		}
		String is_in_stock = "";
		if (obj.has("is_show_add_to_basket")) {
			is_in_stock = obj.getAsJsonPrimitive("is_show_add_to_basket").getAsString();
		}
		int stockStatus = 0;
		if ("true".equals(is_in_stock)) {
			stockStatus = 1;
		}
		lSelection.setGoods_id(sku_number);
		lSelection.setPrice_unit(unit);
		lSelection.setStock_status(stockStatus);

		boolean colorFlag = false;
		if (StringUtils.isBlank(skuSize) && StringUtils.isBlank(color_value)) {
			return;
		} else if (StringUtils.isNotBlank(skuSize) && StringUtils.isNotBlank(color_value)) {
			if (!StringUtils.containsIgnoreCase(color_value, skuSize)) {
				colorFlag = true;
				lSelection.setStyle_id(color_value);
				setLselection(lSelection, skuSize, selections);
				colorExistMap.put(color_value, color_value);
			} else {
				lSelection.setStyle_id("default");
				setLselection(lSelection, skuSize, selections);
			}
		} else if (StringUtils.isNotBlank(skuSize) && StringUtils.isBlank(color_value)) {
			lSelection.setStyle_id("default");
			setLselection(lSelection, skuSize, selections);
		} else if (StringUtils.isBlank(skuSize) && StringUtils.isNotBlank(color_value)) {
			colorFlag = true;
			lSelection.setStyle_id(color_value);
			setLselection(lSelection, skuSize, selections);
			colorExistMap.put(color_value, color_value);
		}

		if (StringUtils.isBlank(sale_price) && StringUtils.isBlank(value_price)
				&& StringUtils.isBlank(sku_list_price)) {
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "skuPrice parse error");
		} else if (StringUtils.isBlank(sale_price) && StringUtils.isBlank(value_price)) {
			lSelection.setOrig_price(Float.valueOf(sku_list_price));
			lSelection.setSale_price(Float.valueOf(sku_list_price));
		} else if (StringUtils.isNotBlank(value_price) && StringUtils.isBlank(sale_price)
				&& StringUtils.isNotBlank(sku_list_price)) {
			lSelection.setSale_price(Float.valueOf(sku_list_price));
			lSelection.setOrig_price(Float.valueOf(value_price));
		} else if (StringUtils.isBlank(value_price) && StringUtils.isNotBlank(sale_price)
				&& StringUtils.isNotBlank(sku_list_price)) {
			lSelection.setSale_price(Float.valueOf(sale_price));
			lSelection.setOrig_price(Float.valueOf(sku_list_price));
		}
		l_selection_list.add(lSelection);

		if (!defalutMap.get("defalut")) {
			if (colorExistFlag) {
				if (colorFlag) {
					lStyleList.setStyle_id(color_value);
					lStyleList.setStyle_name(color_value);
				} else {
					lStyleList.setStyle_id("default");
					lStyleList.setStyle_name("default");
					defalutMap.put("defalut", true);
				}
				lStyleList.setGood_id(sku_number);
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_switch_img("");
				if (displayMap.get("display")) {
					lStyleList.setDisplay(true);
					displayMap.put("display", false);
				}
				lStyleList.setStyle_cate_name("color");
				List<Image> skuImgList = getImageList(sku_large_images);
				context.getUrl().getImages().put(sku_number, skuImgList);// picture
				l_style_list.add(lStyleList);
			}
		}
	}

	private void setLselection(LSelectionList lSelection, String sizeVal, List<Selection> selections) {
		if (StringUtils.isNotBlank(sizeVal)) {
			Selection selection = new Selection();
			selection.setSelect_name("size");
			selection.setSelect_value(sizeVal);
			selections.add(selection);
		}

		lSelection.setSelections(selections);
	}

	private String crawlerUrl(Context context, String url,String referer) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders(referer)).method(HttpMethod.GET.getValue())
					.resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders(referer)).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

	private  static Map<String, Object> getHeaders(String referer) {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.sephora.com");
		headers.put("referer", referer);
		return headers;
	}
	public static void main(String[] args) throws Exception {
		SephoraUS mk = new SephoraUS();
		Context con = new Context();
		con.setRunInRealTime(true);
		Url url = new Url();
		url.setParentUrl("https://www.sephora.com/lips-makeup");
		con.setUrl(url);
		con.setUrl(new Url(
				"https://www.sephora.com/product/le-rouge-mini-duo-P424999"));
		mk.invoke(con);
	}
}
