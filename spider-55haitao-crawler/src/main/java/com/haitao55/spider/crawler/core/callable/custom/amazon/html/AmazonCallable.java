package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.xobject.XDocument;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

class AmazonCallable implements Callable<SkuBean> {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String AMAZON_SELF = "Amazon.co.jp";//日本自营域名
	//private static final String ITEM_URL_TEMPLATE = "https://www.amazon.com/dp/#itemId#/?th=1&psc=1";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	private String skuId;
	private Url skuUrl;
	private boolean isSkuUrl;
	
	public AmazonCallable(String skuId,Url skuUrl,boolean isSkuUrl){
		this.skuId = skuId;
		this.skuUrl = skuUrl;
		this.isSkuUrl = isSkuUrl;
	}
	

	@Override
	public SkuBean call() {
		SkuBean skuBean = new SkuBean(skuId);
		skuBean.setUrl(skuUrl.getValue());
		try{
			/*int count = 0;
			boolean running = true;
			String content = StringUtils.EMPTY;
			while(running){
				String proxy = Crawler.create().url("http://adsl.55haitao.com/pool?pool=9999").method("get").resultAsString();
				if(StringUtils.isNotBlank(proxy)){
					JsonArray arr = JsonUtils.json2bean(proxy, JsonArray.class);
					if(arr != null && arr.size() > 0 ){
						boolean isSuc = false;
						for(int i =0 ; i < arr.size(); i++){
							String result = arr.get(i).getAsJsonPrimitive().getAsString();
							if(StringUtils.isBlank(result)){
								continue;
							}
							String proxyAddr = StringUtils.substringBefore(result, ":");
							String proxyPort = StringUtils.substringAfter(result, ":");
							Map<String,Object> headers=new HashMap<String,Object>();
							headers.put("Retry-After", 120);
							headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
							headers.put("Accept-Language", "zh-CN,zh;q=0.8");
							headers.put("Avail-Dictionary", "SiwfkXHp");
							headers.put("Connection", "keep-alive");
							headers.put("Upgrade-Insecure-Requests", 1);
							headers.put("Host", "www.amazon.com");
							headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
							try{
								content = Crawler.create().method("get").url(skuUrl.getValue()).proxy(true).proxyAddress(proxyAddr).proxyPort(Integer.valueOf(proxyPort)).resultAsString();
							}catch(ConnectException | NoRouteToHostException | SocketTimeoutException e){
								continue;
							}
							if(StringUtils.contains(content, "{}")){
								continue;
							}
							isSuc = true;
							break;
						}
						logger.info("url {} ,isSuc {}",skuUrl.getValue(),isSuc);
						if(isSuc){
							running = false;
						}
					} 
				}
				Thread.sleep(2000);
				count++;
				if(count > 20){
					running = false;
				}
			}*/
			
			String content = StringUtils.EMPTY;
			for(int i= 0; i < 20; i++){
				try{
					content = HttpUtils.get(skuUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES, false);
					if(StringUtils.isBlank(content)){
						Thread.sleep(10000);
						continue;
					} else {
						break;
					}
				}catch(Exception e){
					if(e instanceof ConnectException
							|| e instanceof NoRouteToHostException
							|| e instanceof SocketTimeoutException
							|| e instanceof UnknownHostException
							|| e instanceof SocketException){
						Thread.sleep(10000);
						//ConnectNetWork.restartAdsl();
						continue;
					}
					throw e;
				}
				
			}
			boolean isSelf = checkAmazonisSelf(skuUrl,content,skuBean);//检查各个网站是否是自营商品，库存状态信息
			/*if(!isSelf){
				return null;
			}*/
			if(isSelf){
				skuBean.setIsSelf(Constants.IS_SELF);
			} else {
				skuBean.setIsSelf(Constants.IS_NOT_SELF);
			}
			Pattern p = Pattern.compile("<span[^>]*id=[^>]*priceblock[^>]*>(.*?)<");
			Matcher m = p.matcher(content);
			String salePrice = StringUtils.EMPTY;
			String unit = StringUtils.EMPTY;
			if(m.find()){
				salePrice = m.group(1);
				unit = getCurrencyValue(salePrice);//得到货币代码
				if(Currency.EUR.name().equals(unit)){
					salePrice = salePrice.replaceAll("[EUR]", "");
				}else{
					salePrice = salePrice.replaceAll("[\\s￥,EUR]", "");
				}
			}
			p = Pattern.compile("<span[^>]*a-text-strike[^>]*>(.*?)<");
			m = p.matcher(content);
			String origPrice = StringUtils.EMPTY;
			if(m.find()){
				origPrice = m.group(1);
				if(Currency.EUR.name().equals(unit)){
					origPrice = origPrice.replaceAll("[EUR]", "");
				}else{
					origPrice = origPrice.replaceAll("[\\s￥,EUR]", "");
				}
			}
			
			p = Pattern.compile("<tr[^>]*regularprice_savings[^>]*>[^<]*<td[^>]*>[^<]*<[^>]*td>[^<]*<td[^>]*>([^<]*)<");
			m = p.matcher(content);
			String save = StringUtils.EMPTY;
			if(m.find()){
				save = StringUtils.substringBetween(replace(m.group(1)), "(", "%");
			}
			if(StringUtils.containsIgnoreCase(skuUrl.getValue(),"amazon.de")){
				origPrice=convertPrice(origPrice);
				salePrice=convertPrice(salePrice);
			}
			if(StringUtils.isBlank(replace(origPrice)) ){
				origPrice = salePrice;
			}
			if(StringUtils.isBlank(replace(salePrice)) ){
				salePrice = origPrice;
			}
			if(StringUtils.isBlank(replace(origPrice)) && StringUtils.isBlank(replace(salePrice))){
				logger.error("Error while crawling amazon price ,url {}",skuUrl);
				return null;
			}
			if(StringUtils.isBlank(origPrice) || Float.valueOf(replace(origPrice)) < Float.valueOf(replace(salePrice)) ){
				origPrice = salePrice;
			}
			if(StringUtils.isBlank(save)){
				save = Math.round((1 - Float.valueOf(replace(salePrice)) / Float.valueOf(replace(origPrice))) * 100)+"";// discount
			}
			skuBean.setOrig(replace(origPrice));
			skuBean.setSale(replace(salePrice));
			skuBean.setSave(save);
			skuBean.setUnit(unit);
		}catch(HttpException e){
			e.printStackTrace();
		} catch(Exception e1){
			e1.printStackTrace();
		}
		return skuBean;
	}
	
