package com.haitao55.spider.crawler.core.pipeline.valve;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.core.pipeline.context.ValveContext;
import com.haitao55.spider.crawler.service.DocumentService;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：向Controller层反馈Urls信息的Valve实现类
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 下午4:11:08
 * @version 1.0
 */
public class EchoValve implements Valve {
	private Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private Logger loggerItems = LoggerFactory.getLogger(Constants.LOGGER_NAME_ALLITEMS);

	private DocumentService documentService;

	@Override
	public String getInfo() {
		String info = (new StringBuilder()).append(this.getClass().getName()).append(SEPARATOR_INFO_FIELDS)
				.append(Thread.currentThread().toString()).append(SEPARATOR_INFO_FIELDS).append(this.toString())
				.toString();
		return info;
	}

	@Override
	public void invoke() throws Exception {
		List<Url> urls = ValveContext.getUrls();
		logger.info("save {} urls to controller!", (CollectionUtils.isEmpty(urls) ? 0 : urls.size()));

		// 向种子数据库中回存处理过的Urls；
		// 1.更新/删除当初从Controller传递上来的Urls
		this.documentService.updelDocuments(urls);

		// 2.新增(或修改)新迭代出来的newUrls
		Set<Url> newUrlSet = new HashSet<Url>();
		for (Url url : urls) {
			newUrlSet.addAll(url.getNewUrls());
			//log全部新发现的item类型的url
			logNewItemUrls(url);
		}
		List<Url> newUrlList = new ArrayList<Url>(newUrlSet);
		this.documentService.upsertDocuments(newUrlList);
	}

	private void logNewItemUrls(Url url) {
		for(Url newUrl : url.getNewUrls()){
			if(UrlType.ITEM.equals(newUrl.getUrlType())){
				loggerItems.info(newUrl.getValue());
			}
		}
	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}
}