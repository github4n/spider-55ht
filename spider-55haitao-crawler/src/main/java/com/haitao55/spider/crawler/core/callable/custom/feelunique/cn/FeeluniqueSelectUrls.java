package com.haitao55.spider.crawler.core.callable.custom.feelunique.cn;






import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
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
import com.haitao55.spider.crawler.utils.Constants;

public class FeeluniqueSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String parentUrl = context.getUrl().getParentUrl();
		String url = context.getCurrentUrl();
		String content = crawlerUrl(context,url,parentUrl);
		List<String> newUrlValues = new ArrayList<String>();
		if(StringUtils.isNotBlank(content)){
			Pattern p = Pattern.compile("\"url_path\":\"(.*?)\",");
			Matcher m = p.matcher(content);
			while(m.find()){
				String productUrl = m.group(1);
				productUrl = productUrl.replaceAll("[\\\\]", "");
				newUrlValues.add(productUrl);
			}
		}

		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("FeeluniqueSelectUrls item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
	private String crawlerUrl(Context context,String url,String parentUrl) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders(parentUrl)).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders(parentUrl)).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	private static Map<String,Object> getHeaders(String parentUrl){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		 headers.put("Accept", "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*; q=0.01");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Host", "cn.feelunique.com");
		 headers.put("Referer", parentUrl);
		 headers.put("X-Requested-With", "XMLHttpRequest");
		 headers.put("Cookie", "_uuid=42C6A44F-339C-4533-AF3F-5D50DD04AD31; CACHED_FRONT_FORM_KEY=5aOh4meVn1rvokpL; SERVER_ID=8a54179b-dc620426; Qs_lvt_169767=1504334051%2C1504681022%2C1504751569; Qs_pv_169767=2007569198997790000%2C4302610400900554000%2C1469955728204846600%2C3300557603081560000%2C701725176119030100; mediav=%7B%22eid%22%3A%22417541%22%2C%22ep%22%3A%22%22%2C%22vid%22%3A%22Qa6i8fA8%3FB%3A%3F_%27+E%28L.o%22%2C%22ctn%22%3A%22%22%7D; VIEWED_PRODUCT_IDS=129802%2C136134%2C121984%2C141694%2C141692; external_no_cache=1; scarab.mayAdd=%5B%7B%22i%22%3A%2250219-27549%22%7D%2C%7B%22i%22%3A%2240265-0%22%7D%2C%7B%22i%22%3A%2240264-0%22%7D%2C%7B%22i%22%3A%2265873-29061%22%7D%2C%7B%22i%22%3A%2232354-0%22%7D%2C%7B%22i%22%3A%2267345-30034%22%7D%2C%7B%22i%22%3A%2267345-30036%22%7D%2C%7B%22i%22%3A%2215074-0%22%7D%2C%7B%22i%22%3A%2263224-0%22%7D%2C%7B%22i%22%3A%2245726-0%22%7D%5D; __xsptplusUT_718=1; CATEGORY_INFO=%5B%5D; LAST_CATEGORY=3969; sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%2215c9b6ac41f28c-0f224ea8d6cd69-24414032-1049088-15c9b6ac4214ef%22%2C%22props%22%3A%7B%22%24latest_referrer%22%3A%22%22%2C%22%24latest_referrer_host%22%3A%22%22%7D%7D; Hm_lvt_32ddf50f15b8e3b56c7e0a49202c521e=1504333752; Hm_lpvt_32ddf50f15b8e3b56c7e0a49202c521e=1505887887; __xsptplus718=718.13.1505885902.1505887886.14%233%7Cjson.cn%7C%7C%7C%7C%23%233OvRNrm0KxSQbadrdpxa787CavHggUog%23; frontend=jq14sc1l8fdo6itr5bp478tgs3; scarab.visitor=%226A012990BC6CBE74%22");
		return headers;
	}
}
