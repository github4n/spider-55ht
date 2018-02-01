package com.haitao55.spider.crawler.core.callable.custom.columbia;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class ColumbiaCallable implements Callable<JSONArray> {
	private static final String OUT_OF_STOCK = "out-of-stock";
	private String request_url;
	private Context context;

	public ColumbiaCallable(String request_url, Context context) {
		this.request_url = request_url;
		this.context = context;
	}

	public String getRequest_url() {
		return request_url;
	}

	public void setRequest_url(String request_url) {
		this.request_url = request_url;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public JSONArray call() throws Exception {
		String content = crawler_package(context, request_url);

		Document doc = JsoupUtils.parse(content);

		String colorCode = StringUtils.substringBetween(request_url, "variationColor=", "&");

		JSONArray skuJSONArray = new JSONArray();

		// 对应size
		Elements sizeElements = doc.select("ul.variationboxes.variationsize li a");
		if (CollectionUtils.isNotEmpty(sizeElements)) {
			for (Element element : sizeElements) {
				String size = element.attr("xx-vv-id");
				String stockFlag = element.attr("class");
				int stockStatus = 1;
				if (StringUtils.containsIgnoreCase(stockFlag, OUT_OF_STOCK)) {
					stockStatus = 0;
				}
				String dataLgimg = element.attr("data-lgimg");
				String gender = StringUtils.substringBetween(dataLgimg, "title\":\"", "\"");

				// price
				String salePrice = StringUtils.EMPTY;
				String orignPrice = StringUtils.EMPTY;
				Elements saleElements = doc.select("div.product-price span.reg-price");
				Elements orignElements = null;
				if (CollectionUtils.isEmpty(saleElements)) {
					saleElements = doc.select("div.product-price span.price-sales");
					orignElements = doc.select("div.product-price span.price-standard");
				}
				if (CollectionUtils.isNotEmpty(saleElements)) {
					salePrice = saleElements.text();
				}
				if (CollectionUtils.isNotEmpty(orignElements)) {
					orignPrice = orignElements.text();
				}
				if (StringUtils.isBlank(orignPrice)) {
					orignPrice = salePrice;
				}
				salePrice = salePrice.replaceAll("[$, ]", "");
				orignPrice = orignPrice.replaceAll("[$, ]", "");
				if (Float.parseFloat(orignPrice) < Float.parseFloat(salePrice)) {
					orignPrice = salePrice;
				}

				JSONObject skuJSONObject = new JSONObject();
				skuJSONObject.put("skuId", colorCode+size);
				skuJSONObject.put("size", size);
				skuJSONObject.put("colorCode", colorCode);
				skuJSONObject.put("stock_status", stockStatus);
				skuJSONObject.put("gender", gender);
				skuJSONObject.put("salePrice", salePrice);
				skuJSONObject.put("orignPrice", orignPrice);

				skuJSONArray.add(skuJSONObject);
			}
		}

		return skuJSONArray;
	}

	private String crawler_package(Context context, String url)
			throws ClientProtocolException, HttpException, IOException {
		Map<String, Object> headers = new HashMap<String, Object>();
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(15000).url(url).header(headers).method(HttpMethod.GET.getValue())
					.resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(15000).url(url).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

}
