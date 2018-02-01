package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;
/**
 * 
  * @ClassName: ProdUrl
  * @Description: 爬取结果json的产品地址节点
  * @author songsong.xu
  * @date 2016年9月20日 下午2:05:20
  *
 */
public class ProdUrl implements Serializable{

	private static final long serialVersionUID = 8934008728263971346L;
	
	private String url;
	private long discovery_time;
	private String url_no;
	
	public ProdUrl(String url, long discovery_time, String url_no) {
		super();
		this.url = url;
		this.discovery_time = discovery_time;
		this.url_no = url_no;
	}
	
	public ProdUrl(String url){
		super();
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getDiscovery_time() {
		return discovery_time;
	}
	public void setDiscovery_time(long discovery_time) {
		this.discovery_time = discovery_time;
	}
	public String getUrl_no() {
		return url_no;
	}
	public void setUrl_no(String url_no) {
		this.url_no = url_no;
	}
	@Override
	public String toString() {
		return "ProdUrl [url=" + url + ", discovery_time=" + discovery_time
				+ ", url_no=" + url_no + "]";
	}
	
}
