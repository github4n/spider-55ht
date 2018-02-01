package com.haitao55.spider.realtime.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haitao55.spider.common.dao.CurrentItemDAO;
import com.haitao55.spider.common.dao.ImageDAO;
import com.haitao55.spider.common.dao.UrlDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.dos.RealtimeStatisticsDO;
import com.haitao55.spider.common.entity.RTReturnCode;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.kafka.SpiderKafkaProducer;
import com.haitao55.spider.common.service.MonitorService;
import com.haitao55.spider.common.service.impl.RedisService;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.TaskCache;
import com.haitao55.spider.crawler.core.callable.base.Callable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.Rule;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlStatus;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.image.service.ImageService;
import com.haitao55.spider.realtime.async.RealtimeCrawlerCallable;
import com.haitao55.spider.realtime.async.RealtimeCrawlerThreadPoolExecutor;
import com.haitao55.spider.realtime.async.RealtimeThreadPoolExecutor;
import com.haitao55.spider.realtime.cache.IgnoreDomainCache;
import com.haitao55.spider.realtime.common.util.CrawlerResultUtil;
import com.haitao55.spider.realtime.common.util.OutPutDataUtil;
import com.haitao55.spider.realtime.common.util.RealtimeHttpUtil;
import com.haitao55.spider.realtime.controller.google.GoogleRealtimeCrawler;
import com.haitao55.spider.realtime.controller.reloadDomain.ReloadDomainUtil;
import com.haitao55.spider.realtime.controller.service.amazonapi.AmazonApiRealtimeService;
import com.haitao55.spider.realtime.service.OutputDataDealService;
import com.haitao55.spider.realtime.service.RealtimeStatisticsService;

/**
 * 
 * 功能：实时核价功能的爬虫,执行实时抓取功能；
 * <p>
 * 每家目标网站的实时抓取,在各个方法中执行；
 * </p>
 * 
 * @author Arthur.Liu
 * @time 2016年9月7日 下午5:08:46
 * @version 1.0
 */
@Controller
@RequestMapping("/realtime-crawler")
public class RealtimeCrawlerController {
	private static final Logger logger = LoggerFactory.getLogger(RealtimeCrawlerController.class);

	private static final String URL_TO_MONGO_REDIS_KEY = "url_to_mongo";
	private static final String HOST_SEPARATE = ",";

	// 阿里云地址
	private static final String ALI_PATH = "http://114.55.10.105/spider-55haitao-realtime/realtime-crawler/pricing.action";
	private static final String QUERY_ITEM_ADDRESS = "http://114.55.61.171/spider-55haitao-data-service/items/queryItem.action?url=";
	private static final String AMAZON_CO_JP_PRICE_REMINDER = "http://114.55.10.105:9938/pricing/get?multi=0&url=";
	

	private static final Logger LOGGER_TIME_CONSUMING = LoggerFactory.getLogger("realtime_time_consuming");
	private static final Logger RESULT_DATA_LOGGER = LoggerFactory.getLogger("result_data_logger");
	private static final Logger LOGGER_REALTIME_RECEIVED_URLS = LoggerFactory.getLogger("realtime_received_urls");
	private static final Logger LOGGER_REALTIME_OUTCOME_RESULT = LoggerFactory.getLogger("realtime_outcome_result");

	// 及时上架 logger收集
	private static final Logger IMMEDIATE_URL_LOGGER = LoggerFactory.getLogger("immediate_url_logger");

	private static final long CRAWLING_WAIT_TIME = 12 * 1000;// 在线抓取最多等待时间
	private static final TimeUnit CRAWLING_WAIT_UNIT = TimeUnit.MILLISECONDS;// 在线抓取最多等待时间单位
	private static final int REDIS_EXPIRED_TIME = 3600;// redis key过期时间
	
	private static final int  URL_SECOND_REALTIME_TIME= 3000; // 第二次核价时间
	private final static String SECOND_REALTIME_REQUEST_FROM_VALUE ="redis_second_realTime";

	private static final String DEFAULT_REQUEST_FROM = "se";// 核价标识，默认的核价标识为从官网直购搜索引擎产品页面过来的
	private static final String REQUEST_FROM_IMMEDIATE = "immediate";// 即时上架核价标识
	private static final String REQUEST_FROM_REALTIME = "prelease";// 预核价标识
	
	private static final String PARAMETER_NAME_URL = "url";
	private static final String PARAMETER_NAME_REQUEST_FROM = "request_from";
	private static final String PARAMETER_NAME_TIMEOUT = "timeout";
	private static final String PARAMETER_NAME_IS_SEND_MESSAGE = "is_send_message";

	private static final String REALTIME_RESULT_FROM_REDIS = "realtime_result_from_redis";

