package com.haitao55.spider.crawler.core.callable.custom.houseoffraser;


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

public class HouseoffraserSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASE_URL = "https://www.houseoffraser.co.uk";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		
		String product = StringUtils.substringBetween(content, "ProductListingPageSetupDataViewModel(", "),");
		
		if(StringUtils.isNotBlank(product)){
			JSONObject jsonObject = JSONObject.parseObject(product);
			String searchResponse  = jsonObject.getString("SearchResponse");
			JSONObject srJson = JSONObject.parseObject(searchResponse);
			String productListing = srJson.getString("ProductListing");
			JSONObject productListJson = JSONObject.parseObject(productListing);
			JSONArray jsonArray = productListJson.getJSONArray("Items");
			for(int i = 0; i < jsonArray.size(); i++){
				JSONObject itemJson = jsonArray.getJSONObject(i);
				String url = itemJson.getString("ProductDetailsLink");
				if(StringUtils.isNotBlank(url)){
					newUrlValues.add(BASE_URL+url);
				}
			}
			
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Houseoffraser item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
