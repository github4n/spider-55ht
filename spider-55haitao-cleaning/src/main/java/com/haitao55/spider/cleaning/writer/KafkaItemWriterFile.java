package com.haitao55.spider.cleaning.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.cleaning.utils.KafkaItemBolckingQueue;
import com.haitao55.spider.common.utils.SpiderDateTimeUtil;


/**
 * 将kafka数据写到文件目录下
 * @author denghuan
 *
 */
public class KafkaItemWriterFile extends Thread{
	
	private static final Logger logger = LoggerFactory.getLogger(KafkaItemWriterFile.class);
	
	private String outputRootPath;
	private String outputRootFileName;

	public KafkaItemWriterFile(String outputRootPath, String outputRootFileName) {
		this.outputRootPath = outputRootPath;
		this.outputRootFileName = outputRootFileName;

	}
	
	@Override
	public void run() {
		
		BlockingQueue<String> queue = KafkaItemBolckingQueue.getInstance();
		BufferedWriter writer = createOutputWriter();
		String yesterday = getDataTime();
		while (true) {
			try {
				String value = queue.poll();
				String toDay = getDataTime();
				if (StringUtils.isNotBlank(value)) {
					if(!yesterday.equals(toDay)){
						writer = createOutputWriter();
						logger.info("CleaningAfterItemWriterFile -> yesterday: {},toDay: {}", yesterday, toDay);
						yesterday = toDay;
					}
					writeLastedItem(value, writer);
				}else{
					Thread.sleep(1000);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public String getOutputRootPath() {
		return outputRootPath;
	}

	public void setOutputRootPath(String outputRootPath) {
		this.outputRootPath = outputRootPath;
	}

	public String getOutputRootFileName() {
		return outputRootFileName;
	}

	public void setOutputRootFileName(String outputRootFileName) {
		this.outputRootFileName = outputRootFileName;
	}

	private String getDataTime(){
		return SpiderDateTimeUtil.format(new Date(), SpiderDateTimeUtil.FORMAT_SHORT_DATE);
	}

	private BufferedWriter createOutputWriter() {
		
		BufferedWriter bw = null;
		String dateTime = SpiderDateTimeUtil.format(new Date(), SpiderDateTimeUtil.FORMAT_SHORT_DATE);
		
		String outputFilePath = this.outputRootPath + File.separator + dateTime +  File.separator + outputRootFileName;
		
		File file = new File(outputFilePath);
		
		try {
			if (!file.exists()) {
				 if (!file.getParentFile().exists()) {
					 file.getParentFile().mkdirs();
				 }
			     file.createNewFile();
			  }
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error while create parentFile file, ", e);
			return null;
		}
	
		try {
			bw = new BufferedWriter(new FileWriter(file,true));
		} catch (IOException e) {
			logger.error("Error create BufferedWriter", e);
			return null;
		}

		return bw;
	}

	private void writeLastedItem(String value, BufferedWriter writer) {
		try {
			writer.append(value);
			writer.newLine();
		} catch (IOException e) {
			logger.error("Error writting item-value", e);
		}
	}
	
	
}
