package com.haitao55.spider.crawler.core.callable.custom.shopbop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
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
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

/**
 * ShopBop网站收录
 * 
 * @author denghuan
 *
 */
public class ShopBop extends AbstractSelect {

	private static final String domain = "cn.shopbop.com";

	private String crawlerUrl(Context context, String url) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

	private static Map<String, Object> getHeaders() {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put(":authority", "cn.shopbop.com");
		headers.put("Cookie",
				"optimizelySegments=%7B%222007780185%22%3A%22none%22%2C%222009630327%22%3A%22false%22%2C%222017280295%22%3A%22gc%22%2C%222018080123%22%3A%22referral%22%2C%228294042270%22%3A%22true%22%2C%228296461724%22%3A%22true%22%7D; optimizelyEndUserId=oeu1503047687934r0.7664023901980996; optimizelyBuckets=%7B%7D; bopVisitorData=\"H4sIAAAAAAAAAEsuyYjPTLE1NDIzszA1NzY2MjMHsg3MdQqKUtOcS4uKUvOSK21Dg13AAj6JeemliemptlUZOmUgfUAdxibGxoZG5hYQHfmleSVFlbbOfijq/dP8izLTM/NA+pCUwYWd/QDjBJ5TiAAAAA==\"; bopsecure=\"r0qfzT9NZXbFh96Omw4g0uGvz7o=\"; ASESSIONID=320EA606ABE4B80326DEE43517DFD7B3-n3; csm-sid=376-4351231-2334622; com.shopbop.capabilities:js=true; authState=anonymous; s_pers=%20s_fid%3D62E5A043D541E691-097606FB3F0D84CF%7C1574329143075%3B%20s_visit%3D1%7C1511258943099%3B; s_sess=%20s_v5%3D30%2525%2520Off%3B%20s_cc%3Dtrue%3B%20s_v11%3Dzh%3B%20s_sq%3D%3B");
		return headers;
	}

	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
		content = luminatiHttpClient.request(context.getCurrentUrl(), getHeaders());
		
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String exisStock = StringUtils.substringBetween(content, "productPage.isInStock = '", "';");
			String defalueColor = doc.select("span.selectedColorLabel").text();
			Map<String, String> saleMap = new HashMap<String, String>();

			String pricingData = doc.select(".product-pricing-data").text();
			if (StringUtils.isNotBlank(pricingData)) {
				String pdp = StringUtils.substringBetween(content, "data-key=\"pdp.state\">", "</script>");
				if (StringUtils.isNotBlank(pdp)) {
					JSONObject jsonObject = JSONObject.parseObject(pdp);
					JSONObject productJson = jsonObject.getJSONObject("product");
					JSONArray styleJsonArr = productJson.getJSONArray("styleColors");
					for (int i = 0; i < styleJsonArr.size(); i++) {
						JSONObject styleJsonObj = styleJsonArr.getJSONObject(i);
						JSONObject colorJson = styleJsonObj.getJSONObject("color");
						String label = colorJson.getString("label");
						String spuOrigPrice = StringUtils.substringBetween(styleJsonObj.toString(), "retailAmount\":",
								",");
						String spuSalePrice = StringUtils.substringBetween(styleJsonObj.toString(), "saleAmount\":",
								",");
						if (StringUtils.isNotBlank(label) && StringUtils.isNotBlank(spuSalePrice)) {
							if (StringUtils.isNotBlank(spuOrigPrice)) {
								saleMap.put(label, spuSalePrice + "_" + spuOrigPrice);
							} else {
								saleMap.put(label, spuSalePrice);
							}
						}
					}
				}
			}
			
