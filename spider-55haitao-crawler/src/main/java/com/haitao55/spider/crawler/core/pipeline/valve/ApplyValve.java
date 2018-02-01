package com.haitao55.spider.crawler.core.pipeline.valve;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.pipeline.context.ValveContext;
import com.haitao55.spider.crawler.service.DocumentQueueService;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：获取待抓取urls的Valve实现类，批量获取
 * 
 * @author Arthur.Liu
 * @time 2016年8月2日 下午2:24:41
 * @version 1.0
 */
public class ApplyValve implements Valve {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	/**
	 * 当获取不到待抓取的urls时，线程休眠时间，单位：毫秒
	 */
	private int sleepTimeNoneUrls = 3000;

	private DocumentQueueService documentQueueService;

	@Override
	public String getInfo() {
		String info = (new StringBuilder()).append(this.getClass().getName()).append(SEPARATOR_INFO_FIELDS)
				.append(Thread.currentThread().toString()).append(SEPARATOR_INFO_FIELDS).append(this.toString())
				.toString();
		return info;
	}

	@Override
	public void invoke() throws Exception {
		List<Url> urls = this.documentQueueService.getUrls();

		while (CollectionUtils.isEmpty(urls)) {// 如果从DocumentService中获取不到待抓取的urls，则休眠线程，避免后续Valve的无谓执行
			logger.info(
					"SeedsValve.invoke() is called, got none urls from document queue service, will sleep {} milliseconds:(",
					sleepTimeNoneUrls);
			Thread.sleep(sleepTimeNoneUrls);

			urls = this.documentQueueService.getUrls();
		}

		logger.info("SeedsValve.invoke() is called, got {} urls from document service:)", urls.size());
		ValveContext.clear();
		ValveContext.putUrls(urls);
	}

	public DocumentQueueService getDocumentQueueService() {
		return documentQueueService;
	}

	public void setDocumentQueueService(DocumentQueueService documentQueueService) {
		this.documentQueueService = documentQueueService;
	}

	public int getSleepTimeNoneUrls() {
		return sleepTimeNoneUrls;
	}

	public void setSleepTimeNoneUrls(int sleepTimeNoneUrls) {
		this.sleepTimeNoneUrls = sleepTimeNoneUrls;
	}
}