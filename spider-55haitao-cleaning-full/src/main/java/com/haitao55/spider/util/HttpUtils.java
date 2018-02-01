package com.haitao55.spider.util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * httpUtil 
 * @author denghuan
 *
 */
public class HttpUtils {
	
	private static final String POST_ATTR_NAME_IP = "ip";
	private static final String POST_ATTR_NAME_CONTENT = "content";

	public static String post(String content,
			String monilorCollectorServerAddr)throws Exception{
		String result = "";
		CloseableHttpResponse httpResponse = null;

		@SuppressWarnings("serial")
		List<NameValuePair> attrs = new ArrayList<NameValuePair>() {
			{
				add(new BasicNameValuePair(POST_ATTR_NAME_IP, InetAddress.getLocalHost().getHostAddress().toString()));
				add(new BasicNameValuePair(POST_ATTR_NAME_CONTENT, content));
			}
		};

		try {
			// 设置参数到请求对象中
			HttpPost httpPost = new HttpPost(monilorCollectorServerAddr);
			httpPost.setEntity(new UrlEncodedFormEntity(attrs, "UTF-8"));

			CloseableHttpClient client = HttpClients.createDefault();
			httpResponse = client.execute(httpPost);
			// 获取结果实体
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
				EntityUtils.consume(entity);
			}
		} finally {
			IOUtils.closeQuietly(httpResponse);
		}

		return result;
	}
}
