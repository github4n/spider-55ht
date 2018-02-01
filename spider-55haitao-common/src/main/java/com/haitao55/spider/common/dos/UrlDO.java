package com.haitao55.spider.common.dos;

import java.io.Serializable;

/**
 * 
 * 功能：种子url实体类
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 上午11:35:10
 * @version 1.0
 */
public class UrlDO implements Serializable {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String value;
	
	private String parentUrl;
	
	private int grade;

	private String type;

	private String status;

	private long createTime;

	private long lastCrawledTime;

	private String lastCrawledIp;

	private int latelyFailedCount;

	private String latelyErrorCode;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	

	public String getParentUrl() {
		return parentUrl;
	}

	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getLastCrawledTime() {
		return lastCrawledTime;
	}

	public void setLastCrawledTime(long lastCrawledTime) {
		this.lastCrawledTime = lastCrawledTime;
	}

	public String getLastCrawledIp() {
		return lastCrawledIp;
	}

	public void setLastCrawledIp(String lastCrawledIp) {
		this.lastCrawledIp = lastCrawledIp;
	}

	public int getLatelyFailedCount() {
		return latelyFailedCount;
	}

	public void setLatelyFailedCount(int latelyFailedCount) {
		this.latelyFailedCount = latelyFailedCount;
	}

	public String getLatelyErrorCode() {
		return latelyErrorCode;
	}

	public void setLatelyErrorCode(String latelyErrorCode) {
		this.latelyErrorCode = latelyErrorCode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		UrlDO other = (UrlDO) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}