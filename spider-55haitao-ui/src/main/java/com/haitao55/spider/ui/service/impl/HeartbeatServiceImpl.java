/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HeartbeatServiceImpl.java 
 * @Prject: spider-55haitao-ui
 * @Package: com.haitao55.spider.ui.service.impl 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月18日 上午11:57:31 
 * @version: V1.0   
 */
package com.haitao55.spider.ui.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.haitao55.spider.common.dao.HeartbeatDao;
import com.haitao55.spider.common.dos.HeartbeatDO;
import com.haitao55.spider.ui.common.util.ConvertPageInstance;
import com.haitao55.spider.ui.service.HeartbeatService;
import com.haitao55.spider.ui.view.HeartbeatView;

/** 
 * @ClassName: HeartbeatServiceImpl 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年9月18日 上午11:57:31  
 */
@Service("heartbeatService")
public class HeartbeatServiceImpl implements HeartbeatService{

	@Autowired
	private HeartbeatDao heartbeatDao;
	/* (non Javadoc) 
	 * @Title: getAllLatestHeartbeat
	 * @Description: TODO
	 * @param pageNum
	 * @param pageSize
	 * @return 
	 * @see com.haitao55.spider.ui.service.HeartbeatService#getAllLatestHeartbeat(int, int) 
	 */
	@Override
	public List<HeartbeatView> getAllLatestHeartbeat(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		List<HeartbeatDO> list = this.heartbeatDao.selectAllLatestHeartbeat();
		Page<HeartbeatDO> page = (Page<HeartbeatDO>) list;
		Page<HeartbeatView> pageView = new Page<>();
		ConvertPageInstance.convert(page, pageView);
		if(list != null) {
			for(HeartbeatDO hdo : list){
				pageView.add(this.convertDO2View(hdo));
			}
		}
		return pageView;
	}
	
	private HeartbeatView convertDO2View(HeartbeatDO hdo){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		HeartbeatView hv = new HeartbeatView();
		hv.setIp(hdo.getIp());
		hv.setAccurateTime(hdo.getTime());
		hv.setTime(sdf.format(new Date(hdo.getTime())));
		hv.setProcId(hdo.getProcId());
		hv.setThreadCount(hdo.getThreadCount());
		return hv;
	}

}
