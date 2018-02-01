package com.haitao55.spider.monitor_collector.task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.Constants;

/**
 * 
 * 功能：爬虫日志收集定时执行任务类
 * 
 * @author Arthur.Liu
 * @time 2016年11月25日 下午11:03:38
 * @version 1.0
 */
public class MonitorCollectorTask {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	private static final String COMMAND_PREFIX = "tail -1 ";

	private static final String POST_ATTR_NAME_IP = "ip";
	private static final String POST_ATTR_NAME_MODULE = "module";
	private static final String POST_ATTR_NAME_CONTENT = "content";
	private static final String POST_ATTR_NAME_COMEFROM = "comefrom";

	private String monitorLogFile;
	private String monilorCollectorServerAddr;
	private String monitorModule;
	private String comeFrom;

	public String getMonitorLogFile() {
		return monitorLogFile;
	}

	public void setMonitorLogFile(String monitorLogFile) {
		this.monitorLogFile = monitorLogFile;
	}

	public String getMonilorCollectorServerAddr() {
		return monilorCollectorServerAddr;
	}

	public void setMonilorCollectorServerAddr(String monilorCollectorServerAddr) {
		this.monilorCollectorServerAddr = monilorCollectorServerAddr;
	}

	public String getMonitorModule() {
		return monitorModule;
	}

	public void setMonitorModule(String monitorModule) {
		this.monitorModule = monitorModule;
	}

	public String getComeFrom() {
		return comeFrom;
	}

	public void setComeFrom(String comeFrom) {
		this.comeFrom = comeFrom;
	}

	public void init() {
		// nothing
	}

	public void destroy() {
		// nothing
	}

	/**
	 * 定时执行方法,读取监控日志文件最后一行,并发送给统一收集服务器
	 */
	public void collectMonitorLog() {
		BufferedReader reader = null;

		try {
			String command = COMMAND_PREFIX + this.monitorLogFile;
			Process process = Runtime.getRuntime().exec(command);
			int exitCode = process.waitFor();

			if (exitCode != 0) {
				logger.error("Error invoking linux shell command, exit-code==" + exitCode);
				return;
			}

			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String lastLineContent = reader.readLine();

			this.postMonitorLog(lastLineContent);
		} catch (Exception e) {
			logger.error("Error invoking linux shell command!", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	private String postMonitorLog(String content) throws Exception {
		String result = "";
		CloseableHttpResponse httpResponse = null;

		@SuppressWarnings("serial")
		List<NameValuePair> attrs = new ArrayList<NameValuePair>() {
			{
				add(new BasicNameValuePair(POST_ATTR_NAME_IP, InetAddress.getLocalHost().getHostAddress().toString()));
				add(new BasicNameValuePair(POST_ATTR_NAME_MODULE, MonitorCollectorTask.this.monitorModule));
				add(new BasicNameValuePair(POST_ATTR_NAME_CONTENT, content));
				add(new BasicNameValuePair(POST_ATTR_NAME_COMEFROM, MonitorCollectorTask.this.comeFrom));
			}
		};

		try {
			// 设置参数到请求对象中
			HttpPost httpPost = new HttpPost(this.monilorCollectorServerAddr);
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