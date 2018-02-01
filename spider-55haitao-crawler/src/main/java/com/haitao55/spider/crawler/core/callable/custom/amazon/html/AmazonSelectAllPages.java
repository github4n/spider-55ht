package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * 
  * @ClassName: AmazonSelectAllPages
  * @Description: 從接口中获取所有的列表页面作为种子
  * @author songsong.xu
  * @date 2016年10月13日 下午1:41:39
  *
 */
public class AmazonSelectAllPages extends SelectUrls {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	public String BASE_URL = "https://www.#domain#/mn/search/ajax/?";
	
	@Override
	public void invoke(Context context) throws Exception {
		//String content = super.getInputString(context);
		String url = StringUtils.trim(context.getUrl().getValue());
		if(!StringUtils.startsWith(url, "http://") && StringUtils.startsWith(url, "www")){
			url = "https://"+url;
		} else if(StringUtils.startsWith(url, "http://")){
			url = url.replace("http://", "https://");
		}
		String domain = SpiderStringUtil.getAmazonDomain(url);
		String baseUrl = BASE_URL.replace("#domain#", domain);
		if(StringUtils.isBlank(domain)){
			logger.error("Error while fetching domain from url {}",url);
			return;//無域名無法爬取
		}
		////url轉換
		String currUrl = url(url, 1,baseUrl);
		Url currentUrl = new Url(currUrl);
		currentUrl.setTask(context.getUrl().getTask());
		String content = HttpUtils.get(currentUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES);
		List<String> newUrlValues = new ArrayList<String>();
		//one links
		Pattern p = Pattern.compile("http(.*?)/mn/search/ajax(.*?)section=BTF(.*?)");
		Matcher m = p.matcher(currUrl);
		if(m.find()){
			p = Pattern.compile("<span[^>]*class=\\\\\"pagnDisabled\\\\\">(.*?)</span>");
			m = p.matcher(content);
			if(m.find()){
				String totalPage = m.group(1);
				int totalPagesInt = 1;
				if(StringUtils.isNotBlank(totalPage)){
					totalPagesInt = Integer.parseInt(totalPage);
				}
				for (int i = 0; i <= totalPagesInt; i++) {
					int pageNum = i;
					String itemUrl = currUrl.replace("&page=1", "&page="+pageNum);
					if(pageNum == 0 ){
						pageNum = 1;
						itemUrl = currUrl.replace("&section=BTF", "&section=ATF").replace("&page=1", "&page="+pageNum);
					}
					
					if(totalPagesInt == 400){
						newUrlValues.add(itemUrl+"&sort=price-asc-rank");
						newUrlValues.add(itemUrl+"&sort=price-desc-rank");
					} else {
						newUrlValues.add(itemUrl);
					}
				}
			}
		}
		//other links
		//https://www.amazon.com/gp/most-gifted/baby-products/ref=zg_mg_baby-products_pg_2?ie=UTF8&pg=2&ajax=1
		//https://www.amazon.com/Best-Sellers-Electronics-Tablet-Accessories/zgbs/electronics/2348628011/ref=zg_bs_2348628011_pg_2?_encoding=UTF8&pg=2&ajax=1
		//https://www.amazon.com/Best-Sellers-Electronics-Tablet-Accessories/zgbs/electronics/2348628011/ref=zg_bs_2348628011_pg_2?_encoding=UTF8&pg=2&ajax=1&isAboveTheFold=0
		Pattern otherp = Pattern.compile("http(.*?)Best-Sellers-Electronics(.*?)|http(.*?)/gp/(.*?)");
		Matcher otherm = otherp.matcher(currUrl);
		if(otherm.find()){
			otherp = Pattern.compile("<li[^>]*class[^>]*zg_page[^>]*id[^>]*zg_page[^>]*>[^<]*<a[^>]*ajaxUrl=\\\"(.*?)\\\"[^>]*");
			otherm = otherp.matcher(content);
			while(otherm.find()){
				String itemUrl = otherm.group(1);
				newUrlValues.add(itemUrl);
			}
		}
		Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(), grade);
		context.getUrl().getNewUrls().addAll(value);
	}
	
	public String url(String origUrl,int page,String baseUrl){
		Map<String,String> params = paramsFromOrigUrl(origUrl);
		if(params.get("rh") == null){
			return keywordUrl(origUrl,params,page,baseUrl);
		} else {
			return catUrl(origUrl,params,page,baseUrl);
		}
	}
	
	private String keywordUrl(String origUrl,Map<String,String> params,int page,String baseUrl){
		String	node = params.get("node");
		if(StringUtils.isBlank(node)){
			 node = StringUtils.substringBetween(origUrl, "node=", "/");
		}
		StringBuilder rh = new StringBuilder();
		if(StringUtils.isNotBlank(node)){
			if(StringUtils.contains(node, ",")){
				String[] arr = StringUtils.split(node, ",");
				for(String cat : arr){
					rh.append("n:").append(cat).append(",");
				}
				if(rh.length() > 0){
					rh.deleteCharAt(rh.length() - 1);
				}
			} else {
				rh.append("n:").append(node);
			}
		}
		
		String keywords = params.get("field-keywords");
		if(StringUtils.isNotBlank(keywords)){
			rh.append(",k:").append(keywords);
		}
		if(StringUtils.isBlank(node) && StringUtils.isBlank(keywords)){
			return origUrl;
		}
		
		String section = "BTF";
		if(page == 0 ){
			section = "ATF";
			page = 1;
		}
		String qid = params.get("qid");
		if(rh.length() > 0){
			rh.append(",p_6:ATVPDKIKX0DER,p_n_is-min-purchase-required:5016683011");//自营
		}
		StringBuilder url = new StringBuilder(baseUrl);
		try {
			String rhEncode = URLEncoder.encode(rh.toString(), "UTF-8");
			url.append("rh=").append(rhEncode);
			url.append("&page=").append(page);
			if(StringUtils.isNotBlank(keywords)){
				url.append("&keywords=").append(keywords);
			}
			url.append("&ie=UTF8");
			if(StringUtils.isNotBlank(qid)){
				url.append("&qid=").append(params.get("qid"));
			}
			url.append("&fromHash=");
			url.append("&fromRH=").append(rhEncode);
			url.append("&section=").append(section);
			url.append("&fromApp=gp%2Fsearch");
			url.append("&fromPage=results");
			url.append("&fromPageConstruction=auisearch");
			url.append("&version=2");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url.toString();
	}
	
	
	private String catUrl(String origUrl,Map<String,String> params,int page,String baseUrl){
		
		String section = "BTF";
		if(page == 0 ){
			section = "ATF";
			page = 1;
		}
		String rh = params.get("rh");
		String qid = params.get("qid");
		if(StringUtils.isBlank(rh)){
			return origUrl;
		}
		StringBuilder url = new StringBuilder(baseUrl);
		try {
			url.append("rh=").append(rh+URLEncoder.encode(",p_6:ATVPDKIKX0DER,p_n_is-min-purchase-required:5016683011", "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		url.append("&page=").append(page);
		url.append("&ie=UTF8");
		if(StringUtils.isNotBlank(qid)){
			url.append("&qid=").append(qid);
		}
		url.append("&fromHash=");
		url.append("&fromRH=").append(rh);
		url.append("&section=").append(section);
		url.append("&fromApp=gp%2Fsearch");
		url.append("&fromPage=results");
		url.append("&fromPageConstruction=auisearch");
		url.append("&version=2");
		return url.toString();
	}
	
	private Map<String,String> paramsFromOrigUrl(String origUrl){
		Map<String,String> params = new HashMap<String,String>();
		if(StringUtils.isBlank(origUrl)){
			return params;
		}
		String[] paramArr = StringUtils.split(StringUtils.substringAfter(origUrl, "?"), "&");
		for(String param : paramArr){
			String key = StringUtils.substringBefore(param, "=");
			String value = StringUtils.substringAfter(param, "=");
			params.put(key, value);
		}
		return params;
	}
	
	public static void main(String[] args) throws Exception {
		//http://www.amazon.com/s/ref=lp_1040660_ex_n_3?rh=n%3A7141123011%2Cn%3A7147440011%2Cn%3A1040660%2Cn%3A1045024&bbn=1040660&ie=UTF8&qid=1444914201
	/*	String value = "http://www.amazon.com/s/ref=lp_1040660_ex_n_3?rh=n%3A7141123011%2Cn%3A7147440011%2Cn%3A1040660%2Cn%3A1045024&bbn=1040660&ie=UTF8&qid=1444914201";
		AmazonSelectAllPages select = new AmazonSelectAllPages();
		Context context = new Context();
		context.setUrl(new Url(value));
		select.invoke(context);*/
	/*	String currUrl = "https://www.amazon.com/Best-Sellers-Electronics-Touch-Screen-Tablet-Accessories/zgbs/electronics/2348628011/ref=amb_link_363077422_25?pf_rd_m=ATVPDKIKX0DER&pf_rd_s=merchandised-search-leftnav&pf_rd_r=0PYVN3G21F4NPP3HJ4ZA&pf_rd_t=101&pf_rd_p=2230827362&pf_rd_i=541966";
		String content = HttpUtils.get(currUrl);
		Pattern p = Pattern.compile("<li[^>]*class[^>]*zg_page[^>]*id[^>]*zg_page[^>]*>[^<]*<a[^>]*ajaxUrl=\\\"(.*?)\\\"[^>]*");
		Matcher m = p.matcher(content);
		while(m.find()){
			System.out.println(m.group(1));
		}*/
		
		System.out.println(URLDecoder.decode("https://www.amazon.com/s/ref=lp_166764011_nr_n_0?fst=as%3Aoff&rh=n%3A165796011%2Cn%3A%21165797011%2Cn%3A166764011%2Cn%3A166765011&bbn=166764011&ie=UTF8&qid=1481169447&rnid=166764011"));
	}

}