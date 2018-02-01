package com.haitao55.spider.crawler.core.callable.custom.compartiotor.ebates;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
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
import com.haitao55.spider.common.kafka.SpiderKafkaProducer;
import com.haitao55.spider.common.kafka.SpiderKafkaResult;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.service.impl.OutputServiceKafka;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.SpringUtils;

public class Ebates extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);
	private static final String preffix = "https://www.ebates.com";
	private static final String domain = "www.ebates.com";
//	private static final String SPRING_CONTEXT_FILE = "config/applicationContext-crawler-beans.xml";
	private static OutputServiceKafka outputServiceKafka;
	static{
		outputServiceKafka = SpringUtils.getBean("outputServiceKafka");
	}
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = crawler_package(context);
		context.put(input, content);
		Document doc = this.getDocument(context);
		
		List<CtorRetBody> list = new ArrayList<CtorRetBody>();
		
		Elements hotElements = doc.select("div.premium-store");
		if(CollectionUtils.isNotEmpty(hotElements)){
			for (Element element : hotElements) {
				hot_item_send_kafka(element,url,"热门",list);
			}
		}
		
		Elements newElements = doc.select("div#coupons div.coupon-blk");
		if(CollectionUtils.isNotEmpty(newElements)){
			for (Element element : newElements) {
				new_item_send_kafka(element,url,"最新",list);
			}
		}
		
		for (CtorRetBody ctorRetBody : list) {
			CtorRetBody body = JsonUtils.json2bean(ctorRetBody.parseTo(), CtorRetBody.class);
			CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, body, context.getUrl().getTaskId()+"",
					DocType.INSERT.toString());
			String msg = jsonResult.parseTo();
			SpiderKafkaProducer producer = outputServiceKafka.getProducer();
			String topic = outputServiceKafka.getTopic();
			if(StringUtils.isNotBlank(msg)){
				for(int i =0 ; i < 20; i++){
					try {
						SpiderKafkaResult result = producer.sendbyCallBack(topic, msg);
						if(result != null){
							logger.info("send a message offset :{}, message:{}", result.getOffset(), msg);
							break;
						}
					} catch (Exception e) {
						if(e instanceof ConnectException
								|| e instanceof NoRouteToHostException
								|| e instanceof SocketTimeoutException
								|| e instanceof UnknownHostException
								|| e instanceof SocketException){
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							continue;
						}
						logger.error("url:{},send message to topic {},exception {}", ctorRetBody.getCtorProdUrl().getUrl().toString(), topic, e);
					}
					logger.info("send a message failed, url:{}", ctorRetBody.getCtorProdUrl().getUrl().toString());
				}
				
			} else {
				logger.warn("url:{},send message to topic {},msg is null", ctorRetBody.getCtorProdUrl().getUrl().toString(), topic);
			}
		}
		
		return ;
	}
	/**
	 * 发送热门商品到kafka
	 * @param element
	 * @param url 
	 * @param tag 
	 * @param list 
	 */
	private static void hot_item_send_kafka(Element element,String url, String tag, List<CtorRetBody> list) {
		Elements buyUrlElements = element.select("a:nth-child(1)");
		Elements infoElemengts = element.select("a:nth-child(2)");
		
		CtorRetBody ctorRetBody = new CtorRetBody();
		
		//title
		String main_title = StringUtils.EMPTY;
		Elements mainTitleElements = infoElemengts.select("span.premium-desc.bblk.f-gry.f-16.lh-20.prox-b");
		if(CollectionUtils.isNotEmpty(mainTitleElements)){
			main_title = mainTitleElements.text();
		}
		
		String deputy_title = StringUtils.EMPTY;
		Elements deputyTitleElements = infoElemengts.select("span.cb.bblk.f-16.lh-40.pad-5-t");
		if(CollectionUtils.isNotEmpty(deputyTitleElements)){
			deputy_title = deputyTitleElements.text();
		}
		
		//优惠码
		List<String> promoCodes = new ArrayList<String>();
		Elements promoCodeElements = infoElemengts.select("span.premium-code.bblk.f-14.f-gry.lh-30");
		if(CollectionUtils.isNotEmpty(promoCodeElements)){
			for (Element promoCodeElement : promoCodeElements) {
				promoCodes.add(promoCodeElement.text());
			}
		}
		
		//end_time
		String end_time = StringUtils.EMPTY;
		Elements endTimeElements = infoElemengts.select("span.premium-expire.bblk.f-12.f-gry-dk2.lh-40");
		if(CollectionUtils.isNotEmpty(endTimeElements)){
			end_time = endTimeElements.text();
			end_time = StringUtils.replacePattern(end_time, "[Expires ]", "");
			if(StringUtils.equalsIgnoreCase("today", end_time)){
				long end_time_temp = System.currentTimeMillis()-24*60*60*1000;
				end_time = new SimpleDateFormat("MM/dd/yyyy").format(new Date(end_time_temp));
			}
		}
		
		//ctorRetBody 封装
		ctorRetBody_package(ctorRetBody,buyUrlElements,end_time,url);
		
		//商城
		String mall = StringUtils.EMPTY;
		mall = StringUtils.substringBetween(element.toString(), "Get a great deal from ", " plus");
		
		ctorRetBody.setTag(new Tag(tag));
		
		ctorRetBody.setPromoCode(new PromoCode(promoCodes));

		ctorRetBody.setCtorTitle(new CtorTitle(main_title, deputy_title));


		ctorRetBody.setMall(new Mall(mall));

		ctorRetBody.setBrand(new Brand("",""));
		
		//add
		list.add(ctorRetBody);
	}

	/**
	 * 发送最新商品到kafka
	 * @param element
	 * @param url 
	 * @param tag 
	 * @param list 
	 */
	private static void new_item_send_kafka(Element element, String url,String tag, List<CtorRetBody> list) {
		Elements buyUrlElements = element.select("div.merchlogo.flt a");
		Elements infoElemengts = element.select("ul");
		
		CtorRetBody ctorRetBody = new CtorRetBody();
		
		//title
		String main_title = StringUtils.EMPTY;
		Elements mainTitleElements = infoElemengts.select("li:nth-child(1) div span");
		if(CollectionUtils.isNotEmpty(mainTitleElements)){
			main_title = mainTitleElements.text();
		}
		
		String deputy_title = StringUtils.EMPTY;
		Elements deputyTitleElements = infoElemengts.select("li:nth-child(1)>span");
		if(CollectionUtils.isNotEmpty(deputyTitleElements)){
			deputy_title = deputyTitleElements.text();
		}
		
		//优惠码
		List<String> promoCodes = new ArrayList<String>();
		Elements promoCodeElements = infoElemengts.select("li:nth-child(2) span.prox-b.space-1.pad-20-r");
		if(CollectionUtils.isNotEmpty(promoCodeElements)){
			for (Element promoCodeElement : promoCodeElements) {
				promoCodes.add(promoCodeElement.text());
			}
		}
		
		//end_time
		String end_time = StringUtils.EMPTY;
		Elements endTimeElements = infoElemengts.select("li:nth-child(2) span.premium-expire.flt.f-14.lh-17.f-gry-dk-8");
		if(CollectionUtils.isNotEmpty(endTimeElements)){
			end_time = endTimeElements.text();
			end_time = StringUtils.replacePattern(end_time, "[Expires ]", "");
			if(StringUtils.equalsIgnoreCase("today", end_time)){
				long end_time_temp = System.currentTimeMillis()-24*60*60*1000l;
				end_time = new SimpleDateFormat("MM/dd/yyyy").format(new Date(end_time_temp));
			}
		}
		
		//商城
		String mall = StringUtils.EMPTY;
		mall = StringUtils.substringBetween(element.toString(), "Shop now at ", " plus");
		
		//ctorRetBody 封装
		ctorRetBody_package(ctorRetBody,buyUrlElements,end_time,url);

		ctorRetBody.setTag(new Tag(tag));

		ctorRetBody.setPromoCode(new PromoCode(promoCodes));

		ctorRetBody.setCtorTitle(new CtorTitle(main_title, deputy_title));

		ctorRetBody.setMall(new Mall(mall));

		ctorRetBody.setBrand(new Brand("",""));
		
		//add
		list.add(ctorRetBody);
	}
	
	/**
	 * 热门 最新  ctorRetBody 封装
	 * @param ctorRetBody
	 * @param buyUrlElements
	 * @param end_time 
	 * @param parent_url 
	 */
	private static void ctorRetBody_package(CtorRetBody ctorRetBody, Elements buyUrlElements, String end_time, String parent_url) {
		
		//购买链接
		String buy_url = StringUtils.EMPTY;
		if(CollectionUtils.isNotEmpty(buyUrlElements)){
			buy_url = buyUrlElements.attr("href");
			if(!StringUtils.contains(buy_url, "http")){
				buy_url = preffix+buy_url;
			}
		}
		
		//来源链接
//		String parent_url = StringUtils.EMPTY;
		
		long now = System.currentTimeMillis();
		Date date = new Date(now);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String discovery_time = format.format(date);
		
		//更新时间 update_time
		String update_time = StringUtils.EMPTY;
		
		//image
		List<Picture> l_image_list = new ArrayList<Picture>();
		
		//分类
		List<String> cats = new ArrayList<String>();
		
		//内容
		Map<String, Object> featureMap = new HashMap<String, Object>();
		
		ctorRetBody.setDOCID(SpiderStringUtil.md5Encode(buy_url));
		
		ctorRetBody.setSite(new Site(domain));
		
		ctorRetBody.setFeatureList(featureMap);

		ctorRetBody.setCtorProdUrl(new CtorProdUrl(buy_url, parent_url, buy_url, discovery_time, update_time, end_time));

		ctorRetBody.setImage(new LImageList(l_image_list));

		ctorRetBody.setCategory(cats);
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
}
