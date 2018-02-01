/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HeartbeatView.java 
 * @Prject: spider-55haitao-ui
 * @Package: com.haitao55.spider.ui.view 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月18日 上午11:10:21 
 * @version: V1.0   
 */
package com.haitao55.spider.ui.view;

/**
 * @ClassName: HeartbeatView
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年9月18日 上午11:10:21
 */
public class HeartbeatView {

	private String time; // 格式化之后的心跳发生时间
	private Long accurateTime; // 心跳发生的精确时间
	private String ip; // 客户端机器IP
	private String procId; // 客户端进程ID
	private int threadCount; // 客户端进程中爬虫线程数量
	private String vitalSign;// 生命体征

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
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

	public Long getAccurateTime() {
		return accurateTime;
	}

	public void setAccurateTime(Long accurateTime) {
		this.accurateTime = accurateTime;
	}

	public String getVitalSign() {
		return vitalSign;
	}

	public void setVitalSign(String vitalSign) {
		this.vitalSign = vitalSign;
	}
}