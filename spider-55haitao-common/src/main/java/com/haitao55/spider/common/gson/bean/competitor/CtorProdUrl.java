package com.haitao55.spider.common.gson.bean.competitor;

import java.io.Serializable;
/**
 * 
  * @ClassName: CtorProdUrl
  * @Description: 爬取结果json的产品地址节点
  * @author 赵新落
  * @date 2017年2月21日 下午2:05:20
  *
 */
public class CtorProdUrl implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7767725860171871751L;
	/**帖子链接*/
	private String url;
	/**网址链接，指从当前链接迭代出 url*/
	private String parentUrl;
	/**购买链接*/
	private String buyUrl;
	/**发现时间*/
	private String discovery_time;
	/**更新时间*/
	private String update_time;
	/**结束时间*/
	private String end_time;
	
	public CtorProdUrl() {
		super();
	}
	public CtorProdUrl(String url, String parentUrl, String buyUrl, String discovery_time, String update_time,
			String end_time) {
		super();
		this.url = url;
		this.parentUrl = parentUrl;
		this.buyUrl = buyUrl;
		this.discovery_time = discovery_time;
		this.update_time = update_time;
		this.end_time = end_time;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getParentUrl() {
		return parentUrl;
	}
	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}
	public String getBuyUrl() {
		return buyUrl;
	}
	public void setBuyUrl(String buyUrl) {
		this.buyUrl = buyUrl;
	}
	public String getDiscovery_time() {
		return discovery_time;
	}
	public void setDiscovery_time(String discovery_time) {
		this.discovery_time = discovery_time;
	}
	public String getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	@Override
	public String toString() {
		return "CtorProdUrl [url=" + url + ", parentUrl=" + parentUrl + ", buyUrl=" + buyUrl + ", discovery_time="
				+ discovery_time + ", update_time=" + update_time + ", end_time=" + end_time + "]";
	}
	
}
