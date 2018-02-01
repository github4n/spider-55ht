/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HeartbeatMapper.java 
 * @Prject: spider-55haitao-common
 * @Package: com.haitao55.spider.common.dao.impl.mysql 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月18日 上午11:21:50 
 * @version: V1.0   
 */
package com.haitao55.spider.common.dao.impl.mysql;

import java.util.List;

import com.haitao55.spider.common.dos.HeartbeatDO;
import com.haitao55.spider.common.util.MyMapper;

/**
 * @ClassName: HeartbeatMapper
 * @Description: 心跳管理模块的DAO接口的MyBatis实现类
 * @author: zhoushuo
 * @date: 2016年9月18日 上午11:21:50
 */
public interface HeartbeatMapper extends MyMapper<HeartbeatDO> {
	public List<HeartbeatDO> selectAllLatest();

	public HeartbeatDO getHeartbeatByIpAndProcId(HeartbeatDO heartbeatDO);

	public void addHeartbeat(HeartbeatDO heartbeatDO);
	
	public void updateHeartbeat(HeartbeatDO heartbeatDO);
	
	public void clear();
}