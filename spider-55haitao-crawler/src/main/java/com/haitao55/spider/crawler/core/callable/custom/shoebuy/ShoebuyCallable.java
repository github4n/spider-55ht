package com.haitao55.spider.crawler.core.callable.custom.shoebuy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

public class ShoebuyCallable implements Callable<JSONObject> {
	private String colorId;
	private List<Image> value;
	private Context context;
	private JSONObject imageJSONObject;

	public ShoebuyCallable(String colorId, List<Image> value, Context context, JSONObject imageJSONObject) {
		this.colorId = colorId;
		this.value = value;
		this.context = context;
		this.imageJSONObject = imageJSONObject;
	}

	public String getColorId() {
		return colorId;
	}

	public void setColorId(String colorId) {
		this.colorId = colorId;
	}

	public List<Image> getValue() {
		return value;
	}

	public void setValue(List<Image> value) {
		this.value = value;
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
		List<Image> images = new ArrayList<Image>();
		for (Image image : value) {
			String image_url = image.getOriginalUrl();
			try {
				crawler_package(context, image_url);
			} catch (HttpException e) {
				image_url = StringUtils.replacePattern(image_url, "pi", "pm");
			}catch (Exception e) {
			}
			images.add(new Image(image_url));
		}
		imageJSONObject.put(colorId, images);
		return null;
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
