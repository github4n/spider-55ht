package com.haitao55.spider.crawler.core.callable.custom.barneyscom;

import java.io.IOException;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class BarneysCallaber implements Callable<JSONArray> {
	private static final String url = "http://www.barneys.com/browse/ajaxProductDetailDisplay.jsp";
	private static final String stockFLag = "In Stock";
	private JSONObject param;
	private JSONObject imageJSONObject;
	private String currentUrl;
	private Context context;

	public BarneysCallaber(JSONObject param, JSONObject imageJSONObject, String currentUrl , Context context) {
		super();
		this.param = param;
		this.imageJSONObject = imageJSONObject;
		this.currentUrl = currentUrl;
		this.context = context;
	}


	@Override
	public JSONArray call() throws Exception {
		JSONArray jsonArray = new JSONArray();
		
		String content = crawler_package(url,param,currentUrl,context);
		
		Document doc = JsoupUtils.parse(content);
		
		//productId
		String productId = StringUtils.EMPTY;
		
		//images
		List<Image> images = new ArrayList<Image>();
		Elements imageElements = doc.select("div.atg_store_productImage a img");
		if(CollectionUtils.isNotEmpty(imageElements)){
			for (Element element : imageElements) {
				String image_url = element.attr("src");
				images.add(new Image(image_url));
			}
		}
		
		Elements sizeElements = doc.select("span[class=selector] a");
		if(CollectionUtils.isNotEmpty(sizeElements)){
			for (Element element : sizeElements) {
				JSONObject jsonObject = new JSONObject();
				String product_id_temp = element.attr("data-productid");
				if(StringUtils.isNotBlank(product_id_temp)){
					productId = product_id_temp;
				}
				String skuId = element.attr("data-skuid");
				String sale_price = element.attr("data-sale-price");
				String orign_price = element.attr("data-list-price");
				String color = element.attr("data-vendorcolor");
				String size = element.text();
				
				int stock_status = 0;
				String invStatus = element.attr("data-onhand-quantity");
				if(Integer.parseInt(invStatus) > 0 ){
					stock_status = 1;
				}
				jsonObject.put("sku", skuId);
				jsonObject.put("color", color);
				jsonObject.put("size", size);
				jsonObject.put("stock_status", stock_status);
				jsonObject.put("productId", product_id_temp);
				jsonObject.put("sale_price", sale_price);
				jsonObject.put("orign_price", orign_price);
				
				//add
				jsonArray.add(jsonObject);
			}
		}
		//商品只有颜色属性
		else{
			JSONObject jsonObject = new JSONObject();
			Elements elements = doc.select("span#add_item_cart_fp");
			String product_id_temp = elements.attr("data-fpproductid");
			if(StringUtils.isNotBlank(product_id_temp)){
				productId = product_id_temp;
			}
			String skuId = elements.attr("data-fpskuid");
			String color = elements.attr("data-fpcolor");
			String size = "1 SZ";
			
			Elements stockElements = doc.select("input#atg_behavior_addItemToCart");
			String invStatus = stockElements.attr("value");
			int stock_status = 0;
			if(StringUtils.containsIgnoreCase(stockFLag, invStatus)){
				stock_status = 1;
			}
			
			elements = doc.select("div.atg_store_productPrice div.red-discountPrice");
			//没有折扣
			if(CollectionUtils.isEmpty(elements)){
				elements = doc.select("div.atg_store_productPrice");
				String price = elements.text();
				price = StringUtils.replacePattern(price, "[$, ]", "");
				jsonObject.put("sale_price", price);
				jsonObject.put("orign_price", price);
			}
			
			//存在折扣
			else{
				elements = doc.select("div.atg_store_productPrice");
				Elements salePriceElements = elements.select("div.red-discountPrice");
				Elements orignPriceElements = elements.select("del.red-strike");
				String sale_price = salePriceElements.text();
				String orign_price = orignPriceElements.text();
				sale_price = StringUtils.replacePattern(sale_price, "[$, ]", "");
				orign_price = StringUtils.replacePattern(orign_price, "[$, ]", "");
				jsonObject.put("sale_price", sale_price);
				jsonObject.put("orign_price", orign_price);
			}
			
			jsonObject.put("sku", skuId);
			jsonObject.put("color", color);
			jsonObject.put("size", size);
			jsonObject.put("stock_status", stock_status);
			jsonObject.put("productId", product_id_temp);
			
			
			
			//add
			jsonArray.add(jsonObject);
		}
		
		//image jsonobject
		imageJSONObject.put(productId, images);
		
		return jsonArray;
	}

	
	/**
	 * 对应颜色 sku 数据封装
	 * @param url
	 * @param param
	 * @param currentUrl 
	 * @return
	 * @throws NumberFormatException
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	private static String crawler_package(String url, JSONObject param, String currentUrl,Context context) throws NumberFormatException, ClientProtocolException, HttpException, IOException{
		
		String productId = param.getString("colorId");
		String data_picker = param.getString("data_picker");
		
		//post 数据
		Map<String,Object> payload = new HashMap<String,Object>();
		payload.put("productId", productId);
		payload.put("picker", data_picker);
		payload.put("changeStyle", true);
		payload.put("isLandingPage", true);
		
		//headers 数据
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		
		String content = StringUtils.EMPTY;
		
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).method(HttpMethod.POST.name()).payload(payload).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress("172.16.7.190").proxyPort(24000).resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).method(HttpMethod.POST.name()).payload(payload).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress("172.16.7.190").proxyPort(24000).resultAsString();
		}
		
		
		return content;
	}
	

	public JSONObject getParam() {
		return param;
	}


	public void setParam(JSONObject param) {
		this.param = param;
	}


	public JSONObject getImageJSONObject() {
		return imageJSONObject;
	}


	public void setImageJSONObject(JSONObject imageJSONObject) {
		this.imageJSONObject = imageJSONObject;
	}


	public String getCurrentUrl() {
		return currentUrl;
	}


	public void setCurrentUrl(String currentUrl) {
		this.currentUrl = currentUrl;
	}

	
	
}