	// result from tag
	private static final String REALTIME_RESULT_FROM_MONGO_TAG = "mongo";
	private static final String REALTIME_RESULT_FROM_REDIS_TAG = "redis";
	private static final String REALTIME_RESULT_FROM_CRAWLER_TAG = "crawler";

	// result json error flag
	private static final String MESSAGE_ERROR = "error";
	private static final String DELETE_TYPE = "DELETE";
	private static final String NOT_SELECT_REQUIRED_PROPERTY = "NOT_SELECT_REQUIRED_PROPERTY";
	
	private static ExecutorService service = Executors.newFixedThreadPool(10);

	// amazon domain
	private static final String AMAZON_DOMAIN = "www.amazon.com";

	@Autowired
	private CurrentItemDAO currentItemDAOImpl;

	@Autowired
	@Resource(name = "aliUrlDao")
	private UrlDAO aliUrlDao;

	@Autowired
	@Resource(name = "googleUrlDao")
	private UrlDAO googleUrlDao;

	@Autowired
	private RedisService redisService;

	@Autowired
	private SpiderKafkaProducer producer;

	@Autowired
	private MonitorService monitorService;

	@Autowired
	private OutputDataDealService outputDataDealService;

	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageDAO imageDAO;

	@Autowired
	private RealtimeStatisticsService realtimeStatisticsService;

	@Value("#{configProperties['kafka.haitao.topic.realtime']}")
	private String topic;// kafka topic

	@Value("#{configProperties['google.realtime']}")
	private String googleRealtime;

	@Value("#{configProperties['realtime.website.whitelist']}")
	private String webSiteWhiteList;// 网站白名单

	@RequestMapping(value = "pricing", method = RequestMethod.POST)
	public @ResponseBody void pricing(HttpServletRequest request, HttpServletResponse response, Model model) {
		String url = request.getParameter(PARAMETER_NAME_URL);
		if (StringUtils.isBlank(url)) {
			url = (String) request.getAttribute(PARAMETER_NAME_URL);
		}
		
		LOGGER_REALTIME_RECEIVED_URLS.info("realtime_received_original_url : {}",url);//监控所有进来核价链接urls

		String requestFrom = request.getParameter(PARAMETER_NAME_REQUEST_FROM);
		if (StringUtils.isBlank(requestFrom)) {
			requestFrom = (String) request.getAttribute(PARAMETER_NAME_REQUEST_FROM);
		}

		// 如果客户端来调用时，没有携带‘request_from’参数，则假定一个默认值（当成官网直购系统来的）
		if (StringUtils.isBlank(requestFrom)) {
			requestFrom = DEFAULT_REQUEST_FROM;
		}

		long startTime = System.currentTimeMillis();

		try {// 实质核价功能
			this.crawlerAsync(request, response, model);
			LOGGER_REALTIME_RECEIVED_URLS.info("realTime_execution_success, url : {}, result :{}",url,"success");
		} catch (Exception e) {
			logger.error("Error:realtime inside error! :::: url : {}, execption :{}", url, e.getMessage());
			LOGGER_REALTIME_RECEIVED_URLS.error("realTime_execution_exception, url : {}, execption :{}",url,e.getMessage());
			this.writeResponseInErrorCase(response, RTReturnCode.RT_INNER_ERROR, url);
		} finally {
			long endTime = System.currentTimeMillis();
			long totalTime = getTimeResult(startTime, endTime);
			LOGGER_TIME_CONSUMING.info("realtime_time_consuming : " + totalTime + " from " + requestFrom + " url :{}",
					url);
			this.monitorService.incField("realtime_time_consuming_" + totalTime + " from " + requestFrom);
		}
	}

	private void writeResponseInErrorCase(HttpServletResponse response, RTReturnCode rtReturnCode, String url) {
		CrawlerJSONResult jsonResult = new CrawlerJSONResult();

		jsonResult.setRetcode(1);
		jsonResult.setRtReturnCode(rtReturnCode.getValue());

		jsonResult.setMessage(MESSAGE_ERROR);

		RetBody retBody = new RetBody();
		retBody.setProdUrl(new ProdUrl(url));
		retBody.setStock(new Stock(0));
		jsonResult.setRetbody(retBody);

		jsonResult.setTaskId("");
		jsonResult.setDocType("");
		jsonResult.setFromTag("");

		String jsonStr = jsonResult.parseTo();

		this.writeResponse(response, jsonStr,url);
	}

