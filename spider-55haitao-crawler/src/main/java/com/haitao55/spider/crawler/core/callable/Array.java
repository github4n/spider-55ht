package com.haitao55.spider.crawler.core.callable;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
* Title:
* Description: 解析input数据　输出List
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月25日 上午11:25:43
* @version 1.0
 */
public class Array extends AbstractSelect {
	//分隔字符
	private String splitChar;
	
	public String getSplitChar() {
		return splitChar;
	}

	public void setSplitChar(String splitChar) {
		this.splitChar = splitChar;
	}

	@Override
	public void invoke(Context context) throws Exception {
		List<String> list=new ArrayList<String>();
		String inputString = this.getInputString(context);
		if(StringUtils.isNotBlank(splitChar)){
			String[] array = inputString.split(splitChar);
			for (String res : array) {
				if(StringUtils.isNotBlank(res)){
					list.add(res);
				}
			}
		}
		setOutput(context,list);
	}

}
