/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HttpProxyAction.java 
 * @Prject: spider-55haitao-ui
 * @Package: com.haitao55.spider.ui.action 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年10月8日 上午11:50:12 
 * @version: V1.0   
 */
package com.haitao55.spider.ui.action;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.ui.service.ProxyService;
import com.haitao55.spider.ui.view.ProxyView;

/** 
 * @ClassName: HttpProxyAction 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年10月8日 上午11:50:12  
 */
@Controller
@RequestMapping("/httpproxy")
public class HttpProxyAction {

	private static int TIME_OUT=15*1000;//默认超时时间
	private static int RETRY_TIMES=3;//默认重试次数
	private static String REGION_ID="";//阿里云测试本来是为了使用代理，这里预留不使用代理的接口
	private String IP;//代理IP，当需要确定该代理IP可用的时候存储IP值
	private int PORT;//代理端口
	
	private static final Logger logger = LoggerFactory.getLogger(HttpProxyAction.class);
	//如果在页面配置了参数，下面静态代码块从配置文件里取值的代码将不起作用，可以不要，但对程序功能没有影响，先留着，等以后确定了再删除
	static {
		Properties pro = new Properties();
		try {
			pro.load(new FileInputStream(HttpProxyAction.class.getResource("/").getPath()+"httpproxy.properties"));
			TIME_OUT = Integer.parseInt(pro.getProperty("timeOut"));
			RETRY_TIMES = Integer.parseInt(pro.getProperty("retryTimes"));
		} catch (FileNotFoundException e) {
			logger.error("配置文件没找到", e);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Autowired
	private ProxyService proxyService;
	
	@RequestMapping("/gotoConfigPage")
	public String gotoConfigPage(Model model){
		model.addAttribute("proxyRegionList", proxyService.queryDistinctRegions());
		return "httpproxy/config";
	}
	/**
	 * 
	 * @Title: getAllProxies 
	 * @Description: 获取返回结果
	 * @param url
	 * @param proxyRegionId
	 * @param timeOut
	 * @param retry
	 * @param response
	 * @return
	 * @return: String
	 */
	@RequestMapping("/get")
	public void getAllProxies(String url, String proxyRegionId, String timeOut, String retry, HttpServletResponse response) {
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException pwException) {
			logger.error("get printWriter failed.", pwException);
		}
		if(!checkParams(proxyRegionId, null, null, timeOut, retry)){
			pw.print("timeOut or retry input error.");
			pw.flush();
			return;
		}
		Proxy proxy = null;
		String content = null;
		if(!"".equals(REGION_ID)){
			List<ProxyView> list = this.proxyService.queryByRegionId(REGION_ID);
			for(ProxyView p : list){
				proxy = new Proxy(){{
					setId(p.getId());
					setIp(p.getIp());
					setPort(p.getPort());
					setRegionId(p.getRegionId());
					setRegionName(p.getRegionName());
				}};
				try {
					content = HttpUtils.get(url, TIME_OUT, RETRY_TIMES, proxy);
					logger.info("代理ip:"+proxy.getIp()+"，端口："+proxy.getPort()+",地区："+proxy.getRegionName());
					pw.println("USE PROXY IP:  "+proxy.getIp()+"<br/>USE PROXY PORT:  "+proxy.getPort()+"<br/>");
					pw.print(content);
					pw.flush();
					return;
				} catch (Exception e) {
					continue;
				}
			}
		}
		if(proxy == null){//不使用代理
			content = HttpUtils.get(url, TIME_OUT, RETRY_TIMES, null);
			pw.println("Not Use Proxy.");
			pw.print(content);
			pw.flush();
			return;
		}
		pw.print("Not Found!");
		pw.flush();
	}
	
	/**
	 * 
	 * @Title: search 
	 * @Description: 根据区域找出数据库中所有可用的代理IP
	 * @param url
	 * @param proxyRegionId
	 * @param timeOut
	 * @param retry
	 * @param response
	 * @return
	 * @return: String
	 */
	@RequestMapping("/search")
	public void search(String url, String proxyRegionId, String timeOut, String retry, HttpServletResponse response){
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException pwException) {
			logger.error("get printWriter failed.", pwException);
		}
		if(!checkParams(proxyRegionId, null, null, timeOut, retry)){
			pw.print("timeOut or retry input error.");
			pw.flush();
			return;
		}
		Proxy proxy = null;
		String content = null;
		StringBuilder sb = new StringBuilder("");
		if(!"".equals(REGION_ID)){
			List<ProxyView> list = this.proxyService.queryByRegionId(REGION_ID);
			for(ProxyView p : list){
				proxy = new Proxy(){{
					setId(p.getId());
					setIp(p.getIp());
					setPort(p.getPort());
					setRegionId(p.getRegionId());
					setRegionName(p.getRegionName());
				}};
				try {
					content = HttpUtils.get(url, TIME_OUT, RETRY_TIMES, proxy);
					logger.info("代理ip:"+proxy.getIp()+"，端口："+proxy.getPort()+",国家："+proxy.getRegionName());
					sb.append("ip=").append(proxy.getIp()).append(",port=").append(proxy.getPort())
					.append(",RegionId=").append(proxy.getRegionId()).append("<br/>");
					pw.println("PROXY_IP:  "+proxy.getIp()+",&nbsp;&nbsp;&nbsp;PROXY_PORT:  "+proxy.getPort()
					+",&nbsp;&nbsp;&nbsp;REGION_ID:  "+proxy.getRegionId()
					+",&nbsp;&nbsp;&nbsp;REGION_NAME:  "+proxy.getRegionName()+"<br/>");
					pw.flush();
				} catch (Exception e) {
					continue;
				}
			}
		}
		if(StringUtils.isBlank(sb.toString())){
			pw.print("Not Found!");
			pw.flush();
		}
	}
	
