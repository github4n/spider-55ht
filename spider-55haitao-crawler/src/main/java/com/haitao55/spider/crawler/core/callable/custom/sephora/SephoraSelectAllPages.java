package com.haitao55.spider.crawler.core.callable.custom.sephora;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * 
  * @ClassName: Sephora
  * @Description: Sephora的
  * @author denghuan
  * @date 2016年10月21日 下午2:19:57
  *
 */
public class SephoraSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private String SEOHORA_API="http://www.sephora.com/rest/products/?currentPage=";
	private  static final  String SEOHORA_SUFFIX = "&include_categories=true&include_refinements=true";
	private  static final String SHOP_API="http://www.res-x.com/ws/r2/Resonance.aspx?appid=sephora01&tk=805375703600887&bx=true&sc=content1_rr&no=1000&page=1&categoryid=";

	@Override
	public void invoke(Context context) throws Exception {
		try {
			String content = super.getInputString(context);
			String currentUrl = context.getCurrentUrl();
			String categoryName= currentUrl.substring(currentUrl.lastIndexOf("/")+1,currentUrl.length());
			//String content = HttpUtils.get(currentUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES);
			List<String> newUrlValues = new ArrayList<String>();
			Set<Url> value = null;
			if(StringUtils.isNotBlank(content)){
				String pageSize = StringUtils.substringBetween(content, "{\"page_size\":", ",");
				String pageTotal = StringUtils.substringBetween(content, "total_products\":", "}");
				if(StringUtils.isNotBlank(pageTotal)){
					Double page  = Double.valueOf(pageTotal) / Double.valueOf(pageSize);
					int pageNumber=  (int)Math.ceil(page);
					for(int i = 1 ;i <= pageNumber; i++){
						newUrlValues.add(SEOHORA_API+""+i+"&categoryName="+categoryName+""+SEOHORA_SUFFIX);
					}
					value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);
				}else{
					String categoryId =  StringUtils.substringBetween(content, "categoryid\": \"", "\"}");
					Url cUrl = new Url(SHOP_API+categoryId);
					cUrl.setTask(context.getUrl().getTask());
					String productUrls = HttpUtils.get(cUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES);
					Pattern p = Pattern.compile("\"product_url\":\"(.*?)\",");
					Matcher m = p.matcher(productUrls);
					while(m.find()){
						newUrlValues.add(m.group(1));
					}
					value = this.buildNewUrls(newUrlValues, context, "ITEM",3);
				}
			}
			if(value != null){
				context.getUrl().getNewUrls().addAll(value);
			}
		} catch (Exception e) {
			logger.error("Sephora crawling list url {} ,exception {}", context.getCurrentUrl(),e);
		}
		
	}
}
