package com.haitao55.spider.crawling.service.service;

import com.haitao55.spider.common.gson.bean.xiaohongshu.XiaoHongShu;

/** 
 * @Description: 小红书Service接口
 * @author: denghuan
 * @date: 2017年3月23日 下午4:34:25  
 */
public interface XiaoHongShuService {
	
	/**
	 * 爬起小红书帖子信息
	 * @param url
	 */
	public XiaoHongShu crawlItemInfo(String url);
}
