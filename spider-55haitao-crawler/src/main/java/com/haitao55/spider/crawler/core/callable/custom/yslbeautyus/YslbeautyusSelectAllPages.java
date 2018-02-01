package com.haitao55.spider.crawler.core.callable.custom.yslbeautyus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: YslbeautyusSelectAllPages
  * @Description: 分类页面爬取
  * @author songsong.xu
  * @date 2016年11月28日 下午2:32:50
  *
 */
public class YslbeautyusSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private int grade;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		Document d = JsoupUtils.parse(content);
		//div.main_navigation_wrapper > div.navigation.js_sticky > div > ul > li > div.navigation_dropdown > div.content > ul > li a
		Elements es = d.select("div.main_navigation_wrapper > div.navigation > div.content > ul > li > div.navigation_dropdown > div.content > ul > li > a");
		List<String> newUrlValues = new ArrayList<String>();
		if(es != null && es.size() > 0 ){
			for(Element ele : es){
				String link = JsoupUtils.attr(ele, "href");
				if(StringUtils.isBlank(link) || !StringUtils.startsWith(link, "http://")){
					continue;
				}
				if(StringUtils.contains(link, "?")){
					link = StringUtils.substringBefore(link, "?");
				}
				newUrlValues.add(link+"?sz=999&viewall=1&format=ajax");
			}
			logger.info("fetch {} categories url from yslbeautyus.com's init url",newUrlValues.size());
			
		} else {
			logger.error("Error while fetching categories url https://www.yslbeautyus.com");
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"yslbeautyus.com itemUrl:"+context.getUrl().toString()+" categories element size is 0");
		}
		Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);
		context.getUrl().getNewUrls().addAll(value);
	}
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}

	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String u = "http://www.yslbeautyus.com/";
		Map<String,Object> headers = new HashMap<String,Object>();
		String content = Crawler.create().method("get").timeOut(30000).url(u).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).header(headers).resultAsString();
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("div.main_navigation_wrapper > div.navigation > div.content > ul > li > div.navigation_dropdown > div.content > ul > li a");
		
		//Elements es = d.select("div.main_navigation_wrapper > div.navigation");
		for(Element e : es){
			System.out.println(JsoupUtils.attr(e, "href"));
		}
	}

}
