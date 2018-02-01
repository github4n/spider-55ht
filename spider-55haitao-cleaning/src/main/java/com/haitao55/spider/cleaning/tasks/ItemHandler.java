package com.haitao55.spider.cleaning.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.cleaning.service.ICleaningService;

/**
 * 
 * 功能：网站Json数据处理线程类,存放mongodb
 */
public class ItemHandler implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private String item;
	private ICleaningService cleanService;

	public ItemHandler(String item, ICleaningService cleanService) {
		this.item = item;
		this.cleanService = cleanService;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		
		if (item == null) {
			logger.error("item is null!");
			return;
		}

		this.cleanService.handleItem(item);
		
		logger.info("Item Handler, current-time:{}", System.currentTimeMillis() - startTime);
	}
}