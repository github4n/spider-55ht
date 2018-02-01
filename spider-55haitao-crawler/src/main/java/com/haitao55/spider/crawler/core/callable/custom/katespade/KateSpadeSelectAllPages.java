package com.haitao55.spider.crawler.core.callable.custom.katespade;

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
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: KateSpadeSelectAllPages
  * @Description: kate category page
  * @author songsong.xu
  * @date 2016年11月19日 下午2:39:50
  *
 */
public class KateSpadeSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private int grade;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("#navigation > ul.menu-category > li a");
		List<String> newUrlValues = new ArrayList<String>();
		if(es != null && es.size() > 0 ){
			for(Element ele : es){
				String link = JsoupUtils.attr(ele, "href");
				if(StringUtils.isBlank(link)){
					continue;
				}
				if(StringUtils.contains(link, "?")){
					link = StringUtils.substringBefore(link, "?");
				}
				try{
					Url currentUrl = new Url(link);
					currentUrl.setTask(context.getUrl().getTask());
					content = HttpUtils.get(currentUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
					Document doc = JsoupUtils.parse(content);
					Elements eles = doc.select("div.search-result-count > span");
					String resultCount = StringUtils.substringBetween(eles.html(), "(", ")");
					int page = 0;
					if(StringUtils.isNotBlank(resultCount)){
						int count = Integer.valueOf(resultCount);
						page = (count + 99 -1)/99;
					}
					for(int i = 0 ; i < page;i++ ){
						String newLink = link + "?sz=99&start="+i*99+"&instart_disable_injection=true";
						newUrlValues.add(newLink);
					}
					Thread.sleep(500);
				}catch(Throwable e){
					e.printStackTrace();
				}
				
			}
			logger.info("fetch {} categories url from katespade.com's init url",newUrlValues.size());
			
		} else {
			logger.error("Error while fetching categories url https://www.katespade.com");
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"katespade.com itemUrl:"+context.getUrl().toString()+" categories element size is 0");
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
		//section.menu.secondary.closed> div > div.columns.wide > div > ul > li > span > a
		String u = "https://www.katespade.com";
		Map<String,Object> headers = new HashMap<String,Object>();
		//headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36");
		String content = Crawler.create().method("get").timeOut(60000).url(u).proxy(true).proxyAddress("104.196.30.199").proxyPort(3128).retry(3).header(headers).resultAsString();
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("#navigation > ul.menu-category > li a");
		List<String> newUrlValues = new ArrayList<String>();
		if(es != null && es.size() > 0 ){
			for(Element ele : es){
				String text = JsoupUtils.text(ele);
				if(StringUtils.contains(text, "view all")){
					continue;
				}
				String link = JsoupUtils.attr(ele, "href");
				if(StringUtils.isBlank(link)){
					continue;
				}
				System.out.println(link ); 
			}
			logger.info("fetch {} categories url from katespade.com's init url",newUrlValues.size());
		} else {
			logger.error("Error while fetching categories url from katespade.com");
		}
	}

}
