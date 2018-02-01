package com.haitao55.spider.common.kafka;

/**
 * 
  * @ClassName: SpiderKafkaResult
  * @Description: kafka message result
  * @author songsong.xu
  * @date 2016年9月20日 下午2:36:35
  *
 */
public class SpiderKafkaResult {
	private long offset;
	private long timestamp;
	private String topic;
	private int partition;

	public SpiderKafkaResult() {
	}

	public SpiderKafkaResult(long offset, long timestamp, String topic, int partition) {
		super();
		this.offset = offset;
		this.timestamp = timestamp;
		this.topic = topic;
		this.partition = partition;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}
}