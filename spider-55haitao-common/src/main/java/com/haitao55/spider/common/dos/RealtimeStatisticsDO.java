package com.haitao55.spider.common.dos;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.haitao55.spider.common.dos.cons.RealtimeStatisticsPK;

/***
 * 实时核价统计 do
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月19日 下午2:13:45
* @version 1.0
 */
@Entity
@IdClass(RealtimeStatisticsPK.class)
@Table(name="realtime_statistics")
public class RealtimeStatisticsDO {
	@Id
	private Long taskId;
	
	@Id
	private String realtimeTime;

	@Column(name="task_name")
	private String taskName;
	
	@Column(name="crawler")
	private int crawler;
	
	@Column(name="mongo")
	private int mongo;
	
	@Column(name="redis")
	private int redis;
	
	@Column(name="exception")
	private int exception;

	public RealtimeStatisticsDO(Long taskId, String taskName, String realtimeTime, int crawler, int mongo,
			int redis,int exception) {
		super();
		this.taskId = taskId;
		this.taskName = taskName;
		this.realtimeTime = realtimeTime;
		this.crawler = crawler;
		this.mongo = mongo;
		this.redis = redis;
		this.exception = exception;
	}

	public RealtimeStatisticsDO() {
		super();
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getRealtimeTime() {
		return realtimeTime;
	}

	public void setRealtimeTime(String realtimeTime) {
		this.realtimeTime = realtimeTime;
	}

	public int getCrawler() {
		return crawler;
	}

	public void setCrawler(int crawler) {
		this.crawler = crawler;
	}

	public int getMongo() {
		return mongo;
	}

	public void setMongo(int mongo) {
		this.mongo = mongo;
	}

	public int getRedis() {
		return redis;
	}

	public void setRedis(int redis) {
		this.redis = redis;
	}

	public int getException() {
		return exception;
	}

	public void setException(int exception) {
		this.exception = exception;
	}

	@Override
	public String toString() {
		return "RealtimeStatisticsDO [taskId=" + taskId + ", taskName=" + taskName + ", realtimeTime=" + realtimeTime
				+ ", crawler=" + crawler + ", mongo=" + mongo + ", redis=" + redis + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + crawler;
		result = prime * result + mongo;
		result = prime * result + ((realtimeTime == null) ? 0 : realtimeTime.hashCode());
		result = prime * result + redis;
		result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
		result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RealtimeStatisticsDO other = (RealtimeStatisticsDO) obj;
		if (crawler != other.crawler)
			return false;
		if (mongo != other.mongo)
			return false;
		if (realtimeTime == null) {
			if (other.realtimeTime != null)
				return false;
		} else if (!realtimeTime.equals(other.realtimeTime))
			return false;
		if (redis != other.redis)
			return false;
		if (taskId == null) {
			if (other.taskId != null)
				return false;
		} else if (!taskId.equals(other.taskId))
			return false;
		if (taskName == null) {
			if (other.taskName != null)
				return false;
		} else if (!taskName.equals(other.taskName))
			return false;
		return true;
	}


	
}
