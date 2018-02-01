/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HeartbeatDao.java 
 * @Prject: spider-55haitao-common
 * @Package: com.haitao55.spider.common.dao 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月18日 上午11:34:42 
 * @version: V1.0   
 */
package com.haitao55.spider.common.dao;

import java.util.List;

import com.haitao55.spider.common.dos.HeartbeatDO;

/**
 * @ClassName: HeartbeatDao
 * @Description: 心跳管理Dao层接口
 * @author: zhoushuo
 * @date: 2016年9月18日 上午11:34:42
 */
public interface HeartbeatDao {
	/**
	 * 
	 * @Title: selectAllHeartbeat
	 * @Description: 查询获取所有IP最新Heartbeat
	 * @return
	 * @return: List<HeartbeatDO>
	 */
	public List<HeartbeatDO> selectAllLatestHeartbeat();

	/**
	 * 新增或更新一条心跳记录
	 */
	public void upsertHeartbeat(HeartbeatDO heartbeatDO);

	/**
	 * 清空心跳数据库
	 */
	public void clear();
}