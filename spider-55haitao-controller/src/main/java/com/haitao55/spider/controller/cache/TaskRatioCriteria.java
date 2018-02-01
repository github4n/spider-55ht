package com.haitao55.spider.controller.cache;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 功能：任务执行速率控制的封装类
 * 
 * @author Arthur.Liu
 * @time 2016年7月29日 下午6:18:25
 * @version 1.0
 */
public class TaskRatioCriteria {
	/** 任务ID */
	private Long taskId;
	/** 计算urls数量时的权重 */
	private int weight;
	/** 单位时间内的上限 */
	private int limit;
	/** 单位时间内已经分发的urls数量 */
	private AtomicInteger currentValue;

	public TaskRatioCriteria(Long taskId, int weight,int limit) {
		this.taskId = taskId;
		this.weight = weight;
		this.limit = limit;
		this.currentValue = new AtomicInteger(0);
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public AtomicInteger getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(AtomicInteger currentValue) {
		this.currentValue = currentValue;
	}

	public void accumulateCurrentValue(int increment) {
		this.currentValue.addAndGet(increment);
	}

	public void cleanCurrentValue() {
		this.currentValue.set(0);
	}

	public boolean isStagedFull() {
		return this.currentValue.get() >= this.limit;
	}

	public int getRemainder() {
		return this.limit - this.currentValue.get();
	}
}