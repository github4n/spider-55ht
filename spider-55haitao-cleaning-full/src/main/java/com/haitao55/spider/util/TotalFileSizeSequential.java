package com.haitao55.spider.util;

import java.io.File;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统计全量文件大小
 * @author denghuan
 *date : 2017-4-28
 */
public class TotalFileSizeSequential {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CLEANING_FULL);

    // 递归方式 计算文件的大小
	public static Long getTotalSizeOfFilesInDir(final File file) {
    	try {
    		 if (file.isFile())
 	            return file.length();
 	        final File[] children = file.listFiles();
 	        Long total = 0L;
 	        if (children != null){
 	        	 for (final File child : children){
 	        		total += getTotalSizeOfFilesInDir(child);
 	        	 }
 	        }
 	        return total;	 
		} catch (Exception e) {
			logger.error("File NotFund Exception....");
			e.printStackTrace();
		}
    	return 0L;
    }
    
   public static String getTotalFileSize(String fileName){
	    final Long total = getTotalSizeOfFilesInDir(new File(fileName));
        DecimalFormat df = new DecimalFormat("#.00");
        String totalSzie = "";
        if (total < 1024) {
        	totalSzie = df.format((double) total) + "B";
        } else if (total < 1048576) {
        	totalSzie = df.format((double) total / 1024) + "K";
        } else if (total < 1073741824) {
        	totalSzie = df.format((double) total / 1048576) + "M";
        } else {
        	totalSzie = df.format((double) total / 1073741824) +"G";
        }
        logger.info("TotalFileSize ---> fileName : {} , totalSzie : {}",fileName,totalSzie);
        return totalSzie;
    }
}
