/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HeartbeatService.java 
 * @Prject: spider-55haitao-ui
 * @Package: com.haitao55.spider.ui.service 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月18日 上午11:54:27 
 * @version: V1.0   
 */
package com.haitao55.spider.ui.service;

import java.util.List;

import com.haitao55.spider.ui.view.HeartbeatView;

/** 
 * @ClassName: HeartbeatService 
 * @Description: 心跳管理模块Service层接口
 * @author: zhoushuo
 * @date: 2016年9月18日 上午11:54:27  
 */
public interface HeartbeatService {
	public List<HeartbeatView> getAllLatestHeartbeat(int pageNum, int pageSize);
}
