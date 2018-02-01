package com.haitao55.spider.ui.tasks;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.haitao55.spider.common.dao.ProxyDAO;
import com.haitao55.spider.common.dos.ProxyDO;
import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.thrift.ProxyModel;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.common.cache.TaskCache;
import com.haitao55.spider.crawler.common.cache.TaskDOCache;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Rule;
import com.haitao55.spider.crawler.core.model.Rules;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.service.XmlParseService;
import com.haitao55.spider.ui.service.TaskService;
import com.haitao55.spider.ui.view.TaskView;

/**
 * 
 * 功能：UI模块范围内使用的定时器任务类
 * 
 * @author Arthur.Liu
 * @time 2016年10月31日 下午9:39:44
 * @version 1.0
 */
public class UITimedTasks {
	private static final Logger logger = LoggerFactory.getLogger(UITimedTasks.class);

	private static final String TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";

	@Autowired
	private TaskService taskService;

	@Autowired
	private XmlParseService xmlParseService;
	
	@Autowired
	private ProxyDAO proxyDAO;

	/**
	 * 定时从mysql数据库中加载爬虫任务配置信息
	 */
	public void reloadTasks() {
		StringBuilder logString = new StringBuilder();
		logString.append("UITimedTask.reloadTasks() start::");
		logString.append((new SimpleDateFormat(TIME_FORMAT_STRING)).format(System.currentTimeMillis()));
		logString.append(System.getProperty("line.separator"));

		List<TaskView> taskViewList = this.taskService.queryAllTasks(1, 2000);

		if (CollectionUtils.isEmpty(taskViewList)) {
			logger.warn("UITimedTasks.reloadTask() got none tasks!");
			return;
		} else {
			logger.info("UITimedTasks.reloadTask() got {} tasks!", taskViewList.size());
		}

		Map<Long, TaskView> taskViewMap = new HashMap<Long, TaskView>();
		for (TaskView taskView : taskViewList) {
			taskViewMap.put(taskView.getId(), taskView);
		}

		TaskCache taskCache = TaskCache.getInstance();
		TaskDOCache taskDoCache = TaskDOCache.getInstance();

		for (Entry<Long, Task> entry : taskCache.entrySet()) {
			if (!taskViewMap.containsKey(entry.getKey())) {
				taskCache.remove(entry.getKey());
				taskDoCache.remove(entry.getKey());
			}
		}

		for (TaskView taskView : taskViewList) {
			Task task = new Task();
			task.setTaskId(taskView.getId());
			task.setTaskName(taskView.getName());
			task.setSiteRegion(taskView.getSiteRegion());
			task.setProxyRegionId(taskView.getProxyRegionId());
			try {
				Rules rulesCurrennt = new Rules();

				Rules rules = this.xmlParseService.parse(taskView.getConfig());
				for (Rule rule : rules) {
					if (StringUtils.isNotBlank(rule.getRegex())) {
						rulesCurrennt.add(rule);
					}
				}

				task.setRules(rulesCurrennt);
			} catch (Exception e) {
				logger.error("Error while parsing task-rules in UITimedTasks.reloadTasks()", e);
			}

			taskCache.put(task.getTaskId(), task);
			
			//taskDoCache　封装
			TaskDO taskDo = new TaskDO();
			taskDo.setId(taskView.getId());
			taskDo.setInitUrl(taskView.getInitUrl());
			
			taskDoCache.put(taskDo.getId(), taskDo);
		}

		logString.append("UITimedTask.reloadTasks() end:");
		logString.append((new SimpleDateFormat(TIME_FORMAT_STRING)).format(System.currentTimeMillis()));
		logString.append(System.getProperty("line.separator"));
		logString.append("taskCache.size():").append(taskCache.size());

		logger.info(logString.toString());
	}
	
	
	/**
	 * mysql读取代理ip
	 */
	public void reloadProxy(){
		Map<Long, ProxyModel> result = new HashMap<Long, ProxyModel>();

		List<ProxyDO> proxyDOs = this.proxyDAO.getAllProxies();
		if (CollectionUtils.isEmpty(proxyDOs)) {
			return ;
		}

		for (ProxyDO proxyDO : proxyDOs) {
			ProxyModel proxyModel = this.convertProxyDO2ProxyModel(proxyDO);
			result.put(proxyModel.getId(), proxyModel);
		}
	
		// 转换到本地缓存中,以待以后系统执行抓取时使用
		Map<String, Queue<Proxy>> temp = new HashMap<String, Queue<Proxy>>();
		Iterator<ProxyModel> it = result.values().iterator();
		while (it.hasNext()) {
			ProxyModel model = it.next();
			Proxy proxy = this.convertProxyModel2Proxy(model);

			if (!temp.containsKey(proxy.getRegionId())) {
				temp.put(proxy.getRegionId(), new ConcurrentLinkedQueue<Proxy>());
			}
			if(proxy != null){
				temp.get(proxy.getRegionId()).offer(proxy);
			}
		}

		ProxyCache.getInstance().clear();
		ProxyCache.getInstance().putAll(temp);
	}
	
	private Proxy convertProxyModel2Proxy(ProxyModel model) {
		Proxy proxyIp = new Proxy();

		proxyIp.setId(model.getId());
		proxyIp.setRegionId(model.getRegionId());
		proxyIp.setRegionName(model.getRegionName());
		proxyIp.setIp(model.getIp());
		proxyIp.setPort(model.getPort());

		return proxyIp;
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