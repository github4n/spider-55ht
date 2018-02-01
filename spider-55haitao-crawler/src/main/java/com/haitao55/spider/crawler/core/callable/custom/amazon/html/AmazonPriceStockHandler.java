package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.service.impl.RedisService;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.SpringUtils;

/**
 * 
  * @ClassName: AmazonPriceStockHandler
  * @Description: 亞馬遜價格和庫存的處理器
  * @author songsong.xu
  * @date 2016年10月8日 上午11:36:23
  *
 */
public class AmazonPriceStockHandler {
	
	public static final int nThreads = 3;
	private ExecutorService service;
	
	private RedisService redisService = SpringUtils.getBean("redisService");
	
	public AmazonPriceStockHandler(){
		service = Executors.newFixedThreadPool(nThreads);
	}
	
	/*private static class Holder {
		public static final AmazonPriceStockHandler handler = new AmazonPriceStockHandler();
	}
	
	public static AmazonPriceStockHandler getInstance(){
		return Holder.handler;
	}*/

	public Map<String,SkuBean> process(Map<String,Url> skuUrls){
		Map<String,SkuBean> result = new HashMap<String,SkuBean>(); 
		try {
			if(skuUrls == null || skuUrls.size() == 0){
				return result;
			}
			List<Callable<SkuBean>> calls = new ArrayList<Callable<SkuBean>>();
			for(Map.Entry<String, Url> skuIdUrl: skuUrls.entrySet()){
				String skuId = skuIdUrl.getKey();
				Url url = skuIdUrl.getValue();
				String isSelf = redisService.hget(SpiderStringUtil.getAmazonDomain(url.getValue()), skuId);
				if(StringUtils.isBlank(isSelf) || StringUtils.equals(isSelf, Constants.IS_SELF)){
					calls.add(new AmazonCallable(skuId,url,false));
				}
			}
			if(calls.size() == 0 ){
				return result;
			}
			List<Future<SkuBean>> futures = service.invokeAll(calls);
			for(Future<SkuBean> f : futures){
				try {
					SkuBean skuBean = f.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					if(skuBean != null){
						if(Constants.IS_SELF.equals(skuBean.getIsSelf())){
							result.put(skuBean.getSkuId(), skuBean);
						}
						String domain = SpiderStringUtil.getAmazonDomain(skuBean.getUrl());
						if(StringUtils.isBlank(redisService.hget(domain, skuBean.getSkuId())) && StringUtils.isNotBlank(skuBean.getIsSelf())){
							redisService.hSet(domain, skuBean.getSkuId(), skuBean.getIsSelf());
						}
					}
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			service.shutdownNow();
		}
		return result;
	}

	private String replace(String dest){
		if(StringUtils.isBlank(dest)){
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));
		
	}
	
	public RedisService getRedisService() {
		return redisService;
	}

	public void setRedisService(RedisService redisService) {
		this.redisService = redisService;
	}

	public static void main(String[] args) {
		//System.out.println(Math.round((1 - Float.valueOf("49.99") / Float.valueOf("64.44")) * 100));
		AmazonPriceStockHandler handler = new AmazonPriceStockHandler();
		String content = HttpUtils.get("https://www.amazon.com/dp/B01LZHTBUD/?th=1");
		//"https://www.amazon.com/gp/twister/ajaxv2?sid=155-5166022-1324155&ptd=HOME_FURNITURE_AND_DECOR&sCac=1&twisterView=glance&pgid=furniture_display_on_website&rid=CQV3Q036ZRFF0DAN33GE&dStr=color_name%2Csize_name&auiAjax=1&json=1&dpxAjaxFlag=1&isUDPFlag=1&ee=2&nodeID=1057794&parentAsin=B019JB1DD8&enPre=1&dcm=1&storeID=furniture&psc=1&asinList=B00OL1469U&isFlushing=2&dpEnvironment=softlines&id=B00OL1469U&mType=full"
		//https://www.amazon.com/gp/twister/ajaxv2?sid=155-5166022-1324155&ptd=PANTS&sCac=1&twisterView=glance&pgid=apparel_display_on_website&rid=GQ1YMS7KTQ5DREK0BCZ8&dStr=size_name%2Ccolor_name&auiAjax=1&json=1&dpxAjaxFlag=1&isUDPFlag=1&ee=2&nodeID=1036592&parentAsin=B00VFUIKEC&enPre=1&storeID=apparel&psc=1&asinList=B00RNBQJHC&isFlushing=2&dpEnvironment=softlines&id=B00RNBQJHC&mType=full
		//System.out.println(content);
		//https://www.amazon.com/dp/B00RNBQJHC/?th=1&psc=1
		
		Pattern pShips = Pattern.compile("Ships from and sold by Amazon.com|Sold by(.*?)by Amazon",Pattern.CASE_INSENSITIVE);
		Matcher mShips = pShips.matcher(content);
		if(mShips.find()){
			System.out.println("iself");
		}
		
		if(StringUtils.isNotBlank(content)){
			Pattern p = Pattern.compile("<span[^>]*id=[^>]*priceblock[^>]*>(.*?)<");
			//#price > table > tbody > tr:nth-child(1) > td.a-span12.a-color-secondary.a-size-base > span
			Document document = Jsoup.parse(content);
			Elements es = document.select("span.a-text-strike");
			if(es != null && es.size() > 0 ){
				System.out.println("======================="+es.get(0).text());
			}
			Matcher m = p.matcher(content);
			while(m.find()){
				System.out.println("======================="+handler.replace(m.group(1)));
				
			}
			
			p = Pattern.compile("<span[^>]*a-text-strike[^>]*>(.*?)<");
			m = p.matcher(content);
			while(m.find()){
				System.out.println("======================="+handler.replace(m.group(1)));
			}
			//p = Pattern.compile("<tr[^>]*id=[^>]*regularprice_savings[^>]*>[^<]*<td[^>]*>[^<]*Save[^<]*<[^>]*>[^<]*<td[^>]*>(.*?)<");
			p = Pattern.compile("<tr[^>]*regularprice_savings[^>]*>[^<]*<td[^>]*>[^<]*<[^>]*td>[^<]*<td[^>]*>([^<]*)<");//(.*?)<
			m = p.matcher(content);
			while(m.find()){
				System.out.println("======================="+handler.replace(m.group(1)));
				
			}
			
			p = Pattern.compile("<div[^>]*id=[^>]*availability[^>]*>[^<]*<span[^>]*>([^<]*)<");
			m = p.matcher(content);
			while(m.find()){
				System.out.println("======================="+handler.replace(m.group(1)));
				Pattern pattern = Pattern.compile("in stock",Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher("In Stock");
				if(matcher.find()){
					System.out.println("----------"+matcher.group());
				} else {
					System.out.println("----------no matcher");
				}
			}
		}
	}
}
