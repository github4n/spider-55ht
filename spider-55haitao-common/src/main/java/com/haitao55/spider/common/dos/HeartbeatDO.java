/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HeartbeatDO.java 
 * @Prject: spider-55haitao-common
 * @Package: com.haitao55.spider.common.dos 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月18日 上午11:25:57 
 * @version: V1.0   
 */
package com.haitao55.spider.common.dos;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.haitao55.spider.common.dos.cons.HeartbeatPK;

/**
 * @ClassName: HeartbeatDO
 * @Description: 心跳模块的数据库表映射类,在service层与TaskView对象做转换
 * @author: zhoushuo
 * @date: 2016年9月18日 上午11:25:57
 */
@Entity
@IdClass(HeartbeatPK.class)
@Table(name = "heartbeat")
public class HeartbeatDO {
	@Id
	private Long time;
	@Id
	private String ip;
	@Column
	private String procId;
	@Column
	private int threadCount;

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getProcId() {
		return procId;
	}

	public void setProcId(String procId) {
		this.procId = procId;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

}
