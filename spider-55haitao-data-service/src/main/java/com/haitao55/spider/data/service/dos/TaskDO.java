package com.haitao55.spider.data.service.dos;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * 功能：任务模块的数据库表映射类,在service层与TaskView对象做转换
 * 
 * @author Arthur.Liu
 * @time 2016年7月21日 下午5:49:36
 * @version 1.0
 */
@Table(name="task")
public class TaskDO {
	@Id
	private Long id;
	@Column
	private String name;
	@Column
	private String description;
	@Column
	private String domain;
	@Column
	private String initUrl;
	@Column
	private String type;
	@Column
	private String status;
	@Column
	private Long period;
	@Column
	private Long winStart;
	@Column
	private Long winEnd;
	@Column
	private Long createTime;
	@Column
	private Long updateTime;
	@Column
	private String master;
	@Column
	private String updateOnly;
	@Column
	private String config;
	@Column
	private Integer ratio;
	@Column
	private Integer weight;
	@Column
	private String siteRegion;
	@Column
	private String proxyRegionId;
	@Column
	private String pretreatConfig;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getInitUrl() {
		return initUrl;
	}
	public void setInitUrl(String initUrl) {
		this.initUrl = initUrl;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getPeriod() {
		return period;
	}
	public void setPeriod(Long period) {
		this.period = period;
	}
	public Long getWinStart() {
		return winStart;
	}
	public void setWinStart(Long winStart) {
		this.winStart = winStart;
	}
	public Long getWinEnd() {
		return winEnd;
	}
	public void setWinEnd(Long winEnd) {
		this.winEnd = winEnd;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public Long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}
	public String getMaster() {
		return master;
	}
	public void setMaster(String master) {
		this.master = master;
	}
	public String getUpdateOnly() {
		return updateOnly;
	}
	public void setUpdateOnly(String updateOnly) {
		this.updateOnly = updateOnly;
	}
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
	}
	public Integer getRatio() {
		return ratio;
	}
	public void setRatio(Integer ratio) {
		this.ratio = ratio;
	}
	public Integer getWeight() {
		return weight;
	}
	public void setWeight(Integer weight) {
		this.weight = weight;
	}
	public String getSiteRegion() {
		return siteRegion;
	}
	public void setSiteRegion(String siteRegion) {
		this.siteRegion = siteRegion;
	}
	public String getProxyRegionId() {
		return proxyRegionId;
	}
	public void setProxyRegionId(String proxyRegionId) {
		this.proxyRegionId = proxyRegionId;
	}
	public String getPretreatConfig() {
		return pretreatConfig;
	}
	public void setPretreatConfig(String pretreatConfig) {
		this.pretreatConfig = pretreatConfig;
	}
	
}