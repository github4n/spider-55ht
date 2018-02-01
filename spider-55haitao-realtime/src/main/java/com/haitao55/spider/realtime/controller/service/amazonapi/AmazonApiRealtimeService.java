package com.haitao55.spider.realtime.controller.service.amazonapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import com.haitao55.spider.common.dao.CurrentItemDAO;
import com.haitao55.spider.common.dao.ImageDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.dos.RealtimeStatisticsDO;
import com.haitao55.spider.common.entity.RTReturnCode;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.kafka.SpiderKafkaProducer;
import com.haitao55.spider.common.service.MonitorService;
import com.haitao55.spider.common.service.impl.RedisService;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.amazon.api.AmazonRealTimePriceAWSKeyPool;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.image.service.ImageService;
import com.haitao55.spider.realtime.async.AmazonApiRealtimeServiceCallable;
import com.haitao55.spider.realtime.async.RealtimeCrawlerThreadPoolExecutor;
import com.haitao55.spider.realtime.async.RealtimeThreadPoolExecutor;
import com.haitao55.spider.realtime.common.util.CrawlerResultUtil;
import com.haitao55.spider.realtime.common.util.OutPutDataUtil;
import com.haitao55.spider.realtime.controller.RealtimeCrawlerController;
import com.haitao55.spider.realtime.service.OutputDataDealService;

/**
 * amazon api 方式核价 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月20日 下午2:29:45
 * @version 1.0
 */
public class AmazonApiRealtimeService {
	private static final Logger logger = LoggerFactory.getLogger(RealtimeCrawlerController.class);
	private static final Logger logger_time_consuming = LoggerFactory.getLogger("realtime_time_consuming");
	private static final Logger result_data_logger = LoggerFactory.getLogger("result_data_logger");

	private static final String URL_TO_MONGO_REDIS_KEY = "url_to_mongo";
	private static final String HOST_SEPARATE = ",";

	// private static long CRAWLING_WAIT_TIME = 7 * 1000;// 在线抓取最多等待时间
	private static final TimeUnit CRAWLING_WAIT_UNIT = TimeUnit.MILLISECONDS;// 在线抓取最多等待时间单位
	private static final int REDIS_EXPIRED_TIME = 3600;// redis key过期时间
	// result json error flag
	private static String errorMsg = "ERROR";
	// 阿里云地址
	private static final String ali_path = "http://114.55.10.105/spider-55haitao-realtime/realtime-crawler/pricing.action";

	// result from tag
	private static String REALTIME_RESULT_FROM_MONGO_TAG = "mongo";
	private static String REALTIME_RESULT_FROM_CRAWLER_TAG = "crawler";

	private static String DELETE_TYPE = "DELETE";

