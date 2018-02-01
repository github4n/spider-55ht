package com.haitao55.spider.ui.view;

/**
 * 
 * 功能：任务管理模块展示层对象,在service层与TaskDO对象做转换
 * 
 * @author Arthur.Liu
 * @time 2016年7月21日 下午5:54:09
 * @version 1.0
 */
public class TaskView {
	private Long id;
	private String name;
	private String description;
	private String domain;
	private String initUrl;
	private String type;
	private String status;
	private Long period;
	private Long winStart;
	private Long winEnd;
	private Long createTime;
	private Long updateTime;
	private String master;
	private String updateOnly;
	private String config;
	private Integer ratio;
	private Integer weight;
	private String siteRegion;
	private String proxyRegionId;
	private String pretreatConfig;

	private PeriodView periodView;

	private TimeWindowView startTimeWindowView;
	private TimeWindowView endTimeWindowView;

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

	public PeriodView getPeriodView() {
		return periodView;
	}

	public void setPeriodView(PeriodView periodView) {
		this.periodView = periodView;
	}

	public TimeWindowView getStartTimeWindowView() {
		return startTimeWindowView;
	}

	public void setStartTimeWindowView(TimeWindowView startTimeWindowView) {
		this.startTimeWindowView = startTimeWindowView;
	}

	public TimeWindowView getEndTimeWindowView() {
		return endTimeWindowView;
	}

	public void setEndTimeWindowView(TimeWindowView endTimeWindowView) {
		this.endTimeWindowView = endTimeWindowView;
	}

	public static class PeriodView {
		private int hour;
		private int minute;

		public int getHour() {
			return hour;
		}

		public void setHour(int hour) {
			this.hour = hour;
		}

		public int getMinute() {
			return minute;
		}

		public void setMinute(int minute) {
			this.minute = minute;
		}
	}

	public static class TimeWindowView {
		private int year;
		private int month;
		private int day;
		private int hour;
		private int minute;

		public int getYear() {
			return year;
		}

		public void setYear(int year) {
			this.year = year;
		}

		public int getMonth() {
			return month;
		}

		public void setMonth(int month) {
			this.month = month;
		}

		public int getDay() {
			return day;
		}

		public void setDay(int day) {
			this.day = day;
		}

		public int getHour() {
			return hour;
		}

		public void setHour(int hour) {
			this.hour = hour;
		}

		public int getMinute() {
			return minute;
		}

		public void setMinute(int minute) {
			this.minute = minute;
		}
	}
}