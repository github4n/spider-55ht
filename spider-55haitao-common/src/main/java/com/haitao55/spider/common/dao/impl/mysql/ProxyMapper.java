package com.haitao55.spider.common.dao.impl.mysql;

import java.util.List;

import com.haitao55.spider.common.dos.ProxyDO;
import com.haitao55.spider.common.util.MyMapper;

/**
 * 
 * 功能：代理IP管理模块的DAO接口的MyBatis实现类
 * 
 * @author Arthur.Liu
 * @time 2016年8月23日 上午11:14:46
 * @version 1.0
 */
public interface ProxyMapper extends MyMapper<ProxyDO>{

	List<ProxyDO> getAllProxies();

	void insertProxy(ProxyDO proxyDO);

	ProxyDO selectProxy(ProxyDO convertView2DO);

	List<ProxyDO> queryDistinctRegions();

	List<ProxyDO> selectByRegionId(String regionId);
}