	private static AmazonRealTimePriceAWSKeyPool awsKeyPool;
	static {
		try {
			awsKeyPool = new AmazonRealTimePriceAWSKeyPool();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取amazon核价结果
	 * 
	 * @param task
	 * @param url
	 * @param timeout
	 * @param isSendMessage
	 * @param request
	 * @param response
	 * @param model
	 * @param realtimeStatisticsDO
	 * @param context
	 * @param imageService
	 * @param imageDAO
	 * @param outputDataDealService
	 * @param redisService
	 * @param currentItemDAOImpl
	 * @param producer
	 * @param topic
	 * @param monitorService
	 */
	public static void realtime_servcie(Task task, String url, long timeout, String isSendMessage,
			HttpServletRequest request, HttpServletResponse response, Model model,
			RealtimeStatisticsDO realtimeStatisticsDO, Context context, ImageService imageService, ImageDAO imageDAO,
			OutputDataDealService outputDataDealService, RedisService redisService, CurrentItemDAO currentItemDAOImpl,
			SpiderKafkaProducer producer, String topic, MonitorService monitorService) {

		/* 针对url 进行 url mongo库插入 **/
		redisService.lpush(URL_TO_MONGO_REDIS_KEY, task.getTaskId() + HOST_SEPARATE + url);

		long start = System.currentTimeMillis();
		logger.info("request url : {} to realtime service", url);
		// 调用接口
		final String urlTemp = url;
		Long taskId = task.getTaskId();
		String jsonResult = StringUtils.EMPTY;
		RealtimeThreadPoolExecutor postExecutorService = RealtimeThreadPoolExecutor.getInstance();
		ExecutorService executorService = RealtimeCrawlerThreadPoolExecutor.getInstance();
		AmazonApiRealtimeServiceCallable callable = new AmazonApiRealtimeServiceCallable(awsKeyPool, context, taskId);
		Future<OutputObject> future = executorService.submit(callable);
		OutputObject oo = null;
		try {
			// 当前线程最多等待一定时间,如果还获取不到抓取结果,则当前线程继续执行
			// 这里的"当前线程"也就是应用程序服务器(如tomcat)为处理当前http请求而启动的处理线程；
			oo = future.get(timeout, CRAWLING_WAIT_UNIT);
		} catch (InterruptedException e) {
			logger.error("Error while realtime-crawling::", e);
		} catch (ExecutionException e) {
			logger.error("Error while realtime-crawling::", e);
		} catch (TimeoutException e) {
			// 针对超时线程，终止线程执行
			future.cancel(true);

			logger_time_consuming.error("realtime timeout url: {}", url);
			postExecutorService.submit(new Runnable() {
				@Override
				public void run() {
					// googleCloudTransfer(urlTemp,requestFrom,"60000",isSendMessage,response);
					Map<String, String> params = new HashMap<String, String>();
					params.put("url", urlTemp);
					params.put("request_from", "timeout realtime service");
					params.put("timeout", "60000");
					params.put("is_send_message", isSendMessage);
					HttpClientUtil.post(ali_path, params);
				}
			});
			logger.error("Error while realtime-crawling::", e);
		}
		long end = System.currentTimeMillis();
		logger_time_consuming.info("realtime_time_consuming : " + getTimeResult(start, end) + " from amazon" + " url :{}", url);
		if (Objects.nonNull(oo)) {
		    
		    logger.info("oo.getImages==> {} ",oo.getImages()); 
			imageService.setImageDAO(imageDAO);
			oo = OutPutDataUtil.writeOutputObjectActually(oo, outputDataDealService, imageService);
			
			jsonResult = oo.convertItem2Message();

			boolean boo = CrawlerResultUtil.isoffLine(jsonResult);
			result_data_logger.info("result json from realtime service  url : {} ,json :{}", url, jsonResult);
			jsonResult = CrawlerResultUtil.crawler_result(oo.convertItem2Message(), boo, task, url, oo, producer, topic,
					currentItemDAOImpl, realtimeStatisticsDO);
			// put redis(key, value, time:1minute)
			CrawlerJSONResult buildFrom = CrawlerJSONResult.buildFrom(jsonResult);
			if (null != buildFrom) {
				if ((buildFrom.getRtReturnCode() == RTReturnCode.RT_SUCCESS.getValue()
						|| buildFrom.getRtReturnCode() == RTReturnCode.RT_STOCK_NONE.getValue())
						&& !StringUtils.endsWithIgnoreCase(buildFrom.getDocType(), DELETE_TYPE)) {
					// when jsonResult is not ERROR, set redis
					redisService.set(SpiderStringUtil.md5Encode(url), jsonResult, REDIS_EXPIRED_TIME);
				}
			}

			// from crawler
			jsonResult = CrawlerResultUtil.result_from_package(jsonResult, REALTIME_RESULT_FROM_CRAWLER_TAG);

			if (realtimeStatisticsDO.getException() == 0) {
				realtimeStatisticsDO.setCrawler(1);
			}
		} else {
			
			start = System.currentTimeMillis();
			
			ItemDO queryMd5UrlLastItem = currentItemDAOImpl.queryMd5UrlLastItem(task.getTaskId(),
					SpiderStringUtil.md5Encode(url));

			jsonResult = CrawlerResultUtil.mongo_result(queryMd5UrlLastItem, task, url, monitorService);
			result_data_logger.info("result json from mongo  url : {} ,json :{}", url, jsonResult);

			// from mongo
			jsonResult = CrawlerResultUtil.result_from_package(jsonResult, REALTIME_RESULT_FROM_MONGO_TAG);

			end = System.currentTimeMillis();
			logger_time_consuming.info("realtime_time_consuming : " + getTimeResult(start, end) + " from amazon" + " url :{}", url);
			
			realtimeStatisticsDO.setMongo(1);
		}
		result_data_logger.info("result json from realtime service  url : {} ,json :{}", url, jsonResult);

		if (StringUtils.isBlank(jsonResult)) {
			writeResponse(response, "Error:got no item neither by crawling nor from database!");
		}

		writeResponse(response, jsonResult);
	}

	private static void writeResponse(HttpServletResponse response, String responseBody) {
		try {
			response.setContentType("text/html; charset=UTF-8");
			response.getWriter().print(responseBody);
		} catch (IOException e) {
			logger.error("Error while writting HttpServletResponse::", e);
		}
	}
	private static long getTimeResult(long startTime, long endTime) {
		if(0l!=endTime && 0l!=startTime){
			return (endTime - startTime) % 1000 == 0 ? (endTime - startTime) / 1000
					: (endTime - startTime) / 1000 + 1;
		}
		return 0l;
	}
}
