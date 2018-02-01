package com.haitao55.spider.image.server.controller;

import java.io.File;
import java.util.Date;

import org.apache.commons.pool.PoolableObjectFactory;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HaiTaoPoolableObjectFactory implements PoolableObjectFactory{
    
    
    static {
        System.setProperty("webdriver.chrome.driver", "C:\\55haitao\\chromedriver.exe");
    }

  @Override
  public Object makeObject() throws Exception {
      ChromeOptions options = new ChromeOptions();
      options.addArguments("no-sandbox");
      options.addArguments("start-maximized");
      //options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
      options.setBinary("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
      options.addExtensions(new File("C:\\55haitao\\block-image_1_1.crx"));
      //options.addExtensions(new File("/home/jerome/.config/google-chrome/Default/Extensions/padekgcemlokbadohgkifijomclgjgif/2.3.21_0.crx"));
      DesiredCapabilities capabilities = new DesiredCapabilities();
      capabilities.setCapability(ChromeOptions.CAPABILITY, options);
      capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
      ChromeDriver dr = new ChromeDriver(capabilities);
      dr.manage().window().maximize();
      return dr;
  }

  @Override
  public void destroyObject(Object obj) throws Exception {
    if(obj != null && obj instanceof ChromeDriver ){
        ChromeDriver dr = (ChromeDriver) obj;
        dr.close();
        dr.quit();
        //waitForAlert(dr);
    }
  }
  
  public void waitForAlert(WebDriver driver) {
      Alert alert = null;
      boolean flag = false;
      try {
          new WebDriverWait(driver,10).until(ExpectedConditions.alertIsPresent());
          alert = driver.switchTo().alert();
          flag = true;
      }catch (Throwable NofindAlert) {
          //NofindAlert.printStackTrace();
          System.out.println("alertIsPresent is timeout"); 
          alert.dismiss();
      }
      if(flag) {
          alert.accept();
      }
  }

  @Override
  public boolean validateObject(Object obj) {
      if(obj != null && obj instanceof ChromeDriver ){
          ChromeDriver dr = (ChromeDriver) obj;
          try{
              dr.get("https://www.google.com/");
              dr.manage().addCookie(new Cookie("test", "test", ".google.com", "/", new Date()));
              return true;
          }catch(Throwable e){
              e.printStackTrace();
          }
      }
      return false;
  }

  @Override
  public void activateObject(Object obj) throws Exception {
    
  }

  @Override
  public void passivateObject(Object obj) throws Exception {
    
  }}
