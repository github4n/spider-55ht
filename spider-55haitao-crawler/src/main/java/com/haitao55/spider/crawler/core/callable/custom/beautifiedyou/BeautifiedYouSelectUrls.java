package com.haitao55.spider.crawler.core.callable.custom.beautifiedyou;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class BeautifiedYouSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = getByApiInterface(context);
		List<String> newUrlValues = new ArrayList<String>();

		if(StringUtils.isNotBlank(content)){
			JSONObject jsonObject = JSONObject.parseObject(content);
			JSONArray jsonArray = jsonObject.getJSONArray("variants");
			for(int i = 0; i < jsonArray.size(); i++){
				JSONObject productJson = jsonArray.getJSONObject(i);
				String product = productJson.getString("product");
				JSONObject productJsonObject = JSONObject.parseObject(product);
				String url = productJsonObject.getString("url");
				if(StringUtils.isNotBlank(url)){
					newUrlValues.add(url);
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("BeautifiedYou item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
		
	
	private String getByApiInterface(Context context) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		 headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/56.0.2924.76 Chrome/56.0.2924.76 Safari/537.36");
		 headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
		// headers.put("Referer", "https://www.beautifiedyou.com/acne-products");
		 headers.put("Host", "www.beautifiedyou.com");
		 headers.put("X-Requested-With", "XMLHttpRequest");
		 headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		 headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
		return headers;
	}
	
}
