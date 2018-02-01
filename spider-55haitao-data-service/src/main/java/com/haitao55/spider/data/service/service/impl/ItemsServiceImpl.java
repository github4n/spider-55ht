package com.haitao55.spider.data.service.service.impl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.haitao55.spider.common.dao.CurrentItemDAO;
import com.haitao55.spider.common.dao.ItemDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.data.service.service.ItemsService;


/**
 * 
 * 功能：商品管理模块的服务层接口实现类
 * 
 * @author denghuan
 * @time 2017年4月19日 下午9:50:05
 * @version 1.0
 */
@Service("itemsService")
public class ItemsServiceImpl implements ItemsService {

	@Autowired
	private CurrentItemDAO currentItemDAO;
	
	@Autowired
	private ItemDAO itemDAO;

	@Override
	public String getLastItemValueByUrlMd5(String taskId, String urlMd5) {
		String rst = "";

		ItemDO itemDO = this.currentItemDAO.queryMd5UrlLastItem(Long.valueOf(taskId), urlMd5);
		if (itemDO != null) {
			rst = itemDO.getValue();
		}

		return rst;
	}

	@Override
	public String getCurrentItemValueByDocId(String taskId, String docId) {
		String rst = "";

		ItemDO itemDO = this.currentItemDAO.queryLastItem(Long.valueOf(taskId), docId);
		if (itemDO != null) {
			rst = itemDO.getValue();
		}

		return rst;
	}
	
	@Override
	public String getHistoryItemByUrlMd5(String taskId, String urlMd5) {
		String rst = "";

		ItemDO itemDO = this.itemDAO.queryMd5UrlLastItem(Long.valueOf(taskId), urlMd5);
		if (itemDO != null) {
			rst = itemDO.getValue();
		}
		return rst;
	}

	@Override
	public String queryCurrentMD5Urls(String taskId, String md5Urls) {
		String rst = "";

		ItemDO itemDO = this.currentItemDAO.queryCurrentMD5Urls(Long.valueOf(taskId), md5Urls);
		if (itemDO != null) {
			rst = itemDO.getValue();
		}
		return rst;
	}
}