package com.haitao55.spider.crawler.core.callable.custom.globaltaobaomerchant;



import java.net.URLEncoder;
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

public class GolobalTaoBaoMerchantCate extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private String TAOBAO_API = "https://g.taobao.com/brand_detail?ajax=true&navigator=all&_input_charset=utf-8&q={keyWord}&sort=credit-desc&s={page}";

	
	@Override
	public void invoke(Context context) throws Exception {
		String curl = context.getCurrentUrl();
		String attr = StringUtils.substringBetween(curl, "q=", "&");
		attr = URLEncoder.encode(attr);
		String tbUrl = TAOBAO_API.replace("{keyWord}", attr).replace("{page}", "0");
		String content = Crawler.create().timeOut(60000).url(tbUrl).method(HttpMethod.GET.getValue())
				.resultAsString();
		List<String> newUrlValues = new ArrayList<String>();
		String pageTotal = StringUtils.substringBetween(content, "totalPage\":", ",");
		if(StringUtils.isNotBlank(pageTotal)){
			for(int i = 0 ;i < Integer.parseInt(pageTotal); i++){
				String url = TAOBAO_API.replace("{keyWord}", attr).replace("{page}", String.valueOf(i*50));
				newUrlValues.add(url);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("GolobalTaoBaoMerchantCate list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
