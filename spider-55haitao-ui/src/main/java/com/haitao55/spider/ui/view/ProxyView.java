package com.haitao55.spider.ui.view;

/**
 * 
 * 功能：任务管理模块展示层对象,在service层与ProxyDO对象做转换
 * 
 * @author zhaoxl
 * @time 2016年8月26日 下午5:54:09
 * @version 1.0
 */
public class ProxyView {
	/**主键*/
	private Long id;
	/**区域编码*/
	private String regionId;
	/**区域名*/
	private String regionName;
	/**代理ip*/
	private String ip;
	/**端口*/
	private Integer port;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getRegionId() {
		return regionId;
	}
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "ProxyView [id=" + id + ", regionId=" + regionId + ", regionName=" + regionName + ", ip=" + ip
				+ ", port=" + port + "]";
	}
	public ProxyView() {
		super();
	}
	public ProxyView(Long id, String regionId, String regionName, String ip, Integer port) {
		super();
		this.id = id;
		this.regionId = regionId;
		this.regionName = regionName;
		this.ip = ip;
		this.port = port;
	}
	
	
}