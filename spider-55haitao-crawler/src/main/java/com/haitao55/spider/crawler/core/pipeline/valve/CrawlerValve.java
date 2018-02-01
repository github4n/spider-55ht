package com.haitao55.spider.crawler.core.pipeline.valve;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.service.MonitorService;
import com.haitao55.spider.crawler.core.callable.base.Callable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Rule;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlStatus;
import com.haitao55.spider.crawler.core.pipeline.context.ValveContext;
import com.haitao55.spider.crawler.exception.CheckException;
import com.haitao55.spider.crawler.exception.CrawlerException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.CrawlingException;
import com.haitao55.spider.crawler.exception.OutputException;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.SpringUtils;

/**
 * 
 * 功能：执行实际抓取工作的Valve实现类
 * 
 * @author Arthur.Liu
 * @time 2016年8月3日 下午1:45:59
 * @version 1.0
 */
public class CrawlerValve implements Valve {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	// 实时核价插入mongo种子库 默认grade级别
	private static final int GRADE = 33;

	private static final String deleteThresholdOnCrawledFailedTimes = "crawler.property.deleteThresholdOnCrawledFailedTimes";

	private static final Logger url_crawler_consume_logger = LoggerFactory.getLogger("url_crawler_consume");

	/**
	 * 定时打印统计各项指标数值
	 */
	private MonitorService monitorService;

	@Override
	public String getInfo() {
		String info = (new StringBuilder()).append(this.getClass().getName()).append(SEPARATOR_INFO_FIELDS)
				.append(Thread.currentThread().toString()).append(SEPARATOR_INFO_FIELDS).append(this.toString())
				.toString();
		return info;
	}

	@Override
	public void invoke() throws Exception {
		// 取出当前线程所属ValveContext中的urls，逐个处理；
		// 这里需要注意Url实例的状态：
		// 1.有些是通过Controller从底层传递上来的Url实例,需要在Crawler层做crawling或deleting；
		// 2.有些是通过某个Url（链接类型）刚刚抓取下来的新发现Url(NEWCOME状态)；
		// 3.有些是处理过的(状态为CRAWLED_OK 或 CRAWLED_ERROR 或 DELETED_OK 或
		// DELETED_ERROR)的Url实例；
		List<Url> urls = ValveContext.getUrls();
		for (Url url : urls) {// 一个一个Url进行处理
			// 不是等待抓取状态,则不处理
			if (!UrlStatus.CRAWLING.equals(url.getUrlStatus())) {
				continue;
			}

			this.processUrl(url);
		}
	}

	/**
	 * 对单个的待抓取Url实例进行处理
	 * 
	 * @param url
	 *            待抓取Url实例
	 */
	private void processUrl(Url url) {
		// 通常不会出现这种情况，但这里判断一下避免在极端情况下引起程序空指针异常
		if (url == null) {
			logger.warn("Url-instance is null!!!");
			return;
		}

		if (StringUtils.isBlank(url.getValue())) {
			url.getRuntimeWatcher().setSuccess(false);
			url.getRuntimeWatcher()
					.setException(new CheckException(CrawlerExceptionCode.CHECK_ERROR, "url value is blank!"));
			url.setUrlStatus(UrlStatus.CRAWLED_ERROR);
			logger.warn("url value is blank!");
			return;
		}

		if (url.getTask() == null) {// Url所属的任务没有做任何配置
			url.getRuntimeWatcher().setSuccess(false);
			url.getRuntimeWatcher()
					.setException(new CheckException(CrawlerExceptionCode.CHECK_ERROR, "crawler config is null!"));
			url.setUrlStatus(UrlStatus.CRAWLED_ERROR);
			logger.warn("task-config is null for url {}", url.getValue());
			return;
		}

		List<Rule> rules = url.getTask().getRules();
		if (CollectionUtils.isEmpty(rules)) {// 此Url对应的TaskConfig没有配置规则内容
			url.getRuntimeWatcher().setSuccess(false);
			url.getRuntimeWatcher()
					.setException(new CheckException(CrawlerExceptionCode.CHECK_ERROR, "rules are empty!"));
			url.setUrlStatus(UrlStatus.CRAWLED_ERROR);
			logger.warn("rules in crawler config is empty for url {}", url.getValue());
			this.monitorService.incField(
					Constants.CRAWLER_MONITOR_FIELD_PREFIX_ERROR + String.valueOf(url.getTask().getTaskName()));
			return;
		}

		// 这一步费时费力，做了很多的事情，包括执行抓取/解析文档/输出结果(输出字段到OutputObject对象中)，具体做了什么，根据Task的实际配置内容而定

		long startTime = System.currentTimeMillis();
		this.processRule(url);
		long endTime = System.currentTimeMillis();
		long result = (endTime - startTime) % 1000 == 0 ? (endTime - startTime) / 1000
				: (endTime - startTime) / 1000 + 1;
		url_crawler_consume_logger.info("crawlervalve url_crawler_consume url:{} time:{}", url, result);
	}

