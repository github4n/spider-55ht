package com.haitao55.spider.crawler.core.callable.custom.levis;

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

public class Selectallproducts_levis extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		Document document = Jsoup.parse(content);
		Elements products = document.select("div[class=product-details]");
		List<String> newUrlValues = new ArrayList<String>();
		for(Element e:products){
			String url = "http://www.levi.com/"+e.select("a").attr("href");
			newUrlValues.add(url);
	}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Levis list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());

}
}
