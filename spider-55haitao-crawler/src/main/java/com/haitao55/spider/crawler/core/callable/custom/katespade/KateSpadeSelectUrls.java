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
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: KateSpadeSelectUrls
  * @Description: kate list page
  * @author songsong.xu
  * @date 2016年11月19日 下午2:57:01
  *
 */
public class KateSpadeSelectUrls extends SelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	@Override
	public void invoke(Context context) throws Exception {

		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("div.product-image > a.thumb-link");
		if(es != null && es.size() > 0 ){
			for(Element e : es){
				String link = JsoupUtils.attr(e, "href");
				if(StringUtils.isBlank(link)){
					continue;
				}
				if(!StringUtils.startsWith(link, "http")){
				    link = "https://www.katespade.com" + link;
				}
				newUrlValues.add(link);
			}
		} else {
			logger.error("Error while fetching 'window.dataLayer' from katespade.com's category page");
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"katespade.com category url :"+context.getUrl().toString()+" cateories is not found.");
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("katespade.com list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String url = "https://www.katespade.com/personalization/initial/?sz=99&start=0&instart_disable_injection=true";
		Map<String,Object> headers = new HashMap<String,Object>();
		String content = Crawler.create().method("get").timeOut(60000).url(url).proxy(true).proxyAddress("104.196.30.199").proxyPort(3128).retry(3).header(headers).resultAsString();
		Document doc = JsoupUtils.parse(content);
		Elements es = doc.select("div.product-image > a.thumb-link");
		for(Element e : es){
			System.out.println(e.attr("href"));
		}
	}

}