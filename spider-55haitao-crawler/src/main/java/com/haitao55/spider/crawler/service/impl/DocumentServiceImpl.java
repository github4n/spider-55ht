package com.haitao55.spider.crawler.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.thrift.TaskModel;
import com.haitao55.spider.common.thrift.ThriftService.Client;
import com.haitao55.spider.common.thrift.UrlModel;
import com.haitao55.spider.common.thrift.UrlTypeModel;
import com.haitao55.spider.crawler.common.cache.TaskCache;
import com.haitao55.spider.crawler.core.model.Rules;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlStatus;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.service.DocumentService;
import com.haitao55.spider.crawler.service.XmlParseService;
import com.haitao55.spider.crawler.thrift.pool.ThriftConnectionProvider;
import com.haitao55.spider.crawler.utils.AddressUtils;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：维护Urls的本地Service接口实现；在这个类中需要做一些转换工作，将远程返回的UrlModel对象转换成本地的Url对象
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午11:19:56
 * @version 1.0
 */
public class DocumentServiceImpl implements DocumentService {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_REMOTE);

	private ThriftConnectionProvider thriftConnectionProvider;

	private XmlParseService xmlParseService;

	@Override
	public List<Url> getDocuments(int limit) {
		Client client = this.thriftConnectionProvider.getObject();

		List<UrlModel> urlModels;
		try {
			urlModels = client.fetchUrls(limit);
			logger.info("Fetch {} urls from remote controller:",
					CollectionUtils.isEmpty(urlModels) ? 0 : urlModels.size());
		} catch (TException e) {
			this.thriftConnectionProvider.invalidateObject(client);
			client = null;
			logger.error("Error occurred while fetch urls from controller via thrift!{}", e);
			return null;
		} finally {
			this.thriftConnectionProvider.returnObject(client);// 及时释放连接对象
		}

		List<Url> urls = this.convert(urlModels);

		// 打乱顺序；
		// 因为从controller端传来的列表可能是根据taskId连续存储的，
		// 如果这样，则在后续遍历url执行抓取的过程中，会在一段时间内连续访问一个网站，这样过于集中是不好的；
		// 所以这里先打乱一下列表中url元素的顺序
		Collections.shuffle(urls);

		return urls;
	}

	private List<Url> convert(List<UrlModel> urlModels) {
		List<Url> rst = new ArrayList<Url>();

		if (CollectionUtils.isEmpty(urlModels)) {
			return rst;
		}

		for (UrlModel urlModel : urlModels) {
			Url url = this.convert(urlModel);
			rst.add(url);
		}

		return rst;
	}

	/**
	 * 
	 * @param urlModel
	 *            Crawler系统与Controller系统之间互相传递的数据对象
	 * @return Crawler系统内部自己使用的数据对象
	 */
	private Url convert(UrlModel urlModel) {
		if (TaskCache.getInstance().get(urlModel.getTaskId()) == null) {
			this.fetchSingleTaskConfigAndFillLocalCache(urlModel.getTaskId());
		}

		Url url = new Url();
		url.setUrlType(UrlType.convertUrlTypeModel2UrlType(urlModel.getType()));
		url.setId(urlModel.getId());
		url.setValue(urlModel.getValue());
		url.setParentUrl(urlModel.getParentUrl());
		url.setTaskId(urlModel.getTaskId());
		url.setTask(TaskCache.getInstance().get(urlModel.getTaskId()));
		url.setLatelyFailedCount(urlModel.getLatelyFailedCount());// 传递这个字段，在提供删除功能的时候可以少查询一次数据库
		url.setUrlStatus(UrlStatus.convertFromUrlStatusModel(urlModel.getUrlStatusModel()));
		url.setGrade(urlModel.getGrade());

		return url;
	}

	private void fetchSingleTaskConfigAndFillLocalCache(long taskId) {
		Client client = this.thriftConnectionProvider.getObject();
		try {
			TaskModel taskModel = client.fetchTask(taskId);

			Rules rules = null;
			try {
				rules = this.xmlParseService.parse(taskModel.getConfig());
			} catch (Exception e) {
				this.noticeTaskConfigError(taskId);
			}

			Task task = new Task();
			task.setTaskId(taskModel.getId());
			task.setTaskName(taskModel.getName());
			task.setRules(rules);
			task.setSiteRegion(taskModel.getSiteRegion());
			task.setProxyRegionId(taskModel.getProxyRegionId());

			TaskCache.getInstance().put(task.getTaskId(), task);// 填充到本地缓存中
		} catch (TException e) {
			this.thriftConnectionProvider.invalidateObject(client);
			client = null;
			logger.error("Error occurred while fatch one job config from controller!{}", e);
		} finally {
			this.thriftConnectionProvider.returnObject(client);
		}
	}

	/**
	 * 向Controller告知一个TaskConfig配置错误
	 * 
	 * @param taskId
	 */
	private void noticeTaskConfigError(long taskId) {
		Client client = this.thriftConnectionProvider.getObject();
		try {
			client.noticeTaskConfigError(taskId);
			throw new TException();
		} catch (TException e) {
			this.thriftConnectionProvider.invalidateObject(client);
			client = null;
			logger.error("Error occurred while notice crawler config error!");
		} finally {
			this.thriftConnectionProvider.returnObject(client);// 及时释放连接对象
		}
	}

	@Override
	public void updelDocuments(List<Url> urls) {
		if (CollectionUtils.isEmpty(urls)) {
			return;
		}

		List<UrlModel> urlModels = new ArrayList<UrlModel>(urls.size());
		for (Url url : urls) {
			UrlModel urlModel = this.convert(url);
			urlModels.add(urlModel);
		}

		Client client = this.thriftConnectionProvider.getObject();
		try {
			logger.info("sendupdel {} urls to remote controller:", CollectionUtils.isEmpty(urlModels) ? 0 : urlModels.size());
			client.updelUrls(urlModels);
		} catch (TException e) {
			this.thriftConnectionProvider.invalidateObject(client);
			client = null;
			logger.error("Error occurred while save url to controller via thrift!{}", e);
			e.printStackTrace();
		} finally {
			this.thriftConnectionProvider.returnObject(client);// 及时释放连接对象
		}
	}

	/**
	 * 
	 * @param url
	 *            Crawler系统内部自己使用的数据对象
	 * @return Crawler系统与Controller系统之间互相传递的数据对象
	 */
	private UrlModel convert(Url url) {
		UrlModel urlModel = new UrlModel();
		
		urlModel.setId(url.getId());
		urlModel.setValue(url.getValue());
		urlModel.setParentUrl(url.getParentUrl());
		urlModel.setTaskId(url.getTaskId());
		urlModel.setLatelyFailedCount(url.getLatelyFailedCount());
		urlModel.setLastCrawledIP(AddressUtils.getIP());
		urlModel.setLastCrawledTime(System.currentTimeMillis());
		urlModel.setType(UrlTypeModel.valueOf(url.getUrlType().getValue()));
		urlModel.setUrlStatusModel(UrlStatus.convert2UrlStatusModel(url.getUrlStatus()));
		urlModel.setLatelyErrorCode("200");//默认成功200
		urlModel.setGrade(url.getGrade());
		// 只有在抓取或处理失败的时候，才会有异常code 
		if (!url.getRuntimeWatcher().isSuccess() || UrlStatus.CRAWLED_ERROR.equals(url.getUrlStatus())
				|| UrlStatus.DELETED_ERROR.equals(url.getUrlStatus())) {
			urlModel.setLatelyErrorCode(String.valueOf(url.getRuntimeWatcher().getException().getCode().getValue()));
		}

		return urlModel;
	}

	public ThriftConnectionProvider getThriftConnectionProvider() {
		return thriftConnectionProvider;
	}

	public void setThriftConnectionProvider(ThriftConnectionProvider thriftConnectionProvider) {
		this.thriftConnectionProvider = thriftConnectionProvider;
	}

	public XmlParseService getXmlParseService() {
		return xmlParseService;
	}

	public void setXmlParseService(XmlParseService xmlParseService) {
		this.xmlParseService = xmlParseService;
	}

	@Override
	public void upsertDocuments(List<Url> urls) {
		if (CollectionUtils.isEmpty(urls)) {
			return;
		}

		List<UrlModel> urlModels = new ArrayList<UrlModel>(urls.size());
		for (Url url : urls) {
			UrlModel urlModel = this.convert(url);
			urlModels.add(urlModel);
		}

		Client client = this.thriftConnectionProvider.getObject();
		try {
			client.upsertUrls(urlModels);
		} catch (TException e) {
			this.thriftConnectionProvider.invalidateObject(client);
			client = null;
			logger.error("Error occurred while save url to controller via thrift!{}", e);
		} finally {
			this.thriftConnectionProvider.returnObject(client);// 及时释放连接对象
		}
	}
}