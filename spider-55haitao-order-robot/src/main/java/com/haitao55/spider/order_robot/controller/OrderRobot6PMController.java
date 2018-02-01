package com.haitao55.spider.order_robot.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * 功能：对www.6pm.com网站商品进行自动下单的Controller类
 * 
 * @author Arthur.Liu
 * @time 2016年10月10日 下午4:14:52
 * @version 1.0
 */
@Controller
@RequestMapping("/6pm")
public class OrderRobot6PMController {
	private static final Logger logger = LoggerFactory.getLogger(OrderRobot6PMController.class);

	private static final String SEPARATOR_SPACE = " ";
	private static final String PHANTOMJS_COMMAND = "phantomjs";
	private static final String PHANTOMJS_FILES_PATH = "/phantomjs/";
	private static final String PHANTOMJS_FILE_6PM_LOGIN = "order_robot_6pm_login.js";
	private static final String PHANTOMJS_FILE_6PM_ORDERING = "order_robot_6pm_ordering.js";

	/**
	 * 登录http://www.6pm.com网站
	 * 访问方式:http://localhost:8080/55haitao-order-robot/6pm/login.action
	 * 
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping("login")
	public @ResponseBody void login(HttpServletRequest request, HttpServletResponse response, Model model) {
		String url = "http://www.6pm.com/login";
		String username = "liushizhen@555haitao.com";
		String password = "123456";

		try {
			URL currentUrl = this.getClass().getProtectionDomain().getCodeSource().getLocation();
			String currentPath = currentUrl.getPath();
			File phantomjsFile = new File(currentPath + PHANTOMJS_FILES_PATH + PHANTOMJS_FILE_6PM_LOGIN);

			StringBuilder command = new StringBuilder(PHANTOMJS_COMMAND);
			command.append(SEPARATOR_SPACE).append(phantomjsFile.getCanonicalPath());
			command.append(SEPARATOR_SPACE).append("url").append(SEPARATOR_SPACE).append(url);
			command.append(SEPARATOR_SPACE).append("username").append(SEPARATOR_SPACE).append(username);
			command.append(SEPARATOR_SPACE).append("password").append(SEPARATOR_SPACE).append(password);

			logger.info("debug login 6pm, command: {}", command.toString());

			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(command.toString(), null, new File(currentPath));
			InputStream inputStream = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String runtimeExecuteResult = this.readRuntimeExecutedResult(bufferedReader);
			this.writeResponse(response, runtimeExecuteResult);

			IOUtils.closeQuietly(bufferedReader);
		} catch (IOException e) {
			logger.error("Error while running Runtime-Instance!");
			this.writeResponse(response, "Request::Error!");
		}
	}

	/**
	 * <p>
	 * 访问方式:
	 * </p>
	 * <p>
	 * http://localhost:8080/55haitao-order-robot/6pm/ordering.action?url=http:/
	 * /www.6pm.com/product/8787974/color/72&username=arthurliu&password=123456
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping("ordering")
	public @ResponseBody void ordering(HttpServletRequest request, HttpServletResponse response, Model model) {
		Arguments args = this.buildArguments(request);
		if (Objects.isNull(args) || !args.isValid()) {
			this.writeResponse(response, "RequestError::got no complete-arguments!");
			return;
		}

		try {
			URL currentUrl = this.getClass().getProtectionDomain().getCodeSource().getLocation();
			String currentPath = currentUrl.getPath();
			File phantomjsFile = new File(currentPath + PHANTOMJS_FILES_PATH + PHANTOMJS_FILE_6PM_ORDERING);

			StringBuilder command = new StringBuilder(PHANTOMJS_COMMAND);
			command.append(phantomjsFile.getCanonicalPath());
			command.append(args.assembleArgumentsString());

			logger.info("debug ordering 6pm, command: {}", command.toString());

			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(command.toString(), null, new File(currentPath));
			InputStream inputStream = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String runtimeExecuteResult = this.readRuntimeExecutedResult(bufferedReader);
			this.writeResponse(response, runtimeExecuteResult);

			IOUtils.closeQuietly(bufferedReader);
		} catch (IOException e) {
			logger.error("Error while running Runtime-Instance!");
			this.writeResponse(response, "Request::Error!");
		}
	}

	private String readRuntimeExecutedResult(BufferedReader bufferedReader) {
		StringBuffer result = new StringBuffer();

		try {
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			logger.error("Error while reading Runtime-Instance!");
		}

		return result.toString();
	}

	private void writeResponse(HttpServletResponse response, String responseBody) {
		try {
			response.setContentType("application/json;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(responseBody);
		} catch (IOException e) {
			logger.error("Error while writting HttpServletResponse::", e);
		}
	}

	private Arguments buildArguments(HttpServletRequest request) {
		Arguments args = this.buildArgumentsViaGet(request);

		if (!args.isValid()) {
			args = this.buildArgumentsViaPost(request);
		}

		if (!args.isValid()) {
			return null;
		} else {
			return args;
		}
	}

	private Arguments buildArgumentsViaGet(HttpServletRequest request) {
		Arguments args = new Arguments();

		args.setUrl(request.getParameter(Arguments.FIELD_NAME_URL));
		args.setUsername(request.getParameter(Arguments.FIELD_NAME_USERNAME));
		args.setPassword(request.getParameter(Arguments.FIELD_NAME_PASSWORD));

		return args;
	}

	private Arguments buildArgumentsViaPost(HttpServletRequest request) {
		Arguments args = new Arguments();

		args.setUrl((String) request.getAttribute(Arguments.FIELD_NAME_URL));
		args.setUsername((String) request.getAttribute(Arguments.FIELD_NAME_USERNAME));
		args.setPassword((String) request.getAttribute(Arguments.FIELD_NAME_PASSWORD));

		return args;
	}

	private class Arguments {
		private static final String FIELD_NAME_URL = "url";
		private static final String FIELD_NAME_USERNAME = "username";
		private static final String FIELD_NAME_PASSWORD = "password";

		private String url;
		private String username;
		private String password;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public boolean isValid() {
			return StringUtils.isNotBlank(this.url) && StringUtils.isNotBlank(this.username)
					&& StringUtils.isNotBlank(this.password);
		}

		public String assembleArgumentsString() {
			StringBuilder sb = new StringBuilder();

			sb.append(SEPARATOR_SPACE).append(Arguments.FIELD_NAME_URL);
			sb.append(SEPARATOR_SPACE).append(this.getUrl());
			sb.append(SEPARATOR_SPACE).append(Arguments.FIELD_NAME_USERNAME);
			sb.append(SEPARATOR_SPACE).append(this.getUsername());
			sb.append(SEPARATOR_SPACE).append(Arguments.FIELD_NAME_PASSWORD);
			sb.append(SEPARATOR_SPACE).append(this.getPassword());

			return sb.toString();
		}
	}
}