	/**
	 * 德亚 金额需要转换
	 * @param origPrice
	 * @param salePrice
	 */
	private String convertPrice(String price) {
		if(StringUtils.isNotBlank(price)){
			price=price.trim().replace(".", "").replace(",", ".");
			return price;
		}
		return "";
	}


	/**
	 * 检查是否是自营商品 && 获取库存信息
	 * @param skuUrl
	 * @param skuBean
	 * @param content
	 */
	public boolean  checkAmazonisSelf(Url skuUrl,String content,SkuBean skuBean){
		String url = skuUrl.getValue();
		if(StringUtils.containsIgnoreCase(url,"amazon.com")){//检查US是否自营
			Pattern pShips = Pattern.compile("Ships from and sold by Amazon.com",Pattern.CASE_INSENSITIVE);
			Matcher mShips = pShips.matcher(content);
			if(!mShips.find()){
				logger.error("url:{}  is not sold by Amazon.com",url);
				return false;
			}
			Pattern p = Pattern.compile("<div[^>]*id=[^>]*availability[^>]*>[^<]*<span[^>]*>([^<]*)<");
			Matcher m = p.matcher(content);
			if(m.find()){
				setStock(replace(m.group(1)),skuBean);
			}
		}else if(StringUtils.containsIgnoreCase(url,"amazon.co.jp")){//JP
			Document document = Jsoup.parse(content);
			String isSelf = document.select("#merchant-info a").text();
			if(StringUtils.isNotBlank(isSelf)){
				if(!StringUtils.containsIgnoreCase(isSelf,AMAZON_SELF)){//非自营
					return false;
				}
				String stock = document.select("div#availability span.a-size-medium").text();
				if(!StringUtils.containsIgnoreCase(stock,"り扱いできません") &&
						!StringUtils.containsIgnoreCase(stock,"入荷時期は未定です")){
					skuBean.setStockStatus(1);
					skuBean.setStockNum(0);
				}else if(StringUtils.containsIgnoreCase(stock,"り扱いできません") ||
						StringUtils.containsIgnoreCase(stock,"入荷時期は未定です")){
					skuBean.setStockStatus(0);
					skuBean.setStockNum(0);
				}else{
					skuBean.setStockStatus(1);
					skuBean.setStockNum(0);
				}
			}
		}else if(StringUtils.containsIgnoreCase(url,"amazon.de")){//德亚
			Pattern pShips = Pattern.compile("Versand durch Amazon",Pattern.CASE_INSENSITIVE);
			Matcher mShips = pShips.matcher(content);
			if(!mShips.find()){
				return false;
			}
			Document document = Jsoup.parse(content);
			String stock =document.select("div#ddmDeliveryMessage").text();
			if(!StringUtils.containsIgnoreCase(stock,"nicht nach")){
				skuBean.setStockStatus(1);
				skuBean.setStockNum(0);
			}else if(StringUtils.containsIgnoreCase(stock,"nicht nach")){
				skuBean.setStockStatus(0);
				skuBean.setStockNum(0);
			}else{
				//没取到库存标识
				skuBean.setStockStatus(1);
				skuBean.setStockNum(0);
			}
			
//			Pattern p = Pattern.compile("<div[^>]*id=[^>]*ddmDeliveryMessage[^>]*>([^<]*)[^<]*<span[^>]*>");
//			Matcher m = p.matcher(content);
//			if(m.find()){
//				content = replace(m.group(1));
//			}
//			Pattern pattern = Pattern.compile("nicht nach",Pattern.CASE_INSENSITIVE);
//			Matcher matcher = pattern.matcher(content);
//			if(matcher.find()){
//				skuBean.setStockStatus(0);
//				skuBean.setStockNum(0);
//				return true;
//			}else{
//				skuBean.setStockStatus(1);
//				skuBean.setStockNum(0);
//				return true;
//			}
		}
		return true;
	}

