package com.haitao55.spider.realtime.controller.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.dao.ProxyDAO;
import com.haitao55.spider.common.dao.UrlDAO;
import com.haitao55.spider.common.dos.ProxyDO;
import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.dos.UrlDO;
import com.haitao55.spider.common.entity.UrlsStatus;
import com.haitao55.spider.common.service.impl.RedisService;
import com.haitao55.spider.common.thrift.ProxyModel;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.common.cache.TaskCache;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Rule;
import com.haitao55.spider.crawler.core.model.Rules;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.service.XmlParseService;
import com.haitao55.spider.realtime.service.TaskService;

/**
 * 
 * 功能：在RealtimeCrawler模块范围内使用的定时任务类
 * 
 * @author Arthur.Liu
 * @time 2016年9月13日 下午8:12:46
 * @version 1.0
 */
public class RealtimeCrawlerTimedTask {
	private static final Logger logger = LoggerFactory.getLogger(RealtimeCrawlerTimedTask.class);

	private static final String TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";

	private static final String URL_TO_MONGO_REDIS_KEY = "url_to_mongo";
	private static final String HOST_SEPARATE = ",";
	
	private static final String ITEM = "ITEM";
	
	private static final int GRADE = 33;

	private UrlDAO aliUrlDao;
	
	private UrlDAO googleUrlDao;
	
	private RedisService redisService;

	private TaskService taskService;

	private XmlParseService xmlParseService;

	private ProxyDAO proxyDAO;

	/**
	 * 定时从mysql数据库中加载爬虫任务配置信息
	 */
	public void reloadTasks() {
		StringBuilder logString = new StringBuilder();
		logString.append("RealtimeCrawlerTimedTask.reloadTasks() start::");
		logString.append((new SimpleDateFormat(TIME_FORMAT_STRING)).format(System.currentTimeMillis()));
		logString.append(System.getProperty("line.separator"));

		Map<Long, TaskDO> dbTasks = null;
		try{// 有时候spring quartz定时器会停止，这里捕捉一下异常，定时器停止据查资料有可能是这里查询数据库异常导致的
			dbTasks = this.taskService.getAllTasks();
		}catch(Exception e){
			logger.error("reloadTasks() error while getAllTasks(), memoryTasks now is:{}", TaskCache.getInstance());
			return;
		}

		if (MapUtils.isEmpty(dbTasks)) {
			logger.warn("reloadTasks() got no task from lower layer!");
			return;
		}

		TaskCache memoryTasks = TaskCache.getInstance();
		for (Entry<Long, Task> entry : memoryTasks.entrySet()) {
			if (!dbTasks.containsKey(entry.getKey())) {
				memoryTasks.remove(entry.getKey());
			}
		}

		for (TaskDO taskDO : dbTasks.values()) {

			Task task = new Task();
			task.setTaskId(taskDO.getId());
			task.setTaskName(taskDO.getName());
			task.setSiteRegion(taskDO.getSiteRegion());
			task.setProxyRegionId(taskDO.getProxyRegionId());
			try {
				Rules rules = this.xmlParseService.parse(taskDO.getConfig());

				Rules rulesCurrennt = new Rules();
				for (Rule rule : rules) {
					if (StringUtils.isNotBlank(rule.getRegex())) {
						rulesCurrennt.add(rule);
					}
				}

				task.setRules(rulesCurrennt);
			} catch (Exception e) {
				logger.error("Error occurred while parsing task-content!e:{}", e);
				continue;
			}

			memoryTasks.put(task.getTaskId(), task);
		}

		logString.append("RealtimeCrawlerTimedTask.reloadTasks() end:");
		logString.append((new SimpleDateFormat(TIME_FORMAT_STRING)).format(System.currentTimeMillis()));
		logString.append(System.getProperty("line.separator"));
		logString.append("memoryTasks.size():").append(memoryTasks.size());

		logger.info(logString.toString());
	}

	/**
	 * mysql读取代理ip
	 */
	public void reloadProxy() {
		Map<Long, ProxyModel> result = new HashMap<Long, ProxyModel>();

		List<ProxyDO> proxyDOs = this.proxyDAO.getAllProxies();
		if (CollectionUtils.isEmpty(proxyDOs)) {
			return;
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
			if (proxy != null) {
				temp.get(proxy.getRegionId()).offer(proxy);
			}
		}

		ProxyCache.getInstance().clear();
		ProxyCache.getInstance().putAll(temp);
	}

	/**
	 * 获取redis存放的url种子，插入到对应mongo 种子库里
	 */
	public void insertUrlFromRedisToMongo(){
		Map<String,Object> map = new HashMap<String,Object>();
		String empty = StringUtils.EMPTY;
		//一次从队列里取200个
		for (int i = 0; i < 1000; i++) {
			String value = redisService.lpop(URL_TO_MONGO_REDIS_KEY);
			if(StringUtils.isBlank(value)){
				break;
			}
			String taskId = StringUtils.substringBefore(value, HOST_SEPARATE);
			String url = StringUtils.substringAfter(value, HOST_SEPARATE);
			
			int grade = 0;
			UrlDO queryUrlByDocId = googleUrlDao.queryUrlByDocId(Long.parseLong(taskId), SpiderStringUtil.md5Encode(url));
			if(null==queryUrlByDocId){
				grade =GRADE;
			}else{
				grade = queryUrlByDocId.getGrade();
			}
			
			UrlDO urlDO = new UrlDO();
			urlDO.setId(SpiderStringUtil.md5Encode(url));
			urlDO.setValue(url);
			urlDO.setParentUrl(empty);
			urlDO.setGrade(grade);
			urlDO.setType(ITEM);
			urlDO.setCreateTime(System.currentTimeMillis());
			urlDO.setLastCrawledIp(empty);
			urlDO.setLastCrawledTime(System.currentTimeMillis());
			urlDO.setLatelyErrorCode(200+"");
			urlDO.setLatelyFailedCount(0);
			urlDO.setStatus(UrlsStatus.INIT.getValue());
			
			if(map.containsKey(taskId)){
				@SuppressWarnings("unchecked")
				List<UrlDO> urlList = (List<UrlDO>)map.get(taskId);
				urlList.add(urlDO);
				map.put(taskId, urlList);
			}else{
				List<UrlDO> list = new ArrayList<UrlDO>();
				list.add(urlDO);
				map.put(taskId, list);
			}
		}
		if(MapUtils.isNotEmpty(map)){
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				@SuppressWarnings("unchecked")
				List<UrlDO> urlList = (List<UrlDO>)entry.getValue();
				
				googleUrlDao.upsertUrls(Long.parseLong(key), urlList);
			}
		}
		
		
	}

	public RedisService getRedisService() {
		return redisService;
	}

	public void setRedisService(RedisService redisService) {
		this.redisService = redisService;
	}

	public ProxyDAO getProxyDAO() {
		return proxyDAO;
	}

	public void setProxyDAO(ProxyDAO proxyDAO) {
		this.proxyDAO = proxyDAO;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public XmlParseService getXmlParseService() {
		return xmlParseService;
	}

	public void setXmlParseService(XmlParseService xmlParseService) {
		this.xmlParseService = xmlParseService;
	}
	
	public UrlDAO getAliUrlDao() {
		return aliUrlDao;
	}

	public void setAliUrlDao(UrlDAO aliUrlDao) {
		this.aliUrlDao = aliUrlDao;
	}

	public UrlDAO getGoogleUrlDao() {
		return googleUrlDao;
	}

	public void setGoogleUrlDao(UrlDAO googleUrlDao) {
		this.googleUrlDao = googleUrlDao;
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