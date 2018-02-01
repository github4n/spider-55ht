package com.haitao55.spider.crawler.core.callable.custom.c21stores;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
* Title:
* Description: C21stores 一级类目种子抓取
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月3日 上午9:55:49
* @version 1.0
 */
public class C21storesSelectLinkUrls extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String ATTR_HREF = "href";
	private static final String DEFAULT_NEW_URLS_TYPE = UrlType.LINK.getValue();
	public int grade;

	// css选择器
	private String css;
	// 使用css选择器进行选择后,取元素的什么属性值；默认是href
	private String attr = ATTR_HREF;
	// 迭代出来的newurls的类型,链接类型还是商品类型,默认是链接类型
	public String type = DEFAULT_NEW_URLS_TYPE;
	
	private String baseuri;
	
	private String cssLink;
	private String attrLink;
	

	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		Document doc =Jsoup.parse(content,baseuri);
		Elements elements = doc.select(css);
		if (CollectionUtils.isNotEmpty(elements)) {
			List<String> newUrlValues = JsoupUtils.attrs(elements, attr);
			List<String> urls = new ArrayList<String>();
			if(null!=newUrlValues&&newUrlValues.size()>0){
				//iterator
				for (String url : newUrlValues) {
					// 请求获取一级类目链接
					String string = HttpUtils.get(url);
					doc = Jsoup.parse(string,baseuri);
					elements = doc.select(cssLink);
					List<String> gradeList = JsoupUtils.attrs(elements, attrLink);
					urls.addAll(gradeList);
				}
			}
			
			// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
			if (CollectionUtils.isNotEmpty(urls) && StringUtils.isNotBlank(getRegex())) {
				urls = this.reformStrings(urls);
			}

			Set<Url> newUrls = this.buildNewUrls(urls, context, type, grade);

			// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
			context.getUrl().getNewUrls().addAll(newUrls);
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), css);
		}
	}


	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
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

	public String getBaseuri() {
		return baseuri;
	}

	public void setBaseuri(String baseuri) {
		this.baseuri = baseuri;
	}


	public String getCssLink() {
		return cssLink;
	}


	public void setCssLink(String cssLink) {
		this.cssLink = cssLink;
	}
	
	public String getAttrLink() {
		return attrLink;
	}


	public void setAttrLink(String attrLink) {
		this.attrLink = attrLink;
	}
}
