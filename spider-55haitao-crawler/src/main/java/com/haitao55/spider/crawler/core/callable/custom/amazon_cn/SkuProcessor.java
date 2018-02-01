package com.haitao55.spider.crawler.core.callable.custom.amazon_cn;

import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.crawler.core.callable.custom.amazon_jp.LumHttpClient;
import com.mashape.unirest.http.Unirest;

public class SkuProcessor implements Callable<String> {
	
	private String itemId;
	public SkuProcessor(String itemId){
		this.itemId = itemId;
	}

	@Override
	public String call() {
		for(int i = 0; i < 3; i++){
			try{
				StringBuilder result = new StringBuilder();
//				LumHttpClient client = new LumHttpClient();
//				LuminatiHttpClient client = new LuminatiHttpClient(AmazonCN.COUNTRY, true);
//				String content = client.request(String.format(AmazonCN.ITEM_URL_TMT, itemId),AmazonCN.getHeaders());
				String content = Unirest.post(String.format(AmazonCNPage.ITEM_URL_TMT, itemId)).asString().getBody();
				Document doc = Jsoup.parse(content);
				
				//stock 
				Elements es = doc.select("#ddmAvailabilityMessage > span");
				String stockString = "";
				if(es != null && es.size() > 0 ){
					stockString = es.get(0).text();
				}
				Pattern pattern = Pattern.compile(AmazonCNPage.STOCK_PATTERN);
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
				if(CollectionUtils.isEmpty(es))
					es = doc.select("#priceblock_saleprice");
				if(es != null && es.size() > 0 ){
					salePriceString = es.get(0).text();
					salePriceString = salePriceString.replace(AmazonCNPage.JPY_SYMBOL, "").replace(",", "");
				}
				
				String origPriceString = "";
				es = doc.select("#price_feature_div span.a-text-strike");
				if(es != null && es.size() > 0 ){
					origPriceString = es.get(0).text();
					origPriceString = origPriceString.replace(AmazonCNPage.JPY_SYMBOL, "").replace(",", "");
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
