package com.haitao55.spider.common.dao;

import java.util.List;

import com.haitao55.spider.common.dos.ProxyDO;

/**
 * 
 * 功能：代理IP操作接口类
 * 
 * @author Arthur.Liu
 * @time 2016年8月23日 上午10:57:02
 * @version 1.0
 */
public interface ProxyDAO {
	/**
	 * 获取所有代理IP实例信息
	 * @param rows 
	 * @param page 
	 * 
	 * @return
	 */
	public List<ProxyDO> getAllProxies();

	public void insertProxy(ProxyDO proxyDO);
	/**
	 * 查询代理ip
	 * @param convertView2DO
	 * @return
	 */
	public ProxyDO selectProxy(ProxyDO convertView2DO);
	/**
	 * 获取去重后到代理ip  regionId,regionName
	 * @return
	 */
	public List<ProxyDO> queryDistinctRegions();
	
	/**
	 * 按regionid查询
	 */
	public List<ProxyDO> selectByRegionId(String regionId);
}