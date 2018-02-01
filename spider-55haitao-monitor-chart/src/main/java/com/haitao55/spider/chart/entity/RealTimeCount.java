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
@Table(name="statistics_realtime_times")
public class RealTimeCount {

	@Id
	private BigInteger id;
	@Column(name="execute_time")
	private Date currentTime;
	@Column(name="cu_count")
	private Integer currentCount;
	private Integer total;
	
	public BigInteger getId() {
		return id;
	}
	public Date getCurrentTime() {
		return currentTime;
	}
	public Integer getCurrentCount() {
		return currentCount;
	}
	public void setId(BigInteger id) {
		this.id = id;
	}
	public void setCurrentTime(Date currentTime) {
		this.currentTime = currentTime;
	}
	public void setCurrentCount(Integer currentCount) {
		this.currentCount = currentCount;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	
}
