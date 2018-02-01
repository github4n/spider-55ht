package com.haitao55.spider.ui.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.haitao55.spider.ui.view.UserView;

/**
 * 
 * 功能：爬虫WebUI工程的基础Action，其他各个模块的Action都继承这个BaseAction
 * 
 * @author Arthur.Liu
 * @time 2016年7月2日 下午4:31:12
 * @version 1.0
 */
public class BaseAction {

	protected UserView getUser(HttpServletRequest request) {
		return (UserView) request.getSession().getAttribute("loginUser");
	}

	protected HttpServletRequest getRequest() {
		return ServletActionContext.getRequest();
	}

	protected HttpServletResponse getResponse() {
		return ServletActionContext.getResponse();
	}
	
	protected int getPage(HttpServletRequest request) {
		String param = request.getParameter("page");
		int page=null==param?1:Integer.parseInt(param);//默认第一页
		return page;
	}
	protected int getPageSize(HttpServletRequest request) {
		String param = request.getParameter("rows");
		int rows=null==param?50:Integer.parseInt(param);//默认50条
		return rows;
	}
}