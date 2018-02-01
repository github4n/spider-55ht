package com.haitao55.spider.crawling.service.utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.core.model.Image;


public class HandleImagesUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_CRAWLING_SERVICE);

	public static void downloadImageData(Image image) {
		try {
			byte[] imageData = null;
				for(int i = 0 ; i < 20; i++){
					try {
						imageData = Crawler.create().retry(1).timeOut(60000)
								.url(image.getOriginalUrl())
								.method(HttpMethod.GET.getValue())
								.resultAsBytes();
						if (imageData != null) {
							break;
						}
						Thread.sleep(10000);
					} catch (IOException e) {
						// ConnectException | NoRouteToHostException |
						// SocketTimeoutException e
						if (e instanceof ConnectException
								|| e instanceof NoRouteToHostException
								|| e instanceof SocketTimeoutException
								|| e instanceof UnknownHostException
								|| e instanceof SocketException) {
							Thread.sleep(10000);
							continue;
						}
						throw e;
					}
				}
			image.setData(imageData);
		} catch (Throwable e) {
			logger.error("Error downloading image,image-original-url:{}, e:{}", image.getOriginalUrl(), e);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}
