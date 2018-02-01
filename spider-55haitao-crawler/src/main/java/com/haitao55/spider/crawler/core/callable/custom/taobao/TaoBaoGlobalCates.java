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

public class TaoBaoGlobalCates  extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private String TAOBAO_API ="https://daigou.taobao.com/json/index/itemList.json?showCategoryId={cateId}&cateName={cateName}";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		
		List<String> newUrlValues = new ArrayList<String>();
		
		if(StringUtils.isNotBlank(content)){
			String middleNav = StringUtils.substringBetween(content, "var middleNav =", ";");
			JSONObject jsonObject = JSONObject.parseObject(middleNav);
			String result = jsonObject.getString("result");
			JSONObject rsJsonObject = JSONObject.parseObject(result);
			JSONArray jsonArray = rsJsonObject.getJSONArray("category");
			for(int i = 0; i < jsonArray.size(); i++){
				JSONObject catejsonObject = jsonArray.getJSONObject(i);
				String id = catejsonObject.getString("id");
				String cateName = catejsonObject.getString("context");
				if(StringUtils.isNotBlank(id) && 
						!"-1".equals(id)){
					String url = TAOBAO_API.replace("{cateId}", id).replace("{cateName}", cateName);
					newUrlValues.add(url);
				}
				
			}
			
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("TaoBaoGlobal cates url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