	private void processRule(Url url) {
		List<Callable> calls = null;
		for (Rule rule : url.getTask().getRules()) {
			int grade = url.getGrade();
			if (rule.matches(grade)) {
				calls = rule.getCalls();
				break;
			}
			
			//实时核价插入url calls调用
			if(StringUtils.isNotBlank(rule.getRegex())){
				if (rule.matches(url.getValue()) && GRADE == grade) {
					calls = rule.getCalls();
					break;
				}
			}
		}

		if (CollectionUtils.isEmpty(calls)) {
			url.getRuntimeWatcher().setSuccess(false);
			url.getRuntimeWatcher().setException(
					new CheckException(CrawlerExceptionCode.CHECK_ERROR, "no callables in rule which matched url!"));
			url.setUrlStatus(UrlStatus.CRAWLED_ERROR);
			url.setLatelyFailedCount(url.getLatelyFailedCount() + 1);
			logger.warn("no callables in rule which matched url {}", url.getValue());
			this.monitorService.incField(
					Constants.CRAWLER_MONITOR_FIELD_PREFIX_ERROR + String.valueOf(url.getTask().getTaskName()));
			return;
		}

		// 每一个Url的执行过程需要一个独立的Context，用完释放
		Context context = createContext(url);
		try {// 对于一个Url来说，所有callables都执行成功才算成功；所以这个try-catch加在for循环块外面
			url.getRuntimeWatcher().setStart(System.currentTimeMillis());// 当前这个Url在处理过程中的统计信息

			for (Callable call : calls) {// 逐个执行xml配置文件中配置的callables
				call.invoke(context);
			}

			url.getRuntimeWatcher().setSuccess(true);// success只能保证没有异常发生，不代表在callable中执行ok
			url.getRuntimeWatcher().setException(null);
			url.setUrlStatus(UrlStatus.CRAWLED_OK);
			url.setLatelyFailedCount(0);// 最后一次处理（抓取/解析/输出）成功，则设置Url的“最近连续失败次数”为0
			this.monitorService.incField(
					Constants.CRAWLER_MONITOR_FIELD_PREFIX_SUCCESS + String.valueOf(url.getTask().getTaskName()));
		} catch (HttpException httpException) {// 这里的异常类型，还可以根据http错误码进一步细分
			if (httpException.getStatus() == 404) {// type=ITEM 404 offline
				url.getRuntimeWatcher().setSuccess(true);
				url.getRuntimeWatcher()
						.setException(new CrawlingException(CrawlerExceptionCode.OFFLINE, httpException.getMessage()));
				url.setUrlStatus(UrlStatus.CRAWLED_ERROR);
				url.setLatelyFailedCount(Integer.valueOf(SpringUtils.getProperty(deleteThresholdOnCrawledFailedTimes)));
				logger.warn("url {} got parse offline:{}", url.getValue(), httpException.getMessage());
				// 识别出商品已经下架也是商品爬取成功的一种方式
				this.monitorService.incField(
						Constants.CRAWLER_MONITOR_FIELD_PREFIX_SUCCESS + String.valueOf(url.getTask().getTaskName()));
			} else {// 50x //30x
				url.getRuntimeWatcher().setSuccess(false);
				url.getRuntimeWatcher()
						.setException(new CrawlingException(CrawlerExceptionCode.CRAWLING_ERROR, "http error"));
				url.setUrlStatus(UrlStatus.CRAWLED_ERROR);
				url.setLatelyFailedCount(url.getLatelyFailedCount() + 1);
				logger.warn("url {} got crawler exception:{}", url.getValue(), httpException.getMessage());
				this.monitorService.incField(
						Constants.CRAWLER_MONITOR_FIELD_PREFIX_ERROR + String.valueOf(url.getTask().getTaskName()));
			}
		} catch (ParseException parseException) {
			if (CrawlerExceptionCode.OFFLINE.equals(parseException.getCode())) {// 200
																				// offline
				url.getRuntimeWatcher().setSuccess(true);
				url.getRuntimeWatcher().setException(parseException);
				url.setUrlStatus(UrlStatus.CRAWLED_ERROR);
				url.setLatelyFailedCount(Integer.valueOf(SpringUtils.getProperty(deleteThresholdOnCrawledFailedTimes)));
				logger.warn("url {} got parse offline:{}", url.getValue(), parseException.getMessage());
				// 识别出商品已经下架也是商品爬取成功的一种方式
				this.monitorService.incField(
						Constants.CRAWLER_MONITOR_FIELD_PREFIX_SUCCESS + String.valueOf(url.getTask().getTaskName()));
			} else {
				url.getRuntimeWatcher().setSuccess(false);
				url.getRuntimeWatcher().setException(parseException);
				url.setUrlStatus(UrlStatus.CRAWLED_ERROR);
				url.setLatelyFailedCount(url.getLatelyFailedCount() + 1);
				logger.warn("url {} got parse exception:{}", url.getValue(), parseException.getMessage());
				this.monitorService.incField(
						Constants.CRAWLER_MONITOR_FIELD_PREFIX_ERROR + String.valueOf(url.getTask().getTaskName()));
			}
		} catch (OutputException outputException) {
			url.getRuntimeWatcher().setSuccess(false);
			url.getRuntimeWatcher().setException(outputException);
			url.setUrlStatus(UrlStatus.CRAWLED_ERROR);
			url.setLatelyFailedCount(url.getLatelyFailedCount() + 1);
			logger.warn("url {} got output exception:{}", url.getValue(), outputException.getMessage());
			this.monitorService.incField(
					Constants.CRAWLER_MONITOR_FIELD_PREFIX_ERROR + String.valueOf(url.getTask().getTaskName()));
		} catch (Exception e) {
			url.getRuntimeWatcher().setSuccess(false);
			url.getRuntimeWatcher()
					.setException(new CrawlerException(CrawlerExceptionCode.UNKNOWN_ERROR, "unknown error"));
			url.setUrlStatus(UrlStatus.CRAWLED_ERROR);
			url.setLatelyFailedCount(url.getLatelyFailedCount() + 1);
			logger.warn("url {} got exception:{}", url.getValue(), e);
			this.monitorService.incField(
					Constants.CRAWLER_MONITOR_FIELD_PREFIX_ERROR + String.valueOf(url.getTask().getTaskName()));
			e.printStackTrace();
		} finally {
			url.getRuntimeWatcher().setEnd(System.currentTimeMillis());
		}
	}

	private Context createContext(Url url) {
		Context context = new Context();
		context.setUrl(url);
		context.setCurrentUrl(url.getValue());
		context.init();
		return context;
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
}