package com.haitao55.spider.common.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.haitao55.spider.common.dao.ProxyDAO;
import com.haitao55.spider.common.dao.impl.mysql.ProxyMapper;
import com.haitao55.spider.common.dos.ProxyDO;

@Repository("proxyDAO")
public class ProxyDAOImpl implements ProxyDAO {
	@Autowired
	private ProxyMapper proxyMapper;

	@Override
	public List<ProxyDO> getAllProxies() {
		List<ProxyDO> allProxys = proxyMapper.getAllProxies();
		return allProxys;
	}

	@Override
	public void insertProxy(ProxyDO proxyDO) {
		proxyMapper.insertProxy(proxyDO);
	}

	@Override
	public ProxyDO selectProxy(ProxyDO convertView2DO) {
		return proxyMapper.selectProxy(convertView2DO);
	}

	@Override
	public List<ProxyDO> queryDistinctRegions() {
		return proxyMapper.queryDistinctRegions();
	}

	@Override
	public List<ProxyDO> selectByRegionId(String regionId) {
		return proxyMapper.selectByRegionId(regionId);
	}

}
