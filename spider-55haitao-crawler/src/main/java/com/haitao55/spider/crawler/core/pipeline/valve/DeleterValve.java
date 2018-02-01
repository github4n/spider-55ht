package com.haitao55.spider.crawler.core.pipeline.valve;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.Output;
import com.haitao55.spider.crawler.core.callable.base.Callable;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.core.model.OutputChannel;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.Rule;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlStatus;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.core.pipeline.context.ValveContext;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：用来执行Url删除(从系统中下线/下架)功能的Valve实现类
 * 
 * @author Arthur.Liu
 * @time 2016年8月3日 下午2:21:08
 * @version 1.0
 * 
 * @see 抓取结果输出文件 Output.java
 */
public class DeleterValve implements Valve {
	private static final Logger logger = LoggerFactory
			.getLogger(Constants.LOGGER_NAME_CRAWLER);

	/**
	 * 最近连续多少次抓取失败，便删除(生成删除类型的文档,并告知Controller从种子DB中删除此Url)；
	 */
	private int deleteThresholdOnCrawledFailedTimes = 3;

	@Override
	public String getInfo() {
		String info = (new StringBuilder()).append(this.getClass().getName())
				.append(SEPARATOR_INFO_FIELDS)
				.append(Thread.currentThread().toString())
				.append(SEPARATOR_INFO_FIELDS).append(this.toString())
				.toString();
		return info;
	}

	@Override
	public void invoke() throws Exception {
		List<Url> urls = ValveContext.getUrls();

		for (Url url : urls) {
			// 如果最后一次抓取失败并且最近连续失败总数大于删除阀值,则将url状态设置为待删除的
			if (UrlStatus.CRAWLED_ERROR.equals(url.getUrlStatus())
					&& url.getLatelyFailedCount() >= this.deleteThresholdOnCrawledFailedTimes) {
				logger.warn(
						"Lately-Failed-Count is greater than delete-threshold, and will be deleted, url:{}",
						url.getValue());
				url.setUrlStatus(UrlStatus.DELETING);
			}

			// 1.上面刚刚设置为deleting状态的,2.从底层Controller中传递上来的时候就是deleting状态的
			if (UrlStatus.DELETING.equals(url.getUrlStatus())) {
				this.processUrl(url);
			}
		}
	}

	/**
	 * 对单个的待删除Url实例进行处理
	 * 
	 * @param url
	 *            待删除的Url实例
	 */
	private void processUrl(Url url) {
		if(UrlType.LINK.equals(url.getUrlType())){
			return;
		}
		OutputObject oo = new OutputObject();
		oo.setTaskId(String.valueOf(url.getTaskId()));
		oo.setOutputChannel(getOutputChannel(url));
		oo.setDocType(DocType.DELETE);
		oo.setUrl(url);
		url.setOutputObject(oo);
		url.setUrlStatus(UrlStatus.DELETED_OK);
	}

	private OutputChannel getOutputChannel(Url url) {
		for (Rule rule : url.getTask().getRules()) {
			if (rule.matches(url.getGrade())) {
				List<Callable> calls = rule.getCalls();
				for (Callable call : calls) {
					if (call instanceof Output) {
						Output output = (Output) call;
						return OutputChannel.codeOf(output.getChannel());
					}
				}
			}
		}

		return null;
	}

	public int getDeleteThresholdOnCrawledFailedTimes() {
		return deleteThresholdOnCrawledFailedTimes;
	}

	public void setDeleteThresholdOnCrawledFailedTimes(
			int deleteThresholdOnCrawledFailedTimes) {
		this.deleteThresholdOnCrawledFailedTimes = deleteThresholdOnCrawledFailedTimes;
	}
}