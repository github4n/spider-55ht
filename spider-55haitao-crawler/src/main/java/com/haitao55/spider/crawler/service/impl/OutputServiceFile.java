package com.haitao55.spider.crawler.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.service.MonitorService;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.service.AbstractOutputService;
import com.haitao55.spider.crawler.service.OutputService;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.writer.OutputTools;

/**
 * 
 * 功能：结果数据输出的Service接口实现，以写本地文件的方式输出结果数据
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午6:30:13
 * @version 1.0
 */
public class OutputServiceFile extends AbstractOutputService implements OutputService {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);

	private MonitorService monitorService;
	private static final String ITEMS_ROOT_PATH = "/output";
	private static final String IMAGES_ROOT_PATH = "/images";
	private static final String filePrefixName = "55HT";
	private static final String fileSuffixName = ".SCD";
	private static final String imageFileSuffixName = ".jpg";

	@Override
	public void write(OutputObject oo)  {
		try{
			logger.info("Output item onto local directory....");
			OutputTools tools = OutputTools.getInstance();
			String website = tools.getWebsite(oo.getUrl().getValue());
			BufferedWriter bw =  tools.getBufferWriter(website);
			if(bw == null){
				String localIP = InetAddress.getLocalHost().getHostAddress().toString();
				String filePath = getFilePath(website, localIP, oo.getTaskId(),ITEMS_ROOT_PATH,fileSuffixName);
				bw = OutputTools.getInstance().getBw(filePath, website);
			}
			synchronized (bw) {
				bw.write(oo.convertItem2Message()+"\n");
				bw.flush();
			}
			logger.info("Output item onto local directory successfully!");
		}catch(Throwable e){
			logger.error("Error occurred while write item data to scd {}", e);
		}
	}

	private static String getFilePath(String website,String ip,String taskId,String path,String suffixName) throws IOException {
		String rootDir = getOutPutDir(website,path);
		String lastIp = StringUtils.substringAfterLast(ip, ".");
		String itemDateRand = (new StringBuilder((new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date())))
				.append("-").append(String.valueOf(Math.round(Math.random() * 10000))).append("-").toString();
		String fileName = filePrefixName+"-"+taskId + "-" + itemDateRand + lastIp + suffixName;
		File itemFile = new File(rootDir + "/" + fileName);
		if (!itemFile.exists()) {
			itemFile.createNewFile();
		}
		logger.info("Created item-file before actually output::file:{}", itemFile.getAbsoluteFile());
		return itemFile.getAbsolutePath();
	}

	private static String getOutPutDir(String website,String path) {
		URL url = OutputServiceFile.class.getProtectionDomain().getCodeSource().getLocation();
		File jarFile = new File(url.getFile());
		String parentPath = jarFile.getParent();
		String itemsRootPath = parentPath + path;
		File itemsRootDir = new File(itemsRootPath);
		if (!itemsRootDir.exists()) {// 根路径
			itemsRootDir.mkdir();
		}
		File websiteDir = new File(itemsRootPath+"/"+website);
		if (!websiteDir.exists()) {// 根路径
			websiteDir.mkdir();
		}
		return websiteDir.getAbsolutePath();
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	@Override
	public boolean existInRepertory(Image image) {
		return false;
	}

	@Override
	public void uploadImage(Image image,OutputObject oo) {
		/*try{
			logger.info("Output image onto local directory....");
			OutputTools tools = OutputTools.getInstance();
			String website = tools.getWebsite(oo.getUrl().getValue());
			BufferedWriter bw =  tools.getBufferWriter(website);
			if(bw == null){
				String localIP = tools.getLinuxUbuntuLocalIPAddr();
				String filePath = getFilePath(website, localIP, oo.getTaskId(),ITEMS_ROOT_PATH,imageFileSuffixName);
				bw = OutputTools.getInstance().getBw(filePath, website);
			}
			synchronized (bw) {
				bw.write(oo.convertItem2Json()+"\n");
				bw.flush();
			}
			logger.info("Output image onto local directory successfully!");
		}catch(Throwable e){
			logger.error("Error occurred while write image data to jpg {}", e);
		}*/
	}

	@Override
	public void createImageRepertoryUrl(Image image,OutputObject oo) {
		try{
			OutputTools tools = OutputTools.getInstance();
			String website = tools.getWebsite(oo.getUrl().getValue());
			String localIP = InetAddress.getLocalHost().getHostAddress().toString();
			String filePath = getFilePath(website, localIP, oo.getTaskId(),IMAGES_ROOT_PATH,imageFileSuffixName);
			image.setRepertoryUrl(filePath);
		}catch(Throwable e){
			logger.error("Error create image repertoryUrl {}", e);
		}
	}

	
}