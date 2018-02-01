package com.haitao55.spider.crawler.core.callable.custom.katespade.surprise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.http.HttpResult;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * surprise katespade urls 获取
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月23日 下午2:29:13
* @version 1.0
 */
public class SelectSurpriseKateSpadeUrls extends AbstractSelectUrls {
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
	private static Map<String,Object> headers = null;
	private final static char[] lower={'a','b','c','d','e','f','g','h','i','j','k','l','m'
	        ,'n','o','p','q','r','s','t','u','v','w','x','y','z'};	     
    private final static char[] upper={'A','B','C','D','E','F','G','H','I','J','K','L','M'
	        ,'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    private final static char[] number={'0','1','2','3','4','5','6','7','8','9'};
    private final static List<String> emailAddress = new ArrayList<String>(Arrays.asList("@bigfoot.com","@gmail.com","@yahoo.com","@mail.com","@hotmail.com"));
	static{
		headers = new HashMap<String,Object>();
		headers.put("Cookie", "LoginBarrierApproved=BarrierApproved;");
	}
	@Override
	public void invoke(Context context) throws Exception {
		String content = HttpUtils.crawler_package(context, headers);
		if(content.contains("valid online only at surprise.katespade.com 11/19/17 through 11/25/17 until 11:59 pm pt. not valid at katespade.com, kate spade new york specialty shops or kate spade new york")){
			Document d = JsoupUtils.parse(content);
			Elements formEs = d.select("form#BarrierForm");
			 if(formEs.size() > 1){
				 throw new ParseException(CrawlerExceptionCode.OFFLINE,"surprise.katespade.com itemUrl: "+context.getUrl().toString()+"item site change need check");
			 }else{
				 Element form = formEs.get(0);
				 String formUrl = "https://surprise.katespade.com"+form.attr("action");
				 Elements formFields = form.select("input");
				 Map<String, Object> payload = new HashMap<String, Object>();
					
					payload.put("fiPageSize", "32");
					payload.put("fsSortOrder", "inDisplayOrder asc");					
				 for(Element e : formFields){
					 String formKey = e.attr("name");
					 String formValue = e.attr("value");
					 if(formKey.equals("dwfrm_loginbarrier_emailAddress")){
						 payload.put("dwfrm_loginbarrier_emailAddress", email()); 
					 }else if(formKey.equals("dwfrm_loginbarrier_postalCode")){
						 payload.put("dwfrm_loginbarrier_postalCode", postCode()); 
					 }else{
						 payload.put(formKey, formValue);
					 }
				 }
				 HttpResult result = Crawler.create().timeOut(30000).url(formUrl).method(HttpMethod.POST.getValue())
							.payload(payload).result();
				 int status = result.getStatus();
				 if(status == 302){
					 List<NameValuePair> headers = result.getHeaders();
					 Map<String,Object> currentHeader = new HashMap<String,Object>();
					 String cookie = null;
					 for(NameValuePair header: headers){
						 String key = header.getName();
						 if(key.equals("Set-Cookie")){
							 cookie = header.getValue();
							 break;						 
						 }
					 }
					 currentHeader.put("Cookie", cookie);
					 currentHeader.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					 currentHeader.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/56.0.2924.76 Chrome/56.0.2924.76 Safari/537.36");
					 content = Crawler.create().timeOut(30000).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue()).header(currentHeader)
								.resultAsString();
				 }				 
			 }			    				   
		 }
		Document doc = JsoupUtils.parse(content);
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

			Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);

			// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
			context.getUrl().getNewUrls().addAll(newUrls);
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), css);
		}
	}
	private String email() {
		int len = (int)(Math.random()*6)+6;
        int len2 = (int)(Math.random()*(len-1));
        String email="";
        for(int j=0;j<len2;j++){
        	email+=lower[((int)(Math.random()*26))];
        }
        email+="";
        for(int j=0;j<len-len2;j++){
        	email+=upper[((int)(Math.random()*26))];
        }
		return email+emailAddress.get(new Random().nextInt(emailAddress.size()));
	}
	private String postCode() {
		 int num = (int)(Math.random())+5;
	     String postCode = "";
	     for(int k=0;k<num;k++){
	    	 postCode+=number[((int)(Math.random()*10))];
	     }
		return postCode;
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
}