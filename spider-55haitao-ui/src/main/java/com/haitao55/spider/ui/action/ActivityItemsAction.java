package com.haitao55.spider.ui.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.TaskCache;
import com.haitao55.spider.crawler.common.cache.TaskDOCache;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.ui.cache.ActivityItemsIgnoreDomainCache;
import com.haitao55.spider.ui.cache.ActivityItemsUrlPatternCache;
import com.haitao55.spider.ui.service.ActivityItemsService;

/**
 * 活动商品查询controller
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月17日 上午10:10:26
* @version 1.0
 */
@Controller
@RequestMapping("/activity")
public class ActivityItemsAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(ActivityItemsAction.class);
	private static final String preffix = "://";
	private static final String suffix = "/";
//	private static final String pages = "pages";
	private static final String items = "items";
	private static final String path = "http://104.197.229.122:9090/spider-55haitao-ui/activity/queryActivityItems.action";
	
	
	
	@Autowired
	private ActivityItemsService activityItemsService;
	
	@RequestMapping("/queryActivityItems")
	@ResponseBody
	public void queryActivityItems(HttpServletRequest request,HttpServletResponse response,Model model) throws ClientProtocolException, HttpException, IOException{
		String url = StringUtils.EMPTY;
		//url 参数
		url = request.getParameter("url");
		if(StringUtils.isBlank(url)){
			url = (String) request.getAttribute("url");
		}
		
		if(StringUtils.isBlank(url)){
			logger.error("ActivityItemsAction queryActivityItems request url is empty");
			this.writeResponse(response, "Error:got no url");
			return;
		}
		
		/***判断是否需要转发请求***/
		ActivityItemsIgnoreDomainCache instance = ActivityItemsIgnoreDomainCache.getInstance();
		if(null == instance || instance.size() == 0){
			reloadIgnoreDomain(request, response);
		}
		if(null != instance && instance.size() > 0){
			boolean boo = googleCloudTranserPackage(url, request, response);
			if(!boo){
				googleCloudTransfer(url, response);
				return ;
			}
		}
		
		
		//获取url的所属域名
		String domain = activityLinkUrl(url);
		
		//taskId 用于定位种子库
		String taskId = StringUtils.EMPTY;
		
		//网站前缀
		String website_preffix = StringUtils.EMPTY;
		
		TaskDOCache taskDOCache = TaskDOCache.getInstance();
		if(null == taskDOCache || taskDOCache.size() == 0){
			logger.error("ActivityItemsAction queryActivityItems taskDOCache is empty");
			this.writeResponse(response, "Error:taskDOCache is empty");
			return;
		}
		
		//匹配 domain　所属任务，找到taskId
		for (Map.Entry<Long, TaskDO> entry: taskDOCache.entrySet()) {
			Long key = entry.getKey();
			TaskDO taskDo = entry.getValue();
			if(StringUtils.containsIgnoreCase(taskDo.getInitUrl(), domain)){
				taskId = String.valueOf(key);
				//String temp = StringUtils.substringAfter(taskDo.getInitUrl(), domain);
				//website_preffix = StringUtils.substringBefore(taskDo.getInitUrl(), temp);
				break;
			}
		}
		/**
		 * update by denghuan, 匹配域名
		 * date : 2017-6-17
		 */
		Pattern p = Pattern.compile("^(http://|https://)?[^//]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE); 
		Matcher matcher = p.matcher(url);  
		if(matcher.find()){
			website_preffix = matcher.group();
		}  
		
		if(StringUtils.isBlank(taskId)){
			logger.error("ActivityItemsAction queryActivityItems taskId is null");
			this.writeResponse(response, "Error:taskId is null");
			return;
		}
		
		/**
		 * 获取任务，防止个别网站需要使用代理ip，后面如果有任务需要使用特定代理ip，再扩展
		 * */
		Task task = this.getTask(taskId);
		
		if (Objects.isNull(task)) {
			this.writeResponse(response, "Error:got no task!");
			return;
		}
		
		//css
		JSONObject cssJsonObject = getCssByTaskId(taskId);
		
//		if(cssJsonObject.isEmpty()){
//			logger.error("ActivityItemsAction queryActivityItems css is empty");
//			this.writeResponse(response, "Error:css is empty");
//			return;
//		}
		
		JSONObject resultJsonObject = activityItemsService.getItemsAndUrls(cssJsonObject, taskId,url,website_preffix,task);
		if(null == resultJsonObject){
			logger.error("ActivityItemsAction queryActivityItems resultJsonObject is null");
			this.writeResponse(response, "Error:resultJsonObject is null");
			return ;
		}
		logger.info("ActivityItemsAction queryActivityItems resultJsonObject : {}",resultJsonObject.toJSONString());
		this.writeResponse(response, resultJsonObject.toJSONString());
	}
	
	/**
	 * 根据传递的url　进行所属域名获取
	 * @param url
	 * @return
	 */
	private String activityLinkUrl(String url) {
		return StringUtils.substringBetween(url, preffix, suffix);
	}

	private void writeResponse(HttpServletResponse response, String responseBody) {
		try {
			response.setContentType("text/html; charset=UTF-8");
			response.getWriter().print(responseBody);
		} catch (IOException e) {
			logger.error("Error while writting HttpServletResponse::", e);
		}
	}
	
	/**
	 * 根据taskId　获取ActivityItemCss.properties　配置文件中对应的css样式
	 * @param taskId
	 * @return
	 */
	private JSONObject getCssByTaskId(String taskId) {
		Properties pro = new Properties();
		JSONObject jsonObject = new JSONObject();
		try {
			URL url = ActivityItemsAction.class.getProtectionDomain().getCodeSource().getLocation();
			File jarFile = new File(url.getFile());
			String filepath = jarFile.getParent();
			
			filepath = StringUtils.replacePattern(filepath, "webapps.*", "");
			String fileName = "activityItemsCss";
			pro.load(new FileInputStream(new File(filepath + "/" + fileName)));
//			String pagesCss = pro.getProperty(taskId+"_pages");
			String itemsCss = pro.getProperty(taskId);
//			if(StringUtils.isNotBlank(pagesCss)){
//				jsonObject.put(pages, pagesCss);
//			}
			jsonObject.put(items, itemsCss);
		} catch (FileNotFoundException e) {
			logger.error("file not exist", e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	
	private Task getTask(String taskId) {
		Task rst = null;
		TaskCache taskCache = TaskCache.getInstance();
		if(null != taskCache && taskCache.size() > 0){
			rst = taskCache.get(Long.parseLong(taskId));
		}
		return rst;
	}
	
	
	/**
	 * 请求分发到google
	 * @param response 
	 * @param isSendMessage 
	 * @param timeout 
	 * @param requestFrom 
	 * @param url 
	 * @param model 
	 * @param request 
	 */
	private boolean googleCloudTranserPackage(String url,HttpServletRequest request, HttpServletResponse response) {
		/**url pattern*/
		ActivityItemsUrlPatternCache urlPatterInstance = ActivityItemsUrlPatternCache.getInstance();
		String subUrl = StringUtils.substringBetween(url, "//", "/");
		String md5Value=StringUtils.EMPTY;
		if(StringUtils.isNotBlank(subUrl)){
			md5Value =SpiderStringUtil.md5Encode(subUrl);
			Pattern pattern = urlPatterInstance.get(md5Value);
			if(null!=pattern){
//				googleCloudTransfer(url,response);
				return true;
			}
		}
		/**domain*/
		ActivityItemsIgnoreDomainCache instance = ActivityItemsIgnoreDomainCache.getInstance();
		if(null!=instance&&instance.size()==0){
			reloadIgnoreDomain(request,response);
		}
		if(null!=instance&&instance.size()>0){
			for (Pattern p : instance) {
				Matcher matcher = p.matcher(url);
				if(matcher.matches()){
					//ActivityItemsUrlPatternCache put value
					ActivityItemsUrlPatternCache.getInstance().put(md5Value, p);
					
//					googleCloudTransfer(url,response);
					return true;
				}
			}
		}
		return false;
	}
	
	public void reloadIgnoreDomain(HttpServletRequest request, HttpServletResponse response) {
		// 加载 配置域名,访问google 云
		// WEB-INF/classes
//		String filepath = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
		URL url = ActivityItemsAction.class.getProtectionDomain().getCodeSource().getLocation();
		File jarFile = new File(url.getFile());
		String filepath = jarFile.getParent();
		
		filepath = StringUtils.replacePattern(filepath, "webapps.*", "");
		String fileName = "activityItemIgnoreDomain";
		StringBuffer stringBuffer = new StringBuffer();
		try {
			Set<Pattern> newSet = new LinkedHashSet<Pattern>();
			InputStream in = new FileInputStream(new File(filepath + "/" + fileName));
			String line = "";
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			line = buffer.readLine();
			while (line != null) {
				stringBuffer.append(line);
				stringBuffer.append("\n");
				line = buffer.readLine();
			}
			buffer.close();
			in.close();
			JSONObject jsonObject = (JSONObject) JSONObject.parse(stringBuffer.toString());
			JSONArray jsonArray = (JSONArray) jsonObject.get("activityItemIgnoreDomain");
			for (Object object : jsonArray) {
				Pattern p = Pattern.compile(object.toString());
				newSet.add(p);
			}

			// 温和替换 IgnoreDomainCache  Pattern 本身不好比较, 这里先采取暴力替换
			ActivityItemsIgnoreDomainCache.getInstance().clear();
			// 加载
			ActivityItemsIgnoreDomainCache.getInstance().addAll(newSet);
		} catch (FileNotFoundException e) {
			logger.error("activityItemIgnoreDomain file is not exists", e);
		} catch (IOException e) {
			logger.error("activityItemIgnoreDomain file io error", e);
		}catch (Exception e) {
			logger.error("activityItemIgnoreDomain transfer error",e);
		}
	}
	
	/**
	 * 调用 google cloud realtime 接口
	 * 
	 * @param url
	 * @param requestFrom
	 * @param timeout
	 * @param isSendMessage
	 * @param response
	 */
	private void googleCloudTransfer(String url,
			HttpServletResponse response) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("url", url);
		String jsonResult = HttpClientUtil.post(path, params,60000);

		// 匹配到google云地址,返回处理结果
		this.writeResponse(response, jsonResult);
	}
}
