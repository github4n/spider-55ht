package com.haitao55.spider.crawler.core.callable.custom.amazon.scan;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.kafka.SpiderKafkaProducer;
import com.haitao55.spider.common.kafka.SpiderKafkaResult;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.custom.amazon.imp.HTQueue;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.SpringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Consumer implements Runnable{

	
	private HTQueue<String> queue;
	private boolean stop = false;
	private static Long taskId = 1482470886504l;
	private SpiderKafkaProducer producer = SpringUtils.getBean("producer");
	private static final String topic = "spider_55haitao_product";
	private MongoTemplate mongoTemplate = SpringUtils.getBean("mongoTemplate");
	public  Consumer(HTQueue<String> queue){
		System.setProperty("webdriver.chrome.driver","/usr/local/bin/chromedriver");
		this.queue = queue;
	}
	
	public ChromeDriver getDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("no-sandbox");
		options.addArguments("start-maximized");
		options.setBinary(new File("/usr/bin/google-chrome-stable"));
		//options.addExtensions(new File(System.getProperty("user.dir")+"/config/2.3.21_0.crx"));
		options.addExtensions(new File(System.getProperty("user.dir")+"/config/block-image_1_1.crx"));
		//options.addExtensions(new File(System.getProperty("user.dir")+"/config/block-image_1_1.crx"));
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		ChromeDriver dr = new ChromeDriver(capabilities);
		dr.manage()
	      .timeouts()
	      .implicitlyWait(10, TimeUnit.SECONDS)
	      .pageLoadTimeout(30, TimeUnit.SECONDS)
	      .setScriptTimeout(30, TimeUnit.SECONDS);
		dr.manage().window().maximize();
		return dr;
	}
	
	
	@Override
	public void run() {
		while(!stop || queue.getTotalCount() > 0){
			String url = StringUtils.EMPTY;
			String json = StringUtils.EMPTY;
			ChromeDriver dr = getDriver();
			try {
				url = queue.get();
				System.out.println("Consumer url:"+url);
				if(StringUtils.isBlank(url)){
					Thread.sleep(3000);
					continue;
				}
				String content =  StringUtils.EMPTY;
				try{
					dr.get(url+"?psc=1&th=1");
					dr.manage().addCookie(new Cookie("test", "test", ".amazon.com", "/", new Date()));
					waitForComplete(dr);
					dr.navigate().refresh();
					content = dr.getPageSource();
				}catch(Exception e){
					e.printStackTrace();
				}finally {
					if(dr != null){
						dr.close();
						dr.quit();
					}
				}
				Pattern pShips = Pattern.compile("Ships from and sold by Amazon.com",Pattern.CASE_INSENSITIVE);
				Matcher mShips = pShips.matcher(content);
				if(!mShips.find()){
					System.out.println("url "+url+" is not sold by Amazon.com");
					offline(url);
				} else {
					try{
						//<div id="addon" class="a-section">
						Pattern p = Pattern.compile("<div[^>]*id=\"addon\"[^>]*class=\"a-section\"[^>]*>");
						Matcher m = p.matcher(content);
						if(m.find()){
							System.out.println(url+"==============Add-on Item");
							offline(url);
						}
						/*WebElement ele = new WebDriverWait(dr, 3).until(new com.google.common.base.Function<WebDriver, WebElement>() {
							@Override
							public WebElement apply(WebDriver driver) {
								System.out.println("====================addon is or not====================");
								driver.navigate().refresh();
						        return driver.findElement(By.id("addon"));
							}
							
						});*/
						//WebElement ele = dr.findElement(By.id("addon"));
						//if(ele != null){
						/*List<WebElement> addon = dr.findElements(By.id("addon"));
						if(addon != null && addon.size() > 0){
							System.out.println(url+"==============Add-on Item");
							offline(url);
						}*/
					}catch(NoSuchElementException nse){
						nse.printStackTrace();
						//noting to do
					}
					try{
						//<img alt="Prime Pantry" src="https://images-na.ssl-images-amazon.com/images/G/01/pantry/badge_pantry._CB342218256_.png" class="pantryBadge" id="pantry-badge" height="13" width="82" data-a-hires="https://images-na.ssl-images-amazon.com/images/G/01/pantry/Pantry_badge_highres._CB340814025_.png">
						Pattern p = Pattern.compile("<img[^>]*id=\"pantry-badge\"[^>]*>");
						Matcher m = p.matcher(content);
						if(m.find()){
							System.out.println(url+"==============pantry-badge");
							offline(url);
						}
						/*WebElement ele = new WebDriverWait(dr, 3).until(new com.google.common.base.Function<WebDriver, WebElement>() {
							@Override
							public WebElement apply(WebDriver driver) {
								System.out.println("====================pantry-badge is or not====================");
								driver.navigate().refresh();
						        return driver.findElement(By.id("pantry-badge"));
							}
							
						});*/
						//WebElement ele = dr.findElement(By.id("pantry-badge"));
						//if(ele != null){
						/*List<WebElement> pantry = dr.findElements(By.id("pantry-badge"));
						if(pantry != null && pantry.size() > 0){
							System.out.println(url+"==============primepantry");
							offline(url);
						}*/
					}catch(NoSuchElementException nse){
						nse.printStackTrace();
						//noting to do
					}
					
				}
			} catch (Throwable e) {
				e.printStackTrace();
				System.out.println("error url:"+url+",json:"+json);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(Thread.currentThread().getName()+"=============stoped============");
	}

	private void offline(String url) {
		RetBody ret = new RetBody();
		String docId = SpiderStringUtil.md5Encode(url);
		ret.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), docId));
		ret.setStock(new Stock(0));
		CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, ret, taskId+"",DocType.DELETE.toString());
		String crawlResult = jsonResult.parseTo();
		System.out.println("crawlResult:"+crawlResult);
		SpiderKafkaResult kafkaResult = producer.sendbyCallBack(topic, crawlResult);
		if(kafkaResult != null){
			System.out.println("send a message offset :"+kafkaResult.getOffset());
			DBObject object = new BasicDBObject();
			object.put("_id", docId);
			mongoTemplate.getCollection("urls1482470886504").remove(object);
		}
	}
	
	private static int waitForComplete(WebDriver webDriver) {
		String stateJs = " return document.readyState; ";
		JavascriptExecutor js = ((JavascriptExecutor) webDriver);
		String onload = js.executeScript(stateJs).toString();
		int ii = 10;
		int statu = 0;
		while (!"complete".equalsIgnoreCase(onload) && ii >= 0) {
			onload = js.executeScript(stateJs).toString();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("firefox url >>>" + webDriver.getCurrentUrl()+ " >>>statu>>>>>>>>>>>" + onload);
			ii--;
			// 浏览器如果超过时间限制就刷新,webDriver.navigate().refresh();
			if (ii % 5 == 0) {// 刷新浏览器
				webDriver.navigate().refresh();
			}
			if (ii == 0) {
				statu = -1;
			}
		}
		try {
			statu = 1;
		} catch (Exception e) {
			e.printStackTrace();
			return statu;
		}
		return statu;
	}
	
	public void stop(){
		queue.notified();
		this.stop = true;
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String url = "https://www.amazon.com/dp/B004XO28IQ/";
		String content = HttpUtils.get(url+"?th=1&psc=1");
		//Pattern p = Pattern.compile("<img[^>]*id=\"pantry-badge\"[^>]*>");
		Pattern p = Pattern.compile("<div[^>]*id=\"addon\"[^>]*class=\"a-section\"[^>]*>");
		Matcher m = p.matcher(content);
		if(m.find()){
			//System.out.println(url+"==============pantry-badge");
			System.out.println(url+"==============addon");
			//offline(url);
		}
		//System.out.println(SpiderStringUtil.md5Encode("https://www.amazon.com/dp/B01FAWAYKS/"));
	}

}
