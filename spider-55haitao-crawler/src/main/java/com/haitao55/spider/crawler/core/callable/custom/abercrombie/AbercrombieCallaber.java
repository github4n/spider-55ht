package com.haitao55.spider.crawler.core.callable.custom.abercrombie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

public class AbercrombieCallaber implements Callable<JSONObject> {
	private static final String IMAGE_URL_TEMP = "http://anf.scene7.com/is/image/()?$product-ofp-anf-v1$";
	private String color;
	private String image_url;
	private JSONObject imageJSONObject;
	private Context context;
	private String image_param;
	private boolean singleColor;

	public AbercrombieCallaber(String color, String image_url, JSONObject imageJSONObject, Context context,String image_param , boolean singleColor) {
		super();
		this.color = color;
		this.image_url = image_url;
		this.imageJSONObject = imageJSONObject;
		this.context = context;
		this.image_param = image_param;
		this.singleColor = singleColor;
	}

	public AbercrombieCallaber() {
		super();
	}

	@Override
	public JSONObject call() throws Exception {
		String content = crawler_package(context,image_url);
//		String content = Crawler.create().url(image_url).timeOut(15000).proxy(true).proxyAddress("104.196.109.105").proxyPort(3128).resultAsString();
		if(StringUtils.isNotBlank(content)){
			List<Image> images = new ArrayList<Image>();
			List<Image> imagesModel = new ArrayList<Image>();
			List<Image> imagesProd = new ArrayList<Image>();
			String image_json = StringUtils.substringBetween(content, "(", ",\""+image_param+"");
			JSONObject parseObject = JSONObject.parseObject(image_json);
			JSONObject setJSONObject = parseObject.getJSONObject("set");
			JSONArray jsonArray = setJSONObject.getJSONArray("item");
			if(null != jsonArray && jsonArray.size() > 0){
				for (Object object : jsonArray) {
					JSONObject jsonObject = (JSONObject)object;
					JSONObject iJSONObject = jsonObject.getJSONObject("i");
					String image_url_section = iJSONObject.getString("n");
					if(StringUtils.containsIgnoreCase(image_url_section, "model")){
						String replacePattern = StringUtils.replacePattern(IMAGE_URL_TEMP, "\\(\\)",image_url_section);
						imagesModel.add(new Image(replacePattern));
					}
					if(StringUtils.containsIgnoreCase(image_url_section, "prod")){
						String replacePattern = StringUtils.replacePattern(IMAGE_URL_TEMP, "\\(\\)",image_url_section);
						imagesProd.add(new Image(replacePattern));
					}
				}
			}
			//网站特殊性  model 先于 prod
			if(singleColor){
				images.addAll(imagesProd);
				images.addAll(imagesModel);
			}else{
				images.addAll(imagesModel);
				images.addAll(imagesProd);
			}
			
			imageJSONObject.put(color, images);
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
	
	public String getColor() {
		return color;
	}


	public void setColor(String color) {
		this.color = color;
	}


	public String getImage_url() {
		return image_url;
	}


	public void setImage_url(String image_url) {
		this.image_url = image_url;
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

	public String getImage_param() {
		return image_param;
	}

	public void setImage_param(String image_param) {
		this.image_param = image_param;
	}

	public boolean isSingleColor() {
		return singleColor;
	}

	public void setSingleColor(boolean singleColor) {
		this.singleColor = singleColor;
	}
	
	
}
