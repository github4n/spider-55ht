package com.haitao55.spider.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * phantomJs util
 * @author denghuan
 *
 *
 */
public class PhantomJsCrawlerUtil {

	private static final String PHANTOMJS_PATH = "/home/gphonebbs/phantomjs-2.1.1-linux-x86_64/bin/phantomjs ";
	
	private static final String PHANTOMJS_JS_FILE_PATH = "/data/apps/phantomjs/phantomjs.js ";
	
	public static String get(String url){
		StringBuffer sbf = new StringBuffer();
//		BufferedReader br = null;
//		try {
//			Process process = Runtime.getRuntime().exec(PHANTOMJS_PATH+PHANTOMJS_JS_FILE_PATH+url);
//			InputStream is = process.getInputStream();
//			br = new BufferedReader(new InputStreamReader(is));
//			
//			String line = "";
//			while ((line = br.readLine()) != null) {
//				sbf.append(line);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally {
//			try {
//				br.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		return sbf.toString();
	}
}
