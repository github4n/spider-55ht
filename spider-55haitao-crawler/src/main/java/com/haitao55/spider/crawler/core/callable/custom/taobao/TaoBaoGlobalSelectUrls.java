package com.haitao55.spider.crawler.core.callable.custom.taobao;


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

public class TaoBaoGlobalSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String curl = context.getCurrentUrl();
		String cateName = StringUtils.substringAfter(curl, "cateName=");
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		
		if(StringUtils.isNotBlank(content)){
			JSONObject jsonObject = JSONObject.parseObject(content);
			JSONArray jsonArray = jsonObject.getJSONArray("result");
			for(int i = 0; i < jsonArray.size(); i++){
				JSONObject urlJsonObject = jsonArray.getJSONObject(i);
				String url = urlJsonObject.getString("itemUrl");
				if(StringUtils.isNotBlank(url)){
					newUrlValues.add(url+"&cateName="+cateName);
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("TaoBaoGlobal item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
