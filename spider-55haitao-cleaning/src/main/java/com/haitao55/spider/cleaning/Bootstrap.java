package com.haitao55.spider.cleaning;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.cleaning.service.ICleaningService;
import com.haitao55.spider.cleaning.tasks.Watcher;
import com.haitao55.spider.cleaning.writer.CleaningAfterItemWriterFile;
import com.haitao55.spider.cleaning.writer.KafkaItemToQueue;
import com.haitao55.spider.cleaning.writer.KafkaItemWriterFile;
import com.haitao55.spider.common.kafka.SpiderKafkaConsumer;

/**
 * 
 * 功能：数据清洗模块的启动类
 * 
 * @author Arthur.Liu
 * @time 2016年8月30日 下午5:56:48
 * @version 1.0
 */
public class Bootstrap {
	private static final Logger logger = LoggerFactory.getLogger("system");

	/**
	 * 线程池属性键值对
	 */
	private Map<String, String> threadPoolProps;
	private SpiderKafkaConsumer consumer;
	private ICleaningService cleanService;
	
	private String outputRootPath; //input kafak data输出路径
	private String outputFile; //input kafka data输出文件名
	private String cleaningAfterOutputRootPath;//清洗后的输出路径

	public void init() {
		logger.info("Bootstrap.init() start....");

		KafkaItemToQueue itemWatcher = new KafkaItemToQueue(consumer);
		itemWatcher.start();
		
		Watcher watcher = new Watcher(threadPoolProps, consumer, cleanService);
		watcher.start();

		KafkaItemWriterFile kafkaItem = new KafkaItemWriterFile(outputRootPath,outputFile);
		kafkaItem.start();
		
		CleaningAfterItemWriterFile cleaningAfterItem = new CleaningAfterItemWriterFile(cleaningAfterOutputRootPath,outputFile);
		cleaningAfterItem.start();
		
		logger.info("Bootstrap.init() start successfully!");
	}

	public Map<String, String> getThreadPoolProps() {
		return threadPoolProps;
	}

	public void setThreadPoolProps(Map<String, String> threadPoolProps) {
		this.threadPoolProps = threadPoolProps;
	}

	public SpiderKafkaConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(SpiderKafkaConsumer consumer) {
		this.consumer = consumer;
	}

	public ICleaningService getCleanService() {
		return cleanService;
	}

	public void setCleanService(ICleaningService cleanService) {
		this.cleanService = cleanService;
	}

	public String getOutputRootPath() {
		return outputRootPath;
	}

	public void setOutputRootPath(String outputRootPath) {
		this.outputRootPath = outputRootPath;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	
	public String getCleaningAfterOutputRootPath() {
		return cleaningAfterOutputRootPath;
	}

	public void setCleaningAfterOutputRootPath(String cleaningAfterOutputRootPath) {
		this.cleaningAfterOutputRootPath = cleaningAfterOutputRootPath;
	}
}