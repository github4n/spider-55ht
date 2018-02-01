package com.haitao55.spider.controller.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.haitao55.spider.common.dao.HeartbeatDao;
import com.haitao55.spider.common.dao.ItemDAO;
import com.haitao55.spider.common.dao.ProxyDAO;
import com.haitao55.spider.common.dao.StatisticsDao;
import com.haitao55.spider.common.dao.TaskDAO;
import com.haitao55.spider.common.dao.UrlDAO;
import com.haitao55.spider.common.dos.HeartbeatDO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.dos.ProxyDO;
import com.haitao55.spider.common.dos.StatisticsDO;
import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.dos.UrlDO;
import com.haitao55.spider.common.entity.TaskStatus;
import com.haitao55.spider.common.entity.UrlsStatus;
import com.haitao55.spider.common.service.MonitorService;
import com.haitao55.spider.common.thrift.HeartbeatModel;
import com.haitao55.spider.common.thrift.ItemModel;
import com.haitao55.spider.common.thrift.ProxyModel;
import com.haitao55.spider.common.thrift.TaskModel;
import com.haitao55.spider.common.thrift.ThriftService;
import com.haitao55.spider.common.thrift.UrlModel;
import com.haitao55.spider.common.thrift.UrlStatusModel;
import com.haitao55.spider.common.thrift.UrlTypeModel;
import com.haitao55.spider.controller.cache.HotTasksCache;
import com.haitao55.spider.controller.cache.StatisticsCache;
import com.haitao55.spider.controller.cache.TaskRatioCriteria;
import com.haitao55.spider.controller.cache.TaskRatioCriteriaCache;
import com.haitao55.spider.controller.utils.Constants;

/**
 * 
 * 功能：Controller模块提供的thrift接口实现,通过Thrift server向外界提供功能
 * 
 * @author Arthur.Liu
 * @time 2016年7月28日 上午11:11:23
 * @version 1.0
 */
public class ControllerService implements ThriftService.Iface {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private TaskDAO taskDAO = null;

	private ProxyDAO proxyDAO;

	@Autowired
	private StatisticsDao statisticsDao;

	@Autowired
	private HeartbeatDao heartbeatDao;

	private UrlDAO urlDAO;

	private ItemDAO itemDAO;

	private MonitorService monitorService;

	public TaskDAO getTaskDAO() {
		return taskDAO;
	}

	public void setTaskDAO(TaskDAO taskDAO) {
		this.taskDAO = taskDAO;
	}

	public ProxyDAO getProxyDAO() {
		return proxyDAO;
	}

	public void setProxyDAO(ProxyDAO proxyDAO) {
		this.proxyDAO = proxyDAO;
	}

	public UrlDAO getUrlDAO() {
		return urlDAO;
	}

	public void setUrlDAO(UrlDAO urlDAO) {
		this.urlDAO = urlDAO;
	}

	public ItemDAO getItemDAO() {
		return itemDAO;
	}

