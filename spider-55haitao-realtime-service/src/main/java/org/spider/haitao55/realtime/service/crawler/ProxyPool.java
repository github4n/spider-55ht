package org.spider.haitao55.realtime.service.crawler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProxyPool {

	private final Queue<ProxyHost> hostQueue = new ConcurrentLinkedQueue<ProxyHost>();

	public ProxyPool() {
		hostQueue.offer(new ProxyHost("10.142.0.2", 3128));
		hostQueue.offer(new ProxyHost("10.142.0.3", 3128));
		hostQueue.offer(new ProxyHost("10.142.0.4", 3128));
		hostQueue.offer(new ProxyHost("10.128.0.3", 3128));
		hostQueue.offer(new ProxyHost("10.128.0.4", 3128));
		hostQueue.offer(new ProxyHost("10.128.0.5", 3128));
		hostQueue.offer(new ProxyHost("10.138.0.2", 3128));
		hostQueue.offer(new ProxyHost("10.138.0.3", 3128));
		hostQueue.offer(new ProxyHost("10.138.0.4", 3128));
		hostQueue.offer(new ProxyHost("10.132.0.2", 3128));
		hostQueue.offer(new ProxyHost("10.132.0.3", 3128));
		hostQueue.offer(new ProxyHost("10.132.0.4", 3128));
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
