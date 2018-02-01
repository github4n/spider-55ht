package com.haitao55.spider.crawling.service.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.haitao55.spider.common.dao.LinkHaiTaoDAO;
import com.haitao55.spider.common.dos.LinksDO;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawling.service.service.CheckUrlsService;

@Service("checkUrlsService")
public class CheckUrlsServiceImpl implements CheckUrlsService {

  private static final Logger logger = LoggerFactory.getLogger(CheckUrlsServiceImpl.class);
  private final int nThreads = 3;
  private ExecutorService service = Executors.newFixedThreadPool(nThreads);
  private Similarity simi = new Similarity();
  private static final String URLS_DIR = "/urls/";
  @Autowired private LinkHaiTaoDAO linkHaiTaoDAO;
  private ConcurrentHashMap<Long, Long> cache = new ConcurrentHashMap<Long, Long>();
  private GenericObjectPool pool;

  static {
    System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
  }

  @PostConstruct
  public void init() {
  System.out.println("==================init start==================");
    HaiTaoPoolableObjectFactory factory = new HaiTaoPoolableObjectFactory();  
    pool = new GenericObjectPool(factory);  
    pool.setMaxActive(3); // 能从池中借出的对象的最大数目  
    pool.setMaxIdle(3); // 池中可以空闲对象的最大数目  
    pool.setMaxWait(-1); // 对象池空时调用borrowObject方法，最多等待多少毫秒  
    pool.setTimeBetweenEvictionRunsMillis(600000);// 间隔每过多少毫秒进行一次后台对象清理的行动  
    pool.setNumTestsPerEvictionRun(-1);// －1表示清理时检查所有线程  
    pool.setMinEvictableIdleTimeMillis(600000);// 设定在进行后台对象清理时，休眠时间超过了3000毫秒的对象为过期
    pool.setTestOnBorrow(true);
    new Thread(
            () -> {
              while (true) {
                Set<String> colls = linkHaiTaoDAO.getAllColls();
                logger.info("Set<String> colls {}",colls);
                colls.forEach(
                    coll -> {
                      String coll_suffix = StringUtils.substringAfter(coll, "links");
                      long taskId = Long.valueOf(coll_suffix);
                      boolean isToday = isToday(taskId);
                      logger.info("collecionforEach {},aa isToday {}",taskId,isToday);
                      if (cache.get(taskId) == null && isToday(taskId)) {
                        List<LinksDO> linksList = linkHaiTaoDAO.queryAllLinks(taskId, "init", 10000);
                        if (linksList != null && linksList.size() > 0) {
                          linksList.forEach(
                              links -> {
                                String currentUrl = null;
                                int count = 1;
                                boolean ex = false;
                                do {
                                  logger.info(
                                      "init start request collecion {}, url {},count {}",
                                      taskId,
                                      links.getOrignal_url(),
                                      count);
                                  ChromeDriver driver = null;
                                  try {
                                    if (StringUtils.contains(
                                        links.getOrignal_url(), "item2.gmarket.co.kr")) {
                                      currentUrl = links.getTarget_url();
                                      break;
                                    }
                                    driver = (ChromeDriver) pool.borrowObject();
                                    driver.get(links.getOrignal_url());
                                    waitForAlert(driver);
                                    waitForComplete(driver);
                                    TimeUnit.SECONDS.sleep(5);
                                    currentUrl = driver.getCurrentUrl();
                                    ex = false;
                                  } catch (Throwable e) {
                                    logger.info("init occured exceptions url {}", links.getOrignal_url());
                                    e.printStackTrace();
                                    count++;
                                    ex = true;
                                  } finally {
                                    try {
                                      pool.returnObject(driver);
                                    } catch (Exception e) {
                                      e.printStackTrace();
                                    }
                                  }
                                } while (ex && count < 5);
                                if (StringUtils.isBlank(currentUrl)) {
                                  logger.info(
                                      "url {} retryed 5 times and error", links.getOrignal_url());
                                  currentUrl = links.getOrignal_url();
                                }
                                double realPercent = simi.similarPercent(links.getTarget_url(), currentUrl);
                                BigDecimal bd = new BigDecimal(realPercent);
                                bd = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
                                links.setResult_url(currentUrl);
                                links.setResult_percent(bd.doubleValue());
                                if (bd.doubleValue() >= links.getExpect_percent()) {
                                  links.setStatus("normal");
                                } else {
                                  links.setStatus("error");
                                }
                                links.setUpdate_time(System.currentTimeMillis());
                                try{
                                    logger.info("taskId {}， links {}",taskId,links.toString());
                                    linkHaiTaoDAO.upsertLinks(taskId, links);
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                
                              });
                        }
                      }
                    });
                try {
                  TimeUnit.MINUTES.sleep(10);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            })
        .start();
  }

  @Override
  public JsonObject checks(JsonArray param, String rootDir) {
    JsonObject ret = new JsonObject();
    if (param == null || param.size() == 0) {
      ret.addProperty("code", "-1");
      ret.addProperty("msg", "fail");
      return ret;
    }
    HTQueue<JsonObject> queue = new HTQueue<JsonObject>();
    List<LinksDO> linksDOList = new ArrayList<LinksDO>();
    param.forEach(
        p -> {
          JsonObject object = p.getAsJsonObject();
          String sourceUrl = object.getAsJsonPrimitive("sourceUrl").getAsString();
          String destUrl = object.getAsJsonPrimitive("destUrl").getAsString();
          double expectPercent = object.getAsJsonPrimitive("percent").getAsDouble();
          LinksDO linksDO = new LinksDO();
          linksDO.setOrignal_doc_id(SpiderStringUtil.md5Encode(sourceUrl));
          linksDO.setOrignal_url(sourceUrl);
          linksDO.setTarget_url(destUrl);
          linksDO.setExpect_percent(expectPercent);
          linksDO.setCreate_time(System.currentTimeMillis());
          linksDO.setStatus("init");
          linksDOList.add(linksDO);
          queue.add(object);
        });
    long taskId = System.currentTimeMillis();
    cache.put(taskId, taskId);
    linkHaiTaoDAO.insertLinks(taskId, linksDOList);

    // async start check tasks
    startTasks(queue, taskId);

    ret.addProperty("code", "200");
    ret.addProperty("msg", "success");
    ret.addProperty("data", taskId);
    return ret;
  }

  @Override
  public JsonArray gets(JsonObject param, String rootDir) {

    logger.info("gets param  <{}>  ", param);
    JsonArray arr = new JsonArray();
    String data = param.getAsJsonPrimitive("data").getAsString();
    if (StringUtils.isBlank(data)) {
      JsonObject result = new JsonObject();
      result.addProperty("result", "data is not null");
      arr.add(result);
      return arr;
    }
    long taskId = Long.valueOf(data);
    logger.info("gets taskId  <{}>  ", taskId);
    String urlsDir = rootDir + URLS_DIR;
    File file = new File(urlsDir);
    if (!file.exists()) {
      file.mkdir();
    }
    String fileName = taskId + "";
    String filePath = urlsDir + fileName;
    try {
      BufferedWriter bw =
          new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8"));
      List<LinksDO> linksList = linkHaiTaoDAO.queryAllLinks(taskId, null, 10000);
      linksList.forEach(
          links -> {
            JsonObject result = new JsonObject();
            result.addProperty("orignal_url", links.getOrignal_url());
            result.addProperty("target_url", links.getTarget_url());
            result.addProperty("result_url", links.getResult_url());
            result.addProperty("expect_percent", links.getExpect_percent());
            result.addProperty("result_percent", links.getResult_percent());
            result.addProperty("status", links.getStatus());
            result.addProperty("check_time", links.getUpdate_time());
            arr.add(result);
            writeToFile(bw, links);
          });
      IOUtils.closeQuietly(bw);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return arr;
  }

  public JsonArray getsFromFile(JsonObject param, String rootDir) {
    String urlsDir = rootDir + URLS_DIR;
    String fileName = urlsDir + param.getAsJsonPrimitive("data").getAsString();
    JsonArray arr = new JsonArray();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
      String line = br.readLine();
      while (line != null) {
        JsonObject obj = JsonUtils.json2bean(line, JsonObject.class);
        if (obj != null) {
          arr.add(obj);
        }
        line = br.readLine();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        IOUtils.closeQuietly(br);
      }
    }
    return arr;
  }

  private void startTasks(final HTQueue<JsonObject> queue, final long taskId) {

    new Thread(
            () -> {
              AtomicLong count = new AtomicLong(0);
              while (true) {
                List<Future<JsonObject>> results = new ArrayList<Future<JsonObject>>();
                int i = 0;
                //每次提交nThreads个任务
                while (queue.getTotalCount() > 0 && i < nThreads) {
                  JsonObject json = queue.get();
                  Future<JsonObject> f =
                      service.submit(
                          () -> {
                            ChromeDriver driver = null;
                            try{
                                String sourceUrl = json.getAsJsonPrimitive("sourceUrl").getAsString();
                                String destUrl = json.getAsJsonPrimitive("destUrl").getAsString();
                                double expectPercent = json.getAsJsonPrimitive("percent").getAsDouble();
                                String currentUrl = null;
                                if(StringUtils.contains(sourceUrl, "item2.gmarket.co.kr")){
                                    currentUrl = destUrl;
                                } else {
                                    driver = (ChromeDriver) pool.borrowObject();
                                    currentUrl = getSourceUrlByChrome(driver, sourceUrl);
                                }
                                if(currentUrl == null ){
                                    currentUrl = sourceUrl;
                                }
                                // check the url similarity
                                double realPercent = simi.similarPercent(destUrl, currentUrl);
                                BigDecimal bd = new BigDecimal(realPercent);
                                bd = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
                                json.addProperty("currentUrl", currentUrl);
                                json.addProperty("similarity", bd.doubleValue());
                                if (bd.doubleValue() >= expectPercent) {
                                  json.addProperty("status", "normal");
                                } else {
                                  json.addProperty("status", "error");
                                }
                                if (count.incrementAndGet() % 5 == 0) {
                                  System.out.println("process urls: " + count.get());
                                }
                                logger.info(
                                    "process sourceUrl {} ,destUrl {} ,currentUrl {},expectPercent {} , realPercent {}",
                                    sourceUrl,
                                    destUrl,
                                    currentUrl,
                                    expectPercent,
                                    realPercent);
                            }catch(Exception e){
                                e.printStackTrace();
                            } finally {
                                pool.returnObject(driver);
                            }
                            return json;
                          });
                  results.add(f);
                  i++;
                }
                //没有任务了结束
                if (results.size() == 0) {
                  logger.info("async task {} is complete", taskId);
                  break;
                }
                results.forEach(
                    future -> {
                      try {
                        JsonObject result = future.get(100, TimeUnit.SECONDS);
                        String sourceUrl = result.getAsJsonPrimitive("sourceUrl").getAsString();
                        String destUrl = result.getAsJsonPrimitive("destUrl").getAsString();
                        double expectPercent = result.getAsJsonPrimitive("percent").getAsDouble();
                        String currentUrl = result.getAsJsonPrimitive("currentUrl").getAsString();
                        double similarity = result.getAsJsonPrimitive("similarity").getAsDouble();
                        String status = result.getAsJsonPrimitive("status").getAsString();
                        LinksDO linksDO = new LinksDO();
                        linksDO.setOrignal_doc_id(SpiderStringUtil.md5Encode(sourceUrl));
                        linksDO.setOrignal_url(sourceUrl);
                        linksDO.setTarget_url(destUrl);
                        linksDO.setResult_url(currentUrl);
                        linksDO.setExpect_percent(expectPercent);
                        linksDO.setResult_percent(similarity);
                        linksDO.setStatus(status);
                        linksDO.setUpdate_time(System.currentTimeMillis());
                        linkHaiTaoDAO.upsertLinks(taskId, linksDO);
                      } catch (InterruptedException e) {
                        e.printStackTrace();
                      } catch (ExecutionException e) {
                        e.printStackTrace();
                      } catch (TimeoutException e) {
                        e.printStackTrace();
                      }
                    });
              }
              cache.remove(taskId);
            })
        .start();
  }

  private String getSourceUrlByChrome(ChromeDriver driver, String sourceUrl) {
    boolean ex = false;
    int count = 0;
    do {
      try {
        driver.get(sourceUrl);
        waitForAlert(driver);
        waitForComplete(driver);
        TimeUnit.SECONDS.sleep(5);
        ex = false;
        return driver.getCurrentUrl();
      } catch (Throwable e) {
        e.printStackTrace();
        count++;
        ex = true;
      }
    } while (ex && count < 5);
    return null;
  }

  public ChromeDriver getDriver() {
    ChromeOptions options = new ChromeOptions();
    // options.setBinary("/opt/google/chrome/chrome");
    options.setBinary("/usr/bin/google-chrome-stable");
    // options.addExtensions(new
    // File("/home/jerome/.config/google-chrome/Default/Extensions/padekgcemlokbadohgkifijomclgjgif/2.3.21_0.crx"));
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
    capabilities.setCapability(
        CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
    ChromeDriver dr = new ChromeDriver(capabilities);
    dr.manage()
        .timeouts()
        .implicitlyWait(200, TimeUnit.SECONDS)
        .pageLoadTimeout(200, TimeUnit.SECONDS)
        .setScriptTimeout(200, TimeUnit.SECONDS);
    dr.manage().window().maximize();
    return dr;
  }

  private int waitForComplete(WebDriver webDriver) {
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
      System.out.println(
          "firefox url >>>" + webDriver.getCurrentUrl() + " >>>statu>>>>>>>>>>>" + onload);
      ii--;
      // 浏览器如果超过时间限制就刷新,webDriver.navigate().refresh();
      if (ii % 5 == 0) { // 刷新浏览器
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

  public void waitForAlert(WebDriver driver) {
      Alert alert = null;
      boolean flag = false;
      try {
          new WebDriverWait(driver,10).until(ExpectedConditions.alertIsPresent());
          alert = driver.switchTo().alert();
          flag = true;
      }catch (Throwable NofindAlert) {
          logger.info("waitForAlert occurred wait for alert is present, {}",NofindAlert.getClass());
          //NofindAlert.printStackTrace();
      }
      if(flag) {
          alert.accept();
      }
  }

  private void writeToFile(BufferedWriter bw, LinksDO links) {
    String sourceUrl = links.getOrignal_url();
    String destUrl = links.getTarget_url();
    double expectPercent = links.getExpect_percent();
    String currentUrl = links.getResult_url();
    double similarity = links.getResult_percent();
    BigDecimal bd = new BigDecimal(similarity);
    bd = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
    String status = links.getStatus();
    try {
      StringBuilder sb = new StringBuilder();
      sb.append(expectPercent).append("^");
      sb.append(bd.doubleValue()).append("^");
      sb.append(status).append("^");
      sb.append(sourceUrl).append("^");
      sb.append(destUrl).append("^");
      sb.append(currentUrl).append("^");
      sb.append("\n");
      bw.write(sb.toString());
      bw.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private boolean isToday(long time){
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
      calendar.setTime(new Date(time));
      int year = calendar.get(Calendar.YEAR);
      int mon = calendar.get(Calendar.MONTH);
      int dayOfMon = calendar.get(Calendar.DAY_OF_MONTH);
      logger.info("year {}, mon {},dayOfMon {}",year,mon,dayOfMon);
      calendar = new GregorianCalendar();
      calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
      int tyear = calendar.get(Calendar.YEAR);
      int today = calendar.get(Calendar.DAY_OF_MONTH);
      int tmonth = calendar.get(Calendar.MONTH);
      logger.info("tyear {}, today {},tmonth {}",tyear,today,tmonth);
      return (year == tyear && mon == tmonth && dayOfMon == today);
  }
  
  
}
