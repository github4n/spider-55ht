package com.haitao55.spider.crawler.core.callable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.xobject.XHtml;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：提供使用htmlunit爬取页面功能
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:39:25
 * @version 1.0
 */
public class HtmlUnit extends AbstractCallable {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_PARSER);

	private static final int DEFAULT_TIME_OUT = 30 * 1000;

	private static final int DEFAULT_RETRY = 3;

	// 超时时间，默认30秒
	private int timeout = DEFAULT_TIME_OUT;

	// 重试次数
	private int retry = DEFAULT_RETRY;

	// 是否支持javascript
	private boolean javascript;

	// 是否支持ajax
	private boolean ajax;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public boolean isJavascript() {
		return javascript;
	}

	public void setJavascript(boolean javascript) {
		this.javascript = javascript;
	}

	public boolean isAjax() {
		return ajax;
	}

	public void setAjax(boolean ajax) {
		this.ajax = ajax;
	}

	@Override
	public void invoke(Context context) throws Exception {
		String url = getInputString(context);
		if (StringUtils.isBlank(url)) {
			url = getInput();
		}

		String html = "";
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isNotBlank(proxyRegionId)) {// 使用代理
			Proxy proxyIp = ProxyCache.getInstance().pickup(proxyRegionId, true);
			html = doCrawl(url, proxyIp);
		} else {// 不使用代理
			html = doCrawl(url);
		}

		XHtml xhtml = new XHtml(url, html);
		setOutput(context, xhtml);
	}

	private String doCrawl(String url) throws Exception {
		return this.doCrawl(url, null);
	}

	private String doCrawl(String url, Proxy proxy) throws Exception {
		HtmlPage page = null;
		WebClient webClient = null;
		try {
			webClient = new WebClient(BrowserVersion.FIREFOX_24);
			if (proxy != null) {// 使用代理
				ProxyConfig proxyConfig = new ProxyConfig();
				proxyConfig.setProxyHost(proxy.getIp());
				proxyConfig.setProxyPort(proxy.getPort());
				webClient.getOptions().setProxyConfig(proxyConfig);
			}
			webClient.getOptions().setCssEnabled(true);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setTimeout(getTimeout());
			webClient.getOptions().setUseInsecureSSL(true);
			webClient.getOptions().setAppletEnabled(false);
			webClient.getOptions().setActiveXNative(false);
			webClient.getOptions().setJavaScriptEnabled(isJavascript());// 是否启用javascript
			if (isAjax()) {// 是否启用ajax
				webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			}
			// add Referer to header resquest
			WebRequest webRequest = new WebRequest(UrlUtils.toUrlUnsafe(url));
			page = crawlInternal(webClient, webRequest);
			if (page == null) {
				return StringUtils.EMPTY;
			}
			return page.asXml();
		} finally {
			if (page != null) {
				page.cleanUp();
			}
			if (webClient != null) {
				webClient.closeAllWindows();
			}
		}
	}

	/**
	 * retry 3次
	 * 
	 * @param webClient
	 * @param webRequest
	 * @return
	 */
	private HtmlPage crawlInternal(WebClient webClient, WebRequest webRequest) {
		for (int i = 0; i < retry; i++) {
			try {
				return webClient.getPage(webRequest);
			} catch (Exception e) {
				logger.error("HtmlUnit :: {} :: {}", e.getMessage(), e);
			}
		}
		return null;
	}
}