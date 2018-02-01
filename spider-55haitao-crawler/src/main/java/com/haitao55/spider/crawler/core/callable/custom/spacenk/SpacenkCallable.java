package com.haitao55.spider.crawler.core.callable.custom.spacenk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class SpacenkCallable implements Callable<JSONObject> {
	private static final String INSTOCK = "In Stock";
	private String request_url;
	private Context context;
	private JSONObject imageJSONObject;

	public SpacenkCallable(String request_url, Context context, JSONObject imageJSONObject) {
		this.request_url = request_url;
		this.context = context;
		this.imageJSONObject = imageJSONObject;
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

	public JSONObject getImageJSONObject() {
		return imageJSONObject;
	}

	public void setImageJSONObject(JSONObject imageJSONObject) {
		this.imageJSONObject = imageJSONObject;
	}

	@Override
	public JSONObject call() throws Exception {
		JSONObject jSONObject = new JSONObject();
		String content = crawler_package(context, request_url);
		Document doc = JsoupUtils.parse(content);
		String product_data = StringUtils
				.trim(StringUtils.substringBetween(content, "window.digitalData.product =", "};"));
		if (StringUtils.isNotBlank(product_data)) {
			JSONObject productJSONObject = JSONObject.parseObject(product_data + "}");
			String skuId = productJSONObject.getString("sku");
			String color = productJSONObject.getString("color");
			String size = productJSONObject.getString("size");
			String orign_price = productJSONObject.getString("unit_price");
			String sale_price = productJSONObject.getString("unit_sale_price");
			jSONObject.put("skuId", skuId);
			jSONObject.put("color", color);
			jSONObject.put("size", size);
			jSONObject.put("orign_price", orign_price);
			jSONObject.put("sale_price", sale_price);

			Elements imagesElements = doc
					.select("div[class=js-slider-for js-carousel] div.product-thumbnails_item a img");
			if (CollectionUtils.isEmpty(imagesElements)) {
				imagesElements = doc.select("div.product-col-1.product-image-container div a>img");
			}
			if (CollectionUtils.isNotEmpty(imagesElements)) {
				List<Image> images = new ArrayList<Image>();
				for (Element element : imagesElements) {
					String image_url = element.attr("src");
					images.add(new Image(image_url));
				}
				String image_param = StringUtils.isBlank(color) ? "default" : color;
				imageJSONObject.put(image_param, images);
			}
		}
		// stock
		int stock_status = 0;
		Elements stockElements = doc.select("div.availability-msg");
		if (CollectionUtils.isNotEmpty(stockElements)) {
			String stock = stockElements.text();
			if (StringUtils.containsIgnoreCase(INSTOCK, stock)) {
				stock_status = 1;
			}
		}
		jSONObject.put("stock_status", stock_status);
		if(stock_status == 0)//过滤无库存的sku，因为这种情况调用接口返回的Color是错误的
			return null;

		return jSONObject;
	}

	private String crawler_package(Context context, String url)
			throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(15000).url(url).method(HttpMethod.GET.getValue()).resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(15000).url(url).method(HttpMethod.GET.getValue()).proxy(true)
					.proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
}
