package com.haitao55.spider.crawler.core.callable.custom.onlineshoes;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

class OnlineshoesCallable implements Callable<JSONObject> {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private String skuUrl;
	private Url url;
	

	public OnlineshoesCallable(String skuUrl, Url url) {
		this.skuUrl = skuUrl;
		this.url = url;
	}

	@Override
	public JSONObject call() {
		JSONObject jsonObject = new JSONObject();
		try {
			String content = StringUtils.EMPTY;
			String proxyRegionId = url.getTask().getProxyRegionId();
			 if(StringUtils.isBlank(proxyRegionId)){
				 content = Crawler.create().timeOut(15000).url(skuUrl).method(HttpMethod.POST.getValue()).proxy(false).resultAsString();
			 }else{
				 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
						 true);
				 String proxyAddress=proxy.getIp();
				 int proxyPort=proxy.getPort();
				 content = Crawler.create().timeOut(15000).url(skuUrl).proxy(true).method(HttpMethod.POST.getValue()).proxyAddress(proxyAddress)
						 .proxyPort(proxyPort).resultAsString();
			 }
			
			if (StringUtils.isNotBlank(content)) {
				Document  doc = Jsoup.parse(content);
				String  shipping = doc.select("#buying .shipping-text").text();
				String  salePrice = doc.select(".show-for-medium-up #product-naming span span.current").text();
				String  origPrice = doc.select(".show-for-medium-up #product-naming span span.regular").text();
				if(StringUtils.isBlank(salePrice)){
					salePrice = StringUtils.substringBetween(content, "itemprop=\"price\">", "<");
				}
				
				String skuId = StringUtils.substringBetween(content, "\"productId\":\"", "\"");

				String save = StringUtils.EMPTY;

				String unit = getCurrencyValue(salePrice);// 得到货币代码
				salePrice = salePrice.replaceAll("[$ ]", "");

				if (StringUtils.isBlank(replace(origPrice))) {
					origPrice = salePrice;
				}
				origPrice = origPrice.replaceAll("[$ ]", "");
				if (StringUtils.isBlank(replace(salePrice))) {
					salePrice = origPrice;
				}
				if (StringUtils.isBlank(origPrice)
						|| Float.valueOf(replace(origPrice)) < Float.valueOf(replace(salePrice))) {
					origPrice = salePrice;
				}
				if (StringUtils.isBlank(save)) {
					save = Math.round((1 - Float.valueOf(replace(salePrice)) / Float.valueOf(replace(origPrice))) * 100)
							+ "";// discount
				}
				
				List<Image> pics = new ArrayList<Image>();
				Elements imageEs = doc.select("ul#pdp-image-slider li picture img");
				for(Element e : imageEs){
					String image = e.attr("srcset");
					if(StringUtils.isNotBlank(image)){
						pics.add(new Image(image));
					}
				}
				
				String styleId = doc.select("span").attr("data-original-color");
				
				List<String> sizeList = new ArrayList<>();
				Elements esSize = doc.select("ul#size-drop a li .size-box");
				int stock_status = 0;
				if(esSize != null && esSize.size() > 0){
					stock_status = 1;
					for(Element size : esSize){
						String value = size.text();
						if(StringUtils.isNotBlank(value)){
							sizeList.add(value);
						}
					}
				}
				if(StringUtils.isNotBlank(shipping) &&
						StringUtils.containsIgnoreCase(shipping, "This item will be ready")){
					stock_status = 1;
				}

				// sku data package
				jsonObject.put("salePrice", Float.valueOf(salePrice));
				jsonObject.put("origPrice", Float.valueOf(origPrice));
				jsonObject.put("save", Integer.parseInt(save));
				jsonObject.put("unit", unit);
				jsonObject.put("styleId", styleId);
				jsonObject.put("size", sizeList);
				jsonObject.put("skuId", skuId);
				jsonObject.put("instock", stock_status);
				jsonObject.put("pics", pics);
			}
		} catch (Exception e) {
			logger.error("OnlineshoesCallable get sku data error", e);
		}
		return jsonObject;
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

	public String getText(Elements es) {
		if (es != null && es.size() > 0) {
			return es.get(0).text();
		}
		return StringUtils.EMPTY;
	}

	public String getAttr(Elements es, String attrKey) {
		if (es != null && es.size() > 0) {
			return es.get(0).attr(attrKey);
		}
		return StringUtils.EMPTY;
	}

	private String replace(String dest) {
		if (StringUtils.isBlank(dest)) {
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));

	}

}
