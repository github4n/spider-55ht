package com.haitao55.spider.crawler.core.model;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 
 * 功能：爬虫程序在爬取过程中需要使用的配置信息，例如解析及抓取规则
 * 
 * @author Arthur.Liu
 * @time 2016年8月2日 下午2:55:06
 * @version 1.0
 */
public class Task {
	/**
	 * 任务ID
	 */
	private Long taskId;
	/**
	 * 任务名称
	 */
	private String taskName;
	/**
	 * 任务规则配置，一般一个Rule实例对应一种格式的Url的处理规则
	 */
	private List<Rule> rules;
	/**
	 * 任务处理的目标网站所在区域
	 */
	private String siteRegion;
	/**
	 * 任务执行抓取时,需要使用的代理IP所在的区域
	 */
	private String proxyRegionId;

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}