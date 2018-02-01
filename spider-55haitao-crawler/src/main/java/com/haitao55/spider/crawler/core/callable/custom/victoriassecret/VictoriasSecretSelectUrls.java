package com.haitao55.spider.crawler.core.callable.custom.victoriassecret;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
  * @ClassName: VictoriasSecretSelectUrls
  * @Description: 维多利亚的秘密 获取列表页面的
  * @author songsong.xu
  * @date 2016年11月8日 下午6:04:07
  *
 */
public class VictoriasSecretSelectUrls extends SelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASEURL = "https://www.victoriassecret.com";
	@Override
	public void invoke(Context context) throws Exception {

		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Pattern p = Pattern.compile("<a[^>]*href=\\\"(.*?)\\\"[^>]*itemprop=\\\"url\\\"");
		Matcher m = p.matcher(content);
		while(m.find()){
			newUrlValues.add(BASEURL+StringUtils.replace(m.group(1), "&amp;", "&"));
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("victoriassecret.com list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String url = "https://www.victoriassecret.com/gifts/50-and-under/more?increment=180&location=0&sortby=REC";
		Map<String,Object> headers = new HashMap<String,Object>();
		String content = Crawler.create().method("get").timeOut(30000).url(url).retry(3).header(headers).resultAsString();
		Pattern p = Pattern.compile("<a[^>]*href=\\\"(.*?)\\\"[^>]*itemprop=\\\"url\\\"");
		Matcher m = p.matcher(content);
		while(m.find()){
			System.out.println(m.group(1));
		}
	}

}