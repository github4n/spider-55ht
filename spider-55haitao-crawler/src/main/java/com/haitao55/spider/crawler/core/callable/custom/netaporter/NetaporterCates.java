package com.haitao55.spider.crawler.core.callable.custom.netaporter;





import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class NetaporterCates extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private static final String BASE_URL = "https://www.net-a-porter.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		Map<String, Object> headers = new HashMap<>();
		headers.put("Cookie", setCookie());
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		
		List<String> newUrlValues = new ArrayList<String>();
		Document doc = Jsoup.parse(content);
		Elements es = doc.select("ul.nav-links li .nav-level-2-container a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				String url = e.attr("href");
				if(StringUtils.isNotBlank(url)){
					newUrlValues.add(BASE_URL+url);
				}
			}
		}
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("NetaporterCates url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
	private String setCookie(){
		String cookie ="geoIP=US; deviceType=Desktop; __gads=ID=ffb6e7dfde42773d:T=1491025481:S=ALNI_Mb1MKT0kPOrVLg9ZOqxlgCtPgXpwA; _ym_uid=149102548451383795; lang_iso=en; _ym_isad=2; JSESSIONID_AM=DC443343B37CDA656308F8168C919A21.nap-am-gs2-01; s_vnum=1493568000295%26vn%3D4; cr_dedup=false; JSESSIONID_INTL=8EF2D02735E31322EE8D3FD81F7DC242.nap-intl-gs2-08; channel=intl; country_iso=GB;";
		return cookie;
	}
}
