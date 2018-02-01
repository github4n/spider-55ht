package com.haitao55.spider.crawler.core.callable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 *  处理　properties
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月25日 上午11:00:14
* @version 1.0
 */
public class Properties extends AbstractSelect {

	//css 解析页面处理properties
	private String css;
	
	//获取context中其他input值
	private String input2;
	
	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	
	public String getInput2() {
		return input2;
	}

	public void setInput2(String input2) {
		this.input2 = input2;
	}

	@Override
	public void invoke(Context context) throws Exception {
		Map<String, Object> propMap = new HashMap<String, Object>();
		Document document = this.getDocument(context);
		if(null!=css){
			Elements es = document.select(css);
			if (es != null && es.size() > 0) {
				for (Element e : es) {
					String key = StringUtils.trim(StringUtils.substringBefore(e.text(), ":"));
					String value = StringUtils.trim(StringUtils.substringAfter(e.text(), ":"));
					if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
						propMap.put(key, value);
					}
				}
			}
		}
		if(StringUtils.isNotBlank(input2)){
			String gender = (String) context.get(input2);
			propMap.put("s_gender", gender);
			
		}
		setOutput(context,propMap);
	}

}
