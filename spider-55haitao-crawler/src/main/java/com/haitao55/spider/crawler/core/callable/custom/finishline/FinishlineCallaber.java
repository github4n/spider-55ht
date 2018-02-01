package com.haitao55.spider.crawler.core.callable.custom.finishline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class FinishlineCallaber implements Callable<JSONObject> {
	private static final String image_url = "https://www.finishline.com/store/browse/gadgets/alternateimage.jsp?colorID=()";
	private static final Map<String, Object> headers = new HashMap<String, Object>() {
		{
			put("Accept-Encoding", "gzip, deflate");
			put("Accept-Language", "zh-CN,zh;q=0.8");
			put("Upgrade-Insecure-Requests", "1");
			put("User-Agent",
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
			put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			put("Cache-Control", "max-age=0");
			put("Connection", "keep-alive");
		}
	};
	private String param;
	private JSONObject resultJsonObject;
	private Proxy proxy;

	public Proxy getProxy() {
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public JSONObject getResultJsonObject() {
		return resultJsonObject;
	}

	public void setResultJsonObject(JSONObject resultJsonObject) {
		this.resultJsonObject = resultJsonObject;
	}

	public FinishlineCallaber(Proxy proxy, String param, JSONObject resultJsonObject) {
		this.proxy = proxy;
		this.param = param;
		this.resultJsonObject = resultJsonObject;
	}

	@Override
	public JSONObject call() throws Exception {
		List<Image> pics = new ArrayList<Image>();
		String url = StringUtils.replacePattern(image_url, "\\(\\)", param);
		String content = Crawler.create().timeOut(60000).url(url).header(headers).method(HttpMethod.GET.getValue())
				.proxy(true).proxyAddress(proxy.getIp()).proxyPort(proxy.getPort()).resultAsString();
		Document doc = JsoupUtils.parse(content);

		Elements picsElements = doc.select("div.altView");
		if (CollectionUtils.isNotEmpty(picsElements)) {
			for (Element element : picsElements) {
				String image_url_temp = element.attr("data-large").replace("\r\n", "");
				pics.add(new Image(image_url_temp));
			}
		}
		resultJsonObject.put(param, pics);
		return resultJsonObject;
	}

}
