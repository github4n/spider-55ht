package com.haitao55.spider.crawler.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.haitao55.spider.common.thrift.UrlStatusModel;

/**
 * 
 * 功能：Url状态 <br/>
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午4:26:26
 * @version 1.0
 */
public enum UrlStatus {
	/** 从互联网上刚被发现到 */
	NEWCOME("NEWCOME"),
	/** 从底层controller中刚传递上来,等待执行抓取 */
	CRAWLING("CRAWLING"),
	/** 已经执行过抓取操作,执行抓取成功 */
	CRAWLED_OK("CRAWLED_OK"),
	/** 已经执行过抓取操作,执行抓取错误 */
	CRAWLED_ERROR("CRAWLED_ERROR"),
	/** 从底层controller中刚传递上来,等待执行删除 */
	DELETING("DELETING"),
	/** 已经执行过删除操作,执行删除成功 */
	DELETED_OK("DELETED_OK"),
	/** 已经执行过删除操作,执行删除错误 */
	DELETED_ERROR("DELETED_ERROR"),
	/** 未知状态 */
	UNKNOWN("UNKNOWN");

	private String value;

	private UrlStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	// 用map增强性能
	private static final Map<String, UrlStatus> map = new HashMap<String, UrlStatus>();
	static {
		for (UrlStatus status : values()) {
			map.put(status.getValue().toLowerCase(), status);
		}
	}

	public static UrlStatus codeOf(String docStatus) {
		return map.get(docStatus == null ? null : docStatus.toLowerCase());
	}

	public static Collection<String> codes() {// 这个list就不全局缓存了，避免外部得到list后改变其中元素
		List<String> list = new ArrayList<String>();
		for (UrlStatus status : values()) {
			list.add(status.getValue());
		}
		return list;
	}

	public static UrlStatusModel convert2UrlStatusModel(UrlStatus urlStatus) {
		switch (urlStatus) {
		case NEWCOME:
			return UrlStatusModel.NEWCOME;
		case CRAWLING:
			return UrlStatusModel.CRAWLING;
		case CRAWLED_OK:
			return UrlStatusModel.CRAWLED_OK;
		case CRAWLED_ERROR:
			return UrlStatusModel.CRAWLED_ERROR;
		case DELETING:
			return UrlStatusModel.DELETING;
		case DELETED_OK:
			return UrlStatusModel.DELETED_OK;
		case DELETED_ERROR:
			return UrlStatusModel.DELETED_ERROR;
		case UNKNOWN:
			return UrlStatusModel.UNKNOWN;
		default:
			return null;
		}
	}

	public static UrlStatus convertFromUrlStatusModel(UrlStatusModel urlStatusModel) {
		switch (urlStatusModel) {
		case NEWCOME:
			return UrlStatus.NEWCOME;
		case CRAWLING:
			return UrlStatus.CRAWLING;
		case CRAWLED_OK:
			return UrlStatus.CRAWLED_OK;
		case CRAWLED_ERROR:
			return UrlStatus.CRAWLED_ERROR;
		case DELETING:
			return UrlStatus.DELETING;
		case DELETED_OK:
			return UrlStatus.DELETED_OK;
		case DELETED_ERROR:
			return UrlStatus.DELETED_ERROR;
		case UNKNOWN:
			return UrlStatus.UNKNOWN;
		default:
			return null;
		}
	}
}