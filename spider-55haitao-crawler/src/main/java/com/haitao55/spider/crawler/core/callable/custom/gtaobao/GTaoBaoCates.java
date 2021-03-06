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

public class GTaoBaoCates  extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String TAOBAO_SUFFIX = "https:";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		
		List<String> newUrlValues = new ArrayList<String>();
		
		if(StringUtils.isNotBlank(content)){
			String jsonp = StringUtils.substringBetween(content, "jsonp(", ")");
			JSONObject jsonObject = JSONObject.parseObject(jsonp);
			JSONArray cateJsonArray = jsonObject.getJSONArray("category");
			for(int j = 0; j < cateJsonArray.size(); j++){
				JSONObject categoryJsonObject = cateJsonArray.getJSONObject(j);
				JSONArray jsonArray = categoryJsonObject.getJSONArray("children");
				String link = categoryJsonObject.getString("link");
				newUrlValues.add(TAOBAO_SUFFIX+link);
				for(int i = 0; i < jsonArray.size(); i++){
					JSONObject catejsonObject = jsonArray.getJSONObject(i);
					JSONArray catesjsonArray = catejsonObject.getJSONArray("children");
					String cateLink = catejsonObject.getString("link");
					if(StringUtils.isNotBlank(cateLink) ){
						newUrlValues.add(TAOBAO_SUFFIX+cateLink);
					}
					for(int c = 0; c < catesjsonArray.size(); c++){
						JSONObject cate3Json = catesjsonArray.getJSONObject(c);
						String url = cate3Json.getString("link");
						if(StringUtils.isNotBlank(url)){
							newUrlValues.add(TAOBAO_SUFFIX+url);
						}
					}
				}
			}
			
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("GTaoBaoCates cates url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