	public void setItemDAO(ItemDAO itemDAO) {
		this.itemDAO = itemDAO;
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	public void clearHeartbeatDbTable() {
		// 清空心跳数据库表
		this.heartbeatDao.clear();
	}

	@Override
	public boolean heartbeat(HeartbeatModel heartbeatModel) throws TException {
		logger.info(Constants.CONTROLLER_LOGGER_MODULE_HEARTBEAT + heartbeatModel.toString());

		HeartbeatDO heartbeatDO = new HeartbeatDO();
		heartbeatDO.setIp(heartbeatModel.getIp());
		heartbeatDO.setProcId(heartbeatModel.getProcId());
		heartbeatDO.setThreadCount(heartbeatModel.getThreadCount());
		heartbeatDO.setTime(heartbeatModel.getTime());

		this.heartbeatDao.upsertHeartbeat(heartbeatDO);

		return true;
	}

	@Override
	public Map<Long, TaskModel> fetchHotTasks() throws TException {
		logger.info("thrift 服务端 fetchHotTasks 调用");
		// return HotTasksCache.getInstance().deepClone();
		return HotTasksCache.getInstance();
	}

	@Override
	public TaskModel fetchTask(long taskId) throws TException {
		logger.info("thrift 服务端 fetchTask 调用");
		// return HotTasksCache.getInstance().deepClone().get(taskId);
		return HotTasksCache.getInstance().get(taskId);
	}

	@Override
	@SuppressWarnings("serial")
	public boolean noticeTaskConfigError(long taskId) throws TException {
		logger.info("thrift 服务端 noticeTaskConfigError 调用");
		this.taskDAO.updateStatus(new HashMap<String, String>() {
			{
				put(TaskDAO.UPDATE_STATUS_PARAMS_TASKID, String.valueOf(taskId));
				put(TaskDAO.UPDATE_STATUS_PARAMS_NEWSTATUS, TaskStatus.ERROR.getValue());
			}
		});
		return true;
	}

	@Override
	public List<UrlModel> fetchUrls(int limit) throws TException {
		logger.info("thrift 服务端 fetchUrls 调用");
		List<UrlModel> result = new ArrayList<UrlModel>();

		Map<Long, List<UrlDO>> urlDOMap = this.getUrls(limit);
		for (Entry<Long, List<UrlDO>> entry : urlDOMap.entrySet()) {
			Long taskId = entry.getKey();
			List<UrlDO> urlDOs = entry.getValue();

			for (UrlDO urlDO : urlDOs) {
				try {
					UrlModel model = this.convertUrlDO2UrlModel4FetchUrls(urlDO, taskId);
					result.add(model);
				} catch (NullPointerException e) {
					logger.error("urlDO transform urlmodel error",e);
				}
			}
		}
		logger.info("thrift 服务端 fetchUrls" + result.size());
		return result;
	}

	/**
	 * <p>
	 * 这个实体对象转换方法,并不通用；
	 * </p>
	 * <p>
	 * 不仅做简单的实体对象转换,还包含一些逻辑功能,所以方法名中用了"4FetchUrls"做后缀
	 * </p>
	 * 
	 * @param urlDO
	 *            待转换的UrlDO对象
	 * @param taskId
	 *            url所属任务的任务ID主键
	 * @return
	 */
	private UrlModel convertUrlDO2UrlModel4FetchUrls(UrlDO urlDO, Long taskId) {
		UrlModel urlModel = new UrlModel();
		urlModel.setId(urlDO.getId());
		urlModel.setValue(urlDO.getValue());
		urlModel.setParentUrl(urlDO.getParentUrl());
		urlModel.setGrade(urlDO.getGrade());
		urlModel.setType(UrlTypeModel.valueOf(urlDO.getType()));
		urlModel.setTaskId(taskId);
		urlModel.setLastCrawledTime(urlDO.getLastCrawledTime());
		urlModel.setLastCrawledIP(urlDO.getLastCrawledIp());
		urlModel.setLatelyFailedCount(urlDO.getLatelyFailedCount());
		urlModel.setLatelyErrorCode(urlDO.getLatelyErrorCode());

		HotTasksCache cache = HotTasksCache.getInstance();
		TaskModel taskModel = cache.get(taskId);
		String taskStatus = taskModel.getStatus();
		if (TaskStatus.ACTIVE.getValue().equals(taskStatus)) {
			urlModel.setUrlStatusModel(UrlStatusModel.CRAWLING);
		} else if (TaskStatus.DISCARDING.getValue().equals(taskStatus)) {
			urlModel.setUrlStatusModel(UrlStatusModel.DELETING);
		}

		return urlModel;
	}

	@SuppressWarnings("serial")
	private Map<Long, List<UrlDO>> getUrls(int limit) {
		Map<Long, List<UrlDO>> result = new HashMap<Long, List<UrlDO>>();

		Map<Long, Integer> taskLimits = this.allocateTaskLimits(limit);
		for (Entry<Long, Integer> entry : taskLimits.entrySet()) {
			Long taskId = entry.getKey();
			int taskLimit = entry.getValue();

			if (taskLimit > 0) {// 只有在限制数大于0时，才到底层去取
				List<UrlDO> urlList = this.queryUrls(taskId, taskLimit);
				if (urlList != null && urlList.size() > 0) {
					result.put(taskId, new ArrayList<UrlDO>() {
						{
							addAll(urlList);
						}
					});
				} else {
					logger.warn("");
				}
			}
		}

		return result;
	}

	private Map<Long, Integer> allocateTaskLimits(int totalLimit) {
		Map<Long, Integer> result = new HashMap<Long, Integer>();

		int tasksWeight = this.summateTasksWeight();

		TaskRatioCriteriaCache ratioCache = TaskRatioCriteriaCache.getInstance();
		for (Entry<Long, TaskRatioCriteria> entry : ratioCache.entrySet()) {
			Long taskId = entry.getKey();
			TaskRatioCriteria ratio = entry.getValue();

			// 排除“阶段性满”的配置
			if (ratio.isStagedFull()) {
				logger.warn("".toString());
				continue;
			}

			// 这里是最终的结果计算:
			int taskLimit = (int) (((ratio.getWeight() * 1.0) / tasksWeight) * totalLimit);
			if (taskLimit > ratio.getRemainder()) {
				taskLimit = ratio.getRemainder();
			}

			ratio.accumulateCurrentValue(taskLimit);

			result.put(taskId, taskLimit);
		}

		return result;
	}

	private int summateTasksWeight() {
		int tasksWeight = 0;

		TaskRatioCriteriaCache ratioCache = TaskRatioCriteriaCache.getInstance();
		for (TaskRatioCriteria ratio : ratioCache.values()) {
			tasksWeight += ratio.getWeight();
		}

		return tasksWeight;
	}

	private List<UrlDO> queryUrls(Long taskId, int limit) {
		TaskDO task = this.taskDAO.getTaskById(String.valueOf(taskId));
		List<UrlDO> result = this.urlDAO.queryUrlsByStatusWithUpdateStatus(taskId, task.getUpdateOnly(),
				UrlsStatus.RELIVE.getValue(), UrlsStatus.PENDING.getValue(), limit);
		if (CollectionUtils.isEmpty(result)) {
			if (!TaskStatus.VOID.getValue().equals(task.getStatus())) {
				// 根据任务 取出mongo 数据时,RELIVE状态数据不存在,则将该任务置为FINISH状态
				task.setStatus(TaskStatus.FINISH.getValue());
				task.setUpdateTime(System.currentTimeMillis());
				this.taskDAO.update(task);
			}
			// 统计表 end_time 赋值 最近task记录
			StatisticsDO statisticsDO = new StatisticsDO();
			statisticsDO.setTaskId(taskId);
			statisticsDO.setEndTime(System.currentTimeMillis());
			this.statisticsDao.update(statisticsDO);
		}
		return result;
	}

	@Override
	public boolean upsertItems(Map<Long, List<ItemModel>> itemMap) throws TException {
		logger.info("thrift 服务端 upsertItems 调用");
		for (Entry<Long, List<ItemModel>> entry : itemMap.entrySet()) {
			List<ItemDO> itemDOList = new ArrayList<ItemDO>();

			Long taskId = entry.getKey();
			List<ItemModel> itemModelList = entry.getValue();
			for (ItemModel itemModel : itemModelList) {
				ItemDO itemDO = new ItemDO();
				itemDO.setId(itemModel.getId());
				itemDO.setValue(itemModel.getValue());

				itemDOList.add(itemDO);
			}

			this.itemDAO.upsertItems(taskId, itemDOList);
		}

		return true;
	}

	/**
	 * <p>
	 * Urls文档处理::新增或修改urls文档；
	 * </p>
	 */
	@Override
	public boolean upsertUrls(List<UrlModel> urls) throws TException {
		logger.info("thrift 服务端 upsertUrls 调用");
		Map<Long, List<UrlDO>> map = new HashMap<Long, List<UrlDO>>();

		for (UrlModel urlModel : urls) {
			UrlDO urlDO = new UrlDO();
			urlDO.setId(urlModel.getId());
			urlDO.setValue(urlModel.getValue());
			urlDO.setParentUrl(urlModel.getParentUrl());
			urlDO.setGrade(urlModel.getGrade());
			urlDO.setType(urlModel.getType().toString());
			urlDO.setCreateTime(urlModel.getCreateTime());
			urlDO.setLastCrawledIp(urlModel.getLastCrawledIP());
			urlDO.setLastCrawledTime(urlModel.getLastCrawledTime());
			urlDO.setLatelyErrorCode(urlModel.getLatelyErrorCode());
			urlDO.setLatelyFailedCount(urlModel.getLatelyFailedCount());
			if (UrlStatusModel.NEWCOME.equals(urlModel.getUrlStatusModel())) {
				urlDO.setStatus(UrlsStatus.INIT.getValue());
			} else {
				urlDO.setStatus(urlModel.getUrlStatusModel().toString());
			}
			Long taskId = urlModel.getTaskId();
			if (!map.containsKey(taskId)) {
				map.put(taskId, new ArrayList<UrlDO>());
			}
			map.get(taskId).add(urlDO);
		}

		for (Entry<Long, List<UrlDO>> entry : map.entrySet()) {
			Long taskId = entry.getKey();
			List<UrlDO> urlDOList = entry.getValue();
			this.urlDAO.upsertUrls(taskId, urlDOList);
		}

		return true;
	}

	/**
	 * <p>
	 * Urls文档处理::修改或删除urls文档；
	 * </p>
	 * <p>
	 * 经过上层Crawler执行"抓取或删除"处理之后,返回来经过这里更新底层urls数据库
	 * </p>
	 */
	@Override
	public boolean updelUrls(List<UrlModel> urls) throws TException {
		logger.info("thrift 服务端 updelUrls 调用");
		Map<Long, List<String>> deleteMap = new HashMap<Long, List<String>>();
		Map<Long, List<UrlDO>> updateMap = new HashMap<Long, List<UrlDO>>();
		for (UrlModel model : urls) {
			Long taskId = model.getTaskId();
			UrlStatusModel status = model.getUrlStatusModel();

			if (UrlStatusModel.DELETED_OK.equals(status)) {// 如果上层Crawler执行删除成功
				if (!deleteMap.containsKey(taskId)) {
					deleteMap.put(taskId, new ArrayList<String>());
				}
				deleteMap.get(taskId).add(model.getId());
			} else {
				UrlDO urlDO = new UrlDO();

				urlDO.setId(model.getId());
				urlDO.setLastCrawledTime(model.getLastCrawledTime());
				urlDO.setLastCrawledIp(model.getLastCrawledIP());
				urlDO.setLatelyFailedCount(model.getLatelyFailedCount());
				urlDO.setLatelyErrorCode(model.getLatelyErrorCode());
				urlDO.setGrade(model.getGrade());
				urlDO.setType(model.getType().toString());
				urlDO.setValue(model.getValue().toString());
				urlDO.setParentUrl(model.getParentUrl().toString());
				if (UrlStatusModel.CRAWLED_OK.equals(status)) {
					urlDO.setStatus(UrlsStatus.SUCCESS.getValue());
				} else if (UrlStatusModel.CRAWLED_ERROR.equals(status) || UrlStatusModel.DELETED_ERROR.equals(status)) {
					urlDO.setStatus(UrlsStatus.ERROR.getValue());
				}

				if (!updateMap.containsKey(taskId)) {
					updateMap.put(taskId, new ArrayList<UrlDO>());
				}
				updateMap.get(taskId).add(urlDO);
			}
		}

		for (Entry<Long, List<String>> entry : deleteMap.entrySet()) {
			Long taskId = entry.getKey();
			List<String> urlIdList = entry.getValue();
			this.urlDAO.deleteUrls(taskId, urlIdList);
		}

		for (Entry<Long, List<UrlDO>> entry : updateMap.entrySet()) {
			Long taskId = entry.getKey();
			List<UrlDO> urlDOList = entry.getValue();
			this.urlDAO.updateUrls(taskId, urlDOList);
		}

		this.reloadStatistics(urls);
		return true;
	}

	/**
	 * 任务统计
	 * 
	 * @param urls
	 */
	private void reloadStatistics(List<UrlModel> urls) {
		StatisticsCache cache = StatisticsCache.getInstance();
		for (UrlModel model : urls) {
			Long taskId = model.getTaskId();
			UrlStatusModel status = model.getUrlStatusModel();
			StatisticsDO statisticsDO = cache.get(taskId);
			if (!cache.containsKey(taskId)) {
				// 初始化
				statisticsDO = new StatisticsDO();
				cache.put(taskId, statisticsDO);
			}
			int handleCount = null == cache.get(taskId).getHandleCount() ? 0
					: cache.get(taskId).getHandleCount().intValue();
			statisticsDO.setHandleCount(++handleCount);
			if (UrlStatusModel.CRAWLED_OK.equals(status) || UrlStatusModel.DELETED_OK.equals(status)) {// url爬取成功
				// 爬取成功计数
				int successCount = null == cache.get(taskId).getSuccessCount() ? 0
						: cache.get(taskId).getSuccessCount().intValue();
				statisticsDO.setSuccessCount(++successCount);
			} else if (UrlStatusModel.CRAWLED_ERROR.equals(status) || UrlStatusModel.DELETED_ERROR.equals(status)) {// url爬取失败
				// 爬取失败计数
				int failedCount = null == cache.get(taskId).getFailedCount() ? 0
						: cache.get(taskId).getFailedCount().intValue();
				statisticsDO.setFailedCount(++failedCount);
			}
			cache.put(taskId, statisticsDO);
		}
	}

	@Override
	public Map<Long, ProxyModel> fetchProxies() throws TException {
		logger.info("thrift 服务端 fetchProxies 调用");
		Map<Long, ProxyModel> result = new HashMap<Long, ProxyModel>();

		List<ProxyDO> proxyDOs = this.proxyDAO.getAllProxies();
		if (CollectionUtils.isEmpty(proxyDOs)) {
			return result;
		}

		for (ProxyDO proxyDO : proxyDOs) {
			ProxyModel proxyModel = this.convertProxyDO2ProxyModel(proxyDO);
			result.put(proxyModel.getId(), proxyModel);
		}

		return result;
	}

	private ProxyModel convertProxyDO2ProxyModel(ProxyDO proxyDO) {
		ProxyModel proxyModel = new ProxyModel();

		proxyModel.setId(proxyDO.getId());
		proxyModel.setRegionId(proxyDO.getRegionId());
		proxyModel.setRegionName(proxyDO.getRegionName());
		proxyModel.setIp(proxyDO.getIp());
		proxyModel.setPort(proxyDO.getPort());

		return proxyModel;
	}
}