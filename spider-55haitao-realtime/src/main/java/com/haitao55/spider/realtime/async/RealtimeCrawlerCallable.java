package com.haitao55.spider.realtime.async;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.callable.base.Callable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.amazon_cn.AmazonCnRealtimeException;
import com.haitao55.spider.crawler.core.callable.custom.amazon_cn.AmazonCnRealtimeExceptionCode;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;

/**
 * 
 * 功能：实时核价爬虫范围内使用的抓取任务实现类
 * 
 * @author Arthur.Liu
 * @time 2016年9月19日 上午10:38:17
 * @version 1.0
 */
public class RealtimeCrawlerCallable implements java.util.concurrent.Callable<OutputObject> {
	private static final Logger logger = LoggerFactory.getLogger(RealtimeCrawlerCallable.class);
	
	private static final Logger LOGGER_REALTIME_HTML_PAGE_SOURCE = LoggerFactory.getLogger("realtime_html_page_source");


	private List<Callable> calls;
	private Context context;

	public RealtimeCrawlerCallable(List<Callable> calls, Context context) {
		this.calls = calls;
		this.context = context;
	}

	@Override
	public OutputObject call() throws Exception {
		try {
			for (Callable call : calls) {
				call.invoke(context);
			}
		}catch (HttpException httpException) {
			LOGGER_REALTIME_HTML_PAGE_SOURCE.error("realTime HttpException url :{}, HtmlPageSource :{}",context.getUrl().getValue(),context.getHtmlPageSource());
			logger.info("item "+httpException.getStatus()+" url:{}",context.getCurrentUrl());
			OutputObject oo2 =new OutputObject();
//			if(httpException.getStatus() == 404){
			oo2.setUrl(context.getUrl());
			oo2.setDocType(DocType.DELETE);
//			}
			return oo2;
		}catch(ParseException e){
			LOGGER_REALTIME_HTML_PAGE_SOURCE.error("realTime ParseException url :{}, HtmlPageSource :{}",context.getUrl().getValue(),context.getHtmlPageSource());
			if(CrawlerExceptionCode.OFFLINE.equals(e.getCode())){
				logger.error("item parse_offline  url:{} , exception:{} ",context.getCurrentUrl(),e);
				OutputObject oo2 =new OutputObject();
				oo2.setUrl(context.getUrl());
				oo2.setDocType(DocType.DELETE);
				return oo2;
			}
		}catch(AmazonCnRealtimeException e){// 针对中亚海外购做的特殊处理
			if(AmazonCnRealtimeExceptionCode.NOT_SELECT_REQUIRED_PROPERTY.equals(e.getCode())){
				logger.error("amazon.cn realtime NOT_SELECT_REQUIRED_PROPERTY exception, url:{} , exception:{} ",context.getCurrentUrl(),e);
				OutputObject oo2 =new OutputObject();
				oo2.setUrl(context.getUrl());
				oo2.setDocType(DocType.NOT_SELECT_REQUIRED_PROPERTY);
				return oo2;
			}
		} catch (Exception e) {
			LOGGER_REALTIME_HTML_PAGE_SOURCE.error("realTime Exception url :{}, HtmlPageSource :{}",context.getUrl().getValue(),context.getHtmlPageSource());
			logger.error("Error while executing crawler!   url:{} ; exception : {}", context.getCurrentUrl(),e);
			OutputObject oo2 =new OutputObject();
			oo2.setUrl(context.getUrl());
			oo2.setDocType(DocType.DELETE);
			return oo2;
		}

		OutputObject oo = context.getUrl().getOutputObject();
		return oo;
	}
}