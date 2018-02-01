package com.haitao55.spider.crawler.core.callable.custom.shopspring;



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

public class ShopspringSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASEURL = "https://www.shopspring.com/";
	private String SHOPSPRING_API ="https://www.shopspring.com/api/1/productsWithRefinements/?taxonomy={}&sortBy=popular&sortOrder=DESC";
	private String START_PAGE_API ="https://www.shopspring.com/api/1/productsWithRefinements/?taxonomy={}&start={start}&sortBy=popular&sortOrder=DESC";
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		String attr = url.replace(BASEURL, "");
		String conversionAttr = attr.replaceAll("/", ":");
		List<String> newUrlValues = new ArrayList<String>();
		if(StringUtils.isNotBlank(conversionAttr)){
			@SuppressWarnings("deprecation")
			String code = URLEncoder.encode(conversionAttr);
			String shopUrl = SHOPSPRING_API.replace("{}", code);
			String content = Crawler.create().timeOut(60000).url(shopUrl)
					.method(HttpMethod.GET.getValue()).resultAsString();
			String pageTotal = StringUtils.substringBetween(content, "total_products_count\":", "}");
			newUrlValues.add(shopUrl);
			if(StringUtils.isNotBlank(pageTotal) 
					&& !"0".equals(pageTotal)){
				Double page  = Double.valueOf(pageTotal) / 80;
				int pageNumber =  (int)Math.ceil(page);
				for(int i = 1 ;i < pageNumber; i++){
					String  startPageUrl = START_PAGE_API.replace("{}", code).replace("{start}",String.valueOf(i*80));
					newUrlValues.add(startPageUrl);
				}
			}
			
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Shopspring list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
}
