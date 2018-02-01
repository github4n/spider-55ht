package com.haitao55.spider.crawler.utils;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.haitao55.spider.crawler.config.AutoRefreshPropertyConfigurer;

/**
 * 
 * 功能：spring 实用类<br>
 * 可以通过该类取得spring中的bean以及属性
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午7:23:45
 * @version 1.0
 */
public class SpringUtils {

    private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

    private static volatile ApplicationContext context;

    public static ApplicationContext getContext() {
		return context;
	}

	public static void setContext(ApplicationContext context) {
		SpringUtils.context = context;
	}

	public static void initFileSystemXmlApplicationContext(String configLocation) {
        context = new FileSystemXmlApplicationContext(configLocation);
    }

    /**
     * 从spring中获取bean对象<br>
     * 如果spring初始化不成功或者没有该对象，则返回NULL
     * 
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        try {
            checkSpring();
            return (T) context.getBean(name);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 从AutoRefreshPropertyConfigurer所关联的property文件中获取property<br>
     * 如果spring初始化不成功或者没有该property，则返回空字符串
     * 
     * @param key
     * @return
     */
    public static String getProperty(String name) {
        try {
            checkSpring();
            AutoRefreshPropertyConfigurer configurer = context.getBean(AutoRefreshPropertyConfigurer.class);
            if (configurer == null) {
                logger.warn("AutoRefreshPropertyConfigurer not found from context");
                return StringUtils.EMPTY;
            }
            return configurer.getContextProperty(name);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return StringUtils.EMPTY;
    }

    /**
     * 从AutoRefreshPropertyConfigurer所关联的property文件中获取property<br>
     * 如果spring初始化不成功或者没有该对象，则返回空map
     * 
     * @param key
     * @return
     */
    public static Map<String, String> getAllProperties() {
        try {
            checkSpring();
            AutoRefreshPropertyConfigurer configurer = context.getBean(AutoRefreshPropertyConfigurer.class);
            if (configurer == null) {
                logger.warn("AutoRefreshPropertyConfigurer not found from context");
                return Collections.emptyMap();
            }
            return configurer.getAllContextProperties();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyMap();
    }

    private static void checkSpring() {
        if (context != null) {
            return;
        }
        // 最多休息10秒,等待spring初始化完成
        int retry = 50;
        for (int i = 0; i < retry; i++) {
            if (context != null) {
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignore) {
            }
        }
    }
}