package com.haitao55.spider.crawler.core.callable.custom.feelunique.cn;

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
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.JsoupUtils;


public class CNFeeluniqueCallaber implements Callable<JSONObject> {
	public static final String URL = "https://cn.feelunique.com/1{}.html";
	public static final String PRODUCT_JSON_URL = "https://cn.feelunique.com/pt_catalog/productservice/list?product_id=";
	private String productId;
	private Context context;
	

	public CNFeeluniqueCallaber(String productId, Context context) {
		super();
		this.productId = productId;
		this.context = context;
	}


	public CNFeeluniqueCallaber() {
		super();
	}


	@Override
	public JSONObject call() throws ClientProtocolException, HttpException, IOException{
		JSONObject jsonObject = new JSONObject();
		String url = StringUtils.replacePattern(URL, "\\{\\}", productId);
		boolean isRunInRealTime = context.isRunInRealTime();
		String content = StringUtils.EMPTY;
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
			content = luminatiHttpClient.request(url, getHeaders(context));
		}else{
			//content = crawler_package(url,context);
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(url, getHeaders(context));
		}
		
		Document doc = JsoupUtils.parse(content);
		
		String productData = StringUtils.EMPTY;
		if (isRunInRealTime) {
//			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
//			productData = luminatiHttpClient.request(url, getCnFeelHeaders(context));
			productData = crawler_package(PRODUCT_JSON_URL+productId,context);
		}else{
			productData = crawler_package(PRODUCT_JSON_URL+productId,context);
//			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
//			productData = luminatiHttpClient.request(PRODUCT_JSON_URL+productId, getCnFeelHeaders(context));
		}
		
