package com.haitao55.spider.crawler.common.cache;

import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：在本地缓存一份所有代理IPs信息，定时更新
 * 
 * @author Arthur.Liu
 * @time 2016年8月23日 下午2:56:12
 * @version 1.0
 */
public class ProxyCache extends ConcurrentHashMap<String, Queue<Proxy>> {
	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = -5173162338600170448L;

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	private static class Holder {// 使用这种方式实现单例类，就最安全了
		public static ProxyCache cache = new ProxyCache();
	}

	private ProxyCache() {
		// nothing
	}

	public static ProxyCache getInstance() {
		return Holder.cache;
	}

	public Proxy pickup(String regionId, boolean required) {
		Proxy result = null;

		result = this.pickupInternal(regionId);
		while (result == null && required) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.warn("Sleep 100 millisecond after picking-up none proxy-ip!");
			}

			result = this.pickupInternal(regionId);
		}
		Queue<Proxy> queue = this.get(regionId);
		if(queue != null){
			queue.offer(result);
		}

		return result;
	}

	private Proxy pickupInternal(String regionId) {
		Proxy result = null;

		try {
			Queue<Proxy> queue = this.get(regionId);
			if (CollectionUtils.isEmpty(queue)) {
				return null;
			}
			if(queue != null){
				result = queue.poll();
			}

		} catch (Exception e) {
			logger.error("Error while picking-up proxy!", e);
		}

		return result;
	}
}