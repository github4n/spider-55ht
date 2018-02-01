package com.haitao55.spider.crawler.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 功能：对要抓取的url值和属性的封装
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午4:27:53
 * @version 1.0
 */
public class Url {
	
	
	public Url(){}
	
	public Url(String value){
		this.value =value;
	}
	/**
	 * Url对象在系统中的唯一标识
	 */
	private String id;
	/**
	 * Url本身字符串值
	 */
	private String value;

	/**
	 * url 来源，是被哪个链接获取到的
	 */
	private String parentUrl;
	/**
	 * Url的类型
	 */
	private UrlType urlType;
	/**
	 * 状态
	 */
	private UrlStatus urlStatus;
	/**
	 * 任务的ID号
	 */
	private long taskId;
	/**
	 * Url所属任务的任务配置
	 */
	private Task task;
	/**
	 * 最后一次抓取时间
	 */
	private long lastCrawledTime;
	/**
	 * 最后一次抓取的爬虫机器IP地址
	 */
	private String lastCrawledIP;
	/**
	 * 最近连续抓取失败次数
	 */
	private int latelyFailedCount;
	/**
	 * 最近一次抓取失败,则记录errorCode
	 */
	private String latelyErrorCode;
	/**
	 * Url纳入爬虫系统的时间
	 */
	private long createTime;
	
	private int grade;
	
	/**
	 * 新urls集合
	 */
	private Set<Url> newUrls = new HashSet<Url>();
	/**
	 * 根据当前Url执行抓取并解析后，最终生产成果
	 */
	private OutputObject outputObject;
	/**
	 * 商品图片的数据集合
	 */
	private Map<String,List<Image>> images = new HashMap<String,List<Image>>();
	/**
	 * 一个Url执行过程中的运行时监控信息
	 */
	private RuntimeWatcher runtimeWatcher = new RuntimeWatcher();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getParentUrl() {
		return parentUrl;
	}

	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	public UrlType getUrlType() {
		return urlType;
	}

	public void setUrlType(UrlType urlType) {
		this.urlType = urlType;
	}

	public UrlStatus getUrlStatus() {
		return urlStatus;
	}

	public void setUrlStatus(UrlStatus urlStatus) {
		this.urlStatus = urlStatus;
	}

	public long getLastCrawledTime() {
		return lastCrawledTime;
	}

	public void setLastCrawledTime(long lastCrawledTime) {
		this.lastCrawledTime = lastCrawledTime;
	}

	public String getLastCrawledIP() {
		return lastCrawledIP;
	}

	public void setLastCrawledIP(String lastCrawledIP) {
		this.lastCrawledIP = lastCrawledIP;
	}

	public int getLatelyFailedCount() {
		return latelyFailedCount;
	}

	public void setLatelyFailedCount(int latelyFailedCount) {
		this.latelyFailedCount = latelyFailedCount;
	}

	public String getLatelyErrorCode() {
		return latelyErrorCode;
	}

	public void setLatelyErrorCode(String latelyErrorCode) {
		this.latelyErrorCode = latelyErrorCode;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	

	public OutputObject getOutputObject() {
		return outputObject;
	}

	public void setOutputObject(OutputObject outputObject) {
		this.outputObject = outputObject;
	}
	public Map<String, List<Image>> getImages() {
		return images;
	}
	public void setImages(Map<String, List<Image>> images) {
		this.images = images;
	}

	public RuntimeWatcher getRuntimeWatcher() {
		return runtimeWatcher;
	}

	public void setRuntimeWatcher(RuntimeWatcher runtimeWatcher) {
		this.runtimeWatcher = runtimeWatcher;
	}

	public Set<Url> getNewUrls() {
		return newUrls;
	}

	public void setNewUrls(Set<Url> newUrls) {
		this.newUrls = newUrls;
	}
	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	/**
	 * 判断两个Url对象是否相等，仅关注其value值
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		Url other = (Url) obj;
		return StringUtils.equals(this.value, other.value);
	}

	/**
	 * 计算Url实例的hashCode值，只关注其value值
	 */
	@Override
	public int hashCode() {
		if (this.value == null) {
			return 0;
		}

		return this.value.hashCode();
	}

	/**
	 * 将Url对象转换成字符串时，只关注其value值
	 */
	@Override
	public String toString() {
		return StringUtils.trimToNull(this.value);
	}
}