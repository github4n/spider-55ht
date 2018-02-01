package com.haitao55.spider.common.http;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.Constants;

/**
 * 
 * 功能：通用工具，通过发送http请求，获取网页内容
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午2:42:02
 * @version 1.0
 */
public class Crawler {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_HTTP);

	private Options options = new Options();

	public static Crawler create() {
		return new Crawler();
	}

	public Crawler url(String url) {
		getOptions().setUrl(url);
		return this;
	}

	public Crawler timeOut(int timeOutInMills) {
		getOptions().setTimeOut(timeOutInMills);
		return this;
	}

	public Crawler charset(String charset) {
		getOptions().setCharset(charset);
		return this;
	}

	public Crawler retry(int maxCount) {
		getOptions().setRetry(maxCount);
		return this;
	}

	public Crawler method(String method) {
		HttpMethod hMethod = HttpMethod.codeOf(method);
		if (hMethod == null) {
			throw new IllegalArgumentException("illegal method " + method);
		}
		getOptions().setMethod(hMethod);
		return this;
	}

	public Crawler proxy(boolean useProxy) {
		getOptions().setUseProxy(useProxy);
		return this;
	}

	public Crawler proxyAddress(String proxyAddr) {
		getOptions().setProxyAddr(proxyAddr);
		return this;
	}

	public Crawler proxyPort(int proxyPort) {
		getOptions().setProxyPort(proxyPort);
		return this;
	}

	public Crawler header(String name, Object value) {
		getOptions().getHeaders().put(name, value);
		return this;
	}

	public Crawler header(Map<String, Object> headers) {
		getOptions().getHeaders().putAll(headers);
		return this;
	}

	public Crawler payload(Map<String, Object> payload) {
		getOptions().getPayload().putAll(payload);
		return this;
	}

	public Crawler payload(String name, Object value) {
		getOptions().getPayload().put(name, value);
		return this;
	}

	public Crawler payload(String payload) {
		getOptions().getPayload().put(payload, null);
		return this;
	}

	public Crawler payloadCharset(String payloadCharset) {
		getOptions().setPayloadCharset(payloadCharset);
		return this;
	}

	public String resultAsString() throws ClientProtocolException, HttpException, IOException {
		HttpResult result = getResult();
		checkHttpStatus(result);
		if (getOptions().getCharset() != null) {
			result.setCharset(getOptions().getCharset());
		}
		return result.getContentAsString();
	}

	public byte[] resultAsBytes() throws ClientProtocolException, HttpException, IOException {
		HttpResult result = getResult();
		checkHttpStatus(result);
		return result.getContent();
	}

	public HttpResult result() throws ClientProtocolException, IOException {
		return getResult();
	}

	private void checkHttpStatus(HttpResult result) throws HttpException {
		Options options = getOptions();
		String url = options.getUrl();

		if (result == null) {
			throw new HttpException("http got null from url " + url);
		}

		int status = result.getStatus();
		if (status != 200) {
			HttpException e = new HttpException("http error,status " + status + ",url " + url);
			e.setStatus(status);
			throw e;
		}
	}

	private HttpResult getResult() throws ClientProtocolException, HttpException, IOException {
		Options options = getOptions();
		String url = options.getUrl();
		int retry = options.getRetry();
		int timeoutMills = options.getTimeOut();
		HttpMethod method = options.getMethod();

		boolean useProxy = options.isUseProxy();
		String proxyAddr = options.getProxyAddr();
		int proxyPort = options.getProxyPort();

		if (StringUtils.isBlank(url)) {
			throw new IllegalArgumentException("invalid url " + url);
		}
		if (useProxy && StringUtils.isBlank(proxyAddr)) {
			throw new IllegalArgumentException("proxy address:" + proxyAddr + " is invalid.");
		}
		if (useProxy && proxyPort <= 0) {
			throw new IllegalArgumentException("proxy port:" + proxyPort + " is invalid.");
		}

		// support http only :)
		if (!StringUtils.startsWithIgnoreCase(url, "http")) {
			url = "http://" + url;
		}

		if (retry <= 0) {
			retry = 1;
		}

		if (timeoutMills <= 0) {
			timeoutMills = 5000;
		}

		HttpResult result = null;
		for (int count = 0; count < retry; count++) {
			try {
				if (HttpMethod.GET.equals(method)) {
					Map<String, Object> headers = options.getHeaders();
					if (useProxy) {
						result = Holder.http.get(url, timeoutMills, proxyAddr, proxyPort,headers);
					} else {
						result = Holder.http.get(url, timeoutMills, headers);
					}
				} else if (HttpMethod.HEAD.equals(method)) {
					Map<String, Object> headers = options.getHeaders();
					result = Holder.http.head(url, timeoutMills, headers);
				} else if (HttpMethod.POST.equals(method)) {
					Map<String, Object> headers = options.getHeaders();
					Map<String, Object> entity = options.getPayload();
					String payloadCharset = options.getPayloadCharset();
					if (useProxy) {
						result = Holder.http.post(url, headers, entity, timeoutMills, payloadCharset, proxyAddr, proxyPort);
					}else{
						result = Holder.http.post(url, headers, entity, timeoutMills, payloadCharset);
					}
				} else {
					throw new IllegalArgumentException("not support method " + method);
				}
				return result;
			} catch (ClientProtocolException e) {
				throw e;
				// needs to throw HttpException,we can't catch all
				// exception
			} catch (IOException e) {
				if (count >= retry) {
					throw e;
				} else {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					logger.error("{} got error,retrying {},proxyAddr {},proxyPort {}", url, count, proxyAddr, proxyPort, e);
				}
			}
		}

		return null;
	}

	private Options getOptions() {
		return options;
	}

	/**
	 * 
	 * 功能：Crawler类的内部类，用于维护客户端传递进Crawler内部的各项属性
	 *
	 * @author Arthur.Liu
	 * @time Jul 15, 2015 12:25:42 PM
	 * @version 1.0
	 *
	 */
	private static class Options {

		private String url;

		private int retry = 1;

		private int timeOut = 5000;

		private HttpMethod method = HttpMethod.GET;

		private String charset;

		private String payloadCharset;

		private Map<String, Object> headers = new LinkedHashMap<String, Object>();

		private Map<String, Object> payload = new LinkedHashMap<String, Object>();

		private boolean useProxy = false;

		private String proxyAddr;

		private int proxyPort;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public int getRetry() {
			return retry;
		}

		public void setRetry(int retry) {
			this.retry = retry;
		}

		public int getTimeOut() {
			return timeOut;
		}

		public void setTimeOut(int timeOut) {
			this.timeOut = timeOut;
		}

		public HttpMethod getMethod() {
			return method;
		}

		public void setMethod(HttpMethod method) {
			this.method = method;
		}

		public String getCharset() {
			return charset;
		}

		public void setCharset(String charset) {
			this.charset = charset;
		}

		public String getPayloadCharset() {
			return payloadCharset;
		}

		public void setPayloadCharset(String payloadCharset) {
			this.payloadCharset = payloadCharset;
		}

		public Map<String, Object> getHeaders() {
			return headers;
		}

		@SuppressWarnings("unused")
		public void setHeaders(Map<String, Object> headers) {
			this.headers = headers;
		}

		public Map<String, Object> getPayload() {
			return payload;
		}

		@SuppressWarnings("unused")
		public void setPayload(Map<String, Object> payload) {
			this.payload = payload;
		}

		public boolean isUseProxy() {
			return useProxy;
		}

		public void setUseProxy(boolean useProxy) {
			this.useProxy = useProxy;
		}

		public String getProxyAddr() {
			return proxyAddr;
		}

		public void setProxyAddr(String proxyAddr) {
			this.proxyAddr = proxyAddr;
		}

		public int getProxyPort() {
			return proxyPort;
		}

		public void setProxyPort(int proxyPort) {
			this.proxyPort = proxyPort;
		}
	}

	private static class Holder {// 用这种方式实现单例类，是最保险和安全的
		public static final HttpClient http = new HttpClient();
	}

	public static void main(String[] args) throws ClientProtocolException, IOException, HttpException {

		String payload = "dataCount=&pageCount=&pageNo=2&cacheSize=40&keyword=&maker=&seasonFLG=&Bunrui1=1&Bunrui2=1&bunrui3=1&minPrice=&maxPrice=&size=&sizeflg=&sortOrder=&listKBN=&shubetsu=&sBrand_NG=-&errormsg=&requestNb=1";
		System.out.println(
				Crawler.create().timeOut(10000).url("http://www.jshoppers.com/gb/kensaku2_if.asp").method("post")
						.header("Content-Type", "application/x-www-form-urlencoded").payload(payload).resultAsString());
	}
}