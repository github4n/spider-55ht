package com.haitao55.spider.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.cache.OnlineWebSiteCache;

/**
 * 获取上架网站域名
 * @author denghuan
 *
 */
public class PropertiesUtils {

	public static void loadProperties(){
		Properties pps = new Properties();
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream("config/onlineWebSite.properties"));
			pps.load(in);
			String value = pps.getProperty(Constants.ONLINE_WEB_SITE_KEY);
			if (StringUtils.isNotBlank(value)) {
				String[] doMains = StringUtils.split(value, ",");
				for (String doMain : doMains) {
					if (StringUtils.isNotBlank(doMain)) {
						OnlineWebSiteCache.getInstance().put(doMain, doMain);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
