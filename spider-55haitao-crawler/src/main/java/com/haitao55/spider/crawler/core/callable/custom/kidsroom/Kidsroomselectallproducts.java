package com.haitao55.spider.crawler.core.callable.custom.kidsroom;

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

public class Kidsroomselectallproducts extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASEURL = "http://www.kidsroom.de";
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document document = Jsoup.parse(content);
		Elements grid = document.select("div[id=ICProductListImageBox]");
		if(!grid.isEmpty()){
			for(Element prod:grid.select("div[class=ProductInfo]")){
				if(!prod.select("a").isEmpty()&!prod.select("a").attr("href").isEmpty()){
					String url = BASEURL+prod.select("a").attr("href");
					newUrlValues.add(url);
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Kidsroom item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}

}
