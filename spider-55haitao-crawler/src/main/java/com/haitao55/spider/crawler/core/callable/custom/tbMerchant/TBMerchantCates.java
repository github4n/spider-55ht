package com.haitao55.spider.crawler.core.callable.custom.tbMerchant;



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

public class TBMerchantCates  extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String TAOBAO_SUFFIX = "https:";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		
		List<String> newUrlValues = new ArrayList<String>();
		
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			Elements es = doc.select("ul#J_category li.item a");
			for(Element e : es){
				String url = e.attr("href");
				if(StringUtils.isNotBlank(url)){
					newUrlValues.add(TAOBAO_SUFFIX+url);
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("TBMerchantCates cates url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
