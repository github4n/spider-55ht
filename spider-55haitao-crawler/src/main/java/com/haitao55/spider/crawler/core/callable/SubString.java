package com.haitao55.spider.crawler.core.callable;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.base.BatchInputOutputCallable;

/**
 * 
 * 功能：字符串截取支持
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 下午2:30:09
 * @version 1.0
 */
public class SubString extends BatchInputOutputCallable {

	private static final String MODE_SINGLE = "single";
	private static final String MODE_BATCH = "batch";

	private String start;

	private String end;

	private int length;

	// 取单个字符串还是一批字符串 single/batch，默认是取单个字符串
	private String mode = MODE_SINGLE;

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	@Override
	protected Object process(Object obj) throws Exception {
		if (getInput() == null) {
			return StringUtils.EMPTY;
		}

		String input = Objects.toString(obj, "");

		if (MODE_BATCH.equalsIgnoreCase(getMode())) {
			return StringUtils.substringsBetween(input, start, end);
		}

		String start = getStart();
		if (start == null) {
			start = StringUtils.EMPTY;
		}

		String end = getEnd();
		int length = getLength();
		String output = null;

		if (length > 0 && end == null) {
			int index = 0;

			if (StringUtils.isEmpty(start)) {
				index = 0;
			} else {
				index = StringUtils.indexOf(input, start);
			}

			if (index < 0) {
				index = 0;
			}

			output = StringUtils.substring(input, index, index + length);
		} else {
			output = StringUtils.substringBetween(input, start, end);
		}

		return output;
	}
}