package com.haitao55.spider.crawler.core.model;

/**
 * 
 * 功能：图片数据封装类
 * 
 * @author Arthur.Liu
 * @time 2016年8月24日 下午5:55:39
 * @version 1.0
 */
public class Image {
	private String originalUrl;
	private String repertoryUrl;
	private byte[] data;

	public Image(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public String getRepertoryUrl() {
		return repertoryUrl;
	}

	public void setRepertoryUrl(String repertoryUrl) {
		this.repertoryUrl = repertoryUrl;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

  @Override
  public String toString() {
    return "Image [originalUrl=" + originalUrl + ", repertoryUrl=" + repertoryUrl + "]";
  }
	
	
}