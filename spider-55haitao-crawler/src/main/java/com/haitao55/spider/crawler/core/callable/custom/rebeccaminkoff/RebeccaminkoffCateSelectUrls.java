package com.haitao55.spider.crawler.core.callable.custom.rebeccaminkoff;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
  * @ClassName: RebeccaminkoffCateSelectUrls
  * @Description: Shopspring的列表页面处理器
  * @author denghuan
  * @date 2017年2月9日 下午6:29:50
  *
 */
public class RebeccaminkoffCateSelectUrls extends SelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASE_URL = "https://www.rebeccaminkoff.com";
	@Override
	public void invoke(Context context) throws Exception {
		String content = crawlerUrl(context,context.getCurrentUrl());
		List<String> newUrlValues = new ArrayList<String>();
		Document doc = Jsoup.parse(content);
		Elements es = doc.select(".header-nav .header-nav--item .header-nav-item--sub-menu .header-sub-menu-inner a");
		for(Element e : es){
			String url = e.attr("href");
			if(StringUtils.isNotBlank(url)){
				newUrlValues.add(BASE_URL+url+"?limit=all");
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Rebeccaminkoff list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
	private String crawlerUrl(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content =CurlCrawlerUtil.get(url, 20);
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = CurlCrawlerUtil.get(url, 20, proxyAddress, proxyPort);
		}
		return content;
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
//		String content = Crawler.create().timeOut(30000).url("https://www.rebeccaminkoff.com/").method(HttpMethod.GET.getValue())
//				.proxy(true).proxyAddress("104.196.30.199").proxyPort(22088).resultAsString();
		String content =  CurlCrawlerUtil.get("https://www.rebeccaminkoff.com/collections/fave-fall-shoes?limit=all", 20, "104.196.30.199", 22088);
		Document doc = Jsoup.parse(content);
		Elements es = doc.select(".collection-grid--block-img a");
		for(Element e : es){
			String url = e.attr("href");
			if(StringUtils.isNotBlank(url)){
				System.out.println(url);
			}
		}
	}
	
}