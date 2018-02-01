package com.haitao55.spider.ui.action;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haitao55.spider.ui.common.util.PageBean;
import com.haitao55.spider.ui.service.UserService;
import com.haitao55.spider.ui.view.UserView;

/**
 * 
 * 功能：用户管理模块的Action类
 * 
 * @author Arthur.Liu
 * @time 2016年6月6日 下午6:06:49
 * @version 1.0
 */
@Controller
@RequestMapping("/user")
public class UserAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(UserAction.class);

	private static final String LOGIN_USER_NAME = "login_user_name";

	private UserService userService;

	/**
	 * 分页参数
	 */
	private int pageId;

	private String userName;

	/**
	 * 验证用户名是否存在
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/checkUserName")
	@ResponseBody
	public String checkUserName(HttpServletRequest request, HttpServletResponse response, Model model,
			UserView userView) throws Exception {
		response.setContentType("UTF-8");
		PrintWriter out = response.getWriter();
		if (null == getUser(request)) {
			out.println(0);
		} else {
			boolean b = userService.checkUserName(userName);
			if (b) {
				out.println(0);
			} else {
				out.println(1);
			}
		}
		out.flush();
		out.close();
		return null;
	}

	/**
	 * 添加用户
	 * 
	 * @return
	 */
	@RequestMapping("/addUser")
	public String addUser(HttpServletRequest request, HttpServletResponse response, Model model, UserView userView) {
		if (null == getUser(request)) {
			return "login";
		}

		try {
			userView.setEmails(userView.getUserName() + "@b5m.com");
			userService.addUser(userView, userView.getDescription());
		} catch (Exception e) {
			logger.error("UserAction中addUser方法操作数据库时出现异常", e);
		}
		return "redirect:getAllUser.action";
	}

	/**
	 * 查询所有权限
	 * 
	 * @return 返回到添加用户页面
	 */
	@RequestMapping("/getAllPrivilege")
	public String getAllPrivilege(HttpServletRequest request, HttpServletResponse response, Model model,
			UserView userView) {
		if (null == getUser(request)) {
			return "login";
		}
		try {
			// List<Privilege> privilegeList = userService.getAllPrivileges();
			model.addAttribute("privilegeList", null);
		} catch (Exception e) {
			logger.error("UserAction中getAllPrivilege方法操作数据库时出现异常", e);
		}
		return "user/user-add";
	}

	/**
	 * 查询所有用户
	 * 
	 * @return
	 */
	@RequestMapping("/getAllUsers")
	public String getAllUsers(HttpServletRequest request, HttpServletResponse response, Model model,
			UserView userView) {
		if (null == getUser(request)) {
			return "login";
		}
		PageBean pageBean = new PageBean();
		int pageSize = pageBean.getPageNum() + pageId;
		try {
			List<UserView> userList = userService.getUsersByPage(pageBean, "userid", true, pageId, pageSize);
			pageBean = pageBean.getPageBean();
			model.addAttribute("userList", userList);
			model.addAttribute("pageBean", pageBean);
		} catch (Exception e) {
			logger.error("UserAction中getAllUsers()方法操作数据库时出现异常", e);
		}
		return "user/user-home";
	}

	/**
	 * 
	 * 功能：用户登录
	 * 
	 * @return
	 */
	@RequestMapping("/userLogin")
	public String userLogin(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("userName") String userName, @RequestParam("password") String password) {
		boolean isAdmin = StringUtils.equals(userName, "admin") && StringUtils.equals(password, "crawler_admin_5566");
		boolean isOperator = StringUtils.equals(userName, "operator") && StringUtils.equals(password, "1qaz2wsxfdjfteyy");
		boolean isVisitor = StringUtils.equals(userName, "visitor") && StringUtils.equals(password, "17890214567");
		if (isAdmin || isOperator || isVisitor) {// 登录成功
			request.getSession().setAttribute(LOGIN_USER_NAME, userName);
			request.getSession().setMaxInactiveInterval(-1);// 设置session永不失效
			return "index";
		} else {// 登录失败
			model.addAttribute("NO", "loginError");
			return "login";
		}
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getPageId() {
		return pageId;
	}

	public void setPageId(int pageId) {
		this.pageId = pageId;
	}

	public UserService getUserService() {
		return userService;
	}
}