package com.haitao55.spider.discover;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 
 * 功能：用来执行种子抓取的主启动类
 * 
 * @author Arthur.Liu
 * @time 2016年6月2日 下午1:58:07
 * @version 1.0
 */
public class DiscoverMain {

    static {// 这段代码先于main()方法执行，这是需要的，用于加载jar包外部路径上的日志配置文件
        PropertyConfigurator.configure("config/log4j.properties");
    }

    private static final Logger logger = LoggerFactory.getLogger("system");

    public static void main(String... args) {
        try {
            Thread.sleep(20 * 1000);// For debug only
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 1.检查参数个数是否正确
        if (ArrayUtils.isEmpty(args) || args.length != 2) {
            logger.error("args is invalid:: args:{}", args.toString());
            return;
        }

        String jobId = "";
        if (StringUtils.equals(args[0], "-DjobId")) {
            jobId = args[1];
            logger.info("Will crawle seeds by jobId::" + jobId);
        }

        logger.info("##########Seeds-Crawler Start::{}##########", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
        @SuppressWarnings("resource")
        final FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("conf/applicationContext-seeds-beans.xml");
        logger.info("Create and init spring context successfully::" + context.toString());

//        SeedsManager seedsManager = (SeedsManager) context.getBean("seedsManager");
//        try{
//            seedsManager.crawlSeeds(Long.parseLong(jobId));
//        }catch(Exception e){
//            logger.error("Error while crawling seeds,E::", e);
//        }
        logger.info("##########Seeds-Crawler End::{}##########", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
    }
}