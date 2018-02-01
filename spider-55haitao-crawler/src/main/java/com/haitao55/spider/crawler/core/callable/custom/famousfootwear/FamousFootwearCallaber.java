package com.haitao55.spider.crawler.core.callable.custom.famousfootwear;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;

public class FamousFootwearCallaber implements Callable<JSONArray> {
	private static final String item_url = "http://www.famousfootwear.com/product/data?p=";
	private static final String switch_image_flag = "Right";
	private static final String preffix = "http://www.famousfootwear.com";
	private static final String suffix = "?trim.threshold=105&height=48&width=56&anchor=bottomcenter";
	private static final String size_flag = "men";
	
	private String param;
	private Url url;
	
	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public Url getUrl() {
		return url;
	}

	public void setUrl(Url url) {
		this.url = url;
	}

	public FamousFootwearCallaber(String param, Url url) {
		this.param = param;
		this.url = url;
	}

	@Override
	public JSONArray call() throws Exception {
		//sku JsonArray
		JSONArray skuJsonArray = new JSONArray();
		
		String url_temp = item_url.concat(param);
		
		String content = crawler_result(url,url_temp);
		
		JSONObject skuJsonObject = JSONObject.parseObject(content);
		JSONObject imageJsonObject = skuJsonObject.getJSONObject("metaTags");
		//images
		JSONArray imageJsonArray = imageJsonObject.getJSONArray("og:image");
		List<Image> pics = new ArrayList<Image>();
		if(null != imageJsonArray && imageJsonArray.size() > 0){
			for (Object object : imageJsonArray) {
				pics.add(new Image(object.toString()));
			}
		}
		
		//unit
		String unit = imageJsonObject.getString("og:price:currency");
		
		//color
		JSONObject colorJsonObject = skuJsonObject.getJSONObject("mobileOmniture");
		colorJsonObject = colorJsonObject.getJSONObject("values");
		String color = colorJsonObject.getString("prop8");
		
		//price
		JSONObject productJsonObject = skuJsonObject.getJSONObject("productDetails");
		
		//sale price
		String sale_price = productJsonObject.getString("currentPrice");
		if(StringUtils.isBlank(sale_price)){
			sale_price = imageJsonObject.getString("og:price:amount");
		}
		sale_price = StringUtils.replacePattern(sale_price, "[$, ]", "");
		
		//orign price
		String orign_price = productJsonObject.getString("previousPrice");
		if(StringUtils.isBlank(orign_price) || StringUtils.equals(orign_price, "null")){
			orign_price = StringUtils.EMPTY;
		}
		orign_price = StringUtils.replacePattern(orign_price, "[$, ]", "");
		if(StringUtils.isBlank(orign_price)){
			orign_price = sale_price;
		}
		
		//save
		String save = productJsonObject.getString("percentSaved");
		if(StringUtils.isBlank(save) || StringUtils.equals(save, "null")){
			save = "0";
		}
		
		//switch_image
		JSONArray switchJsonArray = productJsonObject.getJSONArray("images");
		String switch_image_url = StringUtils.EMPTY;
		if(null != switchJsonArray && switchJsonArray.size() > 0){
			for (Object object : switchJsonArray) {
				JSONObject jsonObject = (JSONObject)object;
				String switch_image_flag_temp = jsonObject.getString("imageAngle");
				if(StringUtils.equals(switch_image_flag, switch_image_flag_temp)){
					switch_image_url = jsonObject.getString("imageUrl");
					switch_image_url = preffix.concat(switch_image_url).concat(suffix);
				}
			}
		}
		
		
		//widths
		JSONObject widthsJsonObject = productJsonObject.getJSONObject("widths");
		
		//sizes
		JSONObject sizesJsonObject = productJsonObject.getJSONObject("sizes");
		if(!sizesJsonObject.isEmpty()){
			for (Map.Entry<String, Object> sizeEntry : sizesJsonObject.entrySet()) {
//				String key = sizeEntry.getKey();
				Object value = sizeEntry.getValue();
				JSONObject sizeValueJson = (JSONObject)value;
				String displayName = sizeValueJson.getString("displayName");
				String name = sizeValueJson.getString("name");
				JSONArray sizeJsonArray = sizeValueJson.getJSONArray("values");
				if(null != sizeJsonArray && sizeJsonArray.size()>0){
					for (Object object : sizeJsonArray) {
						JSONObject jsonObject = new JSONObject();
						StringBuffer buffer = new StringBuffer();
						
						JSONObject json = (JSONObject)object;
						
						String size_displayName = json.getString("displayName");
//						String size_name = json.getString("name");
						
						//通过返回的json数据，封装　size,width组合，一般一种类型的size对应的width　　只有一个，
						int stock_status = 1;
						String width_displayName = StringUtils.EMPTY;
						JSONArray jsonArray = json.getJSONArray("widths");
						
						if(null != jsonArray && jsonArray.size() > 0){
							JSONObject widthJsonObject = jsonArray.getJSONObject(0);
							boolean isAvailableOnline = widthJsonObject.getBooleanValue("isAvailableOnline");
							if(!isAvailableOnline){
								stock_status = 0;
							}
							String widthId = widthJsonObject.getString("widthId");
							
							width_displayName = getWidthDisplayName(name,widthId,widthsJsonObject);
						}
						
						buffer.append(size_displayName).append(" ").append(width_displayName);
						if(!StringUtils.containsIgnoreCase(displayName, size_flag)){
							buffer.append(" ").append(displayName);
						}
						
						jsonObject.put("color", color);
						jsonObject.put("colorId", param);
						jsonObject.put("stock_status", stock_status);
						jsonObject.put("sale_price", sale_price);
						jsonObject.put("orign_price", orign_price);
						jsonObject.put("unit", unit);
						jsonObject.put("save", save);
						jsonObject.put("size", buffer.toString());
						jsonObject.put("images", pics);
						jsonObject.put("switch", switch_image_url);
						
						//add
						skuJsonArray.add(jsonObject);
					}
			    }
			}
			
		}
		
		//不存在size
		else{
			JSONObject jsonObject = new JSONObject();
			
			int stock_status = 1;
			boolean is_out_of_stock = productJsonObject.getBooleanValue("isOutOfStock");
			if(is_out_of_stock){
				stock_status = 0;
			}
			
			jsonObject.put("color", color);
			jsonObject.put("colorId", param);
			jsonObject.put("stock_status", stock_status);
			jsonObject.put("sale_price", sale_price);
			jsonObject.put("orign_price", orign_price);
			jsonObject.put("unit", unit);
			jsonObject.put("save", save);
//					jsonObject.put("size", buffer.toString());
			jsonObject.put("images", pics);
			jsonObject.put("switch", switch_image_url);
			
			//add
			skuJsonArray.add(jsonObject);
		}
		
		return skuJsonArray;
	}

