package com.haitao55.spider.crawler.core.callable.custom.lordandtaylor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;


public class LordAndTaylorCallaber implements Callable<JSONObject> {
	public static final String image_temp = "http://s7d9.scene7.com/is/image/LordandTaylor/{}?$PDPLARGE$";
	private Entry<String, Object> entry;
	private JSONObject image_result_json;
	private List<String> image_suffix_list;
	private Url url;

	public LordAndTaylorCallaber(Entry<String, Object> entry , JSONObject image_result_json , List<String> image_suffix_list, Url url) {
		super();
		this.entry = entry;
		this.image_result_json = image_result_json;
		this.url = url;
		this.image_suffix_list = image_suffix_list;
	}

	public LordAndTaylorCallaber() {
		super();
	}



	@Override
	public JSONObject call(){
		
		List<Image> pics = new ArrayList<Image>();
		
		String style_id = entry.getKey();
		JSONObject jsonObject = (JSONObject)entry.getValue();
		//item image upc 
		String image_upc = jsonObject.getString("ItemThumbUPC");
		for (String image_suffix : image_suffix_list) {
			String image_flag = image_upc.concat("_"+image_suffix);
			String image_url=image_temp.replaceAll("\\{\\}", image_flag);
			if("main".equals(image_suffix)){
				pics.add(new Image(image_url));
			}
			boolean image_exists = image_exists(image_url,url);
			if(image_exists&&!"main".equals(image_suffix)){//main　为主图，肯定存在，减少发送请求次数
				pics.add(new Image(image_url));
			}
		}
		
		image_result_json.put(style_id, pics);
		
		return image_result_json;
	}
	
	/**
	 * 验证图片是否存在
	 * @param image_url
	 * @param url
	 * @return
	 */
	private boolean image_exists(String image_url, Url url) {
		try {
			String proxyRegionId = url.getTask().getProxyRegionId();
			if(StringUtils.isBlank(proxyRegionId)){
				Crawler.create().timeOut(15000).url(image_url).proxy(false).resultAsString();
			}else{
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String ip = proxy.getIp();
				int port = proxy.getPort();
				Crawler.create().timeOut(15000).url(image_url).proxy(true).proxyAddress(ip)
				.proxyPort(port).resultAsString();
			}
			return true;
		} catch (ClientProtocolException e) {
		} catch (HttpException e) {
			if(200!=e.getStatus()){
				return false;
			}
		} catch (IOException e) {
		}
		return false;
	}
}
