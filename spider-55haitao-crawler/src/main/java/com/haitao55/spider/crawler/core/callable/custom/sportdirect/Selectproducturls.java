package com.haitao55.spider.crawler.core.callable.custom.sportdirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class Selectproducturls extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASEURL = "http://www.sportsdirect.com";
	@Override
	public void invoke(Context context) throws Exception {	
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		JSONObject json = JSONObject.parseObject(content);
		JSONArray product_list = json.getJSONArray("products");
		for(Object product:product_list){
			JSONObject product_converted = (JSONObject)product;
			String product_url = product_converted.getString("PrdUrl");
			String url = BASEURL+product_url;
			newUrlValues.add(url);
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Sportsdirect item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
}
