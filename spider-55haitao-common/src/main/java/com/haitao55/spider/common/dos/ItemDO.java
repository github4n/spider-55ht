package com.haitao55.spider.common.dos;

import java.io.Serializable;

/**
 * 
 * 功能：商品item实体类
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 上午11:56:48
 * @version 1.0
 */
public class ItemDO implements Serializable {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String id;
    
	private String  docId;
	
	private String value;
	
	private long createTime;
	
	private String urlMD5;
	
	private String status;
	
	private String[] urls;
	
	private String[] md5Urls;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getUrlMD5() {
		return urlMD5;
	}

	public void setUrlMD5(String urlMD5) {
		this.urlMD5 = urlMD5;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String[] getUrls() {
		return urls;
	}

	public void setUrls(String[] urls) {
		this.urls = urls;
	}

	public String[] getMd5Urls() {
		return md5Urls;
	}

	public void setMd5Urls(String[] md5Urls) {
		this.md5Urls = md5Urls;
	}
	
}