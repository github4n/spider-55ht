package com.haitao55.spider.chart.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年6月1日 上午10:53:17  
 */
@Table(name="statistics_items_full_data")
public class FullData {
	@Id
	private BigInteger id;
	@Column(name="execute_date")
	private String date;
	private String domain;
	private Integer count;
	public BigInteger getId() {
		return id;
	}
	public String getDate() {
		return date;
	}
	public String getDomain() {
		return domain;
	}
	public Integer getCount() {
		return count;
	}
	public void setId(BigInteger id) {
		this.id = id;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	
}
