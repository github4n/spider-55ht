package com.haitao55.spider.common.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.haitao55.spider.common.http.HttpResult;

/**
 * 
 * Title: Description: 封装 httpclient 请求方式 Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2016年11月23日 上午10:07:01
 * @version 1.0
 */
public class HttpClientUtil {
	public static String post(String path, Map<String, String> params) {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = postForm(path, params);
		request.addHeader("content-type", "application/x-www-form-urlencoded");
		HttpResult response = null;
		try {
			response = httpClient.execute(request, new Handler());
//			System.out.println(params.get("url") + " 结果:" + response.getContentAsString());
			return response.getContentAsString();
		} catch (Exception e) {
		} finally {
			HttpClientUtils.closeQuietly(httpClient);
		}
		return null;
	}
	
	/**
	 * 核价post调用
	 * @param path
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String postDespiteException(String path, Map<String, String> params) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = postForm(path, params);
		request.addHeader("content-type", "application/x-www-form-urlencoded");
		HttpResult response = null;
		try {
			response = httpClient.execute(request, new Handler());
			return response.getContentAsString();
		} catch (Exception e) {
			throw e;
		} finally {
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	
	public static String post(String path, Map<String, String> params,int timeout) {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = postForm(path, params);
		request.addHeader("content-type", "application/x-www-form-urlencoded");
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();//设置请求和传输超时时间
		request.setConfig(requestConfig);
		HttpResult response = null;
		try {
			response = httpClient.execute(request, new Handler());
//			System.out.println(params.get("url") + " 结果:" + response.getContentAsString());
			return response.getContentAsString();
		} catch (Exception e) {
		} finally {
			HttpClientUtils.closeQuietly(httpClient);
		}
		return null;
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

	private static HttpPost postForm(String url, Map<String, String> params) {

		HttpPost httpost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		Set<String> keySet = params.keySet();
		for (String key : keySet) {
			nvps.add(new BasicNameValuePair(key, params.get(key)));
		}

		try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return httpost;
	}
	
	/**
	 *  denghuan
	 * 解决 https ssl unable to find valid certification path to requested target  问题
	 * @param url
	 * @param params
	 * @return
	 */
	public static final String sendHttpsRequestByPost(String url, Map<String, String> params) {
		String responseContent = null;
		HttpClient httpClient = new DefaultHttpClient();
		//创建TrustManager
		X509TrustManager xtm = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		//这个好像是HOST验证
		X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
			public void verify(String arg0, SSLSocket arg1) throws IOException {}
			public void verify(String arg0, String[] arg1, String[] arg2) throws SSLException {}
			public void verify(String arg0, X509Certificate arg1) throws SSLException {}
		};
		try {
			//TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
			SSLContext ctx = SSLContext.getInstance("TLS");
			//使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用
			ctx.init(null, new TrustManager[] { xtm }, null);
			//创建SSLSocketFactory
			SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);
			socketFactory.setHostnameVerifier(hostnameVerifier);
			//通过SchemeRegistry将SSLSocketFactory注册到我们的HttpClient上
			httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", socketFactory, 443));
			HttpPost httpPost = new HttpPost(url);
			List<NameValuePair> formParams = new ArrayList<NameValuePair>(); // 构建POST请求的表单参数
			for (Map.Entry<String, String> entry : params.entrySet()) {
				formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity(); // 获取响应实体
			if (entity != null) {
				responseContent = EntityUtils.toString(entity, "UTF-8");
			}
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭连接,释放资源
			httpClient.getConnectionManager().shutdown();
		}
		return responseContent;
	}
	
}