	/**
	 * 
	 * @Title: comfirm 
	 * @Description: 进一步确认该代理是否可用
	 * @param url
	 * @param ip
	 * @param port
	 * @param timeOut
	 * @param retry
	 * @return
	 * @return: String
	 */
	@RequestMapping("/comfirm")
	public void comfirm(String url, String ip, String port, String timeOut, String retry, HttpServletResponse response){
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (IOException pwException) {
			logger.error("get printWriter failed.", pwException);
		}
		if(!checkParams(null, ip, port, timeOut, retry)){
			pw.print("params set error,please check again.");
			pw.flush();
			return;
		}
		Proxy proxy = new Proxy(){{
			setIp(IP);
			setPort(PORT);
		}};
		try {
			String content = HttpUtils.get(url, TIME_OUT, RETRY_TIMES, proxy);
			logger.info("代理ip:"+proxy.getIp()+"，端口："+proxy.getPort()+",地区："+proxy.getRegionName());
			pw.print(content);
			pw.flush();
		} catch (Exception e) {
			logger.error("获取网页内容失败！", e);
			pw.print("can not get any content. Because of "+e.getMessage());
			pw.flush();
		}
	}
	
	/**
	 * 
	 * @Title: checkParams 
	 * @Description: 参数合法性校验
	 * @param proxyRegionId
	 * @param ip
	 * @param port
	 * @param timeOut
	 * @param retry
	 * @return
	 * @return: boolean
	 */
	private boolean checkParams(String proxyRegionId, String ip, String port, String timeOut, String retry){
		try {
			if(proxyRegionId != null){
				REGION_ID = proxyRegionId.trim();
			}
			if(StringUtils.isNotBlank(ip)){
				IP = ip.trim();
			}
			if(StringUtils.isNotBlank(port)){
				PORT = Integer.parseInt(port.trim());
			}
			if(StringUtils.isNotBlank(timeOut)){
				TIME_OUT = Integer.parseInt(timeOut.trim());
			}
			if(StringUtils.isNotBlank(retry)){
				RETRY_TIMES = Integer.parseInt(retry.trim());
			}
			return true;
		} catch (NumberFormatException e) {
			logger.error("参数输入有误！", e);
			return false;
		}
	}
}
