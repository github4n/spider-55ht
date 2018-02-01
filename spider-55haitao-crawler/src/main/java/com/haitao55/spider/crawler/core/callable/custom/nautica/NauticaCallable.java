package com.haitao55.spider.crawler.core.callable.custom.nautica;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class NauticaCallable implements Callable<JSONObject> {
	private static final String STOCK_STATUS_FLAG = "in stock";
	private String requestUrl;
	private Context context;

	public NauticaCallable(String requestUrl, Context context) {
		super();
		this.requestUrl = requestUrl;
		this.context = context;
	}

	@Override
	public JSONObject call() throws Exception {
		JSONObject jsonObject = new JSONObject();
		try {
			String content = crawler_package(context, requestUrl);
			Document doc = JsoupUtils.parse(content);

			String colorId = StringUtils.substringBetween(requestUrl, "_color=", "&");
			String sizeId = StringUtils.substringBetween(requestUrl, "_size=", "&");

			// stock status
			int stockStatus = 0;
			Elements stockElements = doc.select("p.in-stock-msg");
			if (CollectionUtils.isNotEmpty(stockElements)) {
				String stock = stockElements.text();
				if (StringUtils.equalsIgnoreCase(STOCK_STATUS_FLAG, stock)) {
					stockStatus = 1;
				}
			}

			// price
			Elements saleElements = doc.select("div.product-price span.price-sales");
			Elements orignElements = doc.select("div.product-price span.price-standard");
			String salePrice = StringUtils.EMPTY;
			String orignPrice = StringUtils.EMPTY;
			if (CollectionUtils.isNotEmpty(saleElements)) {
				salePrice = saleElements.text();
			}
			if (CollectionUtils.isNotEmpty(orignElements)) {
				orignPrice = orignElements.text();
			}

			if (StringUtils.isBlank(orignPrice)) {
				orignPrice = salePrice;
			}
			// unit
			String unit = getCurrencyValue(salePrice);

			salePrice = StringUtils.replacePattern(salePrice, "[$, ]", "");
			orignPrice = StringUtils.replacePattern(orignPrice, "[$, ]", "");

			jsonObject.put("skuId", colorId + sizeId);
			jsonObject.put("colorId", colorId);
			jsonObject.put("sizeId", sizeId);
			jsonObject.put("stock_status", stockStatus);
			jsonObject.put("salePrice", salePrice);
			jsonObject.put("orignPrice", orignPrice);
			jsonObject.put("unit", unit);

		} catch (HttpException e) {
		} catch (Exception e) {
		}
		return jsonObject;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	
	private String crawler_package(Context context,String url) throws ClientProtocolException, HttpException, IOException {
		Map<String,Object> headers = new HashMap<String,Object>();
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(url).header(headers).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(url).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
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

}
