package com.haitao55.spider.crawler.core.callable.custom.michaelkors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

public class MichaelkorsCallable implements Callable<JSONObject> {
	private static final String imageUrl = "http://michaelkors.scene7.com/is/image/MichaelKors/()_IS?req=set,json,UTF-8&handler=s7SetCallback&callback=s7SetCallback";

	private JSONObject imageJSONObject;
	private String param;
	private String color;
	private Context context;

	public MichaelkorsCallable(JSONObject imageJSONObject, String param, String color, Context context) {
		super();
		this.imageJSONObject = imageJSONObject;
		this.param = param;
		this.color = color;
		this.context = context;
	}

	public MichaelkorsCallable() {
		super();
	}

	@Override
	public JSONObject call() {
		List<Image> images = new ArrayList<Image>();
		String image_content = StringUtils.EMPTY;
		try {

			String request_imageUrl = StringUtils.replacePattern(imageUrl, "\\(\\)", param);
			image_content = crawler_result(context, request_imageUrl);

			if (StringUtils.isNotBlank(image_content)) {
				images = image_array_package(image_content);
			}
			imageJSONObject.put(color, images);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 根据请求结果封装返回 图片数据
	 * 
	 * @param image_content
	 * @return
	 */
	private List<Image> image_array_package(String image_content) {
		List<Image> images = new ArrayList<Image>();
		if (StringUtils.isBlank(image_content)) {
			return images;
		}

		image_content = StringUtils.substringBetween(image_content, "\"item\":", "]}");

		if (StringUtils.isBlank(image_content)) {
			return images;
		}
		image_content = image_content.concat("]");
		JSONArray parseArray = JSONArray.parseArray(image_content);
		if (null != parseArray && parseArray.size() > 0) {
			for (Object object : parseArray) {
				JSONObject image = (JSONObject) object;
				JSONObject jsonObject = image.getJSONObject("i");
				String imageUrl = jsonObject.getString("n");
				images.add(new Image(imageUrl));
			}
		}

		return images;
	}

	/**
	 * 线上爬取
	 * 
	 * @param context
	 * @param path
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	private String crawler_result(Context context, String path)
			throws ClientProtocolException, HttpException, IOException {
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		String content = StringUtils.EMPTY;
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(30000).url(path).proxy(false).resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(30000).url(path).proxy(true).proxyAddress(proxyAddress)
					.proxyPort(proxyPort).resultAsString();
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
}
