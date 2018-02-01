package com.haitao55.spider.crawler.core.callable.custom.amazon.imp;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProxyPool {

	private final Queue<ProxyHost> hostQueue = new ConcurrentLinkedQueue<ProxyHost>();

	public ProxyPool() {
		hostQueue.offer(new ProxyHost("104.196.30.199", 3128));
		hostQueue.offer(new ProxyHost("104.196.109.105", 3128));
		hostQueue.offer(new ProxyHost("104.196.209.137", 3128));
		hostQueue.offer(new ProxyHost("104.197.153.106", 3128));
		hostQueue.offer(new ProxyHost("104.155.150.136", 3128));
		hostQueue.offer(new ProxyHost("104.154.28.79", 3128));
		hostQueue.offer(new ProxyHost("104.198.5.27", 3128));
		hostQueue.offer(new ProxyHost("104.196.226.93", 3128));
		hostQueue.offer(new ProxyHost("104.154.28.79", 3128));
		hostQueue.offer(new ProxyHost("104.155.62.169", 3128));
		hostQueue.offer(new ProxyHost("104.199.0.246", 3128));
		hostQueue.offer(new ProxyHost("104.198.111.239", 3128));
	}
	
	public synchronized ProxyHost pollHost(){
		ProxyHost proxyAddress = hostQueue.poll();
        while (null == proxyAddress) {
            proxyAddress = hostQueue.poll();
        }
        hostQueue.offer(proxyAddress);
        return proxyAddress;
	}
	
	class ProxyHost {
		private String ip;
		private int port;
		
		public ProxyHost(String ip, int port) {
			super();
			this.ip = ip;
			this.port = port;
		}
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
	}
}
