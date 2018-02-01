package com.haitao55.spider.crawler.core.callable;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * 功能：确定哪些字段需要输出成Xml的<![CDATA[***]]>格式
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:58:38
 * @version 1.0
 */
public class XmlMeta extends AbstractCallable {
	private static final String XML_META_FIELDS = "Xml_Meta_Fields";

	private String fields = "";

	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}

	@Override
	public void invoke(Context context) throws Exception {
		context.put(XML_META_FIELDS + "", getFields());
	}
}