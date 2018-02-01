package com.haitao55.spider.ui.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.haitao55.spider.common.dao.CurrentItemDAO;
import com.haitao55.spider.common.dao.ItemDAO;
import com.haitao55.spider.common.dao.TaskDAO;
import com.haitao55.spider.common.dao.UrlDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.utils.SpiderDateTimeUtil;
import com.haitao55.spider.ui.common.util.ConvertPageInstance;
import com.haitao55.spider.ui.service.ItemsService;
import com.haitao55.spider.ui.view.ItemHistoryView;
import com.haitao55.spider.ui.view.ItemsView;


/**
 * 
 * 功能：商品管理模块的服务层接口实现类
 * 
 * @author Arthur.Liu
 * @time 2016年11月5日 下午9:50:05
 * @version 1.0
 */
@Service("itemsService")
public class ItemsServiceImpl implements ItemsService {

	@Autowired
	private TaskDAO taskDao;

	@Autowired
	private UrlDAO urlDAO;

	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private CurrentItemDAO currentItemDAO;

	@Override
	public List<ItemsView> getAllTaskItems(int pageNum, int pageSize) {
		Page<ItemsView> rst = new Page<ItemsView>();

		PageHelper.startPage(pageNum, pageSize);
		List<TaskDO> allTasks = this.taskDao.getAllTasks();
		for (TaskDO taskDO : allTasks) {
			ItemsView itemsView = new ItemsView();
			itemsView.setTaskId(taskDO.getId());
			itemsView.setTaskName(taskDO.getName());
			itemsView.setAllUrlsCount(this.urlDAO.queryUrlsCountByTaskId(String.valueOf(taskDO.getId())));
			itemsView.setItemUrlsCount(this.urlDAO.queryItemUrlsCountByTaskId(String.valueOf(taskDO.getId())));
			itemsView.setAllItemsCount(
					-1/* (int) this.itemDAO.countItems(taskDO.getId()) */);
			itemsView.setOnlineItemsCount(
					-1/* (int) this.itemDAO.countOnlineItems(taskDO.getId()) */);
			rst.add(itemsView);
		}
		return doList2PageView(rst);
	}

	private Page<ItemsView> doList2PageView(Page<ItemsView> list) {
		Page<ItemsView> page = (Page<ItemsView>) list;
		Page<ItemsView> pageView = new Page<ItemsView>();
		ConvertPageInstance.convert(page, pageView);
		if (list != null) {
			for (ItemsView itemsView : list) {
				pageView.add(itemsView);
			}
		}
		return pageView;
	}

	@Override
	public String getLastItemValueByDocId(String taskId, String docId) {
		String rst = "";

		ItemDO itemDO = this.itemDAO.queryLastItem(Long.valueOf(taskId), docId);
		if (itemDO != null) {
			rst = itemDO.getValue();
		}

		return rst;
	}

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
	public List<String> getHistoryItemList(String taskId, String urlMd5) {
		List<String> itemList = new ArrayList<>();
		long totalRows = this.itemDAO.countUrlMD5Items(Long.valueOf(taskId),urlMd5);
		if(totalRows > 0){
			int totalPages = (int) (totalRows / 10) + 1;
			for(int i = 0; i < totalPages; i++){
				int count = i * 10;
				List<ItemDO> items = this.itemDAO.queryMd5UrlList(Long.valueOf(taskId), urlMd5,count,10);
				if (CollectionUtils.isNotEmpty(items)) {
					for(ItemDO itemDO: items){
						String rst = itemDO.getValue();
						JSONObject jsonObject = JSONObject.parseObject(rst);
						jsonObject.put("createTime", SpiderDateTimeUtil.format(itemDO.getCreateTime(), SpiderDateTimeUtil.FORMAT_LONG_DATE));
						String rs = jsonObject.toJSONString();
						itemList.add(rs);
					}
				}
			}
		}
		return itemList;
	}
	
	public Map<String,Object> getAllItems(String taskId, String urlMd5,int page, int pageSize){
		long totalRows = this.itemDAO.countUrlMD5Items(Long.valueOf(taskId),urlMd5);
		HashMap<String,Object> map = new HashMap<>();
		List<ItemHistoryView> list = new ArrayList<>();
		if(totalRows > 0){
			List<ItemDO> items = this.itemDAO.queryMd5UrlList(Long.valueOf(taskId), urlMd5,page,pageSize);
			if(CollectionUtils.isNotEmpty(items)){
				for(ItemDO item : items){
					String value = item.getValue();
					CrawlerJSONResult rs = CrawlerJSONResult.buildFrom(value);
					ItemHistoryView  itemHistory = new ItemHistoryView();
					Sku sku = rs.getRetbody().getSku();
					long createTime = item.getCreateTime();
					StringBuffer  skusSb =  new StringBuffer();
					if(sku != null){
						List<LSelectionList> lselectList = sku.getL_selection_list();
						if(CollectionUtils.isNotEmpty(lselectList)){
							if(CollectionUtils.isNotEmpty(lselectList)){
								for(LSelectionList select : lselectList){
									String skuId = select.getGoods_id();
									Float skuoOrigPirce = select.getOrig_price();
									Float skuoSalePirce = select.getSale_price();
									skusSb.append(skuId).append("->").append(skuoSalePirce).append("->").append(skuoOrigPirce).append("\n");
								}
							}
						}
					}
					itemHistory.setCreateTime(SpiderDateTimeUtil.format(createTime, SpiderDateTimeUtil.FORMAT_LONG_DATE));
					itemHistory.setSkus(skusSb.toString());
					RetBody retBody= rs.getRetbody();
					Price  price = retBody.getPrice();
					if(!Objects.isNull(price)){
						itemHistory.setOrigPrice(rs.getRetbody().getPrice().getOrig());
						itemHistory.setSalePrice(rs.getRetbody().getPrice().getSale());
						list.add(itemHistory);
					}
				}
			}
		}
		int pages = (int)(totalRows / pageSize ) + 1;
		map.put("pages", pages);
		map.put("count", totalRows);
		map.put("historyList", list);
		return map;
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
}