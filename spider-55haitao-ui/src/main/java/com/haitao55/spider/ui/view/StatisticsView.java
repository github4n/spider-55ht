/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: StatisticsView.java 
 * @Prject: spider-55haitao-ui
 * @Package: com.haitao55.spider.ui.view 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月7日 上午11:47:10 
 * @version: V1.0   
 */
package com.haitao55.spider.ui.view;

/** 
 * @ClassName: StatisticsView 
 * @Description: 统计管理模块展示层对象,在service层与StatisticsViewDO对象做转换
 * @author: zhoushuo
 * @date: 2016年9月7日 上午11:47:10  
 */
public class StatisticsView {
	private String taskId;
	private String taskName;
	private String startTime;
	private String endTime;
	private Integer successCount;
	private Integer failedCount;
	private Integer offlineCount;
	private Integer handleCount;
	private Integer totalCount;
	private Long accurateStartTime;
	
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
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
	public Long getAccurateStartTime() {
		return accurateStartTime;
	}
	public void setAccurateStartTime(Long accurateStartTime) {
		this.accurateStartTime = accurateStartTime;
	}
	
}
