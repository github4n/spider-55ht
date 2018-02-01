package org.spider.haitao55.realtime.service.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * 
  * @ClassName: HelloServlet
  * @Description: test
  * @author songsong.xu
  * @date 2017年1月11日 下午8:37:38
  *
 */
public class HelloServlet extends HttpServlet{

	private static final long serialVersionUID = 9136981230093758506L;
	private String greeting = "Hello World";

	public HelloServlet() {
	}

	public HelloServlet(String greeting) {
		this.greeting = greeting;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("<h1>" + greeting + "</h1>");
		response.getWriter().println(
				"session=" + request.getSession(true).getId());
	}
}
