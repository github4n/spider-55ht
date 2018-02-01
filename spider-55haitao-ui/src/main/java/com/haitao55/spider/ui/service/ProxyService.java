package com.haitao55.spider.ui.service;

import java.util.List;

import com.haitao55.spider.common.dos.ProxyDO;
import com.haitao55.spider.common.service.IService;
import com.haitao55.spider.ui.view.ProxyView;

/**
 * 
* Title: 代理ip管理service接口
* Description: 代理ip管理service接口
* Company: 55海淘
* @author zhaoxl 
* @date 2016年8月26日 上午10:37:43
* @version 1.0
 */
public interface ProxyService extends IService<ProxyDO>{
	/**
	 * 获取所有代理ip
	 * @param pageSize 
	 * @param page 
	 * @param proxyView 
	 * @return
	 */
	List<ProxyView> getAllProxies(ProxyView proxyView, int page, int pageSize);
	
	/**
	 * 添加代理ip
	 * @param proxyView
	 */
	void insertProxy(ProxyView proxyView);
	/**
	 * 修改
	 * @param proxyView
	 */
	void updateProxy(ProxyView proxyView);
	ProxyView selectProxy(ProxyView proxyView);
	/**
	 * 使用通用mapper进行查询
	 * @param proxyView
	 * @return
	 */
	ProxyView selectProxyByMapper(ProxyView proxyView);
	/**
	 * 获取去重后到代理ip  regionId,regionName
	 * @return
	 */
	List<ProxyView> queryDistinctRegions();
	
	List<ProxyView> queryByRegionId(String regionId);
	
}