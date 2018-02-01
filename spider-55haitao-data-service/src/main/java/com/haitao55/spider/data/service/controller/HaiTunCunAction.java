package com.haitao55.spider.data.service.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.data.service.service.HaituncunService;
import com.haitao55.spider.data.service.utils.Constants;
import com.haitao55.spider.data.service.utils.HttpUtils;
/** 
 * @Description: 海豚村接口
 * @author: zhoushuo
 * @date: 2017年3月6日 下午6:09:05  
 */
@Controller
@RequestMapping("/haituncun")
public class HaiTunCunAction {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_MONITOR);
	
	private int page = 1;
	private int pageSize = 100;
	
	
	@Autowired
	private HaituncunService haituncunService;

	@RequestMapping(method=RequestMethod.POST, value="/getData.action")
	public void getData(String user, String password,HttpServletRequest request, HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");//设置字符流编码格式
		String clientIp = "";
		try {
			clientIp = HttpUtils.getIpAddr(request);
		} catch (Exception e) {
			clientIp = "unknow";
			logger.error("无法从Request中获取客户端ip地址!");
		}
		logger.info("客户端{}发起了请求!", clientIp);
		BufferedWriter bw = null;
		try {
			PrintWriter pw = response.getWriter();
			bw = new BufferedWriter(pw);
			
			//1.检测是否拥有获取海豚村数据的权限
			if(!(Constants.CHECK_WORDS.equals(user) && SpiderStringUtil.md5Encode(Constants.CHECK_WORDS).equals(password))){
				response.setContentType("text/html;charset=UTF-8");//通知浏览器用那种UTF-8编码进行解析
				pw.println("用户名或密码错误");
				logger.error("用户名或密码错误,user:{},password:{}",user, password);
				return;
			}
			
			//2.设置response为文件下载类型
			response.setHeader("Content-Disposition", "attachment;filename=haituncun.txt");
			response.setContentType("multipart/form-data");
			
			//3.向客户端传送数据
			long sum = haituncunService.writeAllDataToClient(bw, page, pageSize);
			
			logger.info("=============================已经成功响应{}的请求，并完成推送，商品数量为：{}=========================", clientIp, sum);
		} catch (IOException e) {
			logger.error("IO异常导致海豚村获取数据失败！");
		} finally {
			//4.关闭流
			IOUtils.closeQuietly(bw);
		}
	}
	public static void main(String[] args) {
		String str = SpiderStringUtil.md5Encode(Constants.CHECK_WORDS);//32dc01246faccb7f5b3cad5016dd5033
		System.out.println(str);
	}
}