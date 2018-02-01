package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.context.Keyword;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
  * @ClassName: AmazonUS
  * @Description: 美國亞馬遜的解析處理器
  * @author songsong.xu
  * @date 2016年9月28日 上午9:37:46
  *
 */
public class Copy_2_of_AmazonUS extends AbstractSelect{
	
	private final String ITEM_URL_TEMPLATE = "https://www.amazon.com/dp/#itemId#/?th=1&psc=1";
	private static final String HTTPS = "https://";
	private static final String DOMAIN = "www.amazon.com";
	private static final String HOST   = HTTPS + DOMAIN;
	private static final String SUFFIX = "&psc=1&asinList=#skuId#&isFlushing=2&dpEnvironment=softlines&id=#skuId#&mType=full";
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_AMAZON);

	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		/*String url = context.getUrl().getValue();
		long start = System.currentTimeMillis();
		for(int i =0 ; i < 3; i++){
			URL wUrl = new URL(context.getUrl().getValue());
			File target = new File("/data/");
			String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
			DownloadInfo info = null;
			if(StringUtils.isNotBlank(proxyRegionId)){
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				ProxyInfo p = new ProxyInfo(proxy.getIp(), proxy.getPort());
				info = new DownloadInfo(wUrl,p);
			} else {
				info = new DownloadInfo(wUrl);
			}
			WGet wget = new WGet(info,  WGet.calcName(wUrl, target));
			wget.download();
			content = wget.getHtml(info);
			logger.info("url {} ,status {}",context.getUrl().getValue(),wget.getInfo().getState().toString());
			if(StringUtils.isNotBlank(content)){
				Document doc = JsoupUtils.parse(content, url);
				XDocument xdoc = new XDocument(url, doc);
				context.setCurrentDoc(xdoc);
				break;
			}
			Thread.sleep(5000);
		}
		long end = System.currentTimeMillis();*/
		
		/*long start = System.currentTimeMillis();
		DesiredCapabilities caps = DesiredCapabilities.phantomjs();
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,"/home/gphonebbs/phantomjs-2.1.1-linux-x86_64/bin/phantomjs");
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "useragent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "loadImages", false);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "javascriptEnabled", true);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "resourceTimeout", 10000);
		caps.setBrowserName("Google Chrome");
		caps.setVersion("55");
		caps.setCapability("cookiesEnabled", true);
		caps.setCapability("takesScreenshot", false);
		caps.setCapability("acceptSslCerts", true);
		WebDriver dr = new PhantomJSDriver(caps);
		dr.manage().addCookie(new Cookie.Builder("s_ev22", "%5B%5B%27stock%27%2C%271475913104439%27%5D%5D").domain(".amazon.com").expiresOn(new Date()).build());
		dr.manage()
	      .timeouts()
	      .implicitlyWait(10, TimeUnit.SECONDS)
	      .pageLoadTimeout(10, TimeUnit.SECONDS)
	      .setScriptTimeout(10, TimeUnit.SECONDS);
		dr.manage().window().setSize(new Dimension(1024, 768));
		String url = context.getUrl().getValue();
		for(int i =0 ; i < 3; i++){
			dr.navigate().to(url);
			content = dr.getPageSource();
			if(StringUtils.isNotBlank(content)){
				Document doc = JsoupUtils.parse(content, url);
				XDocument xdoc = new XDocument(url, doc);
				context.setCurrentDoc(xdoc);
				break;
			}
			Thread.sleep(3000);//retry sleep 3s
		}
		if(dr != null){
			dr.quit();
		}*/
		long start = System.currentTimeMillis();
		for(int i = 0 ; i < 10;i++){
			try{
				content = this.getInputString(context);
				context.get(Keyword.DOC.getValue());
				if(StringUtils.isBlank(content) || StringUtils.containsIgnoreCase(content, "Robot Check")){
					ConnectNetWork.restartAdsl();
					continue;
				} else {
					break;
				}
			}catch(IOException e){
				//ConnectException | NoRouteToHostException | SocketTimeoutException e
				if(e instanceof ConnectException
						|| e instanceof NoRouteToHostException
						|| e instanceof SocketTimeoutException ){
					Thread.sleep(3000);
					continue;
				}
				throw e;
			}
		}
		
		/*String proxy = Crawler.create().url("http://adsl.55haitao.com/pool?pool=9999").method("get").resultAsString();
		JsonArray arr = new JsonArray();
		if(StringUtils.isNotBlank(proxy)){
			arr = JsonUtils.json2bean(proxy, JsonArray.class);
		}
		int count = 0;
		boolean running = true;
		while(running){
			proxy = Crawler.create().url("http://adsl.55haitao.com/pool?pool=9999").method("get").resultAsString();
			if(StringUtils.isNotBlank(proxy)){
				arr = JsonUtils.json2bean(proxy, JsonArray.class);
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
							content = Crawler.create().method("get").url(url).proxy(true).proxyAddress(proxyAddr).proxyPort(Integer.valueOf(proxyPort)).resultAsString();
						}catch(ConnectException | NoRouteToHostException | SocketTimeoutException e){
							continue;
						}
						if(StringUtils.isBlank(content) || StringUtils.containsIgnoreCase(content, "Robot Check")){
							content = StringUtils.EMPTY;
							Thread.sleep(3000);
							continue;
						}
						Document doc = JsoupUtils.parse(content, url);
						XDocument xdoc = new XDocument(url, doc);
						context.setCurrentDoc(xdoc);
						isSuc = true;
						break;
					}
					logger.info("url {} ,isSuc {}",url,isSuc);
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
		String testData = StringUtils.substringBetween(content, "\"indexToColor\"", "</script>");
		String title = StringUtils.EMPTY;
		if(StringUtils.isNotBlank(testData)){
			title = StringUtils.substringBetween(testData, ",\"title\":\"", "\",");
		}
		long end = System.currentTimeMillis();
		logger.info("get url {} ,title {}, consume time {}",context.getUrl().getValue(),title,(end-start));
		//Currently unavailable
		Pattern p = Pattern.compile("Currently unavailable|Available from these sellers");
		Matcher m = p.matcher(content);
		if(m.find()){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"amazon.com itemUrl:"+context.getUrl().toString()+" is offline.");
		}
		Map<String,Url> skuUrls = new HashMap<String,Url>();
		if (StringUtils.isNotBlank(content)) {
			String skuData = StringUtils.substringBetween(content, "window.DetailPage", "</script>");
			if(StringUtils.isNotBlank(skuData)){//有sku的情況
				String stockUrl = StringUtils.substringBetween(skuData, "\"immutableURLPrefix\":\"", "\",");
				String asin_variation_values = StringUtils.substringBetween(skuData, "\"asin_variation_values\":", ",\"contextMetaData\"");
				Type typeMapMap = new TypeToken<Map<String,Map<String,String>>>(){}.getType();
				Map<String,Map<String,String>> asin_variation_valuesMapMap =  JsonUtils.json2bean(asin_variation_values, typeMapMap);
				if(asin_variation_valuesMapMap != null && asin_variation_valuesMapMap.size() > 0 ){
					for(Map.Entry<String,Map<String,String>> entry : asin_variation_valuesMapMap.entrySet()){
						String skuId = entry.getKey();
						Url skuUrl = new Url(HOST+stockUrl+SUFFIX.replace("#skuId#", skuId));
						skuUrl.setTask(context.getUrl().getTask());
						skuUrls.put(skuId,skuUrl);
					}
				}
			}  else {//無sku的情況
				String skuId = StringUtils.substringBetween(context.getCurrentUrl(), "/dp/", "/");
				Url skuUrl = new Url(ITEM_URL_TEMPLATE.replace("#itemId#", skuId));
				skuUrl.setTask(context.getUrl().getTask());
				skuUrls.put(skuId, skuUrl);
			}
			
			Map<String,SkuBean> skuResult = new AmazonPriceStockHandler().process(skuUrls);
			if(skuResult == null || skuResult.size() == 0 ){//無自營商品 或者失敗
				throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"amazon.com itemUrl:"+context.getUrl().toString()+" parsered error.");
			}
			logger.info("url {},skuId {}",context.getUrl().toString(),Arrays.toString(skuResult.keySet().toArray()));
			DefaultAmazonParser defaultAmazon = new DefaultAmazonParser(skuResult);
			RetBody ret = defaultAmazon.retboby(context);
			setOutput(context, ret);
		}
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		/*JsonArray arr = JsonUtils.json2bean("[\"49.89.137.172:8888\",\"49.89.137.172:3128\"]", JsonArray.class);
		if(arr != null && arr.size() > 0 ){
			for(int i =0 ; i < arr.size(); i++){
				String result = arr.get(i).getAsJsonPrimitive().getAsString();
				System.out.println(result);
			}
		}
		System.out.println(JsonUtils.json2bean("[\"49.89.137.172:8888\"]", JsonArray.class));*/
//		/System.out.println(URLDecoder.decode("%7B%22support%22%3A%221%22%7D"));
	/*	String url = "https://www.groupon.com/deals/gg-lady-cotton-hooded-parka-jacket";
		String html = Crawler.create().timeOut(10000).retry(3).url(url).resultAsString();
		String sku = StringUtils.substringBetween(html, "\"variations\":", "\"uuid\"");
		System.out.println(sku);
		String content = HttpUtils.get("https://www.amazon.de/dp/B01IHS4IIQ/?th=1&psc=1");
		Document document = Jsoup.parse(content);
		//div#detailBullets_feature_div > ul > li > span
		Elements es = document.select("div#detailBullets_feature_div > ul > li > span");
		if(es != null && es.size() > 0){
			for(Element e : es){
				System.out.println(e.text());
			}
		}*/
	}
	

}
