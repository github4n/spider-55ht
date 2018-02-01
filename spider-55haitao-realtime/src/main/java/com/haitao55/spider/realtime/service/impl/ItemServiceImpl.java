package com.haitao55.spider.realtime.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.haitao55.spider.common.dao.ItemDAO;
import com.haitao55.spider.realtime.service.ItemService;
import com.haitao55.spider.realtime.view.ItemView;

/**
 * 
 * 功能：实时抓取(RealtimeCrawler)模块范围内使用的商品服务接口实现类
 * 
 * @author Arthur.Liu
 * @time 2016年9月7日 下午6:38:02
 * @version 1.0
 */
@Service("itemService")
public class ItemServiceImpl implements ItemService {
	private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

	private ItemDAO itemDAO;

	@Override
	public ItemView getLastItem(Long taskId, String docId, String skuId) {
		logger.info(this.itemDAO.toString());

		return null;
	}

	public ItemDAO getItemDAO() {
		return itemDAO;
	}

	public void setItemDAO(ItemDAO itemDAO) {
		this.itemDAO = itemDAO;
	}
}