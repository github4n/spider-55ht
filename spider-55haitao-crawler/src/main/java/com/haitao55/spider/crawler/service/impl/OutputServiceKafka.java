package com.haitao55.spider.crawler.service.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.commons.collections.set.SynchronizedSortedSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.kafka.SpiderKafkaProducer;
import com.haitao55.spider.common.kafka.SpiderKafkaResult;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.service.AbstractOutputService;
import com.haitao55.spider.crawler.service.OutputService;
import com.haitao55.spider.crawler.utils.Constants;

import main.java.com.UpYun;

/**
 * 
 * 结果数据输出通过消息服务转发出去
 * 
 */
public class OutputServiceKafka extends AbstractOutputService implements OutputService {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);
	private static final String UPYUN_DIR = "/prodimage/";
	private UpYun upyun;
	private SpiderKafkaProducer producer;
	private String topic;// kafka topic
	public UpYun getUpyun() {
		return upyun;
	}

	public void setUpyun(UpYun upyun) {
		this.upyun = upyun;
	}
	
	private String upyunAddress;
	
	public String getUpyunAddress() {
		return upyunAddress;
	}

	public void setUpyunAddress(String upyunAddress) {
		this.upyunAddress = upyunAddress;
	}

	public void write(OutputObject oo) {
		
		if (oo == null) {
			logger.error("OutputServiceKafka write[OutputObject] is null");
			return;
		}
		String msg = oo.convertItem2Message();
		if(StringUtils.isNotBlank(msg)){
			for(int i =0 ; i < 20; i++){
				try {
					SpiderKafkaResult result = producer.sendbyCallBack(topic, msg);
					if(result != null){
						logger.info("send a message offset :{}, message:{}", result.getOffset(), msg);
						break;
					}
				} catch (Exception e) {
					if(e instanceof ConnectException
							|| e instanceof NoRouteToHostException
							|| e instanceof SocketTimeoutException
							|| e instanceof UnknownHostException
							|| e instanceof SocketException){
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						continue;
					}
					logger.error("url:{},send message to topic {},exception {}", oo.getUrl().toString(), topic, e);
				}
				logger.info("send a message failed, url:{}", oo.getUrl().toString());
			}
			
		} else {
			logger.warn("url:{},send message to topic {},msg is null", oo.getUrl().toString(), topic);
		}
	}

	public SpiderKafkaProducer getProducer() {
		return producer;
	}

	public void setProducer(SpiderKafkaProducer producer) {
		this.producer = producer;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	@Override
	public boolean existInRepertory(Image image) {
		String path=StringUtils.substring(image.getRepertoryUrl(), StringUtils.indexOf(image.getRepertoryUrl(), UPYUN_DIR));
		Map<String, String> fileInfo = upyun.getFileInfo(path);
		if(null==fileInfo||fileInfo.isEmpty()){
			return false;//不存在
		}
		return true;//存在
	}
	@Override
	public void uploadImage(Image image,OutputObject oo) {
		upyun.setTimeout(60);
		upyun.setApiDomain(UpYun.ED_AUTO);
		String path=StringUtils.substring(image.getRepertoryUrl(), StringUtils.indexOf(image.getRepertoryUrl(), UPYUN_DIR));
		boolean  result = false;
		for(int i =0; i < 5; i++){
			try{
				result = this.upyun.writeFile(path, image.getData());
			}catch(Throwable e){
				e.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			if(result){
				image.setData(null);
				break;
			}
		}
	}

	@Override
	public void createImageRepertoryUrl(Image image,OutputObject oo) {
		if (image == null) {
			logger.error("image is null while fill image-url");
			return;
		}
		String repertoryImageUrl = (new StringBuilder())
				.append(this.getRepertoryImageAddressPrefix())
				.append(UPYUN_DIR)
				.append(SpiderStringUtil.upYunFileName(image.getOriginalUrl()))
				.append(this.getRepertoryImageAddressSuffix()).toString();
		image.setRepertoryUrl(repertoryImageUrl);
	}
	
	private String getRepertoryImageAddressPrefix() {
		return upyunAddress;
	}
	private String getRepertoryImageAddressSuffix() {
		return ".jpg";
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
	    String img = "https://images-na.ssl-images-amazon.com/images/I/31gtpU4zkEL.jpg";
	    byte[] result = Crawler.create().url(img).proxy(true).proxyAddress("47.88.25.82").proxyPort(3128).resultAsBytes();
	    UpYun upyun = new UpYun("st-prod", "shantao", "qDnnAdOTfpoWc");
	    upyun.setApiDomain(UpYun.ED_AUTO);
	    upyun.setTimeout(60);
	    String repertoryImageUrl = (new StringBuilder())
                .append("http://st-prod.b0.upaiyun.com")
                .append(UPYUN_DIR)
                .append(SpiderStringUtil.upYunFileName(img))
                .append(".jpg").toString();
	    System.out.println(repertoryImageUrl ); 
	    String path=StringUtils.substring(repertoryImageUrl, StringUtils.indexOf(repertoryImageUrl, UPYUN_DIR));
	    upyun.writeFile(path, result);
	}
}