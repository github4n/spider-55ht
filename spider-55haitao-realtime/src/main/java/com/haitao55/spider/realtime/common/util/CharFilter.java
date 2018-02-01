package com.haitao55.spider.realtime.common.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 
 * 功能：全局的请求/响应字符编码过滤器
 * 
 * @author Arthur.Liu
 * @time 2016年9月7日 下午2:01:30
 * @version 1.0
 */
public class CharFilter implements Filter {
	private static final String DEFAULT_REQUEST_CHARACTER = "UTF-8";
	private static final String DEFAULT_RESPONSE_CHARACTER = "UTF-8";

	@Override
	public void init(FilterConfig config) throws ServletException {
		// nothing
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		request.setCharacterEncoding(DEFAULT_REQUEST_CHARACTER);
		response.setCharacterEncoding(DEFAULT_RESPONSE_CHARACTER);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// nothing
	}
}