package com.haitao55.spider.crawler.core.callable.custom.gtaobao;



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

public class GTaoBaoSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String cateName = StringUtils.substringBetween(context.getCurrentUrl(), "q=","&");
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		
		if(StringUtils.isNotBlank(content)){
			String jsoup = content.substring(content.indexOf("(")+1, content.lastIndexOf(")"));
			//String jsoup = StringUtils.substringBetween(content, "jsonp(", ")");
			JSONObject jsonObject = JSONObject.parseObject(jsoup);
			JSONArray jsonArray = jsonObject.getJSONArray("itemList");
			for(int i = 0; i < jsonArray.size(); i++){
				JSONObject urlJsonObject = jsonArray.getJSONObject(i);
				String url = urlJsonObject.getString("href");
				if(StringUtils.isNotBlank(url)){
					newUrlValues.add("https:"+url+"&cateName="+cateName);
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("GTaoBao item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
}
