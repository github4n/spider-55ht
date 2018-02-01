/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HeartbeatPK.java 
 * @Prject: spider-55haitao-common
 * @Package: com.haitao55.spider.common.dos.cons 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月18日 上午11:23:31 
 * @version: V1.0   
 */
package com.haitao55.spider.common.dos.cons;

import java.io.Serializable;

/** 
 * @ClassName: HeartbeatPK 
 * @Description: 心跳管理DO的联合主键类
 * @author: zhoushuo
 * @date: 2016年9月18日 上午11:23:31  
 */
public class HeartbeatPK implements Serializable{
	private Long time;
	private String ip;
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
}
