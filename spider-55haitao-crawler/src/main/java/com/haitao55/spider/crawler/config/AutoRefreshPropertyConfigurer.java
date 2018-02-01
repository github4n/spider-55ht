package com.haitao55.spider.crawler.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：保存所有配置信息,可自动刷新配置文件
 *
 * @author Arthur.Liu
 * @time Jul 15, 2015 6:05:19 PM
 * @version 1.0
 *
 */
public class AutoRefreshPropertyConfigurer extends PropertyPlaceholderConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	private static final Map<String, String> ctxProperties = new ConcurrentHashMap<String, String>();

	private int refreshInterval;

	public int getRefreshInterval() {
		return refreshInterval;
	}

	public void setRefreshInterval(int refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {
		super.processProperties(beanFactoryToProcess, props);

		Map<String, String> map = new HashMap<String, String>();
		for (Object key : props.keySet()) {
			String keyStr = key.toString();
			String value = props.getProperty(keyStr);
			map.put(keyStr, value);
		}

		// 温和的重新加载配置项
		// 避免了使用map.clear()之后造成的某一瞬间取不到任何值的问题
		// entryset遍历性能要高于keyset
		// ConcurrentHashMap不存在ConcurrentModificationException问题
		for (Map.Entry<String, String> entry : ctxProperties.entrySet()) {
			String key = entry.getKey();
			if (!map.containsKey(key)) {
				ctxProperties.remove(key);
			}
		}
		ctxProperties.putAll(map);
		logger.info("AutoRefreshPropertyConfigurer.processProperties() is running and ctxProperties.size() is:"
				+ ctxProperties.size());
	}

	public String getContextProperty(String name) {
		return ctxProperties.get(name);
	}

	public Map<String, String> getAllContextProperties() {
		return Collections.unmodifiableMap(ctxProperties);
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		super.postProcessBeanFactory(beanFactory);
		logger.info("AutoRefreshPropertyConfigurer.postProcessBeanFactory() is running and only run once!");

		// initAutoRefresh(this, beanFactory);//
		// 取消定时刷新功能，因为这里会引起堆内存泄露的bug（2015-11-16 by arthur.liu）
	}

	// private Timer timer;

	// @SuppressWarnings("unused")
	// private void initAutoRefresh(
	// final PropertyPlaceholderConfigurer configurer,
	// final ConfigurableListableBeanFactory beanFactory) {
	// if (refreshInterval <= 0) {
	// return;
	// }
	// if (timer == null) {
	// timer = new Timer("AutoRefreshPropertyTimer", true);
	// timer.schedule(new TimerTask() {
	//
	// public void run() {
	// configurer.postProcessBeanFactory(beanFactory);
	// logger.info("Refresh properties in timer::{}", ctxProperties.toString());
	// }
	// }, refreshInterval, refreshInterval);
	// }
	// }
}