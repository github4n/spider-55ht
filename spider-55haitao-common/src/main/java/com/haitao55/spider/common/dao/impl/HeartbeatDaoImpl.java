/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HeartbeatDaoImpl.java 
 * @Prject: spider-55haitao-common
 * @Package: com.haitao55.spider.common.dao.impl 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月18日 上午11:37:51 
 * @version: V1.0   
 */
package com.haitao55.spider.common.dao.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.haitao55.spider.common.dao.HeartbeatDao;
import com.haitao55.spider.common.dao.impl.mysql.HeartbeatMapper;
import com.haitao55.spider.common.dos.HeartbeatDO;

/**
 * @ClassName: HeartbeatDaoImpl
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年9月18日 上午11:37:51
 */
@Repository("heartbeatDao")
public class HeartbeatDaoImpl implements HeartbeatDao {

	@Autowired
	private HeartbeatMapper heartbeatMapper;

	/*
	 * (non Javadoc)
	 * 
	 * @Title: selectAllHeartbeat
	 * 
	 * @Description: TODO
	 * 
	 * @return
	 * 
	 * @see com.haitao55.spider.common.dao.HeartbeatDao#selectAllHeartbeat()
	 */
	@Override
	public List<HeartbeatDO> selectAllLatestHeartbeat() {
		return this.heartbeatMapper.selectAllLatest();
	}

	@Override
	public void upsertHeartbeat(HeartbeatDO heartbeatDO) {
		HeartbeatDO heartbeatDOFromDB = this.heartbeatMapper.getHeartbeatByIpAndProcId(heartbeatDO);

		if (Objects.isNull(heartbeatDOFromDB)) {
			this.heartbeatMapper.addHeartbeat(heartbeatDO);
		} else {
			this.heartbeatMapper.updateHeartbeat(heartbeatDO);
		}
	}

	@Override
	public void clear() {
		this.heartbeatMapper.clear();
	}
}