	/**
	 * <p>
	 * 以异步的方式,执行商品数据的抓取；
	 * </p>
	 * <p>
	 * 如果在一定时间内在线抓取不成功,则从数据库中取最新一条返回给调用方；
	 * </p>
	 * 
	 * @param request
	 * @param response
	 */
	private void crawlerAsync(HttpServletRequest request, HttpServletResponse response, Model model) {
		String url = request.getParameter(PARAMETER_NAME_URL);
		if (StringUtils.isBlank(url)) {
			url = (String) request.getAttribute(PARAMETER_NAME_URL);
		}

		if (StringUtils.isBlank(url)) {// 客户端压根儿就没传来要核价的url参数
			this.writeResponseInErrorCase(response, RTReturnCode.RT_URL_NONE, url);
			return;
		}

		// 要核价的商品链接url，是否‘格式上合法’
		if (!StringUtils.startsWith(url, "http://") && !StringUtils.startsWith(url, "https://")
				&& !StringUtils.startsWith(url, "HTTP://") && !StringUtils.startsWith(url, "HTTPS://")) {
			this.writeResponseInErrorCase(response, RTReturnCode.RT_URL_ILLEGAL, url);
			return;
		}

		String requestFrom = request.getParameter(PARAMETER_NAME_REQUEST_FROM);
		if (StringUtils.isBlank(requestFrom)) {
			requestFrom = (String) request.getAttribute(PARAMETER_NAME_REQUEST_FROM);
		}

		if (StringUtils.isBlank(requestFrom)) {
			requestFrom = DEFAULT_REQUEST_FROM;
		}

		final String primitiveUrl = url;

		// 调用系统中的通用工具，根据一定的规则，清洗url
		try {
			LOGGER_REALTIME_RECEIVED_URLS.info("realTime_cleaning_url_before, url : {}",url);
			url = DetailUrlCleaningTool.getInstance().cleanDetailUrl(url);
			LOGGER_REALTIME_RECEIVED_URLS.info("realTime_cleaning_url_after, url : {}",url);
		} catch (Exception e) {
			LOGGER_REALTIME_RECEIVED_URLS.info("realTime_cleaning_url_exception, url : {} , exception : {}",url,e.getMessage());
		}
		
		if (StringUtils.isBlank(url) ) {// 客户端压根儿就没传来要核价的url参数
			LOGGER_REALTIME_RECEIVED_URLS.info("realTime_cleaning_url_after_blank, url : {}",url);
			this.writeResponseInErrorCase(response, RTReturnCode.RT_URL_NONE, url);
			return;
		}
		
		long crawlingWaitTimeTemp = CRAWLING_WAIT_TIME;

		if (REQUEST_FROM_IMMEDIATE.equals(requestFrom)) {
			crawlingWaitTimeTemp = 30 * 1000;
			IMMEDIATE_URL_LOGGER.info("immediate shelves url : {}", url);
		}

		if (StringUtils.containsIgnoreCase(requestFrom, REQUEST_FROM_REALTIME)) {
			logger.info("prerelease realtime url : {}", url);
		}
		
		// 对中亚海外购的核价超时时间做特殊设置
		if(StringUtils.startsWithIgnoreCase(url, "https://www.amazon.cn")){
			crawlingWaitTimeTemp = 30 * 1000;
		}

		String timeout = request.getParameter(PARAMETER_NAME_TIMEOUT);
		if (StringUtils.isBlank(timeout)) {
			timeout = (String) request.getAttribute(PARAMETER_NAME_TIMEOUT);
		}
		if (StringUtils.isNotBlank(timeout)) {
			crawlingWaitTimeTemp = Long.parseLong(timeout);
		}

		String isSendMessageTemp = request.getParameter(PARAMETER_NAME_IS_SEND_MESSAGE);
		if (StringUtils.isBlank(isSendMessageTemp)) {
			isSendMessageTemp = (String) request.getAttribute(PARAMETER_NAME_IS_SEND_MESSAGE);
		}
		final String isSendMessage = isSendMessageTemp;// 因为后面的代码需要在内部类中使用这个变量，所以需要是final的

		// 根据系统中的配置，验证当前这次核价，是该转发到google vps执行，还是留在本地aliyun vps执行
		IgnoreDomainCache instance = IgnoreDomainCache.getInstance();
		if (null != instance && instance.size() == 0) {
			reloadIgnoreDomain(request, response, model);
		}
		
		if (null != instance && instance.size() > 0) {
			boolean googleHandled = GoogleRealtimeCrawler.googleCloudTranserPackage(url, requestFrom,
					crawlingWaitTimeTemp + "", isSendMessage, request, response, model);
			if (googleHandled) {
				LOGGER_REALTIME_RECEIVED_URLS.info("url have bean executed in google url : {}",url);
				return;
			}else{
				LOGGER_REALTIME_RECEIVED_URLS.info("url have NOT bean executed in google url : {}",url);
			}
		}

		// =========================================================================//
		// ====================== 以下为‘aliyun vps’或‘google vps’本地的实际执行过程 ====//
		// =========================================================================//

		Task task = this.getTask(url);
		if (Objects.isNull(task)) {
			LOGGER_REALTIME_RECEIVED_URLS.info("realTime_not_support_url, url : {}",url);
			this.writeResponseInErrorCase(response, RTReturnCode.RT_WEBSITE_NOT_SUPPORT, url);
			return;
		}

		RealtimeStatisticsDO realtimeStatisticsDO = new RealtimeStatisticsDO();
		realtimeStatisticsDO.setTaskId(task.getTaskId());
		realtimeStatisticsDO.setTaskName(task.getTaskName());

		// 这个变量，保存着将来要写入response中的数据内容
		String jsonResult = "";

		long startTime = System.currentTimeMillis();
		Future<CrawlerJSONResult> victFuture = null;
		if(StringUtils.containsIgnoreCase(url, "www.victoriassecret.com")){//xusongsong
			victFuture = service.submit(new VictoriassecretProcesser(currentItemDAOImpl,url,task.getTaskId()));
		}

		// 从redis缓存中获取，看看当前要核价的商品链接在redis缓存中是否有对应的缓存数据
		if(!SECOND_REALTIME_REQUEST_FROM_VALUE.equals(requestFrom)){
			jsonResult = redisService.get(SpiderStringUtil.md5Encode(url));
		}
		if (StringUtils.isNotBlank(jsonResult)) {
			RESULT_DATA_LOGGER.info("result json from redis  url : {} ,json :{}", url, jsonResult);
			this.monitorService.incField(REALTIME_RESULT_FROM_REDIS);

			jsonResult = CrawlerResultUtil.result_from_package(jsonResult, REALTIME_RESULT_FROM_REDIS_TAG);
			jsonResult = CrawlerResultUtil.result_from_package(jsonResult, RTReturnCode.RT_SUCCESS);// 从redis中获取到数据，一定是有效（success/成功）的
			this.writeResponse(response, jsonResult,url);

			realtimeStatisticsDO.setRedis(1);
			if (!StringUtils.equalsIgnoreCase(requestFrom, REQUEST_FROM_REALTIME)) {
				realtime_statistics_package(realtimeStatisticsDO);
			}

			long endTime = System.currentTimeMillis();
			LOGGER_TIME_CONSUMING.info(
					"realtime_time_consuming : " + getTimeResult(startTime, endTime) + " from redis" + " url :{}", url);
			
			/**
			 * 计算url md5再redis存活时间，进行url第二次核价，但要再设定时间规则范围
			 */
			Long overplusRedisTime =  redisService.ttl(SpiderStringUtil.md5Encode(url));//剩余时间
			LOGGER_REALTIME_RECEIVED_URLS.info("calculation url redis overPlus time , url : {},overplusRedisTime : {}",primitiveUrl,overplusRedisTime);
			
			if(overplusRedisTime < URL_SECOND_REALTIME_TIME){ //小于，后台发送第二次核价请求
				RealtimeHttpUtil.realTimeCloudTransfer(primitiveUrl,SECOND_REALTIME_REQUEST_FROM_VALUE, CRAWLING_WAIT_TIME+"", isSendMessage,googleRealtime);
			}
			return;
		}

		// 检查白名单是否存在：：这里的‘白名单’的概念，是指，对一些特殊的网站（核价无意义（铁定超时）），不进行实时核价，而是直接从历史商品数据库中取数据
		boolean isWebSiteExist = checkWebSiteWhiteListExist(url);
		if (isWebSiteExist) {// 存在
			
			//amazon.co.jp use this code
			if(StringUtils.containsIgnoreCase(url, "amazon.co.jp")  && (StringUtils.equalsIgnoreCase(requestFrom , "price_r_r_single") || StringUtils.equalsIgnoreCase(requestFrom , "price_r_r_task"))){
			    try {
			    	String reqUrl = AMAZON_CO_JP_PRICE_REMINDER + url;
			    	String json = Executor.newInstance().execute(Request.Get(reqUrl)).returnContent().asString();
			    	logger.info("amazon.co.jp price reminder  url [{}] , json >>{}<<", url,json);
			    	this.writeResponse(response, json,url);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("error occured while amazon.co.jp price reminding  url [{}] ", url);
				}
			    return;
			}
			
			mongo_urls_insert(task, url);// 将进行核价的商品链接url加入mongodb种子数据库
			logger.info("checkWebSite whiteList :{}, url :{}", webSiteWhiteList, url);
			jsonResult = callQueryItem(url, task);// 调用queryItem接口

			realtimeStatisticsDO.setMongo(1);
			realtimeStatistics(requestFrom, realtimeStatisticsDO);// 核价统计

			jsonResult = CrawlerResultUtil.result_from_package(jsonResult, REALTIME_RESULT_FROM_MONGO_TAG);

			if (StringUtils.isBlank(jsonResult)) {
				this.writeResponseInErrorCase(response, RTReturnCode.RT_CRAWLING_TIMEOUT, url);
				return;
			}

			this.writeResponse(response, jsonResult,url);
			return;
		}

		startTime = System.currentTimeMillis();
		List<Callable> calls = this.getCalls(url);
		long endTime = System.currentTimeMillis();
		if (CollectionUtils.isEmpty(calls)) {
			LOGGER_TIME_CONSUMING.info(
					"realtime_time_consuming : " + getTimeResult(startTime, endTime) + " from callsNull" + " url :{}",
					url);
			LOGGER_REALTIME_RECEIVED_URLS.info("realTime_not_support_url_no_calls, url : {}",url);

			this.writeResponseInErrorCase(response, RTReturnCode.RT_WEBSITE_NOT_SUPPORT, url);
			return;
		}

		startTime = System.currentTimeMillis();
		Url urlInstance = this.createUrl(url, task);
		endTime = System.currentTimeMillis();
		if (Objects.isNull(urlInstance)) {
			LOGGER_TIME_CONSUMING.info("realtime_time_consuming : " + getTimeResult(startTime, endTime)
					+ " from urlInstanceNull" + " url :{}", url);
			this.writeResponseInErrorCase(response, RTReturnCode.RT_INNER_ERROR, url);
			return;
		}

		startTime = System.currentTimeMillis();
		Context context = createContext(urlInstance);
		endTime = System.currentTimeMillis();
		if (Objects.isNull(context)) {
			LOGGER_TIME_CONSUMING.info(
					"realtime_time_consuming : " + getTimeResult(startTime, endTime) + " from contextNull" + " url :{}",
					url);
			this.writeResponseInErrorCase(response, RTReturnCode.RT_INNER_ERROR, url);
			return;
		}

		/** 针对亚马逊采用api方式进行核价，即是对amazon网站核价的特殊处理 */
		if (StringUtils.contains(url, AMAZON_DOMAIN)) {
			AmazonApiRealtimeService.realtime_servcie(task, url, crawlingWaitTimeTemp, isSendMessage, request, response,
					model, realtimeStatisticsDO, context, imageService, imageDAO, outputDataDealService, redisService,
					currentItemDAOImpl, producer, topic, monitorService);
			realtime_statistics_package(realtimeStatisticsDO);
			return;
		}
		/** 针对亚马逊采用api方式进行核价，即是对amazon网站核价的特殊处理--结束 */

		mongo_urls_insert(task, url);// 将进行核价的商品链接url加入mongodb种子数据库

		RealtimeThreadPoolExecutor postExecutorService = RealtimeThreadPoolExecutor.getInstance();
		ExecutorService executorService = RealtimeCrawlerThreadPoolExecutor.getInstance();
		RealtimeCrawlerCallable callable = new RealtimeCrawlerCallable(calls, context);
		Future<OutputObject> future = executorService.submit(callable);

		long time6 = System.currentTimeMillis();
		OutputObject oo = null;
		try {
			// 当前线程最多等待一定时间,如果还获取不到抓取结果,则当前线程继续执行
			// 这里的"当前线程"也就是应用程序服务器(如tomcat)为处理当前http请求而启动的处理线程；
			oo = future.get(crawlingWaitTimeTemp, CRAWLING_WAIT_UNIT);
		} catch (InterruptedException e) {
			logger.error("Error while realtime-crawling::", e);
			LOGGER_REALTIME_RECEIVED_URLS.error("crawling InterruptedException url : {},exception : {}",url,e.getMessage());
		} catch (ExecutionException e) {
			logger.error("Error while realtime-crawling::", e);
			LOGGER_REALTIME_RECEIVED_URLS.error("crawling ExecutionException url : {},exception : {}",url,e.getMessage());
		} catch (TimeoutException e) {
			LOGGER_REALTIME_RECEIVED_URLS.error("crawling TimeoutException url : {},exception : {}",url,e.getMessage());

			future.cancel(true);// 针对超时线程，终止线程执行

			if (!StringUtils.equals(requestFrom, "timeout")) {// 保证超时情况下的二次优化只会进行一次，而不会无限制执行下去
				postExecutorService.submit(new Runnable() {
					@Override
					public void run() {
						Map<String, String> params = new HashMap<String, String>();
						params.put(PARAMETER_NAME_URL, primitiveUrl);
						params.put(PARAMETER_NAME_REQUEST_FROM, "timeout");
						params.put(PARAMETER_NAME_TIMEOUT, "60000");
						params.put(PARAMETER_NAME_IS_SEND_MESSAGE, isSendMessage);
						HttpClientUtil.post(ALI_PATH, params);
					}
				});
			}

			logger.error("Error while realtime-crawling::", e);
		}catch (Exception e) {
			LOGGER_REALTIME_RECEIVED_URLS.error("crawling Exception url : {},exception : {}",url,e.getMessage());
		}

		long time7 = System.currentTimeMillis();
		LOGGER_TIME_CONSUMING
				.info("realtime_time_consuming : " + getTimeResult(time6, time7) + " from crawler" + " url :{}", url);
		
		CrawlerJSONResult victJson = null;
		if(StringUtils.containsIgnoreCase(url, "www.victoriassecret.com")){//xusongsong
			try {
				victJson = victFuture.get(30, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
		}
		
		if (Objects.nonNull(oo)) {
			RESULT_DATA_LOGGER.info("result json from crawler before valid  url : {} ,jsonResult :{}", url,
					oo.convertItem2Message());
			
			/* 根据图片src地址生成cdn地址 */
			imageService.setImageDAO(imageDAO);
			oo = OutPutDataUtil.writeOutputObjectActually(oo, outputDataDealService, imageService);
			jsonResult = oo.convertItem2Message();
			
			// 为中亚海外购核价（且只在'降价提醒'与'到货提醒'功能中）做的hack；这块代码放置在图片处理功能之后
			if (StringUtils.startsWithIgnoreCase(url, "https://www.amazon.cn")) {
				String temp = oo.convertItem2Message();
				LOGGER_REALTIME_OUTCOME_RESULT.warn("got an instance of amazon.cn, url: {}, jsonResult: {}",
						primitiveUrl, temp);
				this.writeResponse(response, temp, primitiveUrl);
				return;
			}

			boolean isItemValid = CrawlerResultUtil.isoffLine(jsonResult);
			jsonResult = CrawlerResultUtil.crawler_result(oo.convertItem2Message(), isItemValid, task, url, oo,
					producer, topic, currentItemDAOImpl, realtimeStatisticsDO);

			// 保存数据到redis缓存中，这样可以减少同一个url到官网的实际核价次数——当然，数据的实时性会有所降低
			CrawlerJSONResult buildFrom = CrawlerJSONResult.buildFrom(jsonResult);
			if(StringUtils.containsIgnoreCase(url, "www.victoriassecret.com") && victJson != null && buildFrom != null){
				if(!StringUtils.equalsIgnoreCase(victJson.getRetbody().getProdUrl().getUrl(), buildFrom.getRetbody().getProdUrl().getUrl())){
					buildFrom.getRetbody().getProdUrl().setUrl(victJson.getRetbody().getProdUrl().getUrl());
					jsonResult = buildFrom.parseTo();
				}
			}
			if (null != buildFrom) {
				if ((buildFrom.getRtReturnCode() == RTReturnCode.RT_SUCCESS.getValue()
						|| buildFrom.getRtReturnCode() == RTReturnCode.RT_STOCK_NONE.getValue())
						&& !StringUtils.endsWithIgnoreCase(buildFrom.getDocType(), DELETE_TYPE)
						&& !StringUtils.endsWithIgnoreCase(buildFrom.getDocType(), NOT_SELECT_REQUIRED_PROPERTY)) {
					redisService.set(SpiderStringUtil.md5Encode(url), jsonResult, REDIS_EXPIRED_TIME);
					Long overplusRedisTime =  redisService.ttl(SpiderStringUtil.md5Encode(url));//剩余时间
					LOGGER_REALTIME_RECEIVED_URLS.info("second calculation url redis overPlus time , url : {},overplusRedisTime : {}",primitiveUrl,overplusRedisTime);
				}
			}

			// 当执行crawler_result exception数没有增加，代表从线上爬取到正确数据，这个是附加的统计功能
			if (realtimeStatisticsDO.getException() == 0) {
				realtimeStatisticsDO.setCrawler(1);
			}

			// 设置返回结果数据的fromTag字段值
			jsonResult = CrawlerResultUtil.result_from_package(jsonResult, REALTIME_RESULT_FROM_CRAWLER_TAG);
			RESULT_DATA_LOGGER.info("result json from crawler  url : {} ,json :{}", url, jsonResult);
		} else {
			long time9 = System.currentTimeMillis();
			
			if(StringUtils.containsIgnoreCase(url, "www.victoriassecret.com") && victJson != null){
				jsonResult = victJson.parseTo();
			} else {
				// 从历史商品数据库中取数据返回；因为google云上的程序无法直接连接阿里云的mongo副本集，所以调用queryitem接口获取对应的数据
				jsonResult = callQueryItem(url, task);
			}
			

			long time10 = System.currentTimeMillis();
			LOGGER_TIME_CONSUMING.info(
					"realtime_time_consuming : " + getTimeResult(time9, time10) + " from mongo" + " url :{}", url);

			realtimeStatisticsDO.setMongo(1);

			// 设置返回结果数据的fromTag字段值
			jsonResult = CrawlerResultUtil.result_from_package(jsonResult, REALTIME_RESULT_FROM_MONGO_TAG);
			RESULT_DATA_LOGGER.info("result json from mongo  url : {} ,json :{}", url, jsonResult);
		}

		if (StringUtils.isBlank(jsonResult)) {
			this.writeResponseInErrorCase(response, RTReturnCode.RT_ITEM_OFFLINE, url);
			return;
		}

		this.writeResponse(response, jsonResult,url);

		realtimeStatistics(requestFrom, realtimeStatisticsDO);
	}

	/**
	 * 核价信息统计
	 * 
	 * @param requestFrom
	 * @param realtimeStatisticsDO
	 */
	private void realtimeStatistics(String requestFrom, RealtimeStatisticsDO realtimeStatisticsDO) {
		if (!StringUtils.equalsIgnoreCase(requestFrom, REQUEST_FROM_REALTIME)) {
			realtime_statistics_package(realtimeStatisticsDO);
		}
	}

	/**
	 * 私有方法，进行url插入到对应mongo种子库
	 * 
	 * @param task
	 * @param url
	 */
	private void mongo_urls_insert(Task task, String url) {
		redisService.lpush(URL_TO_MONGO_REDIS_KEY, task.getTaskId() + HOST_SEPARATE + url);
	}

	/**
	 * 实时核价统计信息
	 * 
	 * @param realtimeStatisticsDO
	 */
	private void realtime_statistics_package(RealtimeStatisticsDO realtimeStatisticsDO) {
		try {
			realtimeStatisticsService.saveOrUpdate(realtimeStatisticsDO);
		} catch (Exception e) {
			logger.error("realtime_statistics 表　操作异常", e);
		}
	}

	@RequestMapping(value = "reloadIgnoreDomain")
	public void reloadIgnoreDomain(HttpServletRequest request, HttpServletResponse response, Model model) {
		ReloadDomainUtil.reloadIgnoreDomain();
	}

	private void writeResponse(HttpServletResponse response, String responseBody,String url) {
		try {
			response.setContentType("text/html; charset=UTF-8");

			responseBody = this.rectifyRTReturnCodeViaStockStatus(responseBody);
			RESULT_DATA_LOGGER.info("output result data in writeResponse method: {}", responseBody);
			LOGGER_REALTIME_OUTCOME_RESULT.info("call aliyun realTime execution result , url : {},result : {}",url,responseBody);
			
			response.getWriter().print(responseBody);
		} catch (IOException e) {
			logger.error("Error while writting HttpServletResponse::", e);
		}
	}

	private String rectifyRTReturnCodeViaStockStatus(String responseBody) {
		try {// 这个最外层的异常，不能少
			CrawlerJSONResult result = CrawlerJSONResult.buildFrom(responseBody);

			// 如果数据是从mongo中取的，则算为‘核价超时’
			String fromTag = result.getFromTag();
			if (StringUtils.equalsIgnoreCase(fromTag, "mongo")) {
				result.setRtReturnCode(RTReturnCode.RT_CRAWLING_TIMEOUT.getValue());
				return result.parseTo();
			}

			// 变量stockStatus为0时，标识该商品无库存——所有sku都无库存
			/* 在这里对“无货”和“无库存”的设置，在前面流程中已经设置了，这里不需要再重复设置了，所以注释掉(2017.11.11) */
			// int stockStatus = result.getRetbody().getStock().getStatus();
			// if (stockStatus == 0) {
			// if (DocType.INSERT.equals(result.getDocType())) {
			// result.setRtReturnCode(RTReturnCode.RT_STOCK_NONE.getValue());
			// } else if (DocType.DELETE.equals(result.getDocType())) {
			// result.setRtReturnCode(RTReturnCode.RT_ITEM_OFFLINE.getValue());
			// } else {
			// LOGGER_REALTIME_OUTCOME_RESULT.error("found DO NOT support
			// DocType, resutl:{}", result.parseTo());
			// }
			//
			// return result.parseTo();
			// }

			return responseBody;
		} catch (Exception e) {
			return responseBody;
		}
	}

	private List<Callable> getCalls(String url) {
		List<Callable> rst = new ArrayList<Callable>();

		Collection<Task> tasks = TaskCache.getInstance().values();
		for (Task task : tasks) {
			if (null == task.getRules()) {
				continue;
			}

			for (Rule rule : task.getRules()) {
				if (StringUtils.isBlank(rule.getRegex())) {
					continue;
				}

				if (rule.matches(url)) {
					rst = rule.getCalls();
					return rst;
				}
			}
		}

		return rst;
	}

	private Task getTask(String url) {
		Task rst = null;

		Collection<Task> tasks = TaskCache.getInstance().values();
		for (Task task : tasks) {
			if (null == task.getRules()) {
				continue;
			}

			for (Rule rule : task.getRules()) {
				if (StringUtils.isBlank(rule.getRegex())) {
					continue;
				}
				if (rule.matches(url)) {
					rst = task;
					return rst;
				}
			}
		}

		return rst;
	}

	private Url createUrl(String urlValue, Task task) {
		if (StringUtils.isBlank(urlValue) || Objects.isNull(task)) {
			return null;
		}

		Url url = new Url();
		url.setUrlType(UrlType.ITEM);
		url.setId(SpiderStringUtil.md5Encode(urlValue));
		url.setValue(urlValue);
		url.setTaskId(task.getTaskId());
		url.setTask(task);
		url.setLatelyFailedCount(0);
		url.setUrlStatus(UrlStatus.CRAWLING);

		return url;
	}

	private Context createContext(Url url) {
		if (Objects.isNull(url)) {
			return null;
		}

		Context context = new Context();
		context.setRunInRealTime(true);
		context.setUrl(url);
		context.setCurrentUrl(url.getValue());
		context.init();
		return context;
	}

	/**
	 * 计算时间差 秒为单位
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private long getTimeResult(long startTime, long endTime) {
		if (0l != endTime && 0l != startTime) {
			return (endTime - startTime) % 1000 == 0 ? (endTime - startTime) / 1000 : (endTime - startTime) / 1000 + 1;
		}

		return 0l;
	}

	/**
	 * 调用queryItem接口封装
	 * 
	 * @param url
	 * @param task
	 * @return
	 */
	private String callQueryItem(String url, Task task) {
		String jsonResult = StringUtils.EMPTY;
		boolean boo = false;
		if (StringUtils.isNotBlank(googleRealtime)) {
			jsonResult = HttpUtils.get(QUERY_ITEM_ADDRESS + url);
			boo = CrawlerResultUtil.isoffLine(jsonResult);
			if (!boo) {
				RetBody retBody = new RetBody();
				retBody.setProdUrl(new ProdUrl(url));
				retBody.setStock(new Stock(0));
				CrawlerJSONResult result = new CrawlerJSONResult("OK", 1, retBody, String.valueOf(task.getTaskId()),
						DocType.DELETE.toString());
				result.setRtReturnCode(RTReturnCode.RT_ITEM_OFFLINE.getValue());// 商品下架

				jsonResult = result.parseTo();
			}
		} else {
			ItemDO queryMd5UrlLastItem = currentItemDAOImpl.queryMd5UrlLastItem(task.getTaskId(),
					SpiderStringUtil.md5Encode(url));

			jsonResult = CrawlerResultUtil.mongo_result(queryMd5UrlLastItem, task, url, monitorService);
		}
		return jsonResult;
	}

	// 检查url中包含网站白名单
	private boolean checkWebSiteWhiteListExist(String url) {
		boolean isWebSiteWhiteListExist = false;
		if (StringUtils.isNotBlank(webSiteWhiteList)) {
			Set<String> webSites = convertIgnoringWebSites(webSiteWhiteList);
			if (!CollectionUtils.isEmpty(webSites)) {
				for (String webSite : webSites) {
					if (StringUtils.containsIgnoreCase(url, webSite)) {
						isWebSiteWhiteListExist = true;
						break;
					}
				}
			}
		}
		return isWebSiteWhiteListExist;
	}

	/**
	 * 网站白名单
	 * 
	 * @param ignoringWebSites
	 * @return
	 */
	private Set<String> convertIgnoringWebSites(String ignoringWebSites) {
		Set<String> result = new HashSet<String>();

		String[] webSites = StringUtils.splitByWholeSeparator(ignoringWebSites, HOST_SEPARATE);

		if (ArrayUtils.isEmpty(webSites)) {
			return result;
		}

		for (String webSite : webSites) {
			result.add(webSite);
		}

		logger.info("converted-ignoring-webSite::HashSet<String>::{}", result.toString());

		return result;
	}

	@RequestMapping(value = "help", method = RequestMethod.GET)
	public @ResponseBody void help(HttpServletRequest request, HttpServletResponse response, Model model) {
		String line_separator = System.getProperty("line.separator");// 换行符

		try {
			StringBuilder responseBody = new StringBuilder();

			responseBody.append("**********RTReturnCode help start**********").append(line_separator);
			com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
			jsonObject.put("0", "RT_SUCCESS：成功");
			jsonObject.put("1", "RT_STOCK_NONE：商品缺库存");
			jsonObject.put("2", "RT_ITEM_OFFLINE：商品已下架");
			jsonObject.put("3", "RT_CRAWLING_TIMEOUT：核价超时");
			jsonObject.put("4", "RT_URL_NONE：缺少核价URL");
			jsonObject.put("5", "RT_URL_ILLEGAL：非法的核价URL");
			jsonObject.put("6", "RT_WEBSITE_NOT_SUPPORT：不支持的商家网站");
			jsonObject.put("7", "RT_INNER_ERROR：核价异常");
			responseBody.append(jsonObject.toJSONString());
			responseBody.append("**********RTReturnCode help end**********").append(line_separator);

			response.setContentType("text/html; charset=UTF-8");
			response.getWriter().print(responseBody.toString());
		} catch (IOException e) {
			// ignore
		}
	}

	public static void main(String[] args) {
		RealtimeCrawlerController r = new RealtimeCrawlerController();
		r.convertIgnoringWebSites("www.faa.com");
	}
}