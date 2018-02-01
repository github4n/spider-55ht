package com.haitao55.spider.crawler.core.callable.custom.shopspring;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
  * @ClassName: ShopspringSelectUrls
  * @Description: Shopspring的列表页面处理器
  * @author denghuan
  * @date 2017年2月9日 下午6:29:50
  *
 */
public class ShopspringCateSelectUrls extends SelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASEURL = "https://www.shopspring.com/";
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Pattern pattern = Pattern.compile("tag\":\"(.*?)\"");
		Matcher matcher = pattern.matcher(content);
		while(matcher.find()){
			String str = matcher.group(1);
			String attr = str.replaceAll(":", "/");
			newUrlValues.add(BASEURL+attr);
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Shopspring list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}