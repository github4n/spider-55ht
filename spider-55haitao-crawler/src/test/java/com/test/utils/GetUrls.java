package com.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年12月2日 下午5:46:01  
 */
public class GetUrls  extends AbstractSelectUrls {
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
	private String itemCss;
	
	private static final int TIME_OUT = 30*1000;
	private static final int RETRY = 1;
	
	public static List<String> list = new ArrayList<>();
	
	public GetUrls(){}
	public GetUrls(String targetUrl, String css, String attr, String type, int grade) throws Exception{
		this.attr = attr;
		this.grade = grade;
		this.css = css;
		this.type = type;
		Context context = new Context(){{
			setUrl(new Url(){{
				setValue(targetUrl);
				setTask(new Task(){{
					setTaskId(System.currentTimeMillis());
				}});
			}});
			setCurrentUrl(targetUrl);
		}};
		this.invoke(context);
	}
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = HttpUtils.get(context.getCurrentUrl(), TIME_OUT, RETRY, null);
		Document document = Jsoup.parse(content, context.getCurrentUrl());
		Elements elements = document.select(css);
		if (CollectionUtils.isNotEmpty(elements)) {
			List<String> newUrlValues = JsoupUtils.attrs(elements, attr);

			// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
			if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex())) {
				newUrlValues = this.reformStrings(newUrlValues);
			}
			if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex2())) {
				newUrlValues = this.replaceNewUrls(newUrlValues);
			}

			Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
			System.err.println("本次共获取"+newUrls.size()+"个url:");
			System.out.println();
			for(Url url : newUrls){
				System.out.println(url.getValue());
				list.add(url.getValue());
			}

			// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
			context.getUrl().getNewUrls().addAll(newUrls);
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

	public String getItemCss() {
		return itemCss;
	}

	public void setItemCss(String itemCss) {
		this.itemCss = itemCss;
	}
	
}
