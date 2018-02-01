package com.haitao55.spider.crawler.core.callable.custom.ebags;




import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class EbagsSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private String EBAGS_API = "http://www.ebags.com/Discovery/SearchResultsAsJson?searchPathAndQuery=";

	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		String cateUrl = url.replace("http://www.ebags.com", "");
		List<String> newUrlValues = new ArrayList<String>();
		String fullPageCount = StringUtils.substringBetween(content, "FullPageCount&quot;:", ",");
		String pageTotal = StringUtils.substringBetween(content, "searchResultsCount\">", "items<");

		if(StringUtils.isNotBlank(pageTotal) && StringUtils.isNotBlank(fullPageCount)){
			pageTotal = pattern(pageTotal);
			fullPageCount = pattern(fullPageCount);
			Double page  = Double.valueOf(pageTotal) / Integer.parseInt(fullPageCount);
			int pageNumber =  (int)Math.ceil(page);
			for(int i = 0 ;i < pageNumber; i++){
				String join = cateUrl+"?from="+i*Integer.parseInt(fullPageCount);
				String decoder = URLEncoder.encode(join);
				String urlApi = EBAGS_API + decoder +"&batchSize="+fullPageCount+"&itemsLoaded=0";
				newUrlValues.add(urlApi);
				//newUrlValues.add(url + "#from"+i*Integer.parseInt(fullPageCount));
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Ebags list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	private  String pattern(String pageCount){
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(pageCount);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
}

