package com.haitao55.spider.ui.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 功能：辅助功能的Action，接收并处理如获取系统时间等辅助性请求
 * 
 * @author Arthur.Liu
 * @time 2016年6月19日 下午2:01:00
 * @version 1.0
 */
public class AssistantAction extends BaseAction {

	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AssistantAction.class);

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 获取当前系统时间
	 */
	public void getCurrentSystemTime() {
		HttpServletResponse response = getResponse();
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
			String currentDate = sdf.format(Calendar.getInstance().getTime());
			out.println(currentDate);
		} catch (IOException e) {
			logger.error("获取服务器系统当前时间失败", e);
		}
		out.close();
	}
}