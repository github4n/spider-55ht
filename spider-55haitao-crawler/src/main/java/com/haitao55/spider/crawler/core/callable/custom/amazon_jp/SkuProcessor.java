package com.haitao55.spider.crawler.core.callable.custom.amazon_jp;

import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.utils.Constants;

public class SkuProcessor implements Callable<String> {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private String itemId;
	public SkuProcessor(String itemId){
		this.itemId = itemId;
	}

	@Override
	public String call() {
		String itemUrl = String.format(AmazonJP.ITEM_URL_TMT, itemId);
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < 20; i++){
			try{
				LumHttpClient client = new LumHttpClient();
				String getIP = client.request(AmazonJP.get_ip,null);
				String currentIp = StringUtils.substringBetween(getIP, "\"ip\":\"", "\",");
				String content = client.request(itemUrl,AmazonJP.getIndexHeaders(itemUrl));
				boolean isforbiden = false;
				if(StringUtils.contains(content, "Amazon CAPTCHA")){
					isforbiden = true;
				} else {
					currentIp = "";
				}
				logger.info("itemUrl={} , isforbiden= {} ,forbiddenIp = {} ",itemUrl, isforbiden,currentIp);
				
				Document doc = Jsoup.parse(content);
				String selfString = "";
				Elements es = doc.select("#shipsFromSoldBy_feature_div > #merchant-info");
				if(es != null && es.size() > 0 ){
					selfString = es.text();
					System.out.println("itemId = " + itemId +" , selfString="+ selfString);
				}
				Pattern pShips = Pattern.compile(AmazonJP.SELF_PATTERN, Pattern.CASE_INSENSITIVE);
				Matcher mShips = pShips.matcher(selfString);
				if(!mShips.find()){
					System.out.println("itemId = "+ itemId + " is not self.");
					return null;
				}
				//stock 
				es = doc.select("#availability_feature_div > #availability > span.a-size-medium");
				String stockString = "";
				if(es != null && es.size() > 0 ){
					stockString = es.get(0).text();
				}
				Pattern pattern = Pattern.compile(AmazonJP.STOCK_PATTERN);
				Matcher matcher = pattern.matcher(stockString);
				int stockStatus = 0;
				int stockNumber = 0;
				if(matcher.find()){
					String stockNumString = matcher.group(1);
					if(StringUtils.isNotBlank(stockNumString)){
						stockStatus = 2;
						stockNumber = Integer.valueOf(stockNumString);
					} else {
						stockStatus = 1;
					}
					
				}
				
				String salePriceString = "";
				es = doc.select("#priceblock_ourprice");
				if(es != null && es.size() > 0 ){
					salePriceString = es.get(0).text();
					salePriceString = salePriceString.replace(AmazonJP.JPY_SYMBOL, "").replace(",", "");
				}
				
				String origPriceString = "";
				es = doc.select("#price_feature_div span.a-text-strike");
				if(es != null && es.size() > 0 ){
					origPriceString = es.get(0).text();
					origPriceString = origPriceString.replace(AmazonJP.JPY_SYMBOL, "").replace(",", "");
				}
				if(StringUtils.isBlank(origPriceString)){
					origPriceString = salePriceString;
				}
				//Ships from and sold by Amazon.co.jp
				
				result.append(itemId).append(":")
				.append(stockStatus).append(":")
				.append(stockNumber).append(":")
				.append(StringUtils.trim(salePriceString)).append(":")
				.append(StringUtils.trim(origPriceString));
				return result.toString();
			}catch(Throwable e ){
				e.printStackTrace();
			}
			
		}
		return null;
		
	}

}
