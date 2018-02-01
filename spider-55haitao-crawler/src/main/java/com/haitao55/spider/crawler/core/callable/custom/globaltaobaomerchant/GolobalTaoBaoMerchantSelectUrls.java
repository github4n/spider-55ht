package com.haitao55.spider.crawler.core.callable.custom.globaltaobaomerchant;




import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class GolobalTaoBaoMerchantSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		String keyWord = StringUtils.substringAfter(url, "keyWord=");
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		
		if(StringUtils.isNotBlank(content)){
			Document doc = JsoupUtils.parse(content);
			String title = doc.select("h3.tb-main-title").text();
			String shopName = doc.select(".tb-shop-name strong a").text();
			String shopUrl = doc.select(".tb-shop-rank a").attr("href");
			String merchantUrl = doc.select(".tb-seller-name").attr("href");
			if(StringUtils.isNotBlank(keyWord)){
				keyWord = URLDecoder.decode(keyWord);
			}
			
			if(StringUtils.isNotBlank(merchantUrl)){
				if(StringUtils.contains(merchantUrl, "?")){
					merchantUrl = merchantUrl.substring(0, merchantUrl.indexOf("?"));
				}
				merchantUrl = "https:"+merchantUrl;
			}
			
			if(StringUtils.isNotBlank(shopName) && 
					StringUtils.isNotBlank(shopUrl)){
				newUrlValues.add("https:"+shopUrl+"?shopName="+shopName+"&keyWord="+keyWord+"&merchantUrl="+merchantUrl+"&title="+title);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("GolobalTaoBaoMerchantSelectUrls item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
}
