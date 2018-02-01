package com.haitao55.spider.crawler.core.callable.custom.samsonite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class SamsoniteSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		String pageCount = StringUtils.substringBetween(content, "class=\"results-mobile mobile-visible\">", "<span");
		if(StringUtils.isNotBlank(pageCount)){
			Url currentUrl = new Url(url+"#sz="+pageCount.trim());
			currentUrl.setTask(context.getUrl().getTask());
			String html = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
			Document docment = Jsoup.parse(html);
			Elements es = docment.select("ul#search-result-items li.grid-tile .product-tile .product-image a");
			if(es != null && es.size() > 0){
				for(Element e : es){
					String pUrl = e.attr("href");
					if(StringUtils.isNotBlank(pUrl)){
						if(StringUtils.containsIgnoreCase(pUrl, "?") ){
							String baseUrl = pUrl.substring(0, pUrl.indexOf("?")+1);
							String productColor = StringUtils.substringBetween(pUrl, "?", "&");
							baseUrl = baseUrl.replace("%22", "");
							newUrlValues.add(baseUrl+productColor);
						}
					}
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("shop.samsonite list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
}
