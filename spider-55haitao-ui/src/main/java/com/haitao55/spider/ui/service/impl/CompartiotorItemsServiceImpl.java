package com.haitao55.spider.ui.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.haitao55.spider.common.dao.CtorItemDAO;
import com.haitao55.spider.common.dos.CtorItemDO;
import com.haitao55.spider.ui.service.CompartiotorItemsService;
/**
 * 竞品 商品查询 service 实现类
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月23日 上午11:01:19
* @version 1.0
 */
@Service("compartiotorItemsService")
public class CompartiotorItemsServiceImpl implements CompartiotorItemsService {
	
	@Autowired
	private CtorItemDAO ctorItemDAO;

	@Override
	public List<String> queryCompartiotorItemsByTaskId(long taskId,int currentDay,int currentHour) {
		List<String> list = new ArrayList<String>();
		List<CtorItemDO> items = ctorItemDAO.queryCompartiotorItemsByTaskId(taskId,currentDay,currentHour);
		if(null != items && items.size() > 0){
			for (CtorItemDO itemDO : items) {
				list.add(itemDO.getValue());
			}
		}
		return list;
	}

}
