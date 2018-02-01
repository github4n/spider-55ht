package com.haitao55.spider.crawler.core.callable.custom.gnc;



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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.JsoupUtils;


public class GncCallaber implements Callable<JSONObject> {
	private static final Logger logger = LoggerFactory.getLogger(GncCallaber.class);
	
	private JSONObject paramJsonObject;
	private Url url;

	public GncCallaber(JSONObject paramJsonObject , Url url) {
		super();
		this.paramJsonObject = paramJsonObject;
		this.url = url;
	}

	public GncCallaber() {
		super();
	}
	
	@Override
	public JSONObject call(){
		JSONObject jsonObject = new JSONObject();

		String address = paramJsonObject.getString("url");
		try {
			String content = crawler_result(url, address);

			Document doc = JsoupUtils.parse(content);

			String colorVal = StringUtils.EMPTY;
			String sizeVal = StringUtils.EMPTY;
			String attributes = doc.select(".product-variations").attr("data-attributes");
			if(StringUtils.isNotBlank(attributes)){
				String value = StringUtils.substringBetween(attributes, "displayValue\":\"", "\"");
				String sizeKey = StringUtils.substringBetween(attributes, "displayName\":\"", "\"");
				if(StringUtils.isNotBlank(sizeKey) && 
						(StringUtils.containsIgnoreCase(sizeKey, "Count") || 
								StringUtils.containsIgnoreCase(sizeKey, "size")) ){
					sizeVal = value;
				}else{
					colorVal = value;
				}
			}
			// skuId
			String skuId = doc.select(".product-number span").text();
			// stock status flag
			int stock_status = 0;
			String stock_status_flag = doc.select("#add-to-cart").text();
			if (StringUtils.equalsIgnoreCase(stock_status_flag, "Add to Cart")) {
				stock_status = 1;
			}

			String salePrice = doc.select(".product-content-container .product-price span.price-sales").text();
			String origPrice = doc.select(".product-content-container .product-price span.price-standard").text()
					.trim();
			
			if(StringUtils.isBlank(salePrice)){
				salePrice = doc.select(".product-content-container .product-price span.price-saleprice").text();
			}

			if (StringUtils.isNotBlank(salePrice)) {
				salePrice = salePrice.replaceAll("[$,]", "");
			}
			if (StringUtils.isBlank(origPrice)) {
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[$,]", "");

			// images
			Elements elements = doc.select("img.productthumbnail");
			List<Image> images = new ArrayList<Image>();
			if (CollectionUtils.isNotEmpty(elements)) {
				for (Element element : elements) {
					String json_image = element.attr("data-lgimg");
					if (StringUtils.isNotBlank(json_image)) {
						String image_url = StringUtils.substringBetween(json_image, "url\":\"", "\",");
						images.add(new Image(image_url));
					}
				}
			}
			
			if(CollectionUtils.isEmpty(images)){
				String src = doc.select("img.primary-image").attr("src");
				if(StringUtils.isNotBlank(src)){
					images.add(new Image(src));
				}
			}
			

			// return jsonObject
			jsonObject.put("color", colorVal);
			jsonObject.put("size", sizeVal);
			jsonObject.put("skuId", skuId);
			jsonObject.put("stock_status", stock_status);
			jsonObject.put("sale_price", salePrice);
			jsonObject.put("orign_price", origPrice);
			jsonObject.put("images", images);

		} catch (HttpException | IOException e) {
			logger.error("GncCallaber request url error , url: {} ,  exception:{} ", address, e);
		}

		return jsonObject;

	}

	/**
	 * 线上爬取
	 * @param url
	 * @param path
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	private String crawler_result(Url url, String path) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = url.getTask().getProxyRegionId();
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(30000).url(path).method(HttpMethod.GET.getValue()).resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(30000).url(path).method(HttpMethod.GET.getValue()).proxy(true)
					.proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
}