			String origPrice = doc.select("#productPrices .priceBlock span.originalRetailPrice").text();
			Elements salePriceEs = doc.select("#productPrices .priceBlock");
			String salePrice = StringUtils.EMPTY;
			if (StringUtils.isBlank(origPrice) && salePriceEs.size() <= 1) {
				origPrice = StringUtils.substringBetween(content, "productPage.listPrice = '", "';");
				salePrice = StringUtils.substringBetween(content, "productPage.sellingPrice = '", "';");
			} else {
				for (int i = 0; i < salePriceEs.size(); i++) {
					String price = salePriceEs.get(i).select("span.salePrice").text();
					if (StringUtils.isBlank(price)) {
						price = salePriceEs.get(i).select("span.regularPrice").text();
					}
					String color = salePriceEs.get(i).select("span.priceColors").text();
					if (StringUtils.isNotBlank(price) && StringUtils.isNotBlank(color)) {
						price = price.replaceAll("[,: ]", "");
						price = pattern(price);
						saleMap.put(color, price);
						if (StringUtils.containsIgnoreCase(color, defalueColor)) {
							salePrice = price;
						}
					}
				}
			}
			if (StringUtils.isNotBlank(origPrice)) {
				origPrice = origPrice.replaceAll("[, ]", "");
				origPrice = pattern(origPrice);
			}

			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			
			String title = doc.select("div.productTitle").text();
			String brand = doc.select("span.brand-name").text();
			String productDetail = StringUtils.substringBetween(content, "var productDetail =", ";");
			String unit = StringUtils.substringBetween(content, "currency\":\"", "\"");
			String productId = StringUtils.substringBetween(content, "productPage.productId = '", "'");

			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));

			if (StringUtils.isNotBlank(origPrice) && StringUtils.isNotBlank(salePrice)) {
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
			} else if (StringUtils.isBlank(origPrice) && StringUtils.isNotBlank(salePrice)) {
				rebody.setPrice(new Price(Float.parseFloat(salePrice), 0, Float.parseFloat(salePrice), unit));
			}

			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();

			Elements cates = doc.select(".bread-crumb-list li a span");
			for (Element e : cates) {
				String cat = e.text();
				if (StringUtils.isNotBlank(cat)) {
					cats.add(cat);
					breads.add(cat);
				}
			}
			if (CollectionUtils.isEmpty(cats)) {
				cats.add(title);
				breads.add(title);
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);

			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String descHtml = StringUtils.substringBetween(content, "itemprop=\"description\">", "</div>");
			String description = Jsoup.parse(descHtml).text();
			String[] sp = descHtml.split("<br>");
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			int count = 1;
			if (sp != null) {
				for (int i = 0; i < sp.length; i++) {
					String attr = sp[i];
					if (StringUtils.isNotBlank(attr) && StringUtils.containsIgnoreCase(attr, ":")) {
						featureMap.put("feature-" + count, attr);
						count++;
						String key = StringUtils.trim(StringUtils.substringBefore(attr, ":"));
						String value = StringUtils.trim(StringUtils.substringAfter(attr, ":"));
						if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
							propMap.put(key, value);
						}
					}
				}
			}
			rebody.setProperties(propMap);

			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);

			if(StringUtils.isNotBlank(productDetail)){
				JSONObject jsonObject = JSON.parseObject(productDetail);
				String colors = jsonObject.getString("colors");
				String sizes = jsonObject.getString("sizes");
				JSONObject jsonObjectColors = JSON.parseObject(colors);
				JSONObject jsonObjectSizes = JSON.parseObject(sizes);
				Set<String> setSzie = jsonObjectSizes.keySet();
				Set<String> colorSet = jsonObjectColors.keySet();

				Sku sku = new Sku();
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				Map<String, String> skuMap = new HashMap<>();
				for (String key : colorSet) {
					String colorKey = jsonObjectColors.getString(key);
					JSONObject jsonObjectkey = JSON.parseObject(colorKey);
					String colorName = jsonObjectkey.getString("colorName");

					if (CollectionUtils.isNotEmpty(setSzie)) {
						for (String size : setSzie) {
							skuMap.put(key, colorName + size);
							LSelectionList lSelectionList = new LSelectionList();
							String sizeKey = jsonObjectSizes.getString(size);
							JSONObject jsonObjectSize = JSON.parseObject(sizeKey);
							JSONArray jsonArray = jsonObjectSize.getJSONArray("colors");
							boolean isStock = false;
							for (int i = 0; i < jsonArray.size(); i++) {
								String colorValue = jsonArray.get(i).toString();
								if (key.equals(colorValue)) {
									isStock = true;
								}
							}
							List<Selection> selections = new ArrayList<>();
							Selection selection = new Selection();
							lSelectionList.setGoods_id(colorName + size);
							selection.setSelect_name("size");
							selection.setSelect_value(size);
							String price = saleMap.get(colorName);
							if (StringUtils.isNotBlank(price)) {
								if(StringUtils.contains(price, "_")){
									lSelectionList.setSale_price(Float.parseFloat(price.split("_")[0]));
									lSelectionList.setOrig_price(Float.parseFloat(price.split("_")[1]));
								}else{
									lSelectionList.setSale_price(Float.parseFloat(price));
									lSelectionList.setOrig_price(Float.parseFloat(price));
								}
							} else {
								lSelectionList.setSale_price(Float.parseFloat(salePrice));
								lSelectionList.setOrig_price(Float.parseFloat(origPrice));
							}

							lSelectionList.setStyle_id(colorName);
							lSelectionList.setPrice_unit(unit);
							selections.add(selection);
							lSelectionList.setSelections(selections);
							int stockStatus = 0;
							if (isStock) {
								stockStatus = 1;
							}
							lSelectionList.setStock_status(stockStatus);
							l_selection_list.add(lSelectionList);
						}

					}
				}
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
					if ("true".equals(exisStock)) {
						spuStock = 1;
					} else {
						List<Image> imageList = new ArrayList<Image>();
						Elements es = doc.select("#thumbnailList li.thumbnailListItem img");
						for (int i = 0; i < es.size() - 1; i++) {
							String image = es.get(i).attr("src");
							if (StringUtils.isNotBlank(image)) {
								String uxImage = image.replace("UX37", "UX336");
								imageList.add(new Image(uxImage));
							}
						}
						context.getUrl().getImages().put(productId, imageList);// picture
					}
				}
				rebody.setStock(new Stock(spuStock));
				if (spuStock > 0) {
					if (CollectionUtils.isNotEmpty(colorSet)) {
						for (String key : colorSet) {
							LStyleList lStyleList = new LStyleList();
							String colorKey = jsonObjectColors.getString(key);
							JSONObject jsonObjectkey = JSON.parseObject(colorKey);
							String colorName = jsonObjectkey.getString("colorName");
							String images = jsonObjectkey.getString("images");
							JSONObject jsonObjectImages = JSON.parseObject(images);
							Set<String> imagesKey = jsonObjectImages.keySet();
							List<String> slotList = new ArrayList<String>();
							for (String slot : imagesKey) {
								slotList.add(slot);
							}
							Collections.sort(slotList, new Comparator<String>() {
								public int compare(String arg0, String arg1) {
									return arg0.compareTo(arg1);
								}
							});

							List<Image> imageList = new ArrayList<Image>();
							for (String image : slotList) {
								String imageKey = jsonObjectImages.getString(image);
								JSONObject jsonObjectImage = JSON.parseObject(imageKey);
								String img = jsonObjectImage.getString("main");
								imageList.add(new Image(img));
							}

							context.getUrl().getImages().put(skuMap.get(key), imageList);// picture
							lStyleList.setGood_id(skuMap.get(key));
							lStyleList.setStyle_name(colorName);
							lStyleList.setStyle_cate_name("color");
							lStyleList.setStyle_id(colorName);
							lStyleList.setStyle_switch_img("");
							if (StringUtils.isNotBlank(defalueColor) && defalueColor.equals(colorName)) {
								lStyleList.setDisplay(true);
							}
							l_style_list.add(lStyleList);
						}
					}
				}
				sku.setL_selection_list(l_selection_list);
				sku.setL_style_list(l_style_list);
				rebody.setSku(sku);
			}
		}
		setOutput(context, rebody);
	}

	private String pattern(String price) {
		Pattern pattern = Pattern.compile("(\\d+(.\\d+)?)");
		Matcher matcher = pattern.matcher(price);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
