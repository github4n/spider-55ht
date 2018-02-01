package com.haitao55.spider.crawler.core.callable.custom.pengpenggou;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonSyntaxException;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 碰碰购 发送邮件功能,调用核价接口，获取完整json Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月27日 下午2:35:56
 * @version 1.0
 */
public class PengPengGou extends AbstractSelect {
	// private static final String SERVICE_URL =
	private static final String SERVICE_URL = "http://10.25.169.237/spider-55haitao-realtime/realtime-crawler/pricing.action";
//	private static final String SERVICE_URL = "http://118.178.57.197:8888/spider-55haitao-realtime/realtime-crawler/pricing.action";
	private long CRAWLING_WAIT_TIME = 60 * 1000;// 在线抓取最多等待时间
	private static final String PRERELEASE_REALTIME = "prelease";
	private static final Map<String, String> post = new HashMap<String, String>();
	// result json error flag
	private static String errorMsg = "ERROR";
	private static String DELETE_TYPE = "DELETE";

	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl().toString();
		post.put("url", url);
		post.put("request_from", PRERELEASE_REALTIME);
		post.put("timeout", CRAWLING_WAIT_TIME + "");
		String result = HttpClientUtil.post(SERVICE_URL, post);

		// 解析result
		try {
			CrawlerJSONResult buildFrom = CrawlerJSONResult.buildFrom(result);
			if (!StringUtils.containsIgnoreCase(buildFrom.getMessage(), errorMsg)
					&& !StringUtils.endsWithIgnoreCase(buildFrom.getDocType(), DELETE_TYPE)) {
				RetBody retbody = buildFrom.getRetbody();
				setOutput(context, retbody);
			}
		} catch (JsonSyntaxException e) {
		}
	}

}
