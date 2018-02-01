package com.haitao55.spider.crawler.core.callable.custom.taobao;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class TaoBaoGlobalSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private String TAOBAO_API = "https://daigou.taobao.com/json/index/itemList.json?currentPage=##&pageSize=100&showCategoryId={cateId}&brandId=-1&shopId=-1&orderType=-1";
	
	@Override
	public void invoke(Context context) throws Exception {
		String curl = context.getCurrentUrl();
		String id = StringUtils.substringBetween(curl, "showCategoryId=", "&");
		String cateName = StringUtils.substringAfter(curl, "cateName=");
		String content = Crawler.create().timeOut(60000).url(TAOBAO_API.replace("{cateId}",id).replace("##", "1")).method(HttpMethod.GET.getValue())
				.resultAsString();
		
		List<String> newUrlValues = new ArrayList<String>();
		String pageTotal = StringUtils.substringBetween(content, "totalPage\":\"", "\"");
		if(StringUtils.isNotBlank(pageTotal) 
				&& !"0".equals(pageTotal)){
			for(int i = 1 ;i <= Integer.parseInt(pageTotal); i++){
				newUrlValues.add(TAOBAO_API.replace("##", String.valueOf(i)).replace("{cateId}",id)+"&cateName="+cateName);
			}
		}else{
			newUrlValues.add(TAOBAO_API.replace("##", "1").replace("{cateId}",id)+"&cateName="+cateName);
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("TaoBaoGlobal list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}

}
