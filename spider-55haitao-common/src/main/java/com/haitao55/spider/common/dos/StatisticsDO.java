/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: StatisticsDO.java 
 * @Prject: spider-55haitao-common
 * @Package: com.haitao55.spider.common.dos 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月7日 下午1:42:10 
 * @version: V1.0   
 */
package com.haitao55.spider.common.dos;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.haitao55.spider.common.dos.cons.StatisticsPK;

/** 
 * @ClassName: StatisticsDO 
 * @Description: 统计模块的数据库表映射类,在service层与TaskView对象做转换
 * @author: zhoushuo
 * @date: 2016年9月7日 下午1:42:10  
 */
@Entity
@IdClass(StatisticsPK.class)
@Table(name="statistics")
public class StatisticsDO {
	@Id
	private Long taskId;
	@Id
	private Long startTime;
	@Column
	private Long endTime;
	@Column
	private Integer successCount;
	@Column
	private Integer failedCount;
	@Column
	private Integer offlineCount;
	@Column
	private Integer handleCount;
	@Column
	private Integer totalCount;
	
	public Long getTaskId() {
		return taskId;
	}
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	public Integer getSuccessCount() {
		return successCount;
	}
	public void setSuccessCount(Integer successCount) {
		this.successCount = successCount;
	}
	public Integer getFailedCount() {
		return failedCount;
	}
	public void setFailedCount(Integer failedCount) {
		this.failedCount = failedCount;
	}
	public Integer getOfflineCount() {
		return offlineCount;
	}
	public void setOfflineCount(Integer offlineCount) {
		this.offlineCount = offlineCount;
	}
	public Integer getHandleCount() {
		return handleCount;
	}
	public void setHandleCount(Integer handleCount) {
		this.handleCount = handleCount;
	}
	public Integer getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}
	
}
