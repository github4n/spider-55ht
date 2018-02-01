package com.haitao55.spider.common.kafka;

import java.util.List;

/**
 * kafka消费
 * 
 * @author denghuan
 */
public interface SpiderKafkaConsumer {

	/**
	 * 初始化
	 */
	public void init();

	/**
	 * 从kafka获取数据
	 * 
	 * @return
	 */
	public List<String> fetchData();

	/**
	 * 关闭
	 */
	public void close();
}