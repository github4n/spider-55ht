package com.haitao55.spider.crawler.core.callable.custom.footlocker;





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
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class FootlockerSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String url = context.getCurrentUrl();
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isNotBlank(proxyRegionId)) {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String ip = proxy.getIp();
			int port = proxy.getPort();
			content = CurlCrawlerUtil.get(url,20,ip,port);
			logger.info("use proxy ip {}, port {},url {}",ip,port,url);
		} else {
			content = CurlCrawlerUtil.get(url);
		}
		
		List<String> newUrlValues = new ArrayList<String>();
		
		String pageTotal = StringUtils.substringBetween(content, "endeca_cm_recordCount = ", ";");

		if(StringUtils.isNotBlank(pageTotal) 
				&& !"0".equals(pageTotal)){
			Double page  = Double.valueOf(pageTotal) / 60;
			int pageNumber =  (int)Math.ceil(page);
			for(int i = 0 ;i < pageNumber; i++){
				newUrlValues.add(url + "?Nao="+i*60+"&cm_PAGE="+i*60+"");
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("FootlockerSelectAl list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String html = Crawler.create().timeOut(60000).retry(3).proxy(true).proxyAddress("114.55.10.105").proxyPort(3128).url("view-source:https://www.footlocker.com/Mens/Casual/Shoes/_-_/N-24Z1h5Zrj").resultAsString();

		//String html = CurlCrawlerUtil.get("https://www.footlocker.com/", 20, "114.55.10.105", 3128);
		
		Document  doc = Jsoup.parse(html);
		int count = 0;
		Elements es = doc.select(".shop_sub_menu a");
		for(Element e : es){
			count ++;
			String url = e.attr("href");
			System.out.println(url);
		}
		System.out.println(count);
	}
	
}
