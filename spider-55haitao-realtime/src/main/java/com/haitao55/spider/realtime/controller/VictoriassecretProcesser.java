package com.haitao55.spider.realtime.controller;

import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import com.haitao55.spider.common.dao.CurrentItemDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.utils.SpiderStringUtil;

public class VictoriassecretProcesser implements Callable<CrawlerJSONResult> {
	
	public static final String DOMAIN = "www.victoriassecret.com";
	
	private CurrentItemDAO currentItemDAOImpl;
	private String url;
	private Long taskId;
	
	public VictoriassecretProcesser(CurrentItemDAO currentItemDAOImpl,String url,Long taskId){
		this.currentItemDAOImpl = currentItemDAOImpl;
		this.url = url;
		this.taskId = taskId;
	}

	@Override
	public CrawlerJSONResult call() throws Exception {
		Pattern pattern = Pattern.compile(".*ProductID=(\\d+)[&]{0,1}.*");
	    Matcher matcher = pattern.matcher(url);
	    String productId = "";
	    if(matcher.find()){
            productId = matcher.group(1);
	    }
	    if(StringUtils.isNotBlank(productId)){
	    	String docid = SpiderStringUtil.md5Encode(DOMAIN+productId);
			ItemDO itemDO = currentItemDAOImpl.queryLastItem(taskId, docid);
			if(itemDO != null){
				return CrawlerJSONResult.buildFrom(itemDO.getValue());
			}
	    }
		return null;
	}
	

}
