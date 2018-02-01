package com.haitao55.spider.common.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.Constants;

/**
 * kafka消费实现类
 * 
 * @author denghuan
 */
public class SpiderKafkaConsumerImpl implements SpiderKafkaConsumer {

	private static final Logger logkafka = LoggerFactory.getLogger(Constants.LOGGER_NAME_KAFKA);
	private KafkaConsumer<String, String> consumer;
	private String servers;
	private int autoCimmitIntervalMs;
	private int sessionTimeOutMs;
	private String enableAutoCommit;
	private int maxPollRecords;
	private String groupId;
	private String topic;
	private String keySerializer;
	private String valueSerializer;
	private String autoOffsetReset;

	@Override
	public void init() {
		Properties props = new Properties();
		props.put("auto.offset.reset", autoOffsetReset);
		props.put("bootstrap.servers", servers);
		props.put("group.id", groupId);
		props.put("enable.auto.commit", enableAutoCommit);
		props.put("max.poll.records", maxPollRecords);
		props.put("auto.commit.interval.ms", autoCimmitIntervalMs);
		props.put("session.timeout.ms", sessionTimeOutMs);
		props.put("key.deserializer", keySerializer);
		props.put("value.deserializer", valueSerializer);
		consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList(topic));
	}

	@Override
	public List<String> fetchData() {
		if (StringUtils.isBlank(topic)) {
			logkafka.warn("topic is blank while fetchData()");
			return null;
		}

		try {
			List<String> list = new ArrayList<String>();
			ConsumerRecords<String, String> records = consumer.poll(10000);
			for (ConsumerRecord<String, String> record : records) {
				logkafka.info("consumer a message offset {} for topic {} partition {}", record.offset(), record.topic(),
						record.partition());
				list.add(record.value());
			}

			logkafka.info("fetchData() result size: {}", list.size());
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			logkafka.error("Interrupted  poll a message", e);
		}
		return null;
	}

	@Override
	public void close() {
		if (consumer != null) {
			consumer.close();
		}
	}

	public String getServers() {
		return servers;
	}

	public void setServers(String servers) {
		this.servers = servers;
	}

	public int getAutoCimmitIntervalMs() {
		return autoCimmitIntervalMs;
	}

	public void setAutoCimmitIntervalMs(int autoCimmitIntervalMs) {
		this.autoCimmitIntervalMs = autoCimmitIntervalMs;
	}

	public int getSessionTimeOutMs() {
		return sessionTimeOutMs;
	}

	public void setSessionTimeOutMs(int sessionTimeOutMs) {
		this.sessionTimeOutMs = sessionTimeOutMs;
	}

	public String getEnableAutoCommit() {
		return enableAutoCommit;
	}

	public void setEnableAutoCommit(String enableAutoCommit) {
		this.enableAutoCommit = enableAutoCommit;
	}

	public int getMaxPollRecords() {
		return maxPollRecords;
	}

	public void setMaxPollRecords(int maxPollRecords) {
		this.maxPollRecords = maxPollRecords;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getKeySerializer() {
		return keySerializer;
	}

	public void setKeySerializer(String keySerializer) {
		this.keySerializer = keySerializer;
	}

	public String getValueSerializer() {
		return valueSerializer;
	}

	public void setValueSerializer(String valueSerializer) {
		this.valueSerializer = valueSerializer;
	}

	public String getAutoOffsetReset() {
		return autoOffsetReset;
	}

	public void setAutoOffsetReset(String autoOffsetReset) {
		this.autoOffsetReset = autoOffsetReset;
	}
	

}