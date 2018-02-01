package com.haitao55.spider.crawler.core.callable.custom.gymboree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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

public class GymboreeCallable implements Callable<JSONObject> {
	private static final String IMAGE_REQUEST_URL = "http://i1.adis.ws/s/gymboree/{}_SET.js?deep=true&arg=%27{}_SET%27&func=amp.jsonReturn";
	private String colorCode;
	private Context context;
	private JSONObject imageJSONObject;

	public GymboreeCallable(String colorCode, Context context, JSONObject imageJSONObject) {
		this.colorCode = colorCode;
		this.context = context;
		this.imageJSONObject = imageJSONObject;
	}

	public String getColorCode() {
		return colorCode;
	}

	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
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
		String image_url = StringUtils.replacePattern(IMAGE_REQUEST_URL, "\\{\\}", colorCode);
		String content = crawler_package(context,image_url);

		List<Image> images = new ArrayList<Image>();
		imagesPackage(content, images);
		imageJSONObject.put(colorCode, images);
		return null;
	}

	private void imagesPackage(String content, List<Image> images) {
		if (StringUtils.isNotBlank(content)) {
			String imageData = StringUtils.substringBetween(content, ", ", ");");
			if (StringUtils.isNotBlank(imageData)) {
				JSONObject imageDataJSONObject = JSONObject.parseObject(imageData);
				if (MapUtils.isNotEmpty(imageDataJSONObject)) {
					// 存在多张图片
					JSONArray imageJSONArray = imageDataJSONObject.getJSONArray("items");
					if (CollectionUtils.isNotEmpty(imageJSONArray)) {
						for (Object object : imageJSONArray) {
							JSONObject jsonObject = (JSONObject) object;
							String image_url = jsonObject.getString("src");
							images.add(new Image(image_url));
						}
					}
				}
			}
		}
	}
	
	private String crawler_package(Context context,String url) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

}
