package com.haitao55.spider.crawler.core.callable.custom.tbMerchant;



import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.taobao.TBMerchantBody;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.Constants;

public class TBMerchantDetail  extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "rate.taobao.com";
	private  String taobao_interface = "https://g.taobao.com/json/shop_dsr.htm?sellerId=###&callback=jsonp475";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		
		String sellerId = StringUtils.substringAfter(context.getCurrentUrl(),"user_number_id=");
		String shopId = StringUtils.substringBetween(content, "shopId\":\"", "\"");
		String wangwang = StringUtils.substringBetween(content, "掌柜：", "</");
		
		String tbUrl = taobao_interface.replace("###", sellerId);
		String rs = getContent(context,tbUrl);
		
		
		TBMerchantBody tbMerchant = new TBMerchantBody();
		
		String docid = StringUtils.EMPTY;
		if(StringUtils.isNotBlank(shopId)){
			docid = SpiderStringUtil.md5Encode(domain+shopId);
		}else{
			docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
		}
		
		String goodvaluationRate = StringUtils.EMPTY;
		String rateLevelPict = StringUtils.EMPTY;
		logger.info("crawler TBMerchantDetail Info --->>> shopId :{} , wangWang :{}",shopId,wangwang);
		if(StringUtils.isBlank(shopId) && StringUtils.isBlank(wangwang)){
			return;
		}
		
		if(StringUtils.isNotBlank(rs)){
			String merchantJson = StringUtils.substringBetween(rs, "(", ")");
			if(StringUtils.isNotBlank(merchantJson)){
				goodvaluationRate = StringUtils.substringBetween(rs, "goodvaluationRate\":\"", "\"");
				rateLevelPict = StringUtils.substringBetween(rs, "rateLevelPict\":\"", "\"");
				if(StringUtils.isNotBlank(rateLevelPict)){
					rateLevelPict = StringUtils.substring(rateLevelPict, 0, rateLevelPict.indexOf(".gif"));
				}
			}
		}
		
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			doc.select(".J_TShopLeft span.shop-name a i").remove();
			String shopName = doc.select(".J_TShopLeft span.shop-name a").text();
			String url = doc.select(".summary-line a.mini-dsr").attr("href");
			
			tbMerchant.setDOCID(docid);
			tbMerchant.setGoodRate(goodvaluationRate);
			tbMerchant.setUrl("https:"+url);
			tbMerchant.setShopName(shopName);
			tbMerchant.setSellerGrade(rateLevelPict);
			tbMerchant.setWangWangName(wangwang);
		}
		setOutput(context, tbMerchant.parseTo());
		
	}
	
	private String getContent(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url)
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url)
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	
}
