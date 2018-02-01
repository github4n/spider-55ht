package com.haitao55.spider.crawler.core.callable.custom.compartiotor.dealmoon;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.competitor.CtorProdUrl;
import com.haitao55.spider.common.gson.bean.competitor.CtorRetBody;
import com.haitao55.spider.common.gson.bean.competitor.CtorTitle;
import com.haitao55.spider.common.gson.bean.competitor.Mall;
import com.haitao55.spider.common.gson.bean.competitor.PromoCode;
import com.haitao55.spider.common.gson.bean.competitor.Tag;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * dealmoon  竞品抓取
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月21日 下午1:44:36
* @version 1.0
 */
public class Dealmoon extends AbstractSelect{
	private static final String HOT = "http://cn.dealmoon.com/Hot-Picks";
	private static final String TOP = "http://cn.dealmoon.com/?v=list";
	private static final String NEW = "http://cn.dealmoon.com/?v=list&sort=time";
	private static final String domain = "cn.dealmoon.com";
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = crawler_package(context);
//		context.put(input, content);
		Document doc = JsoupUtils.parse(content);
		
		CtorRetBody ctorRetBody = new CtorRetBody();
		
		//title
		String main_title = StringUtils.EMPTY;
		Elements mainTitleElements = doc.select("div.article_title a>span:nth-child(2)");
		if(CollectionUtils.isNotEmpty(mainTitleElements)){
			main_title = mainTitleElements.text();
		}
		
		String deputy_title = StringUtils.EMPTY;
		Elements deputyTitleElements = doc.select("div.article_title a>span:nth-child(1)");
		if(CollectionUtils.isNotEmpty(deputyTitleElements)){
			deputy_title = deputyTitleElements.text();
		}
		
		//优惠码
		List<String> promoCodes = new ArrayList<String>();
		Elements promoCodeElements = doc.select("b.coupon");
		if(CollectionUtils.isNotEmpty(promoCodeElements)){
			for (Element element : promoCodeElements) {
				promoCodes.add(element.text());
			}
		}
		
		
		//购买链接
		String buy_url = StringUtils.EMPTY;
		Elements buyUrlElements = doc.select("div.buy>a");
		if(CollectionUtils.isNotEmpty(buyUrlElements)){
			buy_url = buyUrlElements.attr("abs:href");
		}
		
		//来源链接
		String parent_url = context.getUrl().getParentUrl().toString();
		
		long now = System.currentTimeMillis();
		Date date = new Date(now);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String discovery_time = format.format(date);
		
		//更新时间 update_time
		String update_time = StringUtils.EMPTY;
		Elements updateTimeElments = doc.select("span[class=buyinfo]");
		if(CollectionUtils.isNotEmpty(updateTimeElments)){
			String update_time_text = updateTimeElments.text();
			update_time_text = StringUtils.substringBetween(update_time_text, "发布于", "前");
			long update_time_millis = update_time_package(update_time_text);
			date = new Date(now - update_time_millis);
			format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			update_time = format.format(date);
		}
		
		//end_time
		String end_time = StringUtils.EMPTY;
		end_time = StringUtils.substringBetween(content, "促销截止至", "。");
		if(StringUtils.contains(end_time, "<")){
			end_time = StringUtils.substringBetween(content, "促销截止至", ".");
		}
		if(StringUtils.isBlank(end_time)){
			end_time = StringUtils.substringBetween(content, "截止日期为", "。");
		}
		//image
		List<Picture> l_image_list = new ArrayList<Picture>();
		Elements imageElements = doc.select("div.img_wrap>a>img");
		if(CollectionUtils.isNotEmpty(imageElements)){
			l_image_list.add(new Picture(imageElements.attr("src")));
		}
		
