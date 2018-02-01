package com.haitao55.spider.crawler.core.callable.custom.carters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class CartersSelectAllProducts extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document document = Jsoup.parse(content);
		for(Element grid: document.select("ul[id=search-result-items]")){
			for(Element thumblink:grid.select("a[class=thumb-link]")){
				String url = thumblink.attr("href").split("\\?")[0];
				newUrlValues.add(url);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Carters item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
