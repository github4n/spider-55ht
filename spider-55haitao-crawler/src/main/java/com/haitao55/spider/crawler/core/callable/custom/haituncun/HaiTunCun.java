package com.haitao55.spider.crawler.core.callable.custom.haituncun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.service.impl.OutputServiceKafka;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.SpringUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年2月27日 下午4:43:37  
 */
public class HaiTunCun extends AbstractSelect {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);
	private static OutputServiceKafka outputServiceKafka;

	static{
		outputServiceKafka = SpringUtils.getBean("outputServiceKafka");
	}
	
	@Override
	public void invoke(Context context) throws Exception {
		String fileName = HaiTunCunUtils.downloadFileAndReturnFilePath(context.getCurrentUrl());
		System.out.println(fileName);
		HaiTunCunUtils.readCsvFileAndSendMsg(fileName, outputServiceKafka, context.getUrl().getTaskId());
	}
	
	public static void main(String[] args) throws Exception {
		HaiTunCun htc = new HaiTunCun();
		Context context = new Context();
		context.setCurrentUrl("http://23.91.97.48:8001/o_exp/index?user=emarsys&pwd=30318e6405fb67b52692f8c2c81fcbd3");
		htc.invoke(context);
	}
}
