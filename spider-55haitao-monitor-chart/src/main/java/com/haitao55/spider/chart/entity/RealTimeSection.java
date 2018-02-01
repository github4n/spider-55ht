package com.haitao55.spider.chart.entity;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年5月31日 下午1:46:25  
 */
@Table(name="statistics_realtime_time_section")
public class RealTimeSection {

	@Id
	private BigInteger id;
	@Column(name="execute_time")
	private String currentTime;
	@Column(name="time_section")
	private String timeSection;
	private Integer count;
	
	public BigInteger getId() {
		return id;
	}
	public void setId(BigInteger id) {
		this.id = id;
	}
	public String getCurrentTime() {
		return currentTime;
	}
	public Integer getCount() {
		return count;
	}
	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public String getTimeSection() {
		return timeSection;
	}
	public void setTimeSection(String timeSection) {
		this.timeSection = timeSection;
	}
	
}