		JSONObject productJSONObject = new JSONObject();
		if(StringUtils.isNotBlank(productData)){
			System.out.println("crawling CNFeelunique Thread sunccess, productId : " +productId +" , url : " +url +", productData :  " + productData);
			productJSONObject = JSONObject.parseObject(productData);
		}
		//skuId
		String skuId = StringUtils.EMPTY;
		//stock_status
		int stock_status = 0;
		//sale price
		String sale_price = StringUtils.EMPTY;
		//orign price
		String orign_price = StringUtils.EMPTY;
		if(!productJSONObject.isEmpty()){
			JSONArray jsonArray = productJSONObject.getJSONArray("data");
			JSONObject productInfoJSONObject = jsonArray.getJSONObject(jsonArray.size()-1);
			skuId = productInfoJSONObject.getString("sku");
			stock_status = productInfoJSONObject.getIntValue("is_in_stock");
			sale_price = productInfoJSONObject.getString("final_price");
			orign_price = productInfoJSONObject.getString("price");
		}
		//save
		String save = StringUtils.EMPTY;
		if (StringUtils.isBlank(orign_price)
				|| Float.valueOf(orign_price) < Float.valueOf(sale_price)) {
			orign_price = sale_price;
		}
		if (StringUtils.isBlank(save)) {
			save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100)
					+ "";// discount
		}
		
		//color
		String color = StringUtils.EMPTY;
		Elements colorElements = doc.select("em.property-value");
		if(CollectionUtils.isNotEmpty(colorElements)){
			color = colorElements.text();
		}
		
		//pics
		List<Image> pics = new ArrayList<Image>();
		//product main image
		Elements imageElements = doc.select("img#J_picImg");
		
		// image
		Elements selectedImages = doc.select("div.thumbnails ul li.selected a");
		if (CollectionUtils.isNotEmpty(selectedImages)) {
			for (Element element : selectedImages) {
				String src = element.attr("href");
				pics.add(new Image(src));
			}
		}else{
			String imageUrl = StringUtils.EMPTY;
			if(CollectionUtils.isNotEmpty(imageElements)){
				imageUrl = imageElements.attr("src");
			}
			pics.add(new Image(imageUrl));
		}
		
		jsonObject.put("skuId", skuId);
		jsonObject.put("productId", productId);
		jsonObject.put("stock_status", stock_status);
		jsonObject.put("sale_price", sale_price);
		jsonObject.put("orign_price", orign_price);
		jsonObject.put("color", color);
		jsonObject.put(productId, pics);

		return jsonObject;
	}
	
	private String crawler_package(String url , Context context) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders(context)).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders(context)).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	private static Map<String,Object> getCnFeelHeaders(Context context){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/62.0.3202.75 Chrome/62.0.3202.75 Safari/537.36");
		 headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Host", "cn.feelunique.com");
		 headers.put("Accept-Encoding", "gzip, deflate, br");
		 headers.put("Cookie", "uuid=42C6A44F-339C-4533-AF3F-5D50DD04AD31; Qs_lvt_169767=1504334051%2C1504681022%2C1504751569; Qs_pv_169767=2007569198997790000%2C4302610400900554000%2C1469955728204846600%2C3300557603081560000%2C701725176119030100; SERVER_ID=8a54179b-073f8245; CACHED_FRONT_FORM_KEY=nI3vRnr1mAABXWc5; scarab.profile=%2243507%252D0%7C1510902905%22; remember_locale=1; LAST_CATEGORY=3727; CATEGORY_INFO=%5B%5D; small_active_gg=1; scarab.mayAdd=%5B%7B%22i%22%3A%2250205-18701%22%7D%2C%7B%22i%22%3A%2272536-32423%22%7D%2C%7B%22i%22%3A%2263605-27546%22%7D%2C%7B%22i%22%3A%2273711-33401%22%7D%2C%7B%22i%22%3A%2264020-27906%22%7D%2C%7B%22i%22%3A%2263247-27392%22%7D%2C%7B%22i%22%3A%2266343-29435%22%7D%2C%7B%22i%22%3A%2265877-29122%22%7D%2C%7B%22i%22%3A%2259685-24140%22%7D%2C%7B%22i%22%3A%2232089-10649%22%7D%5D; VIEWED_PRODUCT_IDS=139376%2C123845%2C133885%2C139681%2C143199; sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%2215c9b6ac41f28c-0f224ea8d6cd69-24414032-1049088-15c9b6ac4214ef%22%2C%22props%22%3A%7B%22%24latest_referrer%22%3A%22%22%2C%22%24latest_referrer_host%22%3A%22%22%7D%2C%22%24device_id%22%3A%2215ec7202af1915-0e66adea42a6d5-2a044871-1049088-15ec7202af276f%22%7D; Hm_lvt_32ddf50f15b8e3b56c7e0a49202c521e=1510901571; Hm_lpvt_32ddf50f15b8e3b56c7e0a49202c521e=1510994521; external_no_cache=1; __xsptplusUT_718=1; __xsptplus718=718.21.1510988595.1510994521.12%233%7Cjson.cn%7C%7C%7C%7C%23%23rWvehk-qFwflR29xWzvAksfp2eXu_U8v%23; scarab.visitor=%226A012990BC6CBE74%22; frontend=qol6mk5rev6rj1h9mkij6sv9l2");
		 return headers;
	}
	
	
	private static Map<String,Object> getHeaders(Context context){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		 headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Host", "cn.feelunique.com");
		 headers.put("Referer", context.getUrl().getParentUrl());
		 headers.put("X-Requested-With", "XMLHttpRequest");
		 headers.put("Cookie", " D_SID=192.243.119.27:DxlfonMJJUoIesv2gfRD8BzxkWLfN6bjovSPqqhBwNA; D_UID=5076FC3F-90DD-3ED5-9DF1-0C8CA0875C35; D_HID=pBI8DGhuNhFWTj/Ag/GCU4Lwa/VNJQz1q79onPdhBCs;feeluniqueCurr=GBP;");
		return headers;
	}
	
	
	public String getProductId() {
		return productId;
	}


	public void setProductId(String productId) {
		this.productId = productId;
	}


	public Context getContext() {
		return context;
	}


	public void setContext(Context context) {
		this.context = context;
	}
}