	/**
	 * get 货币
	 * @param val
	 * @return
	 */
	private String getCurrencyValue(String val){
	    String currency = StringUtils.substring(val, 0, 1);
	    String unit = StringUtils.EMPTY;
	    if("￥".equals(currency)){
	    	unit = Currency.codeOf("J"+currency).name();//添加一个对日本货币 J前缀
	    }else if("E".equals(currency)){
	    	unit = Currency.codeOf("€").name();//EUR
	    }else{
	    	unit = Currency.codeOf(currency).name();
	    }
	    return unit;
		
   }	
	
	public String getText(Elements es){
		if(es != null && es.size() > 0){
			return es.get(0).text();
		}
		return StringUtils.EMPTY;
	}
	
	public String getAttr(Elements es,String attrKey){
		if(es != null && es.size() > 0){
			return es.get(0).attr(attrKey);
		}
		return StringUtils.EMPTY;
	}

	private String replace(String dest){
		if(StringUtils.isBlank(dest)){
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));
		
	}

	private boolean setStock(String stockStr,SkuBean skuBean) {
		Pattern pattern = Pattern.compile("Only (\\d+) left in stock",Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(stockStr);
		if(matcher.find()){
			int stockNum = Integer.valueOf(replace(matcher.group(1)));
			if(stockNum > 0 ){
				skuBean.setStockStatus(2);
				skuBean.setStockNum(stockNum);
				return true;
			}
		}
		pattern = Pattern.compile("in stock|Usually ships|Want it",Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(stockStr);
		if(matcher.find()){
			skuBean.setStockStatus(1);
			skuBean.setStockNum(0);
			return true;
		}
		pattern = Pattern.compile("Out of Stock|Not yet published|Not yet released|Available for Pre-order|Currently unavailable",Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(stockStr);
		if(matcher.find()){
			skuBean.setStockStatus(0);
			skuBean.setStockNum(0);
			return true;
		}
		return false;
	}
	
	private String getSex(String cat) {
		String gender;
		if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN)){
			gender = SEX_WOMEN;
		} else if(StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = SEX_MEN;
		} else {
			gender = StringUtils.EMPTY;
		}
		return gender;
	}
	
	
}

