package com.haitao55.spider.crawler.core.callable.custom.nordstrom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  * @ClassName: NordstromSelectUrls
  * @Description: nordstrom的列表页面处理器
  * @author songsong.xu
  * @date 2016年10月19日 下午6:29:50
  *
 */
public class NordstromSelectUrls extends SelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASEURL = "http://shop.nordstrom.com";
	@Override
	public void invoke(Context context) throws Exception {

		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Pattern p = Pattern.compile("\"ProductPageUrl\":\"(.*?)\",");
		Matcher m = p.matcher(content);
		while(m.find()){
			newUrlValues.add(BASEURL+m.group(1));
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Nordstrom list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String url = "http://shop.nordstrom.com/api/c/trend-night-out?top=66&page=9";
		String content = Crawler.create().timeOut(30000).url(url).retry(3).proxy(true).proxyAddress("128.199.118.208").proxyPort(3128).resultAsString();
		Pattern p = Pattern.compile("\"ProductPageUrl\":\"(.*?)\",");
		Matcher m = p.matcher(content);
		while(m.find()){
			System.out.println(BASEURL+m.group(1));
		}
		
	}

}