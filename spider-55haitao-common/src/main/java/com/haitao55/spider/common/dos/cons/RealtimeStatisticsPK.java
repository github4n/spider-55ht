package com.haitao55.spider.common.dos.cons;

import java.io.Serializable;

/**
 * 实时核价统计管理　联合主键
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月19日 下午3:44:15
* @version 1.0
 */
public class RealtimeStatisticsPK implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5924042292933252638L;
	private Long taskId;
	private String realtimeTime;
	public Long getTaskId() {
		return taskId;
	}
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	public String getRealtimeTime() {
		return realtimeTime;
	}
	public void setRealtimeTime(String realtimeTime) {
		this.realtimeTime = realtimeTime;
	}
	
}
