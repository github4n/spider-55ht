package com.haitao55.spider.crawler.core.callable.custom.origins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

public class OriginSelectItems extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASEURL = "http://www.origins.com";
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document docment = Jsoup.parse(content);
		Elements es = docment.select("div .product-grid__item");
		logger.info(String.valueOf(es.size()));
		if(es != null && es.size()>0){
			for(Element e:es){
				if(e.select("a").attr("href").indexOf("/product/") != -1){
				  String Url = BASEURL+e.select("a").attr("href");
				  newUrlValues.add(Url);					
				}
			Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
			context.getUrl().getNewUrls().addAll(newUrls);
			logger.info("Toryburch item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
			}
			}
	}

}
