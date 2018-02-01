package com.haitao55.spider.ui.service.impl;

import java.util.List;

import com.haitao55.spider.ui.common.util.PageBean;
import com.haitao55.spider.ui.service.UserService;
import com.haitao55.spider.ui.view.UserView;

/**
 * 
 * 功能：用户管理模块的Service接口实现类
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 下午5:02:43
 * @version 1.0
 */
public class UserServiceImpl implements UserService {

	@Override
	public UserView getUserByName(String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserView> getUsersByPage(PageBean pageBean, String orderByField, boolean isAscending, int fromIdx,
			int toIdx) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserView> getAllUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addUser(UserView user, String description) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkUserName(String userName) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}