package com.haitao55.spider.crawler.core.callable.custom.askderm;

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
* @ClassName: AskdermSelectUrls
* @Description: list page parser
* @author songsong.xu
* @date 2017年6月6日
*
 */
public class AskdermSelectUrls extends SelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String ITEM_CSS_SELECTOR = "div.grid-uniform > div > div.product-container > div.product-image > div.product-thumbnail > a";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getUrl().toString();
		List<String> newUrlValues = new ArrayList<String>();
		Document d = JsoupUtils.parse(content,url);
		Elements es = d.select(ITEM_CSS_SELECTOR);
		if(es != null && es.size() > 0 ){
		    es.forEach( ele -> {
		        String link = ele.attr("abs:href");;
                if(StringUtils.isBlank(link)){
                    return;
                }
                if(StringUtils.contains(link, "?")){
                    link = StringUtils.substringBefore(link, "?");
                }
                newUrlValues.add(link);
		    });
		} else {
			logger.error("Error while fetching items from Askderm.com's category page");
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"Askderm.com category url :"+context.getUrl().toString()+" cateories is not found.");
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Askderm.com list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String url = "https://askderm.com/collections/all?page=2";
		Map<String,Object> headers = new HashMap<String,Object>();
		String content = Crawler.create().method("get").timeOut(30000).url(url).proxy(true).proxyAddress("159.203.15.211").proxyPort(3128).retry(3).header(headers).resultAsString();
		Document doc = JsoupUtils.parse(content,url);
		Elements es = doc.select(ITEM_CSS_SELECTOR);
		for(Element e : es){
			System.out.println(e.attr("abs:href"));
		}
	}

}