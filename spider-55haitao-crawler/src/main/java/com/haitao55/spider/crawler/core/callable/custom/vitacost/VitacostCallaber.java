package com.haitao55.spider.crawler.core.callable.custom.vitacost;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

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
import com.google.common.collect.Lists;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.JsoupUtils;


public class VitacostCallaber implements Callable<JSONObject> {
	private static final Logger logger = LoggerFactory.getLogger(VitacostCallaber.class);
	private static final String BASEW_DOMAIN = "https://www.vitacost.com";

	private JSONObject paramJsonObject;
	private Url url;

	public VitacostCallaber(JSONObject paramJsonObject , Url url) {
		super();
		this.paramJsonObject = paramJsonObject;
		this.url = url;
	}

	public VitacostCallaber() {
		super();
	}


	@Override
	public JSONObject call(){
		JSONObject jsonObject = new JSONObject();
		
		String address = paramJsonObject.getString("url");
		String value = paramJsonObject.getString("value");
		try {
			String content = crawler_result(url,address);
			
			if(StringUtils.isNotBlank(content)){
				Document doc = JsoupUtils.parse(content);
				Elements pdpEs = doc.select("#pdpVariations li");
				Map<String,Integer> flavorMap = new HashMap<>();
				if(pdpEs != null && pdpEs.size() > 1){
					for (Element e : pdpEs) {
						String label = e.select("label").text();
						if (StringUtils.containsIgnoreCase(label, "Flavor")) {
							Elements flavorUrls  = e.select("select option");
							for(Element flavor : flavorUrls){
								String flavorText = flavor.text();
								String flavorUrl = flavor.attr("value");
								if(StringUtils.isNotBlank(flavorText) && StringUtils.isNotBlank(flavorUrl)){
									String skuHtml = crawler_result(url,BASEW_DOMAIN+flavorUrl);
									if(StringUtils.isNotBlank(skuHtml)){
										Document skuDoc = Jsoup.parse(skuHtml);
										int stock_status = 0;
										String instock = skuDoc.select(".pBuyMsgOOS").text();
										if(!StringUtils.equalsIgnoreCase(instock, "Out of stock")){
											stock_status = 1;
										}
										flavorMap.put(flavorText, stock_status);
									}
								}
							}
						}
					}
				}
				
				String skuId = doc.select("#bb-productID").attr("value");
				String origPrice = StringUtils.substringBetween(content, "Retail price: $", "<");
				String salePrice = StringUtils.substringBetween(content, "vPPrice = '", "'");
				if (StringUtils.isBlank(origPrice)) {
					origPrice = salePrice;
				}

				if (StringUtils.isNotBlank(origPrice)) {
					origPrice = origPrice.replaceAll("[$,]", "");
				}
				int stock_status = 0;
				String instock = doc.select(".pBuyMsgOOS").text();
				if(!StringUtils.equalsIgnoreCase(instock, "Out of stock")){
					stock_status = 1;
				}
				
				List<Image> images = new ArrayList<Image>();
				String image = doc.select("#productImage a img").attr("src");
				if(StringUtils.isNotBlank(image)){
					images.add(new Image(BASEW_DOMAIN+image));
				}
				
				//return jsonObject
				jsonObject.put("flavor", flavorMap);
				jsonObject.put("skuId", skuId);
				jsonObject.put("stock_status", stock_status);
				jsonObject.put("sale_price", salePrice);
				jsonObject.put("orign_price", origPrice);
				jsonObject.put("images", images);
				jsonObject.put("size", value);
				
			}
		} catch (HttpException | IOException e) {
			logger.error("VitacostCallaber request url error , url: {} ,  exception:{} ",address,e);
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
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(path).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(path).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		 return content;
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.vitacost.com");
		return headers;
	}
}
