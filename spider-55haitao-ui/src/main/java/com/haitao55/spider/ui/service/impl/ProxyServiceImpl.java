package com.haitao55.spider.ui.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.haitao55.spider.common.dao.ProxyDAO;
import com.haitao55.spider.common.dos.ProxyDO;
import com.haitao55.spider.common.service.impl.BaseService;
import com.haitao55.spider.ui.common.util.ConvertPageInstance;
import com.haitao55.spider.ui.service.ProxyService;
import com.haitao55.spider.ui.view.ProxyView;

/**
 * 
* Title:代理管理service实现类
* Descrtion:代理管理service实现类
* Company: 55海淘
* @author zhaoxl 
* @date 2016年8月26日 上午10:41:53
* @version 1.0
 */
@Service("proxyService")
public class ProxyServiceImpl extends BaseService<ProxyDO> implements ProxyService {

	@Autowired
	private ProxyDAO proxyDao;
	
	@Override
	public List<ProxyView> getAllProxies(ProxyView proxyView, int page, int pageSize) {
		//分页查询
        PageHelper.startPage(page, pageSize);
		List<ProxyDO> allProxys = this.proxyDao.getAllProxies();
		Page<ProxyDO> p=(Page<ProxyDO>)allProxys;
		Page<ProxyView> pv=new Page<ProxyView>();
		ConvertPageInstance.convert(p,pv);
		if (allProxys != null) {
			for (ProxyDO proxyDO : allProxys) {
				ProxyView proxy = this.convertDO2View(proxyDO);
				pv.add(proxy);
			}
		}
		return pv;
	}
	
	
	private ProxyView convertDO2View(ProxyDO proxyDO) {
		ProxyView proxyView = new ProxyView();
		proxyView.setId(proxyDO.getId());
		proxyView.setRegionId(proxyDO.getRegionId());
		proxyView.setRegionName(proxyDO.getRegionName());
		proxyView.setIp(proxyDO.getIp());
		proxyView.setPort(proxyDO.getPort());
		return proxyView;
	}

	
	private ProxyDO convertView2DO(ProxyView proxyView) {
		ProxyDO proxyDO = new ProxyDO();
		proxyDO.setId(proxyView.getId());
		proxyDO.setRegionId(proxyView.getRegionId());
		proxyDO.setRegionName(proxyView.getRegionName());
		proxyDO.setIp(proxyView.getIp());
		proxyDO.setPort(proxyView.getPort());
		return proxyDO;
	}

	@Override
	public void insertProxy(ProxyView proxyView) {
		ProxyDO proxyDO=this.convertView2DO(proxyView);
		proxyDao.insertProxy(proxyDO);
	}


	@Override
	public void updateProxy(ProxyView proxyView) {
		ProxyDO proxy = convertView2DO(proxyView);
		this.updateNotNull(proxy);
	}

	@Override
	public ProxyView selectProxy(ProxyView proxyView) {
		ProxyDO convertView2DO = convertView2DO(proxyView);
		ProxyDO proxyDO = proxyDao.selectProxy(convertView2DO);
		ProxyView view = convertDO2View(proxyDO);
		return view;
	}

	@Override
	public ProxyView selectProxyByMapper(ProxyView proxyView) {
		ProxyDO proxy = convertView2DO(proxyView);
		ProxyDO selectByKey = this.selectByKey(proxy);
		ProxyView view = convertDO2View(selectByKey);
		return view;
	}


	@Override
	public List<ProxyView> queryDistinctRegions() {
		List<ProxyView> pl=new ArrayList<ProxyView>();
		List<ProxyDO> list=proxyDao.queryDistinctRegions();
		if (list != null) {
			for (ProxyDO proxyDO : list) {
				ProxyView proxy = this.convertDO2View(proxyDO);
				pl.add(proxy);
			}
		}
		return pl;
	}


	@Override
	public List<ProxyView> queryByRegionId(String regionId) {
		List<ProxyDO> listDO = this.proxyDao.selectByRegionId(regionId);
		List<ProxyView> listView = new ArrayList<>();
		for(ProxyDO p : listDO){
			listView.add(this.convertDO2View(p));
		}
		return listView;
	}

}
