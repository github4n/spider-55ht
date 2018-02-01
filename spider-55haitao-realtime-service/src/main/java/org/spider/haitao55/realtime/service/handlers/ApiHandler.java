package org.spider.haitao55.realtime.service.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spider.haitao55.realtime.service.crawler.AmazonHtmlApiCrawler;

import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.exception.ParseException;

/**
 * @ClassName: AmazonusServlet
 * @Description: 浏览器+api爬取
 * @author songsong.xu
 * @date 2017年1月11日 下午6:03:41
 *
 */
public class ApiHandler extends AbstractHttpHandler {

	private static final long serialVersionUID = 9136981230093758506L;
	private final static Logger logger = LoggerFactory.getLogger(ApiHandler.class);
	
	public ApiHandler() {}
	
	@Override
	public void initialize() {
	}
	@Override
	public void doTask(HttpServletRequest request, HttpServletResponse response) {
		logger.info("request start ......");
		long start = System.currentTimeMillis();
		String url = request.getParameter("url");
		String taskId = request.getParameter("taskId");
		CrawlerJSONResult jsonResult = new CrawlerJSONResult();
		jsonResult.setTaskId(taskId);
		jsonResult.setMessage("OK");
		jsonResult.setRetcode(0);
		if(StringUtils.isBlank(url)){
			jsonResult.setMessage("ERROR:url_not_found");
			output(JsonUtils.bean2json(jsonResult), response);
			return;
		}
		if(StringUtils.isBlank(taskId)){
			jsonResult.setMessage("ERROR:taskId_not_found");
			output(JsonUtils.bean2json(jsonResult), response);
			return;
		}
		String content = "";
		Pattern p = Pattern.compile("Currently unavailable|Available from these sellers");
		Matcher m = p.matcher(content);
		if(m.find()){
			offline(url, jsonResult);
			output(JsonUtils.bean2json(jsonResult), response);
			return;
		}
		AmazonHtmlApiCrawler htmlApiCrawler = new AmazonHtmlApiCrawler(getPool());
		RetBody ret =  null;
		try{
			ret = htmlApiCrawler.getResult(url);
			if(ret.getStock() == null || ret.getStock().getStatus() == 0){
				offline(url,jsonResult);
				output(JsonUtils.bean2json(jsonResult), response);
				return;
			}
		}catch(ParseException e){
			offline(url,jsonResult);
			output(JsonUtils.bean2json(jsonResult), response);
			return;
		}
		jsonResult.setRetbody(ret);
		jsonResult.setDocType(DocType.INSERT.getValue());
		output(JsonUtils.bean2json(jsonResult), response);
		logger.info("request end ......");
		long end = System.currentTimeMillis();
		logger.info(" url {} by browers and api ,total time : {}", url, (end-start));
	}

	private void offline(String url, CrawlerJSONResult jsonResult) {
		RetBody ret = new RetBody();
		ret.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), SpiderStringUtil.md5Encode(url)));
		ret.setStock(new Stock(0));
		jsonResult.setRetbody(ret);
		jsonResult.setDocType(DocType.DELETE.getValue());
	}
	@Override
	public void finalize() {
		
	}
}
