package com.haitao55.spider.crawler.core.callable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * 功能：获取当前时间,在context中输出成指定名称的值
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午5:01:41
 * @version 1.0
 */
public class Time extends AbstractCallable {
	
	private String unit;
	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	private static final ThreadLocal<DateFormat> DATE_FORMATTER = new ThreadLocal<DateFormat>() {
		public DateFormat initialValue() {
			return new SimpleDateFormat(DATE_FORMAT_PATTERN);
		}
	};

	@Override
	public void invoke(Context context) throws Exception {
		String value = StringUtils.EMPTY;
		if(StringUtils.isNotBlank(unit) && StringUtils.equalsIgnoreCase("milliseconds", unit)){
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			value = cal.getTimeInMillis()+"";
		} else {
			value = DATE_FORMATTER.get().format(new Date());
		}
		setOutput(context, value);
	}

	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		System.out.println(cal.getTimeInMillis());
		System.out.println(System.currentTimeMillis());
	}
	
}