		//分类
		List<String> cats = new ArrayList<String>();
		Elements categoryElements = doc.select("div.mbx a:not(:first-child)");
		if(CollectionUtils.isNotEmpty(categoryElements)){
			for (Element element : categoryElements) {
				String str =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(element.text()));
				if (StringUtils.isNotBlank(str)) {
					cats.add(str);
				}
			}
		}
		
		
		//商城
		String mall = StringUtils.EMPTY;
		mall = StringUtils.substringBetween(content, "查看来自", "的折扣");
		
		//buy url 获取 
		String buy_url_temp  = buy_url;
		try {
			buy_url = buy_url_package(buy_url,mall);
		} catch (Exception e) {
		}
		if(StringUtils.isBlank(buy_url)){
			buy_url = buy_url_temp;
		}
		if(StringUtils.isBlank(buy_url)){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"dealmoon.com itemUrl:"+context.getUrl().toString()+" is exception..");
		}
		
		//内容
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Elements contentElements = doc.select("div.minfor.event_statistics ul li , div.minfor.event_statistics p");
		StringBuilder sb = new StringBuilder();
		if (contentElements != null && contentElements.size() > 0) {
			int count = 1;
			for (Element e : contentElements) {
				String text = e.text();
				if(StringUtils.isNotBlank(text) && !StringUtils.equals(" ", text)){
					featureMap.put("feature-" + count, text);
					count++;
					sb.append(text);
				}
			}
		}
		
		
		//tag
		String tag = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(parent_url, HOT)){
			tag = "热门";
		}
		else if(StringUtils.containsIgnoreCase(parent_url, NEW)){
			tag = "最新";
		}
		else if(StringUtils.containsIgnoreCase(parent_url, TOP) && !StringUtils.containsIgnoreCase(url, NEW)){
			tag = "置顶";
		}
		
		ctorRetBody.setDOCID(SpiderStringUtil.md5Encode(url));
		
		ctorRetBody.setSite(new Site(domain));
		
		ctorRetBody.setTag(new Tag(tag));
		
		ctorRetBody.setFeatureList(featureMap);
		
		ctorRetBody.setCtorProdUrl(new CtorProdUrl(url, parent_url, buy_url, discovery_time, update_time, end_time));
		
		ctorRetBody.setImage(new LImageList(l_image_list));
		
		ctorRetBody.setPromoCode(new PromoCode(promoCodes));
		
		ctorRetBody.setCtorTitle(new CtorTitle(main_title, deputy_title));
		
		ctorRetBody.setCategory(cats);
		
		ctorRetBody.setMall(new Mall(mall));
		
		ctorRetBody.setBrand(new Brand("",""));
		
		setOutput(context, ctorRetBody.parseTo());
		
	}

	/**
	 * 获取更新时间的毫秒值
	 * @param update_time_text
	 * @return
	 */
	private static long update_time_package(String update_time_text) {
		if(StringUtils.isNotBlank(update_time_text)){
			if(StringUtils.contains(update_time_text, "秒")){
				update_time_text = StringUtils.substringBefore(update_time_text, "秒");
				return Integer.parseInt(update_time_text)*1000l;
			}
			if(StringUtils.contains(update_time_text, "分钟")){
				update_time_text = StringUtils.substringBefore(update_time_text, "分钟");
				return Integer.parseInt(update_time_text)*1000*60l;
				
			}
			if(StringUtils.contains(update_time_text, "小时")){
				update_time_text = StringUtils.substringBefore(update_time_text, "小时");
				return Integer.parseInt(update_time_text)*1000*60*60l;
				
			}
			if(StringUtils.contains(update_time_text, "天")){
				update_time_text = StringUtils.substringBefore(update_time_text, "天");
				return Integer.parseInt(update_time_text)*1000*60*60*24l;
				
			}
			if(StringUtils.contains(update_time_text, "月")){
				update_time_text = StringUtils.substringBefore(update_time_text, "月");
				return Integer.parseInt(update_time_text)*1000*60*60*24*30l;
			}
			
		}
		
		return 0;
	}
	
	private String crawler_package(Context context) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).header(getHeaders()).url(context.getCurrentUrl().toString()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).header(getHeaders()).url(context.getCurrentUrl().toString()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	private Map<String,Object> getHeaders(){
		final Map<String,Object> headers = new HashMap<String,Object>(){
			{
				put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				put("Accept-Encoding", "gzip, deflate, sdch");
				put("Accept-Language", "zh-CN,zh;q=0.8");
				put("Cache-Control", "max-age=0");
				put("Connection", "keep-alive");
//				put("Cookie", "__gads=ID=6317950096b83888:T=1487555926:S=ALNI_MbSXcNaILJorNUxthhWYhDRx6JjtQ; PHPSESSID=66f143f821916269de8a42b564b3b7aa; _ga=GA1.2.421631721.1487555928; _gat=1; rip=D; Hm_lvt_aa1bd5db226a1bae87a0ffc02cee3d7b=1487555927; Hm_lpvt_aa1bd5db226a1bae87a0ffc02cee3d7b=1487675306; OX_plg=pm");
				put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/55.0.2883.87 Chrome/55.0.2883.87 Safari/537.36");
			}
		};
		return headers;
	}
	
	private static String buy_url_package(String buy_url, String mall) throws Exception {
		String target = HttpUtils.get(buy_url);
		if(StringUtils.isNotBlank(target)){
			buy_url = StringUtils.substringBetween(target, "murl=", "\"");
			if(StringUtils.isBlank(buy_url)){
				buy_url = StringEscapeUtils.unescapeHtml(StringUtils.substringBetween(target, "&amp;url=", ")"));
				if(StringUtils.isNotBlank(buy_url)){
					buy_url = StringUtils.replacePattern(buy_url, "[']", "");
				}
			}
			if(StringUtils.isBlank(buy_url)){
				buy_url = StringEscapeUtils.unescapeHtml(StringUtils.substringBetween(target, "onload=\"refresher(", ")"));
				if(StringUtils.isNotBlank(buy_url)){
					buy_url = StringUtils.replacePattern(buy_url, "[']", "");
				}
			}
			buy_url = URLDecoder.decode(buy_url, "utf8");
		}
		//buy url 
//		if(StringUtils.isNotBlank(buy_url)){
//			String temp = StringUtils.substringBetween(buy_url, ".", ".");
//			if(!StringUtils.containsIgnoreCase(mall, buy_url)){
//				String redictURL = StringUtils.EMPTY;
//				try {
//					redictURL = getRedirectUrl(buy_url);
//				} catch (Exception e) {
//					
//				}
//				if(StringUtils.isNotBlank(redictURL)){
//					buy_url = redictURL;
//				}
//			}
//		}
		return buy_url;
	}

	/** 
     * 获取重定向地址 
     * @param path 
     * @return 
     * @throws Exception 
     */  
    private static String getRedirectUrl(String path) throws Exception {  
        HttpURLConnection conn = (HttpURLConnection) new URL(path)  
                .openConnection();  
        conn.setInstanceFollowRedirects(false);  
        conn.setConnectTimeout(5000);  
        conn.setReadTimeout(5000);
        return conn.getHeaderField("Location");  
    }  
}
