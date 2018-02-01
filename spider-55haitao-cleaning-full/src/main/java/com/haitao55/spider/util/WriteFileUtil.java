package com.haitao55.spider.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WriteFileUtil {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CLEANING_FULL);

	public BufferedWriter createOutputWriter(String outputFilePath) {
		BufferedWriter bw = null;
		File file = new File(outputFilePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error while create total file size parentFile file, ", e);
			return null;
		}
		try {
			bw = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			logger.error("Error while create total file size BufferedWriter", e);
			return null;
		}

		return bw;
	}
	
	public String readFileLastLineValue(String outputPath){
	    String lastLineValue = StringUtils.EMPTY; 
	    FileReader fr = null;
	    BufferedReader br = null;
	   try {  
    	   fr = new FileReader(outputPath);  
    	   br = new BufferedReader(fr);  
    	   String tempLineValue = StringUtils.EMPTY;  
    	   while ((tempLineValue = br.readLine()) != null) {  
    		  lastLineValue= tempLineValue;  
    	   }  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	        logger.error("Error read lastLineValue error", e);
	    }finally {
	    	IOUtils.closeQuietly(fr);
	    	IOUtils.closeQuietly(br);
		}  
    	return lastLineValue;
	}

	public void writeLasted(String value, BufferedWriter writer) {
		try {
			writer.append(value);
			writer.newLine();
		} catch (IOException e) {
			logger.error("Error writting total file size-value", e);
		}
	}

	public void closeOutputWriter(BufferedWriter writer) {
		IOUtils.closeQuietly(writer);
	}
	
	public static void main(String[] args) {
		
	    String lastLineValue= StringUtils.EMPTY;  
	    try {  
	        FileReader fr = new FileReader("/home/denghuan/data/deng.json");  
	        BufferedReader br = new BufferedReader(fr);  
	        String tempLineValue = StringUtils.EMPTY;  
	        while ((tempLineValue = br.readLine()) != null) {  
	        	lastLineValue= tempLineValue;  
	        }  
	        IOUtils.closeQuietly(fr);
	    	IOUtils.closeQuietly(br);
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
		System.out.println(lastLineValue);
	}
}
