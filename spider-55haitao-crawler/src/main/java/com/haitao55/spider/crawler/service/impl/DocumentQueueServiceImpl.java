package com.haitao55.spider.crawler.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.service.DocumentQueueService;
import com.haitao55.spider.crawler.service.DocumentService;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：相当于爬虫节点内的局部缓存——每次从dsc层拉出来一批待抓取数据，供爬虫程序后续使用
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午10:26:05
 * @version 1.0
 */
public class DocumentQueueServiceImpl implements DocumentQueueService {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_TEMPO);

	private Queue<Url> queue = new ConcurrentLinkedQueue<Url>();

	private final Lock lock = new ReentrantLock();

	@Resource
	private DocumentService documentService;

	private int documentInputCount = 200;

	private int documentOutputCount = 10;

	/**
	 * 用于控制抓取速率的周期
	 */
	private int tempoInterval;

	/**
	 * 类的初始化方法，用来初始化定时器对象
	 */
	public void init() {
		Timer timer = new Timer("Print-Urls-Queue-Timer", true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				loggerQueueContents();
			}
		}, 8 * 1000, 8 * 1000);
	}

	private void loggerQueueContents() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (Url url : queue) {
			Task task = url.getTask();
			String taskName = task.getTaskName();
			map.put(taskName, (map.get(taskName) == null ? 1 : (map.get(taskName).intValue() + 1)));
		}

		logger.info("Print-Urls-Queue-Contents::{}", map);
	}

	/**
	 * 批量出队，最大上限为配置的默认值
	 * 
	 * @return
	 */
	public List<Url> getUrls() {
		return getUrls(documentOutputCount);
	}

	/**
	 * 批量出队
	 * 
	 * @param maxCount
	 * @return
	 */
	public List<Url> getUrls(int maxCount) {
		if (maxCount <= 0) {// 期望出队数量
			maxCount = documentOutputCount;// 默认出队数量
		}

		if (isEmpty()) {
			try {
				lock.lock();
				if (isEmpty()) {
					loadDocuments();
				}
			} finally {
				lock.unlock();
			}
		}

		if (isEmpty()) {
			return Collections.emptyList();
		}

		List<Url> ret = new ArrayList<Url>();
		for (int index = 0; index < maxCount; index++) {
			Url url = queue.poll();
			if (url == null) {
				break;
			}
			ret.add(url);
		}

		return ret;
	}

	private boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * 批量入队
	 */
	private void loadDocuments() {
		List<Url> urlList = documentService.getDocuments(documentInputCount);
		if (CollectionUtils.isEmpty(urlList)) {
			return;
		}

		logger.info("Load {} urls into local queue from document service!", urlList.size());
		queue.addAll(urlList);
	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

	public int getDocumentInputCount() {
		return documentInputCount;
	}

	public void setDocumentInputCount(int documentInputCount) {
		this.documentInputCount = documentInputCount;
	}

	public int getDocumentOutputCount() {
		return documentOutputCount;
	}

	public void setDocumentOutputCount(int documentOutputCount) {
		this.documentOutputCount = documentOutputCount;
	}

	public int getTempoInterval() {
		return tempoInterval;
	}

	public void setTempoInterval(int tempoInterval) {
		this.tempoInterval = tempoInterval;
	}
}