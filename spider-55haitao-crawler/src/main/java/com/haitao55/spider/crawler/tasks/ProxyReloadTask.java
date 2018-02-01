package com.haitao55.spider.crawler.tasks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.collections.MapUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.thrift.ProxyModel;
import com.haitao55.spider.common.thrift.ThriftService.Client;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.thrift.pool.ThriftConnectionProvider;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：定时任务线程,定时从Controller端拉取代理IP信息
 * 
 * @author Arthur.Liu
 * @time 2016年8月23日 下午2:35:35
 * @version 1.0
 */
public class ProxyReloadTask implements DaemonTask {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_REMOTE);

	// 定时从Controller端拉取代理IP信息的时间间隔；默认1分钟，可以被外部配置文件覆盖
	private int proxyFetchInterval = 3 * 60 * 1000;

	private ThriftConnectionProvider thriftConnectionProvider;

	@Override
	public void run() {
		while (true) {
			Client client = this.thriftConnectionProvider.getObject();

			Map<Long, ProxyModel> map = null;
			try {
				map = client.fetchProxies();
			} catch (TException e) {
				this.thriftConnectionProvider.invalidateObject(client);
				client = null;
				logger.error("Error occurred while fetch proxy ips entities from controller!{}", e);
			} finally {
				this.thriftConnectionProvider.returnObject(client);// 及时释放连接对象
			}

			logger.info("Fetch {} proxy ips entities from controller!", MapUtils.isEmpty(map) ? 0 : map.size());

			if (MapUtils.isEmpty(map)) {
				this.sleep();
				continue;
			}

			// 转换到本地缓存中,以待以后系统执行抓取时使用
			Map<String, Queue<Proxy>> temp = new HashMap<String, Queue<Proxy>>();
			Iterator<ProxyModel> it = map.values().iterator();
			while (it.hasNext()) {
				ProxyModel model = it.next();
				Proxy proxy = this.convertProxyModel2Proxy(model);

				if (!temp.containsKey(proxy.getRegionId())) {
					temp.put(proxy.getRegionId(), new ConcurrentLinkedQueue<Proxy>());
				}
				if(proxy != null){
					temp.get(proxy.getRegionId()).offer(proxy);
				}
			}

			ProxyCache.getInstance().clear();
			ProxyCache.getInstance().putAll(temp);

			this.sleep();
		}
	}

	private Proxy convertProxyModel2Proxy(ProxyModel model) {
		Proxy proxyIp = new Proxy();

		proxyIp.setId(model.getId());
		proxyIp.setRegionId(model.getRegionId());
		proxyIp.setRegionName(model.getRegionName());
		proxyIp.setIp(model.getIp());
		proxyIp.setPort(model.getPort());

		return proxyIp;
	}

	private void sleep() {
		try {
			Thread.sleep(this.proxyFetchInterval);
		} catch (InterruptedException e) {
		}
	}

	public int getProxyFetchInterval() {
		return proxyFetchInterval;
	}

	public void setProxyFetchInterval(int proxyFetchInterval) {
		this.proxyFetchInterval = proxyFetchInterval;
	}

	public ThriftConnectionProvider getThriftConnectionProvider() {
		return thriftConnectionProvider;
	}

	public void setThriftConnectionProvider(ThriftConnectionProvider thriftConnectionProvider) {
		this.thriftConnectionProvider = thriftConnectionProvider;
	}
}