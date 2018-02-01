package org.spider.haitao55.realtime.service.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class RealTimeServiceConfig {
	
	private static final String propFileName = System.getProperty("user.dir")+ "/config/realtime-service.properties";
	private static final Properties prop = new Properties();
	
	static{
		try {
			prop.load(new FileInputStream(propFileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static final String JETTY_PORT = prop.getProperty("jetty.port");
	public static final String CHROMEDRIVER_COUNT = prop.getProperty("chromedriver.count");
    
    public static void main(String[] args) {
		System.out.println(JETTY_PORT);
	}
    
}