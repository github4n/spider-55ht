package com.haitao55.spider.ui.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.haitao55.spider.common.entity.UrlsType;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.TaskCache;
import com.haitao55.spider.crawler.core.model.Rule;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.ui.service.TaskService;

/**
 * 
 * 功能：工具功能的Controller
 * 
 * @author Arthur.Liu
 * @time 2016年10月29日 下午10:46:46
 * @version 1.0
 */
@Controller
@RequestMapping("/tools")
public class ToolsAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(ToolsAction.class);

	private static final String IS_SEND_MESSAGE_YES = "Y";

	private static final String ONE_KEY_CRAWLE_ATTR_URL = "url";
	private static final String ONE_KEY_CRAWLE_ATTR_REQUEST_FROM = "request_from";
	private static final String ONE_KEY_CRAWLE_ATTR_TIMEOUT = "timeout";
	private static final String ONE_KEY_CRAWLE_ATTR_IS_SEND_MESSAGE = "is_send_message";

	private static final String ONE_KEY_CRAWLE_ATTR_REQUEST_FROM_VALUE = "ui";
	private static final String ONE_KEY_CRAWLE_ATTR_TIMEOUT_VALUE = "60000";

	@Value("#{configProperties['realtime.address']}")
	private String realtimeAddress;

	@Autowired
	private TaskService taskService;

	@RequestMapping("/gotoOneKeyCrawle")
	public String gotoOneKeyCrawle(HttpServletRequest request, HttpServletResponse response, Model model) {
		return "/tools/one-key-crawle";
	}

	@RequestMapping("/oneKeyCrawle")
	public String oneKeyCrawle(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("targetUrl") String targetUrl, @RequestParam("isSendMessage") String isSendMessage) {
		StringBuilder result = new StringBuilder();
		result.append("TargetUrl : ").append(targetUrl).append("<br />");
		result.append("If Online : ").append(StringUtils.equals(isSendMessage, "Y") ? "YES" : "NO").append("<br />");
		result.append("<br />");

		try {
			String rst = this.invokeRealtimeCrawler(targetUrl, isSendMessage);
			result.append(rst);
			model.addAttribute("oneKeyResult", result.toString());

			if (IS_SEND_MESSAGE_YES.equals(isSendMessage)) {
				this.importUrl(targetUrl);
			}
		} catch (Exception e) {
			logger.error("Error while calling oneKeyCrawle(), ", e);
		}

		return "/tools/one-key-crawle";
	}

	@RequestMapping("/gotoMd5Encode")
	public String gotoMd5Encode(HttpServletRequest request, HttpServletResponse response, Model model) {
		return "/tools/md5-encode";
	}

	@RequestMapping("/md5Encode")
	public String md5Encode(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("sourceString") String sourceString) {
		String md5EncodeResult = SpiderStringUtil.md5Encode(sourceString);

		model.addAttribute("sourceString", sourceString);
		model.addAttribute("md5EncodeResult", md5EncodeResult);

		return "/tools/md5-encode";
	}

	private String invokeRealtimeCrawler(String targetUrl, String isSendMessage) throws Exception {
		String result = "";
		CloseableHttpResponse httpResponse = null;

		@SuppressWarnings("serial")
		List<NameValuePair> attrs = new ArrayList<NameValuePair>() {
			{
				add(new BasicNameValuePair(ONE_KEY_CRAWLE_ATTR_URL, targetUrl));
				add(new BasicNameValuePair(ONE_KEY_CRAWLE_ATTR_REQUEST_FROM, ONE_KEY_CRAWLE_ATTR_REQUEST_FROM_VALUE));
				add(new BasicNameValuePair(ONE_KEY_CRAWLE_ATTR_TIMEOUT, ONE_KEY_CRAWLE_ATTR_TIMEOUT_VALUE));
				add(new BasicNameValuePair(ONE_KEY_CRAWLE_ATTR_IS_SEND_MESSAGE, isSendMessage));
			}
		};

		try {
			// 设置参数到请求对象中
			HttpPost httpPost = new HttpPost(this.realtimeAddress);
			httpPost.setEntity(new UrlEncodedFormEntity(attrs, "UTF-8"));

			CloseableHttpClient client = HttpClients.createDefault();
			httpResponse = client.execute(httpPost);
			// 获取结果实体
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
				EntityUtils.consume(entity);
			}
		} finally {
			IOUtils.closeQuietly(httpResponse);
		}

		return result;
	}

	private void importUrl(String url) {
		Set<Entry<Long, Task>> entries = TaskCache.getInstance().entrySet();
		if (CollectionUtils.isEmpty(entries)) {
			logger.warn("ToolsAction.importUrl() TaskCache is empty!");
			return;
		}

		for (Entry<Long, Task> entry : entries) {
			Task task = entry.getValue();
			List<Rule> rules = task.getRules();
			if (CollectionUtils.isEmpty(rules)) {
				continue;
			}

			for (Rule rule : rules) {
				if (StringUtils.isNotBlank(rule.getRegex()) && rule.matches(url)) {
					this.taskService.importSeeds(String.valueOf(task.getTaskId()), UrlsType.ITEM.getValue(), url,
							rule.getGrade(), null);
				}
			}
		}
	}
}