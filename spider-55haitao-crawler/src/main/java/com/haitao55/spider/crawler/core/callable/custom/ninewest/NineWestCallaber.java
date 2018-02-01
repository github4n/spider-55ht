package com.haitao55.spider.crawler.core.callable.custom.ninewest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

public class NineWestCallaber implements Callable<JSONArray> {
	private static final String SKU_JSON_URL = "http://www.ninewest.com/on/demandware.store/Sites-ninewest-Site/default/FluidProduct-GetProductAvailability?pid=()&color={}&returnObject=ProductMaster,ProductOptions,VariationProducts";
	private static final String INSTOCK = "IN_STOCK";
	private JSONObject paramJSONObject;
	private JSONObject imageJSONObject;
	private Context context;

	public NineWestCallaber(JSONObject paramJSONObject, JSONObject imageJSONObject,Context context) {
		super();
		this.paramJSONObject = paramJSONObject;
		this.imageJSONObject = imageJSONObject;
		this.context = context;
	}

	public NineWestCallaber() {
		super();
	}

	@Override
	public JSONArray call() throws Exception {
		String color = paramJSONObject.getString("color");
		String colorId = paramJSONObject.getString("colorId");
		String productId = paramJSONObject.getString("productId");
		String url = StringUtils.replacePattern(SKU_JSON_URL, "\\(\\)", productId);
		url = StringUtils.replacePattern(url, "\\{\\}", colorId);
		url = StringUtils.replacePattern(url, "[ ]", "%20");
		
		String content = crawler_package(context,url);
//		String content = Crawler.create().url(url).timeOut(15000).proxy(true).proxyAddress("104.196.109.105").proxyPort(3128).resultAsString();
		if(StringUtils.isNotBlank(content)){
			JSONArray jsonArray = new JSONArray();
			//返回content parse to jsonObject
			JSONObject parseObject = JSONObject.parseObject(content);
			//有效数据 json
			JSONObject responseJSONObject = parseObject.getJSONObject("response");
			//sku 相关数据
			JSONObject productMasterJSONObject = responseJSONObject.getJSONObject("productMaster");
			//图片json
			JSONObject skuImageJSONObject = productMasterJSONObject.getJSONObject("defaultImage");
			//sku images
			List<Image> images = new ArrayList<Image>();
			if(!imageJSONObject.isEmpty()){
				for (Map.Entry<String, Object> entry : imageJSONObject.entrySet()) {
					@SuppressWarnings("unchecked")
					List<String> imageOrderList = (List<String>) entry.getValue();
					if(CollectionUtils.isNotEmpty(imageOrderList)){
						for (String image_order : imageOrderList) {
							String image_url = skuImageJSONObject.getString(image_order);
							if(StringUtils.isNotBlank(image_url)){
								images.add(new Image(image_url));
							}
						}
					}
				}
			}
			
			//size size 对应 size name
			JSONObject sizeJSONObject = new JSONObject();
			JSONObject productOptionsJSONObject = productMasterJSONObject.getJSONObject("productOptions");
			JSONObject variantSizesJSONObject = productOptionsJSONObject.getJSONObject("variantSizes");
			if(null != variantSizesJSONObject && !variantSizesJSONObject.isEmpty()){
				for (Map.Entry<String, Object> entry : variantSizesJSONObject.entrySet()) {
					JSONObject jsonObject = (JSONObject)entry.getValue();
					String sizeId = jsonObject.getString("ID");
					String size_name = jsonObject.getString("displayName");
					sizeJSONObject.put(sizeId, size_name);
				}
			}
			
			//width
			JSONObject widthJSONObject = new JSONObject();
//			System.out.println(productMasterJSONObject.toJSONString());
			JSONObject variantWidthJSONObject = productOptionsJSONObject.getJSONObject("variantWidths");
			if(null != variantWidthJSONObject && !variantWidthJSONObject.isEmpty()){
				for (Map.Entry<String, Object> entry : variantWidthJSONObject.entrySet()) {
					JSONObject jsonObject = (JSONObject)entry.getValue();
					String widthId = jsonObject.getString("ID");
					String width_name = jsonObject.getString("displayName");
					widthJSONObject.put(widthId, width_name);
				}
			}
			
			//json 数据  多个sku iterator
			JSONArray skuJSONArray = productMasterJSONObject.getJSONArray("variationProducts");
			if(null != skuJSONArray && skuJSONArray.size() > 0){
				for (Object object : skuJSONArray) {
					JSONObject jsonObject = new JSONObject();
					JSONObject skuJSONObject = (JSONObject)object;
					String skuId = skuJSONObject.getString("ID");
					String color_id = skuJSONObject.getString("variantColor");
					if(!StringUtils.endsWithIgnoreCase(colorId, color_id)){
						continue ;
					}
					String size = StringUtils.EMPTY;
					String size_id = skuJSONObject.getString("variantSize");
					if(StringUtils.isBlank(size_id)){
						size = "OneSize";
					}else{
						size = sizeJSONObject.getString(size_id);
						if(StringUtils.isBlank(size)){
							size = "OneSize";
						}
						
					}
					String width = StringUtils.EMPTY;
					
					String width_id = skuJSONObject.getString("variantWidth");
					if(StringUtils.isBlank(width_id)){
						width = "OneWidth";
					}else{
						width = widthJSONObject.getString(width_id);
						if(StringUtils.isBlank(width)){
							width = "OneWidth";
						}
					}
					
					String stock_flag = skuJSONObject.getString("inventoryType");
					int stock_number = skuJSONObject.getIntValue("inventory");
					int stock_status = 0;
					if(stock_number >0 || StringUtils.containsIgnoreCase(stock_flag, INSTOCK)){
						stock_status = 1;
					}
					String orign_price = skuJSONObject.getString("listPrice");
					String sale_price = skuJSONObject.getString("sellingPrice");
					String unit = getCurrencyValue(sale_price);// 得到货币代码
					String save=StringUtils.EMPTY;
					sale_price = sale_price.replaceAll("[$,]", "");
					orign_price = orign_price.replaceAll("[$,]", "");

					if (StringUtils.isBlank(orign_price)) {
						orign_price = sale_price;
					}
					if (StringUtils.isBlank(sale_price)) {
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
					
					//jsonObject put
					jsonObject.put("skuId", skuId);
					jsonObject.put("color", color);
					jsonObject.put("size", size);
					jsonObject.put("colorId", color_id);
					jsonObject.put("sizeId", size_id);
					jsonObject.put("widthId", width_id);
					jsonObject.put("width", width);
					jsonObject.put("stock_status", stock_status);
					jsonObject.put("sale_price", sale_price);
					jsonObject.put("orign_price", orign_price);
					jsonObject.put("unit", unit);
					jsonObject.put("save", save);
					jsonObject.put("images", images);
					
					//jsonArray add
					jsonArray.add(jsonObject);
				}
			}
			
			return jsonArray;
		}
		return null;
	}


	private String crawler_package(Context context,String image_url) throws ClientProtocolException, HttpException, IOException {
		Map<String,Object> headers = new HashMap<String,Object>();
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(image_url).header(headers).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(image_url).header(headers).method(HttpMethod.GET.getValue())
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

	public JSONObject getParamJSONObject() {
		return paramJSONObject;
	}

	public void setParamJSONObject(JSONObject paramJSONObject) {
		this.paramJSONObject = paramJSONObject;
	}

	public JSONObject getImageJSONObject() {
		return imageJSONObject;
	}

	public void setImageJSONObject(JSONObject imageJSONObject) {
		this.imageJSONObject = imageJSONObject;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	
}
