package com.haitao55.spider.crawler.core.callable;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.Callable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.core.model.OutputChannel;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.UrlStatus;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：输出结果；这里只设置输出商品item的数据值,新迭代出来的newUrls的输出不在这里(在EchoValve中)
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午3:03:11
 * @version 1.0
 */
public class Output implements Callable {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);

	private static final String FIELDS_SEPARATOR = ",";

	private String channel;
	private Set<String> fields = new HashSet<String>();

	@Override
	public void invoke(Context context) throws Exception {
		logger.info("Set output object start, url:{}", context.getUrl().getValue());

		OutputObject oo = new OutputObject();
		oo.setTaskId(String.valueOf(context.getUrl().getTaskId()));
		oo.setOutputChannel(OutputChannel.codeOf(this.channel));
		oo.setImages(context.getUrl().getImages());
		oo.setUrl(context.getUrl());
		oo.setDocType(DocType.INSERT);

		Map<String, Object> map = context.getAll();// 到目前为止context中存放的所有键-值对数据
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();// 首字母不需要一定大写
			Object value = entry.getValue();
			if (fields.contains(key)) {// 只有配置文件中列举的字段，才会被输出
				oo.putItemField(key, Objects.toString(value, ""));
			}
		}

		context.getUrl().setOutputObject(oo);// 最后将需要输出的数据对象放置到Url对象中
		context.getUrl().setUrlStatus(UrlStatus.CRAWLED_OK);

		logger.info("Set output object successfully, url:{}", context.getUrl().getValue());
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Set<String> getFields() {
		return fields;
	}

	public void setFields(String fields) {
		String[] array = StringUtils.split(fields, FIELDS_SEPARATOR);
		if (ArrayUtils.isEmpty(array)) {
			return;
		}

		for (String field : array) {
			if (StringUtils.isBlank(field)) {
				continue;
			}
			this.fields.add(StringUtils.trim(field));
		}
	}

	@Override
	public void init() throws Exception {
		
	}

	@Override
	public void destroy() throws Exception {
		
	}
}