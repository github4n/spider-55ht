package com.haitao55.spider.cleaning_full;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.dao.ItemDAO;
import com.haitao55.spider.common.dao.impl.mongo.ItemDAOImpl;
import com.haitao55.spider.service.CounterService;
import com.haitao55.spider.util.CleaingFullUtil;
import com.haitao55.spider.util.Constants;
import com.haitao55.spider.util.ExportExcelToDisk;
import com.haitao55.spider.util.HttpUtils;
import com.haitao55.spider.util.MailUtils;
import com.haitao55.spider.util.PropertiesUtils;
import com.haitao55.spider.view.DomainCounter;
import com.mongodb.Mongo;

/**
 * 
 * 功能：全量数据清理(CleaningFull)模块业务监控类
 * 
 * @author denghuan
 * @time 2016年9月21日 下午7:10:02
 * @version 1.0
 */
public class Watcher extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CLEANING_FULL);

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String IGNORING_TASK_IDS_SEPARATOR = ",";

	private static final String THREAD_POOL_PROPS_CORE_POOL_SIZE = "corePoolSize";
	private static final String THREAD_POOL_PROPS_MAXIMUM_POOL_SIZE = "maximumPoolSize";
	private static final String THREAD_POOL_PROPS_KEEP_ALIVE_TIME = "keepAliveTime";
	private static final String THREAD_POOL_PROPS_WORK_QUEUE_SIZE = "workQueueSize";
	private static final int DEFAULT_CORE_POOL_SIZE = 5;
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
	private static final int DEFAULT_KEEP_ALIVE_TIME = 10 * 1000;
	private static final int DEFAULT_WORK_QUEUE_SIZE = 1000;

	private Map<String, Integer> threadPoolProps;
	private ItemDAO itemDAO;
	private Set<String> ignoringTaskIds;
	
	private String outputRootPath;
	private String outputRootFileName;
	private ExecutorService executorService;
	private CounterService counterService;
	
	private String emailAddress;
	private String emailPassword;
	private String emailReceiver;
	
	private String excelFilePath;
	
	private String serverAddress;

	public Watcher(Map<String, String> threadPoolProps, ItemDAO itemDAO, String ignoringTaskIds,
			String outputRootPath,String outputRootFileName,CounterService counterService,String emailAddress
			,String emailPassword,String emailReceiver,String excelFilePath,String serverAddress) {
		this.threadPoolProps = this.convertThreadPoolProps(threadPoolProps);
		this.itemDAO = itemDAO;
		this.ignoringTaskIds = this.convertIgnoringTaskIds(ignoringTaskIds);
		this.outputRootPath = outputRootPath;
		this.outputRootFileName = outputRootFileName;
		this.counterService = counterService;
		this.emailAddress = emailAddress;
		this.emailPassword = emailPassword;
		this.emailReceiver = emailReceiver;
		this.excelFilePath = excelFilePath;
		this.serverAddress = serverAddress;
		
		executorService = new ThreadPoolExecutor(this.threadPoolProps.get(THREAD_POOL_PROPS_CORE_POOL_SIZE),
				this.threadPoolProps.get(THREAD_POOL_PROPS_MAXIMUM_POOL_SIZE),
				this.threadPoolProps.get(THREAD_POOL_PROPS_KEEP_ALIVE_TIME), TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(this.threadPoolProps.get(THREAD_POOL_PROPS_WORK_QUEUE_SIZE)),
				Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());// 采用调用者执行的策略
	}

	@Override
	public void run() {
		logger.info("Watcher.run() start: {}", (new SimpleDateFormat(DATE_FORMAT)).format(System.currentTimeMillis()));

		ItemWriteFile itemTask = new ItemWriteFile(outputRootPath, outputRootFileName, executorService);
		itemTask.start();

		Mongo mongo = ((ItemDAOImpl) this.itemDAO).getMongoTemplate().getDb().getMongo();// 获取Mongo实例
		List<String> databaseNames = mongo.getDatabaseNames();// 获取Mongo实例中所有数据库名称

		if (CollectionUtils.isEmpty(databaseNames)) {
			logger.warn("got no database-names from mongo!");
			return;
		}

		CountDownLatch countDownLatchStart = new CountDownLatch(1);
		CountDownLatch countDownLatchEnd = new CountDownLatch(databaseNames.size());

		for (String databaseName : databaseNames) {
			Runnable task = new ItemExportWorker(mongo, databaseName, ignoringTaskIds, countDownLatchStart,
					countDownLatchEnd, counterService);
			this.executorService.submit(task);
		}

		countDownLatchStart.countDown();

		try {
			countDownLatchEnd.await();

			this.executorService.shutdownNow();

		} catch (InterruptedException e) {
			logger.error("Error while countDownLatchEnd.await()", e);
		}

		this.loadProperties();// load webSite config file

		this.itemsStatistics();// 商品统计

		this.sendSiteTotalToHttpServer(); // send http server
		
		logger.info("Watcher.run() end: {}", (new SimpleDateFormat(DATE_FORMAT)).format(System.currentTimeMillis()));
	}

	private Map<String, Integer> convertThreadPoolProps(Map<String, String> map) {
		Map<String, Integer> result = new HashMap<String, Integer>();

		result.put(THREAD_POOL_PROPS_CORE_POOL_SIZE,
				NumberUtils.toInt(map.get(THREAD_POOL_PROPS_CORE_POOL_SIZE), DEFAULT_CORE_POOL_SIZE));
		result.put(THREAD_POOL_PROPS_MAXIMUM_POOL_SIZE,
				NumberUtils.toInt(map.get(THREAD_POOL_PROPS_MAXIMUM_POOL_SIZE), DEFAULT_MAXIMUM_POOL_SIZE));
		result.put(THREAD_POOL_PROPS_KEEP_ALIVE_TIME,
				NumberUtils.toInt(map.get(THREAD_POOL_PROPS_KEEP_ALIVE_TIME), DEFAULT_KEEP_ALIVE_TIME));
		result.put(THREAD_POOL_PROPS_WORK_QUEUE_SIZE,
				NumberUtils.toInt(map.get(THREAD_POOL_PROPS_WORK_QUEUE_SIZE), DEFAULT_WORK_QUEUE_SIZE));

		return result;
	}
	
	private void loadProperties(){
		PropertiesUtils.loadProperties();
	}
	
	private void itemsStatistics(){
		boolean flag = ExportExcelToDisk.exportExcel(excelFilePath);
		if (flag) {
			MailUtils.doSendMail(emailAddress, emailPassword, emailReceiver, excelFilePath, outputRootPath,
					outputRootFileName);// 邮件发送
		}
	}

	private void sendSiteTotalToHttpServer(){
		List<DomainCounter> onLineLIst = CleaingFullUtil
				.getDomainCountList(Constants.CLEANING_FULL_ITEM_ONLINE_FIELD_PREFIX);
		JSONArray jsonArray = new JSONArray();
		if (CollectionUtils.isNotEmpty(onLineLIst)) {
			onLineLIst.forEach(onLine -> {
				JSONObject jsonObject = new JSONObject();
				String domain = onLine.getDomain();
				long count = onLine.getCount();
				if (StringUtils.isNotBlank(domain)) {
					jsonObject.put(domain, count);
					jsonArray.add(jsonObject);
				}
			});
		}
		if (jsonArray != null) {
			try {
				String result = HttpUtils.post(jsonArray.toJSONString(), serverAddress);
				logger.info("send httpServer success ::: result : {} ", result);
			} catch (Exception e) {
				logger.error("cleaning-full item sttatistics ,send httpServer error....");
				e.printStackTrace();
			}
		}
	}
	
	private Set<String> convertIgnoringTaskIds(String ignoringTaskIds) {
		Set<String> result = new HashSet<String>();

		String[] taskIds = StringUtils.splitByWholeSeparator(ignoringTaskIds, IGNORING_TASK_IDS_SEPARATOR);

		if (ArrayUtils.isEmpty(taskIds)) {
			return result;
		}

		for (String taskId : taskIds) {
			result.add(taskId);
		}

		logger.info("converted-ignoring-task-ids::HashSet<String>::{}", result.toString());

		return result;
	}
}