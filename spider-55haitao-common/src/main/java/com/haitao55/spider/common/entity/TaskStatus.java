package com.haitao55.spider.common.entity;

/**
 * 
 * 功能：任务状态的枚举类
 * 
 * @author Arthur.Liu
 * @time 2016年7月22日 下午4:17:53
 * @version 1.0
 */
public enum TaskStatus {
	/**
	 * WAIT("W"):代表任务从运行状态转换成暂停状态，目前主要作用于更新程序时一键休眠功能和一键唤醒功能。
	 * MOTIONLESS("M"):代表任务从挂起状态转换成暂停状态，目前也是作用于更新程序时一键休眠和一键唤醒功能。
	 * 注：之所以挂起的任务也要暂停，是为了避免自动任务在更新程序时碰巧进入到时间窗口被修改为运行状态，造成不必要的数据丢失。
	 */
	INIT("I"), STARTING("S"), ACTIVE("A"), PAUSE("P"), HANGUP("H"), FINISH("F"), ERROR("E"), DISCARDING("D"), VOID("V"), WAIT("W"), MOTIONLESS("M");
	
	private String value;

	public String getValue() {
		return this.value;
	}

	private TaskStatus(String value) {
		this.value = value;
	}

	public static TaskStatus codeOf(String value) {
		for (TaskStatus ts : values()) {
			if (ts.getValue().equals(value)) {
				return ts;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return this.value;
	}
}