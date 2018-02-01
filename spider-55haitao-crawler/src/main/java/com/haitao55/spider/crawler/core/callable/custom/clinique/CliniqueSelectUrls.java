package com.haitao55.spider.crawler.core.callable.custom.clinique;


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

public class CliniqueSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private static final String BASE_URL = "http://www.clinique.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document docment = Jsoup.parse(content);
		Elements es = docment.select("ul#mpp-product-grid li.mpp-box .mpp-product-hover-view a.btn-shopnow");
		
		if(es != null && es.size() > 0){
			for(Element e : es){
				String url = e.attr("href");
				if(StringUtils.isNotBlank(url)){
					newUrlValues.add(BASE_URL+url);
				}
			}
		}
		
		Elements productEs = docment.select("ul.slides li .product-details a.btn-shopnow");
		if(productEs != null && productEs.size() > 0){
			for(Element e : productEs){
				String url = e.attr("href");
				if(StringUtils.isNotBlank(url)){
					newUrlValues.add(BASE_URL+url);
				}
			}
		}
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Clinique item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
