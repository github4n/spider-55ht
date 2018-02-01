package com.haitao55.spider.ui.view;

import java.util.List;

/**
 * 
 * 功能：为页面展示用的数据对象
 * 
 * @author Arthur.Liu
 * @time 2016年7月2日 下午4:30:28
 * @version 1.0
 */
public class UserView {

	private Integer userId = -1;

	private String userName;

	private String password;

	private String emails;

	private List<String> descriptionList;

	private String description;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public List<String> getDescriptionList() {
		return descriptionList;
	}

	public void setDescriptionList(List<String> descriptionList) {
		this.descriptionList = descriptionList;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}