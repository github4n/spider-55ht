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
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
  * @ClassName: MarcjacobsSelectUrls
  * @Description: marcjacobs list page
  * @author songsong.xu
  * @date 2016年11月23日 下午12:51:20
  *
 */
public class MarcjacobsSelectUrls extends SelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private final int pageSize = 12;
	private static final String ITEM_CSS_SELECTOR = "ul#search-result-items > li.grid-tile > div > a";
	
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
		List<String> newUrlValues = new ArrayList<String>();
		Map<String,String> cache = new HashMap<String,String>();
		Document d = JsoupUtils.parse(content,url);
		//#c426fcb699c628ba994a9fd394 > div.product-image > a
		Elements es = d.select(ITEM_CSS_SELECTOR);
		if(es == null || es.size() == 0 ){
			logger.error("Error while fetching items from marcjacobs.com's category page");
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"marcjacobs.com category url :"+context.getUrl().toString()+" cateories is not found.");
		}
		Elements elements = d.select("ul#search-result-items > li.grid-tile > div");
		for(Element ele : elements){
			String itemId = JsoupUtils.attr(ele, "data-itemid");
			cache.put(itemId, itemId);
		}
		createSeeds(es, newUrlValues);
		int exeNum = 0;
		boolean running = true;
		while(running && exeNum < 5){
			try{
				String start = StringUtils.substringBetween(url, "&start=", "&");
				int next = Integer.valueOf(start);
				if(next > 300){
					break;
				}
				String nextUrl = StringUtils.replace(url, "&start="+start, "&start="+(next+pageSize));
				url = nextUrl;
				Url currentUrl = new Url(nextUrl);
				currentUrl.setTask(context.getUrl().getTask());
				content = HttpUtils.get(currentUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,true);
				d = JsoupUtils.parse(content,url);
				es = d.select(ITEM_CSS_SELECTOR);
				elements = d.select("ul#search-result-items > li.grid-tile > div");
				for(Element ele : elements){
					String itemId = JsoupUtils.attr(ele, "data-itemid");
					String item_id = cache.get(itemId);
					if(item_id == null){
						cache.put(itemId, itemId);
					} else {
						running = false;
						break;
					}
				}
				createSeeds(es, newUrlValues);
				Thread.sleep(3000);
			}catch(Throwable e){
				e.printStackTrace();
				Thread.sleep(2000);
				exeNum++;
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("marcjacobs.com list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	private void createSeeds(Elements es, List<String> newUrlValues) {
		if(es != null && es.size() > 0 ){
			for(Element e : es){
				String link = JsoupUtils.attr(e, "abs:href");
				if(StringUtils.isBlank(link)){
					continue;
				}
				if(StringUtils.contains(link, "?")){
					link = StringUtils.substringBefore(link, "?");
				}
				newUrlValues.add(link);
				//System.out.println(link);
			}
		}
	}
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String url = "https://www.marcjacobs.com/women/accessories/?sz=12&start=8148&format=page-element&resultType=product";
		String content = Crawler.create().timeOut(10000).retry(3).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).url(url).resultAsString();
		Document d = JsoupUtils.parse(content,url);
		//#c426fcb699c628ba994a9fd394 > div.product-image > a
		Elements es = d.select(ITEM_CSS_SELECTOR);
		if(es != null && es.size() > 0 ){
			for(Element e : es){
				String link = JsoupUtils.attr(e, "abs:href");
				if(StringUtils.isBlank(link)){
					continue;
				}
				if(StringUtils.contains(link, "?")){
					link = StringUtils.substringBefore(link, "?");
				}
				System.out.println(link);
			}
		}
	}

}