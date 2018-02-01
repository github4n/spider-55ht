package com.haitao55.spider.common.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.Constants;

/**
 * 
  * @ClassName: SpiderKafkaProducerImpl
  * @Description: kafka producer api implement
  * @author songsong.xu
  * @date 2016年9月20日 下午2:35:50
  *
 */
public class SpiderKafkaProducerImpl implements SpiderKafkaProducer {
	private static final Logger logkafka = LoggerFactory.getLogger(Constants.LOGGER_NAME_KAFKA);
	private Producer<String, String> producer;
	private Properties kafkaProps;

	public SpiderKafkaProducerImpl(Properties kafkaProps) {
		this.kafkaProps = kafkaProps;
		producer = new KafkaProducer<String, String>(this.kafkaProps);
	}

	@Override
	public void init() {
		// todo
	}

	@Override
	public SpiderKafkaResult send(String topic, String key, String value) {
		if (StringUtils.isBlank(topic) || StringUtils.isBlank(value)) {
			return null;
		}
		if (producer == null) {
			return null;
		}
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, key, value);
		try {
			Future<RecordMetadata> f = producer.send(record);
			RecordMetadata metaData = f.get();
			logkafka.info("send a message offset {} for topic {} partition {}", metaData.offset(), metaData.topic(),
					metaData.partition());
			return result(metaData);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			logkafka.error("Interrupted while sending a message", e);
		}
		return null;
	}

	@Override
	public SpiderKafkaResult send(String topic, String value) {
		return this.send(topic, null, value);
	}

	@Override
	public SpiderKafkaResult sendbyCallBack(String topic, String value) {
		return this.sendbyCallBack(topic, null, value);
	}

	@Override
	public SpiderKafkaResult sendbyCallBack(String topic, String key, String value) {
		Callback callback = new Callback() {
			public void onCompletion(RecordMetadata metadata, Exception e) {
				if (e != null) {
					e.printStackTrace();
				}
				logkafka.info("send a message offset {} for topic {} partition {}", metadata.offset(), metadata.topic(),
						metadata.partition());
			}
		};
		return this.send(topic, key, value, callback);
	}

	@Override
	public SpiderKafkaResult sendbyCallBack(String topic, String key, String value, Callback callback) {
		return this.send(topic, key, value, callback);
	}

	private SpiderKafkaResult send(String topic, String key, String value, Callback callback) {
		if (StringUtils.isBlank(topic) || StringUtils.isBlank(value)) {
			return null;
		}
		if (producer == null) {
			return null;
		}
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, key, value);
		Future<RecordMetadata> f = producer.send(record, callback);
		try {
			return result(f.get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			logkafka.error("Interrupted while get a message result", e);
		}
		return null;
	}

	private SpiderKafkaResult result(RecordMetadata meta) {
		if (meta != null) {
			return new SpiderKafkaResult(meta.offset(), meta.timestamp(), meta.topic(), meta.partition());
		}
		return null;
	}

	@Override
	public void close() {
		if (producer != null) {
			producer.close();
		}
	}

	public static void main(String[] args) {
		/*
		 * HaiTaoProducer producer = new HaiTaoProducerImpl(); producer.init();
		 * for(int i =1; i < 100;i++){ producer.sendbyCallBack("my-topic",
		 * "hello"+i, i+""); } producer.close();
		 * 
		 * System.out.println(HaiTaoProducer.class.getCanonicalName());
		 */
	}
}