	/**
	 * 获取　width displayName
	 * @param name
	 * @param widthId 
	 * @param widthsJsonObject
	 * @return
	 */
	private String getWidthDisplayName(String name, String widthId, JSONObject widthsJsonObject) {
		String str = StringUtils.EMPTY;
		
		if(null != widthsJsonObject){
			// 遍历width　　封装width属性
			JSONObject valueJson = widthsJsonObject.getJSONObject(name);
//				String width_displayName = valueJson.getString("displayName");
//				String width_name = valueJson.getString("name");
				JSONArray widthsJsonArray = valueJson.getJSONArray("values");
				if(null != widthsJsonArray && widthsJsonArray.size()>0){
					for (Object object : widthsJsonArray) {
						
						JSONObject json = (JSONObject)object;
						String value_displayName = json.getString("displayName");
						String value_widthId = json.getString("widthId");
						if(StringUtils.equals(widthId, value_widthId)){
							str = value_displayName;
							break ;
						}
					}
				}
			
		}
		return str;
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
			 content = Crawler.create().timeOut(15000).url(path).proxy(false).resultAsString();
		 }else{
			 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
					 true);
			 String proxyAddress=proxy.getIp();
			 int proxyPort=proxy.getPort();
			 content = Crawler.create().timeOut(15000).url(path).proxy(true).proxyAddress(proxyAddress)
					 .proxyPort(proxyPort).resultAsString();
		 }
		 return content;
	}

}
