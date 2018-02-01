package com.haitao55.spider.data.service.service.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.haitao55.spider.common.dao.HaiTunCunItemDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.HaiTunCunRetBody;
import com.haitao55.spider.data.service.service.HaituncunService;
import com.haitao55.spider.data.service.utils.Constants;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年3月7日 下午4:35:24  
 */
@Service("haituncunService")
public class HaituncunServiceImpl implements HaituncunService {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_MONITOR);
	
	@Resource
	private HaiTunCunItemDAO haituncunDao;

	@Override
	public long writeAllDataToClient(BufferedWriter bw, int page, int pageSize) throws IOException {
		Long count = this.haituncunDao.countItems();
		page = (int)(count/pageSize) + 1;
		List<ItemDO> itemDOs = null;
		long sum = 0l;
		for(int i=0; i<page; i++){
			itemDOs = this.haituncunDao.queryCompartiotorItemsByTaskId(i*pageSize, pageSize);
			for(ItemDO item : itemDOs){
				CrawlerJSONResult result = JsonUtils.json2bean(item.getValue(), CrawlerJSONResult.class);
				HaiTunCunRetBody retBody = result.getHtcRetBody();
				bw.write(JsonUtils.bean2json(retBody));
				bw.newLine();
				sum++;
			}
			bw.flush();
		}
		return sum;
	}
	
	

}
