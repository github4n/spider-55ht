package com.haitao55.spider.common.http;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * 
 * 功能：http 实用类封装,不提供失败重试机制，需调用者自行保证<br>
 * 只支持get，head请求，post请求<br>
 * 默认最多支持300个并发，超时时间5000ms,UA是chrome 31
 * 
 * @author Arthur.Liu
 * @time 2016年8月19日 上午10:31:31
 * @version 1.0
 */
public class HttpClient {
	
	
	private final String HT_COOKIE_SPEC = "htCookieSpec";

	private CloseableHttpClient httpclient;

	private Handler handler;

	private PoolingHttpClientConnectionManager connManager;

	private static final int DEFAULT_MAX = 300;
	private static final int DEFAULT_TIME_OUT = 5000;
	private static final String DEFAULT_UA = UA.CHROME_31.getValue();

	public HttpClient() {
		this(DEFAULT_UA);
	}

	public HttpClient(String userAgent) {
		this(userAgent, new DefaultRedirectStrategy());
	}

	public HttpClient(String userAgent, RedirectStrategy strategy) {
		if (userAgent == null) {
			throw new IllegalArgumentException("userAgent can't be null");
		}
		if (strategy == null) {
			throw new IllegalArgumentException("strategy can't be null");
		}
		//Certificate
		LayeredConnectionSocketFactory ssl = null;
        try {
            SSLContext sslcontext = null;
            try {
                sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        return true;
                    }
                }).useSSL().build();
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                e.printStackTrace();
            }
            ssl = new SSLConnectionSocketFactory(sslcontext);
        } catch (final SSLInitializationException ex) {
            final SSLContext sslcontext;
            try {
                sslcontext = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
                sslcontext.init(null, null, null);
                ssl = new SSLConnectionSocketFactory(sslcontext);
            }
            catch (final SecurityException ignore) {
            }
            catch (final KeyManagementException ignore) {
            }
            catch (final NoSuchAlgorithmException ignore) {
            }
        }
        
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			ssl = new SSLConnectionSocketFactory(builder.build());
		} catch (NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", ssl).build();
		// 禁用Nagle
		SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
		connManager = new PoolingHttpClientConnectionManager(r);
		connManager.setMaxTotal(DEFAULT_MAX);
		connManager.setDefaultMaxPerRoute(DEFAULT_MAX);
		connManager.setDefaultSocketConfig(socketConfig);
		connManager.closeIdleConnections(30, TimeUnit.SECONDS);
		Registry<CookieSpecProvider> regist =
                RegistryBuilder.<CookieSpecProvider> create()
                    .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
                    .register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory())
                    .register(HT_COOKIE_SPEC, new HaiTaoCookieSpecProvider())
                    .build();
		httpclient = HttpClientBuilder.create()
									  .setRedirectStrategy(strategy)
								      .addInterceptorFirst(new ResponseInterceptor())
								      .setConnectionManager(connManager)
								      .setDefaultCookieSpecRegistry(regist)
								      .setUserAgent(userAgent)
								      .setSSLSocketFactory(ssl)
								      .build();
		handler = new Handler();
	}

	public HttpResult get(String url, int timeoutMills) throws ClientProtocolException, IOException {
		return get(url, timeoutMills, null);
	}
	
	
	
	public HttpResult get1(String url, int timeoutMills, String proxyAddr, int proxyPort)
			throws ClientProtocolException, IOException {
		HttpHost host = new HttpHost(proxyAddr, proxyPort);
		HttpGet get = new HttpGet(url);
		get.setConfig(initConfig(timeoutMills, host));
		final HttpClientContext localContext = HttpClientContext.create();
		localContext.setAttribute(HttpClientContext.CREDS_PROVIDER,new BasicCredentialsProvider());
		localContext.setAttribute(HttpClientContext.AUTH_CACHE, new BasicAuthCache());
		localContext.setAttribute(HttpClientContext.COOKIE_STORE,new BasicCookieStore());
		Registry<CookieSpecProvider> regist =
                RegistryBuilder.<CookieSpecProvider> create()
                    .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
                    .register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory())
                    .register(HT_COOKIE_SPEC, new HaiTaoCookieSpecProvider())
                    .build();
		localContext.setCookieSpecRegistry(regist);
		localContext.setCookieStore(new BasicCookieStore());
		return handler.handleResponse(httpclient.execute(get, localContext));
	}
	
	public HttpResult get(String url, int timeoutMills, String proxyAddr, int proxyPort)
			throws ClientProtocolException, IOException {
		HttpGet get=null;
		try {
			HttpHost host = new HttpHost(proxyAddr, proxyPort);
			get = new HttpGet(url);
			get.setConfig(initConfig(timeoutMills, host));
			return httpclient.execute(get, handler);
		} finally {
			if (get != null) {
				get.releaseConnection();
			}
		}
	}

	public HttpResult get(String url, int timeoutMills, String proxyAddr, int proxyPort, Map<String, Object> headers)
			throws ClientProtocolException, IOException {
		HttpGet get=null;
		try {
			HttpHost host = new HttpHost(proxyAddr, proxyPort);
			get = new HttpGet(url);
			if (MapUtils.isNotEmpty(headers)) {
				List<Header> list = new LinkedList<Header>();
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					Header header = new BasicHeader(entry.getKey(), Objects.toString(entry.getValue(), ""));
					list.add(header);
				}
				get.setHeaders(list.toArray(new Header[list.size()]));
			}
			get.setConfig(initConfig(timeoutMills, host));
			return httpclient.execute(get, handler);
		} finally {
			if (get != null) {
				get.releaseConnection();
			}
		}
	}
	public HttpResult get(String url, int timeoutMills, Map<String, Object> headers)
			throws ClientProtocolException, IOException {
		HttpGet get = null;
		try {
			get = new HttpGet(url);
			if (MapUtils.isNotEmpty(headers)) {
				List<Header> list = new LinkedList<Header>();
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					Header header = new BasicHeader(entry.getKey(), Objects.toString(entry.getValue(), ""));
					list.add(header);
				}
				get.setHeaders(list.toArray(new Header[list.size()]));
			}
			get.setConfig(initConfig(timeoutMills));
			return httpclient.execute(get, handler);
		} finally {
			if (get != null) {
				get.releaseConnection();
			}
		}
	}

	public HttpResult get(String url) throws ClientProtocolException, IOException {
		return get(url, DEFAULT_TIME_OUT, null);
	}

	/**
	 *
	 * @see http://hc.apache.org/httpcomponents-client-4.3.x/examples.html
	 * @param url
	 * @param headers
	 * @param entity
	 * @param timeoutMills
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResult post(String url, Map<String, Object> headers, Map<String, Object> entity, int timeoutMills)
			throws ClientProtocolException, IOException {
		return post(url, headers, entity, timeoutMills, null);
	}

	/**
	 *
	 * @see http://hc.apache.org/httpcomponents-client-4.3.x/examples.html
	 * @param url
	 * @param headers
	 * @param entity
	 * @param timeoutMills
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResult post(String url, Map<String, Object> headers, Map<String, Object> entity, int timeoutMills,
			String charset) throws ClientProtocolException, IOException {
		HttpPost post = null;
		try {
			post = new HttpPost(url);
			if (MapUtils.isNotEmpty(headers)) {
				List<Header> list = new LinkedList<Header>();
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					Header header = new BasicHeader(entry.getKey(), Objects.toString(entry.getValue(), ""));
					list.add(header);
				}
				post.setHeaders(list.toArray(new Header[list.size()]));
			}

			if (MapUtils.isNotEmpty(entity)) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();

				Set<String> keySet = entity.keySet();
				for (String key : keySet) {
					nvps.add(new BasicNameValuePair(key, entity.get(key)+""));
				}
				post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}

			post.setConfig(initConfig(timeoutMills));
			return httpclient.execute(post, handler);
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
		}
	}

	/**
	 * 	
	 * @param url
	 * @param headers
	 * @param entity
	 * @param timeoutMills
	 * @param charset
	 * @param proxyAddr
	 * @param proxyPort
	 * @return
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public HttpResult post(String url, Map<String, Object> headers, Map<String, Object> entity, int timeoutMills,
			String charset, String proxyAddr, int proxyPort) throws ClientProtocolException, IOException {
		HttpPost post = null;
		try {
			HttpHost host = new HttpHost(proxyAddr, proxyPort);
			post = new HttpPost(url);
			if (MapUtils.isNotEmpty(headers)) {
				List<Header> list = new LinkedList<Header>();
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					Header header = new BasicHeader(entry.getKey(), Objects.toString(entry.getValue(), ""));
					list.add(header);
				}
				post.setHeaders(list.toArray(new Header[list.size()]));
			}

			if (MapUtils.isNotEmpty(entity)) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();

				Set<String> keySet = entity.keySet();
				for (String key : keySet) {
					nvps.add(new BasicNameValuePair(key, entity.get(key)+""));
				}
				post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}

			post.setConfig(initConfig(timeoutMills,host));
			return httpclient.execute(post, handler);
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
		}
	}
	/**
	 * @see http://hc.apache.org/httpcomponents-client-4.3.x/examples.html
	 * @param url
	 * @param entity
	 * @param timeoutMills
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResult post(String url, Map<String, Object> entity, int timeoutMills)
			throws ClientProtocolException, IOException {
		return post(url, null, entity, timeoutMills);
	}

	/**
	 * @see http://hc.apache.org/httpcomponents-client-4.3.x/examples.html
	 * @param url
	 * @param entity
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResult post(String url, Map<String, Object> entity) throws ClientProtocolException, IOException {
		return post(url, null, entity, DEFAULT_TIME_OUT);
	}

	public HttpResult head(String url, int timeoutMills) throws ClientProtocolException, IOException {
		return head(url, timeoutMills, null);
	}

	public HttpResult head(String url, int timeoutMills, Map<String, Object> headers)
			throws ClientProtocolException, IOException {
		HttpHead head = null;
		try {
			head = new HttpHead(url);
			if (MapUtils.isNotEmpty(headers)) {
				List<Header> list = new LinkedList<Header>();
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					Header header = new BasicHeader(entry.getKey(), Objects.toString(entry.getValue(), ""));
					list.add(header);
				}
				head.setHeaders(list.toArray(new Header[list.size()]));
			}
			head.setConfig(initConfig(timeoutMills));
			return httpclient.execute(head, handler);
		} finally {
			if (head != null) {
				head.releaseConnection();
			}
		}
	}

	public HttpResult head(String url) throws ClientProtocolException, IOException {
		return head(url, DEFAULT_TIME_OUT, null);
	}

	private RequestConfig initConfig(int timeOutMills) {
		return RequestConfig.custom()
							.setConnectionRequestTimeout(timeOutMills)
				            .setSocketTimeout(timeOutMills)
				            .setConnectTimeout(timeOutMills)
				            .setCookieSpec(HT_COOKIE_SPEC)
				            .build();
	}

	private RequestConfig initConfig(int timeOutMills, HttpHost proxy) {
		return RequestConfig.custom()
							// Waiting for a connection from connection manager
							.setConnectionRequestTimeout(timeOutMills)
							// Waiting for connection to establish
							.setConnectTimeout(timeOutMills)
							// Waiting for data
				            .setSocketTimeout(timeOutMills)
				            .setCookieSpec(HT_COOKIE_SPEC)
				            .setProxy(proxy)
				            .build();
	}

	public void shutdown() {
		connManager.shutdown();
	}

	private static class Handler implements ResponseHandler<HttpResult> {

		/**
		 * Convert 'HttpResponse' to 'HttpResult'
		 */
		public HttpResult handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

			StatusLine statusLine = response.getStatusLine();
			HttpEntity entity = response.getEntity();
			HttpResult ret = new HttpResult();
			ret.setStatus(statusLine.getStatusCode());
			ret.setHeaders(convert(response.getAllHeaders()));
			ret.setContent(entity == null ? null : EntityUtils.toByteArray(entity));
			return ret;
		}

		private List<NameValuePair> convert(Header[] headers) {

			if (ArrayUtils.isEmpty(headers)) {
				return Collections.emptyList();
			}

			List<NameValuePair> ret = new ArrayList<NameValuePair>();
			for (Header header : headers) {
				NameValuePair pair = new BasicNameValuePair(header.getName(), header.getValue());
				ret.add(pair);
			}
			return ret;
		}

	}

	private static class ResponseInterceptor implements HttpResponseInterceptor {

		public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
			HttpEntity entity = response.getEntity();
			if (entity == null) {
				return;
			}
			Header header = entity.getContentEncoding();
			if (header == null) {
				return;
			}
			HeaderElement[] elements = header.getElements();
			if (ArrayUtils.isEmpty(elements)) {
				return;
			}
			for (HeaderElement element : elements) {
				if (StringUtils.equalsIgnoreCase("gzip", element.getName())) {
					response.setEntity(new GzipDecompressingEntity(response.getEntity()));
					return;
				}
			}
		}

	}

}