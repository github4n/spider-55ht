package com.haitao55.spider.common.kafka;

import org.apache.kafka.clients.producer.Callback;

/**
 * 
  * @ClassName: SpiderKafkaProducer
  * @Description: kafka producer api
  * @author songsong.xu
  * @date 2016年9月20日 下午2:35:17
  *
 */
public interface SpiderKafkaProducer {

	public void init();

	public SpiderKafkaResult send(String topic, String value);

	public SpiderKafkaResult send(String topic, String key, String value);

	public SpiderKafkaResult sendbyCallBack(String topic, String value);

	public SpiderKafkaResult sendbyCallBack(String topic, String key, String value);

	public SpiderKafkaResult sendbyCallBack(String topic, String key, String value, Callback callback);

	public void close();
}