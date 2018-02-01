package com.haitao55.spider.realtime.controller.reloadDomain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.realtime.cache.IgnoreDomainCache;
import com.haitao55.spider.realtime.controller.RealtimeCrawlerController;

/**
 * 用作 加载文件 到 内存
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月3日 下午3:35:32
* @version 1.0
 */
public class ReloadDomainUtil {
	private static final Logger logger = LoggerFactory.getLogger(RealtimeCrawlerController.class);
	public static void reloadIgnoreDomain(){
		// 加载 配置域名,访问google 云
		// WEB-INF/classes
//		String filepath = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
		URL url = RealtimeCrawlerController.class.getProtectionDomain().getCodeSource().getLocation();
		File jarFile = new File(url.getFile());
		String filepath = jarFile.getParent();
		
		filepath = StringUtils.replacePattern(filepath, "webapps.*", "");
		String fileName = "ignoreDomain";
		StringBuffer stringBuffer = new StringBuffer();
		try {
			Set<Pattern> newSet = new LinkedHashSet<Pattern>();
			InputStream in = new FileInputStream(new File(filepath + "/" + fileName));
			String line = "";
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			line = buffer.readLine();
			while (line != null) {
				stringBuffer.append(line);
				stringBuffer.append("\n");
				line = buffer.readLine();
			}
			buffer.close();
			in.close();
			JSONObject jsonObject = (JSONObject) JSONObject.parse(stringBuffer.toString());
			JSONArray jsonArray = (JSONArray) jsonObject.get("ignoreDomain");
			for (Object object : jsonArray) {
				Pattern p = Pattern.compile(object.toString());
				newSet.add(p);
			}

			// 温和替换 IgnoreDomainCache  Pattern 本身不好比较, 这里先采取暴力替换
			IgnoreDomainCache.getInstance().clear();
			// 加载
			IgnoreDomainCache.getInstance().addAll(newSet);
		} catch (FileNotFoundException e) {
			logger.error("ignoreDomain file is not exists", e);
		} catch (IOException e) {
			logger.error("ignoreDomain file io error", e);
		}catch (Exception e) {
			logger.error("ignoreDomain transfer error",e);
		}
	}
}
