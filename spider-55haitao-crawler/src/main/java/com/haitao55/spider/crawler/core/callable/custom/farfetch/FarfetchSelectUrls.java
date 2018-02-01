package com.haitao55.spider.crawler.core.callable.custom.farfetch;




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

public class FarfetchSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASE_URL ="https://www.farfetch.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		
		String listing = StringUtils.substringBetween(content, "window.universal_variable.listing = ", ";");

		if(StringUtils.isNotBlank(listing)){
			JSONObject jsonObject = JSONObject.parseObject(listing);
			JSONArray jsonArray = jsonObject.getJSONArray("items");
			for(int i = 0; i < jsonArray.size(); i++){
				JSONObject productJson = jsonArray.getJSONObject(i);
				String url = productJson.getString("url");
				if(StringUtils.containsIgnoreCase(url, "?")){
					url = url.substring(0, url.indexOf("?"));
				}
				 newUrlValues.add(BASE_URL+url);
			}
			
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Farfetch item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
