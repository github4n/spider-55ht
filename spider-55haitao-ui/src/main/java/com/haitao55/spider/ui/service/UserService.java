package com.haitao55.spider.ui.service;

import java.util.List;

import com.haitao55.spider.ui.common.util.PageBean;
import com.haitao55.spider.ui.view.UserView;

/**
 * 
 * 功能：用户管理模块的Service接口
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 下午5:02:30
 * @version 1.0
 */
public interface UserService {

	public UserView getUserByName(String userName);

	public List<UserView> getUsersByPage(PageBean pageBean, String orderByField, boolean isAscending, int fromIdx,
			int toIdx) throws Exception;

	public List<UserView> getAllUsers();

	public boolean addUser(UserView user, String description) throws Exception;

	public boolean checkUserName(String userName) throws Exception;
}