package com.haitao55.spider.crawler.core.callable.custom.gtaobao;



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

public class GTaoBaoSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private String TAOBAO_API = "https://g.taobao.com/brand_detail?callback=jsonp&&ajax=true&";
	
	@Override
	public void invoke(Context context) throws Exception {
		String curl = context.getCurrentUrl();
		String attr = StringUtils.substringAfter(curl, "?");
		String content = Crawler.create().timeOut(60000).url(TAOBAO_API+attr).method(HttpMethod.GET.getValue())
				.resultAsString();
		List<String> newUrlValues = new ArrayList<String>();
		newUrlValues.add(TAOBAO_API+attr);
		String pageTotal = StringUtils.substringBetween(content, "totalPage\":", ",");
		if(StringUtils.isNotBlank(pageTotal) 
				&& !"1".equals(pageTotal)){
			for(int i = 1 ;i < Integer.parseInt(pageTotal); i++){
				newUrlValues.add(TAOBAO_API+"s="+i*50+"&"+attr);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("GTaoBao list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}

}
