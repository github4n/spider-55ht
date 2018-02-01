package com.haitao55.spider.cleaning.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.haitao55.spider.cleaning.service.ICleaningService;
import com.haitao55.spider.cleaning.utils.ConsumerKafkaItemBolckingQueue;
import com.haitao55.spider.common.kafka.SpiderKafkaConsumer;

/**
 * 
 * 功能：数据清洗模块监视器类
 * 
 * @author Arthur.Liu
 * @time 2016年8月31日 下午7:02:14
 * @version 1.0
 */
public class Watcher extends Thread {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private static final String THREAD_POOL_PROPS_CORE_POOL_SIZE = "corePoolSize";
	private static final String THREAD_POOL_PROPS_MAXIMUM_POOL_SIZE = "maximumPoolSize";
	private static final String THREAD_POOL_PROPS_KEEP_ALIVE_TIME = "keepAliveTime";
	private static final String THREAD_POOL_PROPS_WORK_QUEUE_SIZE = "workQueueSize";
	private static final int DEFAULT_CORE_POOL_SIZE = 5;
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 50;
	private static final int DEFAULT_KEEP_ALIVE_TIME = 10 * 1000;
	private static final int DEFAULT_WORK_QUEUE_SIZE = 1000;

	private Map<String, Integer> threadPoolProps;
	private SpiderKafkaConsumer consumer;

	private ICleaningService cleanService;

	private ExecutorService executorService = null;

	public Watcher(Map<String, String> threadPoolProps, SpiderKafkaConsumer consumer, ICleaningService cleanService) {
		this.threadPoolProps = this.convertThreadPoolProps(threadPoolProps);
		this.consumer = consumer;
		this.cleanService = cleanService;

		executorService = new ThreadPoolExecutor(this.threadPoolProps.get(THREAD_POOL_PROPS_CORE_POOL_SIZE),
				this.threadPoolProps.get(THREAD_POOL_PROPS_MAXIMUM_POOL_SIZE),
				this.threadPoolProps.get(THREAD_POOL_PROPS_KEEP_ALIVE_TIME), TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(this.threadPoolProps.get(THREAD_POOL_PROPS_WORK_QUEUE_SIZE)),
				Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());// 采用默默丢弃的策略
	}

	/**
	 * <p>
	 * 第一步：获取爬虫发送到kafka的商品信息，每次while拿多少个
	 * </p>
	 * <p>
	 * 第二步：拿到kafkaMQ信息，然后清理一些不合理的商品信息
	 * <p/>
	 * <p>
	 * 第三步:过滤掉一些不合理数据，留下的数据存放mongodb，先首先判断sku
	 * 价格等字段是否发生变化，价格发生变化，那就像mongodb插入，其他就return
	 * </p>
	 * <p>
	 * 第四步：将发生变化的数据发送到search组中的kafka
	 * </p>
	 * <p>
	 * 第五步：写入到文件当中
	 * </p>
	 * 
	 */
	@Override
	public void run() {
		BlockingQueue<List<String>> queue = ConsumerKafkaItemBolckingQueue.getInstance();
		while (true) {
			try {
				long startTime = System.currentTimeMillis();
				List<String> itemList = queue.poll();
				logger.info("Fetch data from BlockingQueue end, current-time:{}", System.currentTimeMillis() - startTime);

				if (CollectionUtils.isEmpty(itemList)) {// 如果从队列上没有取到数据,当前线程sleep一段时间
					logger.warn("fetch none items from kafka");
					Thread.sleep(1000);
				} else {
					for (String item : itemList) {
						Runnable r = new ItemHandler(item, cleanService);
						executorService.submit(r);
					}
				}
			} catch (Exception e) {
				logger.error("watcher Thread执行出错！", e);
			}
		}
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
}