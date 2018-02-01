package com.haitao55.spider.ui.view;

import javax.persistence.Entity;

/**
 * 
 * 功能：商品管理模块的展示层类
 * 
 * @author Arthur.Liu
 * @time 2016年11月5日 下午9:27:08
 * @version 1.0
 */
@Entity
public class ItemsView {
	private Long taskId;
	private String taskName;
	private Integer allUrlsCount;
	private Integer itemUrlsCount;
	private Integer allItemsCount;
	private Integer onlineItemsCount;

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

	public Integer getAllUrlsCount() {
		return allUrlsCount;
	}

	public void setAllUrlsCount(Integer allUrlsCount) {
		this.allUrlsCount = allUrlsCount;
	}

	public Integer getItemUrlsCount() {
		return itemUrlsCount;
	}

	public void setItemUrlsCount(Integer itemUrlsCount) {
		this.itemUrlsCount = itemUrlsCount;
	}

	public Integer getAllItemsCount() {
		return allItemsCount;
	}

	public void setAllItemsCount(Integer allItemsCount) {
		this.allItemsCount = allItemsCount;
	}

	public Integer getOnlineItemsCount() {
		return onlineItemsCount;
	}

	public void setOnlineItemsCount(Integer onlineItemsCount) {
		this.onlineItemsCount = onlineItemsCount;
	}
}