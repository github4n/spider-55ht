package com.haitao55.spider.crawler.core.callable.custom.sephora;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
  * @ClassName: SephoraSelectUrls
  * @Description: Sephora的列表页面处理器
  * @author denghuan
  * @date 2016年10月21日 下午6:29:50
  *
 */
public class SephoraSelectUrls extends SelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASEURL = "https://www.sephora.com";
	@Override
	public void invoke(Context context) throws Exception {

		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		String productData = StringUtils.substringBetween(content, "seph-pagedata>", "</script>");
		if(StringUtils.isNotBlank(productData)){
			productData = productData.replaceAll("[\\\\]", "");
			Pattern p = Pattern.compile("\"product_url\":\"(.*?)\",");
			Matcher m = p.matcher(productData);
			while(m.find()){
				String url = m.group(1);
				if(StringUtils.isNotBlank(url)){
					newUrlValues.add(BASEURL+url);
				}
			}
		}
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Sephora list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}

}