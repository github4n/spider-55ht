package com.haitao55.spider.realtime.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputChannel;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * 
 * 功能：实时核价爬虫范围内使用的抓取任务实现类
 * 
 * @author Arthur.Liu
 * @time 2016年9月19日 上午10:38:17
 * @version 1.0
 */
public class RealtimeServiceCallable implements java.util.concurrent.Callable<OutputObject> {
	private static final Logger logger = LoggerFactory.getLogger(RealtimeServiceCallable.class);
//	private static final String path = "http://10.128.0.6:5598/55haitao/api";
	private static final String path = "http://realtime-service.com/55haitao/api?url=()&taskId={}";
//	private static final String path = "http://104.155.153.123:5598/55haitao/api?url=()&taskId={}";
	private static final String error_message = "error";
	private static final String delete_docType = "DELETE";
	private Long taskId;
	private String url;

	public RealtimeServiceCallable(Long taskId, String url) {
		this.taskId = taskId;
		this.url = url;
	}

	@Override
	public OutputObject call() throws Exception {
		OutputObject oo = new OutputObject();
		try {
			String request_url = StringUtils.replacePattern(path, "\\(\\)", url);
			request_url = StringUtils.replacePattern(request_url, "\\{\\}", String.valueOf(taskId));
			String jsonResult = HttpUtils.get(request_url);

			Map<String, List<Image>> images = new HashMap<String, List<Image>>();
			
			CrawlerJSONResult buildFrom = CrawlerJSONResult.buildFrom(jsonResult);
			
			String docType = buildFrom.getDocType();
			if(StringUtils.containsIgnoreCase(delete_docType, docType)){
				OutputObject oo2 = new OutputObject();
				return oo2;
			}
			String message = buildFrom.getMessage();
			if(StringUtils.containsIgnoreCase(error_message, message)){
				OutputObject oo2 = new OutputObject();
				return oo2;
			}
			
			

			List<LStyleList> l_style_list = buildFrom.getRetbody().getSku().getL_style_list();
			if (null != l_style_list && l_style_list.size() > 0) {
				for (LStyleList lStyleList : l_style_list) {
					String good_id = lStyleList.getGood_id();
					List<Image> pics = new ArrayList<Image>();
					List<Picture> style_images = lStyleList.getStyle_images();
					if(null != style_images && style_images.size() > 0){
						for (Picture picture : style_images) {
							String originalUrl = picture.getSrc();
							pics.add(new Image(originalUrl));
						}
					}
					images.put(good_id, pics);//images封装
				}
			}
			
			RetBody retBody = buildFrom.getRetbody();
			String json = JsonUtils.bean2json(retBody);
			
			//outputobject taskId封装
			oo.setTaskId(String.valueOf(taskId));
			//outputobject url封装
			oo.setUrl(new Url(url));
			//outputobject outputChannel封装
			oo.setOutputChannel(OutputChannel.codeOf("kafka"));
			oo.setDocType(DocType.INSERT);
			//outputobject images封装
			oo.setImages(images);
			//
			oo.putItemField("retbody", json);

		} catch (HttpException httpException) {
			logger.info("item " + httpException.getStatus() + " url:{}", url);
			OutputObject oo2 = new OutputObject();
			oo2.setDocType(DocType.DELETE);
			return oo2;
		} catch (ParseException e) {
			if (CrawlerExceptionCode.OFFLINE.equals(e.getCode())) {
				logger.error("item parse_offline  url:{}", url);
				OutputObject oo2 = new OutputObject();
				oo2.setDocType(DocType.DELETE);
				return oo2;
			}
		} catch (Exception e) {
			logger.error("Error while executing crawler!   url:{} ; exception : {}", url, e);
		}

		return oo;
	}
}