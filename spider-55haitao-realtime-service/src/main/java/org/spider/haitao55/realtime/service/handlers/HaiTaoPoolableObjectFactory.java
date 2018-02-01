package org.spider.haitao55.realtime.service.handlers;

import java.io.File;
import java.util.Date;
import org.apache.commons.pool.PoolableObjectFactory;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

public class HaiTaoPoolableObjectFactory implements PoolableObjectFactory{

  @Override
  public Object makeObject() throws Exception {
      ChromeOptions options = new ChromeOptions();
      options.addArguments("no-sandbox");
      options.addArguments("start-maximized");
      options.setBinary("/usr/bin/google-chrome-stable");
      options.addExtensions(new File(System.getProperty("user.dir")+"/config/block-image_1_1.crx"));
      //options.addExtensions(new File("/home/jerome/.config/google-chrome/Default/Extensions/padekgcemlokbadohgkifijomclgjgif/2.3.21_0.crx"));
      DesiredCapabilities capabilities = new DesiredCapabilities();
      capabilities.setCapability(ChromeOptions.CAPABILITY, options);
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
