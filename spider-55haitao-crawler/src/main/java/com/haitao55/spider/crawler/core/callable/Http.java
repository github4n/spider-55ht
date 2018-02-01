package com.haitao55.spider.crawler.core.callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.base.CallableUtils;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.xobject.XHtml;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：提供直接使用http爬取页面功能
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:39:57
 * @version 1.0
 */
public class Http extends AbstractCallable {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	// 出现IO异常后的重试次数，默认3次
	private int retry = 3;

	// 超时时间，默认10000ms
	private int timeout = 10000;

	// 页面的字符集，默认取header或者meta中的字符集
	private String charset;

	public void invoke(Context context) throws Exception {
		String url = getInputString(context);// 尝试从context中取值
		if (StringUtils.isBlank(url)) {// context中没有则直接使用input
			url = getInput();
		}
		// 解析url中的变量
		url = CallableUtils.parse(context, url);
		if (StringUtils.contains(url, "${")) {
			logger.warn("url {} seems contains ${}", url);
		}

		String result = "";
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isNotBlank(proxyRegionId)) {// 1.使用代理
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String ip = proxy.getIp();
			int port = proxy.getPort();

			logger.info("Use Proxy Ip in crawling::url:{};proxyIp:{};proxyPort:{}", url, ip, port);// 打印标识日志

			result = Crawler.create().timeOut(getTimeout()).retry(getRetry()).charset(getCharset()).url(url).proxy(true)
					.proxyAddress(ip).proxyPort(port).resultAsString();
		} else {// 2.不使用代理
			logger.info("DO NOT Use Proxy Ip in crawling::url:{}", url);// 打印标识日志
			result = Crawler.create().timeOut(getTimeout()).retry(getRetry()).charset(getCharset()).url(url)
					.resultAsString();
		}

		XHtml xhtml = new XHtml(url, result);
		setOutput(context, xhtml);
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("input", CallableUtils.subString(getInput(), 512));
		builder.append("retry", getRetry());
		builder.append("timeout", getTimeout());
		builder.append("charset", getCharset());
		builder.append("output", getOutput());
		return builder.toString();
	}
}