package com.haitao55.spider.crawler.core.callable.custom.everlane;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * everlane 一级类目获取
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月28日 下午3:44:50
* @version 1.0
 */
public class SelectEverlaneUrls extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String DEFAULT_NEW_URLS_TYPE = UrlType.LINK.getValue();
	public int grade;

	// 迭代出来的newurls的类型,链接类型还是商品类型,默认是链接类型
	public String type = DEFAULT_NEW_URLS_TYPE;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		content = StringEscapeUtils.unescapeHtml(content);
		
		content = StringUtils.substringBetween(content, "E.data = ", "E.config");
		content = StringUtils.trim(content);
		content = StringUtils.substringBeforeLast(content, ";");
		JSONObject jsonObject = JSONObject.parseObject(content);
		JSONObject menuJSONObject = jsonObject.getJSONObject("menu");
		JSONArray menusJSONArray = menuJSONObject.getJSONArray("menus");
		recursiva(menusJSONArray,newUrlValues);

		// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
		if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex())) {
			newUrlValues = this.reformStrings(newUrlValues);
		}

		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);

		// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
		context.getUrl().getNewUrls().addAll(newUrls);
	}

	private static void recursiva(JSONArray jsonArray , List<String> urlList){
		if(null != jsonArray && jsonArray.size() > 0){
			for (Object object : jsonArray) {
				JSONObject json = (JSONObject)object;
				String item_url = json.getString("url");
				if(!StringUtils.containsIgnoreCase(item_url, "http")){
					item_url = "https://www.everlane.com"+item_url;
				}
				urlList.add(item_url);
				JSONArray submenusJSONArray = json.getJSONArray("submenus");
				if(null != submenusJSONArray && submenusJSONArray.size() > 0){
					recursiva(submenusJSONArray,urlList);
				}
			}
		}
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
}