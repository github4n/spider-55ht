package com.haitao55.spider.crawler.core.callable.custom.marcjacobs;

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
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: MarcjacobsSelectAllPages
  * @Description: marcjacobs category page
  * @author songsong.xu
  * @date 2016年11月23日 下午12:48:54
  *
 */
public class MarcjacobsSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private int grade;
	private final int pageSize = 12;
	
	@Override
	public void invoke(Context context) throws Exception {
	    String url = context.getUrl().getValue();
	    String content = StringUtils.EMPTY;
	    String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
        if(StringUtils.isNotBlank(proxyRegionId)){
            Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
            String proxyAddress=proxy.getIp();
            int proxyPort=proxy.getPort();
            content = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
                    .proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
        } else {
            content = super.getInputString(context);
        }
		Document d = JsoupUtils.parse(content);
		//
		Elements es = d.select("#navigation > ul > li.mobile-show > div > ul > li");
		List<String> newUrlValues = new ArrayList<String>();
		if(es != null && es.size() > 0 ){
			for(Element ele : es){
				String clazz = JsoupUtils.attr(ele, "class");
				if(StringUtils.contains(clazz,  "mobile-show")){
					continue;
				}
				String link = JsoupUtils.attr(ele.select("a"), "href");
				if(StringUtils.isBlank(link)){
					continue;
				}
				if(StringUtils.contains(link, "?")){
					link = StringUtils.substringBefore(link, "?");
				}
				if(StringUtils.contains(link, "o-women/")){
					link = StringUtils.replace(link, "o-women/", "");
				} else if(StringUtils.contains(link, "/accessories/")){
					link = StringUtils.replace(link, "women/", "");
				}
				StringBuilder sb = new StringBuilder(link);
				sb.append("?sz=").append(pageSize);
				sb.append("&start=0");
				sb.append("&format=page-element&resultType=product");
				newUrlValues.add(sb.toString());
			}
			logger.info("fetch {} categories url from marcjacobs.com's init url",newUrlValues.size());
			
		} else {
			logger.error("Error while fetching categories url https://www.marcjacobs.com");
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"marcjacobs.com itemUrl:"+context.getUrl().toString()+" categories element size is 0");
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
		String u = "https://www.marcjacobs.com";
		Map<String,Object> headers = new HashMap<String,Object>();
		//headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36");
		String content = Crawler.create().method("get").timeOut(30000).url(u).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).header(headers).resultAsString();
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("#navigation > ul > li.mobile-show > div > ul > li");
		if(es != null && es.size() > 0 ){
			for(Element ele : es){
				String link = JsoupUtils.attr(ele.select("a"), "href");
				if(StringUtils.isBlank(link)){
					continue;
				}
				System.out.println(link);
			}
		} else {
		}
	}

}
