/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: StatisticsPK.java 
 * @Prject: spider-55haitao-common
 * @Package: com.haitao55.spider.common.dos.cons 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月7日 下午2:13:22 
 * @version: V1.0   
 */
package com.haitao55.spider.common.dos.cons;

import java.io.Serializable;

/** 
 * @ClassName: StatisticsPK 
 * @Description: 统计管理DO的联合主键类
 * @author: zhoushuo
 * @date: 2016年9月7日 下午2:13:22  
 */
public class StatisticsPK implements Serializable{
	
	private Long taskId;
	private Long startTime;
	
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
	
}
