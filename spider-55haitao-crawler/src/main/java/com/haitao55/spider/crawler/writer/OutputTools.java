package com.haitao55.spider.crawler.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.utils.Constants;

public class OutputTools {

	private static final String HTTP_HEAD = "http://";
	private static final String HTTPS_HEAD = "https://";

	private Timer timer = new Timer("ScdWriterTimer");
	private ConcurrentHashMap<String, BufferedWriter> bwMap = new ConcurrentHashMap<String, BufferedWriter>();
	private final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);

	public BufferedWriter getBw(String filePath, String website) {
		BufferedWriter buffWriter = getBufferWriter(website);
		if (buffWriter == null) {
			// first
			try {
				BufferedWriter buff = new BufferedWriter(new FileWriter(filePath, true));
				logger.info("website===>" + website + ",first buff:" + buff);
				bwMap.put(website, buff);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			// last
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						BufferedWriter buff = getBufferWriter(website);
						if (null != buff) {
							synchronized (buff) {
								try {
									buff.close();
									logger.info("website===>" + website + ",buff.close()===>" + buff);
								} catch (Exception e) {
									logger.error("Error while close BufferedWriter,website:{}, e:{} ", website, e);
								}

								buff = new BufferedWriter(new FileWriter(filePath, true));
								logger.info("website===>" + website + ",timer===>" + buff);
								bwMap.put(website, buff);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, 10 * 60 * 1000, 10 * 60 * 1000);// 10分钟更换一次文件
		}
		return getBufferWriter(website);
	}

	public BufferedWriter getBufferWriter(String website) {
		return bwMap.get(website);
	}

	public String getLinuxUbuntuLocalIPAddr() throws SocketException {
		String localIP = null;
		Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces();
		while (e1.hasMoreElements()) {
			NetworkInterface ni = e1.nextElement();
			if (ni.getName().equals("eth0") || ni.getName().equals("eth1") || ni.getName().equals("enp0s25")) {
				Enumeration<InetAddress> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					InetAddress ia = e2.nextElement();
					if (ia instanceof Inet6Address)
						continue;
					localIP = ia.getHostAddress();
					return localIP;
				}
			}
		}

		throw new SocketException("Failed to get local IP address");
	}

	public String getWebsite(String url) {
		String website = null;
		if (StringUtils.contains(url, HTTP_HEAD)) {
			website = StringUtils.substringBetween(url, HTTP_HEAD, "/");
		} else if (StringUtils.contains(url, HTTPS_HEAD)) {
			website = StringUtils.substringBetween(url, HTTPS_HEAD, "/");
		}
		return website;
	}

	public static OutputTools getInstance() {
		return Holder.tools;
	}

	private static class Holder {
		private static final OutputTools tools = new OutputTools();
	}

	private OutputTools() {
	}

	public static void main(String[] args) {
		/*
		 * for(int i =0 ; i < 10; i++){ BufferedWriter bw =
		 * OutputTools.getInstance().getBw("/data/www.jd.com", "www.jd.com");
		 * System.out.println(i+" : "+bw); }
		 */
		try {
			System.out.println(OutputTools.getInstance().getLinuxUbuntuLocalIPAddr());
		} catch (SocketException e) {
			e.printStackTrace();
		}
		System.out.println("&amp;");
	}
}
