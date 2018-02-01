package com.haitao55.spider.crawler.core.callable.custom.groupon;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年12月5日 下午5:30:28  
 */
public class GrouponSelectUrls extends AbstractSelectUrls{
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
	private String regex2;
	private String replacement2;
	private String keywords;
	
	@Override
	public void invoke(Context context) throws Exception {
		Document doc = super.getDocument(context);
		Elements elements = doc.select(css);
		if (CollectionUtils.isNotEmpty(elements)) {
			List<String> newUrlValues = JsoupUtils.attrs(elements, attr);

			// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
			if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex())) {
				newUrlValues = this.reformStrings(newUrlValues);
			}
			if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex2())) {
				newUrlValues = this.replaceNewUrls(newUrlValues);
			}
			if(CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getKeywords())) {
				newUrlValues = this.filterNewUrls(newUrlValues, getKeywords());
			}

			Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);

			// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
			context.getUrl().getNewUrls().addAll(newUrls);
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), css);
		}
	}

	private List<String> replaceNewUrls(List<String> newUrlValues) {
		List<String> newUrls=new ArrayList<String>();
		if(null!=newUrlValues&&newUrlValues.size()>0){
			for (String value : newUrlValues) {
				value=value.replaceAll(regex2, replacement2);
				newUrls.add(value);
			}
		}
		return newUrls;
	}
	
	private List<String> filterNewUrls(List<String> newUrls, String keywords){
		List<String> urls = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(newUrls)){
			for(String url : newUrls){
				if(StringUtils.isNotBlank(url) && url.contains(keywords)){
					urls.add(url);
				}
			}
		}
		return urls;
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
	public String getRegex2() {
		return regex2;
	}

	public void setRegex2(String regex2) {
		this.regex2 = regex2;
	}

	public String getReplacement2() {
		return replacement2;
	}

	public void setReplacement2(String replacement2) {
		this.replacement2 = replacement2;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	
}
