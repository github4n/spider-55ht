package com.haitao55.spider.crawler.core.callable.custom.lordandtaylor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class SelectLordAndTaylorUrls_two extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String ATTR_HREF = "href";
	private static final String DEFAULT_NEW_URLS_TYPE = UrlType.LINK.getValue();
	public int grade;

	// css选择器
	private String css;
	// 使用css选择器进行选择后,取元素的什么属性值；默认是href
	private String attr = ATTR_HREF;
	// 迭代出来的newurls的类型,链接类型还是商品类型,默认是链接类型
	public String type = DEFAULT_NEW_URLS_TYPE;
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		return headers;
	}
	@Override
	public void invoke(Context context) throws Exception {
		String content = "";
		String url = context.getUrl().getValue();
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(url, getHeaders());
			context.setHtmlPageSource(content);
		}else{
			String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
			if (StringUtils.isBlank(proxyRegionId)) {
				content = Crawler.create().timeOut(6000).url(context.getCurrentUrl()).header(getHeaders())
						.method(HttpMethod.GET.getValue()).resultAsString();
			} else {
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String proxyAddress = proxy.getIp();
				int proxyPort = proxy.getPort();
				content = Crawler.create().timeOut(6000).url(context.getCurrentUrl()).header(getHeaders())
						.method(HttpMethod.GET.getValue()).proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort)
						.resultAsString();
			}
		}
		Document doc = JsoupUtils.parse(content);
		Elements elements = doc.select(css);
		if (CollectionUtils.isNotEmpty(elements)) {
			List<String> newUrlValues = new ArrayList<String>();
			for (Element element : elements) {
				String urltemp = element.html();
				newUrlValues.add(urltemp);		
			}
			Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);

			// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
			context.getUrl().getNewUrls().addAll(newUrls);
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), css);
		}
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}

}
