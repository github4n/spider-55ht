package com.haitao55.spider.crawler.core.callable.custom.swarovski;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.common.collect.Lists;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.JsoupUtils;


public class SwarovskiCallaber implements Callable<JSONObject> {
	private static final Logger logger = LoggerFactory.getLogger(SwarovskiCallaber.class);
	
	private static final String stock_status_flag = "in stock";
	private String swar_api = "https://www.swarovski.com/Web_US/en/product?VariationSelected=true&SKU=";
	private JSONObject paramJsonObject;
	private Url url;

	public SwarovskiCallaber(JSONObject paramJsonObject , Url url) {
		super();
		this.paramJsonObject = paramJsonObject;
		this.url = url;
	}

	public SwarovskiCallaber() {
		super();
	}


	@SuppressWarnings("deprecation")
	@Override
	public JSONObject call(){
		JSONObject jsonObject = new JSONObject();
		try{
		String address = paramJsonObject.getString("url");
		String tempUrl = address;
		if(StringUtils.isNotBlank(address)){
			String encodeUrl = URLEncoder.encode(address);
			String rpUrl = encodeUrl.replace("%C2%A0", "");
			address = URLDecoder.decode(rpUrl);
			logger.info("handle Swarovski Url ::::: afterUrl :{}, boferUrl :{}",tempUrl,address);
		}
		String switch_image_url = paramJsonObject.getString("switch_image");
		String color = paramJsonObject.getString("color");
		
		try {
			String content = crawler_result(url,address);
			
			Document doc = JsoupUtils.parse(content);

			//skuId
			String skuId = StringUtils.substringBetween(content, "'id': '", "',");
			String notAvailable = doc.select(".product-not-available p").text();
			boolean flag = false;
			if(StringUtils.isNotBlank(notAvailable)){
				if(StringUtils.containsIgnoreCase(notAvailable, "not available")){
					flag = true;
				}
			}
			//stock status flag
			String stock_status_flag_temp = StringUtils.substringBetween(content, "<meta property=\"og:availability\" content=\"", "\" />");
			int stock_status = 1;
			if(!StringUtils.equalsIgnoreCase(stock_status_flag, stock_status_flag_temp) || flag){
				stock_status = 0;
			}
			//unit
			String unit = StringUtils.substringBetween(content, "'currencyCode':'", "',");
			//sale_price
			String sale_price = StringUtils.substringBetween(content, "'price': '", "'");
			//orign_price
			String orign_price = StringUtils.substringBetween(content, "'metric1': '", "',");
			
			String save=StringUtils.EMPTY;
			sale_price = sale_price.replaceAll("[ ,]", "");
			orign_price = orign_price.replaceAll("[ ,]", "");
			
			if (StringUtils.isBlank(orign_price)) {
				orign_price = sale_price;
			}
			if (StringUtils.isBlank(sale_price) || StringUtils.equals(sale_price, "0.00")) {
				sale_price = orign_price;
			}
			if (StringUtils.isBlank(orign_price)
					|| Float.valueOf(orign_price) < Float.valueOf(sale_price)) {
				orign_price = sale_price;
			}
			if (StringUtils.isBlank(save)) {
				save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100)
						+ "";// discount
			}
			
			//images
			Elements elements = doc.select("ul.thumbnails.clearfix li a img");
			List<Image> list = new ArrayList<Image>();
			if(CollectionUtils.isNotEmpty(elements)){
				for (Element element : elements) {
					String image_url = element.attr("data-elevatezoomlargeimg");
					if(StringUtils.isNotBlank(image_url)){
						list.add(new Image(image_url));
					}
				}
			}
			
			Map<String,Integer> sizeMap = new HashMap<>();
			Elements es = doc.select("select#variation option");
			for(Element e : es){
				String value = e.text();
				String skuValue = e.attr("value");
				if(StringUtils.containsIgnoreCase(value, "Select size")){
					continue;
				}
				String sizeHtml = crawler_result(url,swar_api+skuValue);
				int size_stock_status = 1;
				if(StringUtils.isNotBlank(sizeHtml)){
					Document sizeDoc = JsoupUtils.parse(sizeHtml);
					String sizeNotAvailable = sizeDoc.select(".product-not-available p").text();
					boolean size_flag = false;
					if(StringUtils.isNotBlank(sizeNotAvailable)){
						if(StringUtils.containsIgnoreCase(sizeNotAvailable, "not available")){
							size_flag = true;
						}
					}
					//stock status flag
					String size_stock_status_flag_temp = StringUtils.substringBetween(sizeHtml, "<meta property=\"og:availability\" content=\"", "\" />");
					if(!StringUtils.equalsIgnoreCase(stock_status_flag, size_stock_status_flag_temp) || size_flag){
						size_stock_status = 0;
					}
					
				}
				sizeMap.put(value, size_stock_status);
			}
			
			//return jsonObject
			jsonObject.put("color", color);
			jsonObject.put("switch_image", switch_image_url);
			jsonObject.put("skuId", skuId);
			jsonObject.put("stock_status", stock_status);
			jsonObject.put("sale_price", sale_price);
			jsonObject.put("orign_price", orign_price);
			jsonObject.put("save", save);
			jsonObject.put("unit", unit);
			jsonObject.put("images", list);
			jsonObject.put("sizes", sizeMap);
			
		} catch (HttpException | IOException e) {
			logger.error("SwarovskiCallaber request url error , url: {} ,  exception:{} ",address,e);
		}
		} catch(Exception e){
			logger.error("got error while crawler");
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
		String proxyRegionId = url.getTask().getProxyRegionId();
		String content = StringUtils.EMPTY;
		 if(StringUtils.isBlank(proxyRegionId)){
			 content = Crawler.create().timeOut(30000).url(path).proxy(false).resultAsString();
		 }else{
			 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
					 true);
			 String proxyAddress=proxy.getIp();
			 int proxyPort=proxy.getPort();
			 content = Crawler.create().timeOut(30000).url(path).proxy(true).proxyAddress(proxyAddress)
					 .proxyPort(proxyPort).resultAsString();
		 }
		 return content;
	}
	
}
