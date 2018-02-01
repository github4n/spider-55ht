package com.haitao55.spider.cleaning_full;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.SpiderDateTimeUtil;
import com.haitao55.spider.util.Constants;

/**
 * 从queue获取item数据再向文件中写入
 * 
 * @author denghuan
 *
 */
public class ItemWriteFile extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CLEANING_FULL);

	private String outputRootPath;
	private String outputRootFileName;
	private ExecutorService executorService;
	private boolean running = true;

	public ItemWriteFile(String outputRootPath, String outputRootFileName, ExecutorService executorService) {
		this.outputRootPath = outputRootPath;
		this.outputRootFileName = outputRootFileName;
		this.executorService = executorService;

	}

	@Override
	public void run() {
		BufferedWriter writer = createOutputWriter();
		BlockingQueue<String> queue = ItemBolckingQueue.getInstance();
		while (running) {
			try {
				if (executorService.isShutdown() && queue.isEmpty()) {
					running = false;
					closeOutputWriter(writer);
				}
				
				String value = queue.poll();

				if (StringUtils.isNotBlank(value)) {
					writeLastedItem(value, writer);
				}else{
					Thread.sleep(1000);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private BufferedWriter createOutputWriter() {
		BufferedWriter bw = null;
		String time = SpiderDateTimeUtil.format(new Date(), SpiderDateTimeUtil.FORMAT_SHORT_DATE);
		String outputFilePath = outputRootPath + File.separator + this.outputRootFileName + time + Constants.CLEANING_FULL_FILE_SUFFIX;
		File file = new File(outputFilePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error while create parentFile file, ", e);
			return null;
		}
		try {
			bw = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			logger.error("Error while create BufferedWriter", e);
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

	private void closeOutputWriter(BufferedWriter writer) {
		IOUtils.closeQuietly(writer);
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
}
