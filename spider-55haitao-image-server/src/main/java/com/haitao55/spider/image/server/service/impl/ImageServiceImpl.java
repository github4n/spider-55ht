package com.haitao55.spider.image.server.service.impl;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.image.server.service.ImageService;
import com.haitao55.spider.image.server.utils.Constants;
import main.java.com.UpYun;
/**
 * @ClassName: ImageServiceImpl 
 * @Description: 图片下载
 *
 * @author songsong.xu
 * @date 2017年4月10日 下午4:50:15
 */
public class ImageServiceImpl implements ImageService {

  private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_IMAGE_SERVICE);
  private static final String UPYUN_DIR = "/prodimage/";
  private static final int MAX_RETRY = 3;
  private UpYun upyun;
  private String upyunAddress;

  @Override
  public byte[] getImg(Image image) {
    //计算出upyun的图片地址
    createImageRepertoryUrl(image);
    logger.info("image src {},upyun {}",image.getOriginalUrl(),image.getRepertoryUrl());
    //下载图片
    downloadImageData(image);
    
    // 異步上传图片
    new Thread(()-> {
        uploadImage(image);
    }).start();
    
    return image.getData();
  }

  public void uploadImage(Image image) {
    upyun.setTimeout(60);
    upyun.setApiDomain(UpYun.ED_AUTO);
    String path = StringUtils.substring(image.getRepertoryUrl(), StringUtils.indexOf(image.getRepertoryUrl(), UPYUN_DIR));
    boolean result = false;
    for (int i = 0; i < MAX_RETRY; i++) {
      try {
        result = this.upyun.writeFile(path, image.getData());
      } catch (Throwable e) {
        e.printStackTrace();
      }
      if (result) {
        break;
      }
      sleep(500);
    }
  }

  public void downloadImageData(Image image) {
    byte[] imageData = null;
    for (int i = 0; i < MAX_RETRY; i++) {
      try {
        imageData =
            Crawler.create()
                .retry(1)
                .timeOut(60000)
                .url(image.getOriginalUrl())
                .method(HttpMethod.GET.getValue())
                .resultAsBytes();
        if (imageData != null) {
          break;
        }
        this.sleep(500);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    image.setData(imageData);
  }

  public void createImageRepertoryUrl(Image image) {
    if (image == null) {
      logger.error("image is null while fill image-url");
      return;
    }
    String repertoryImageUrl =
        (new StringBuilder())
            .append(this.getRepertoryImageAddressPrefix())
            .append(UPYUN_DIR)
            .append(SpiderStringUtil.upYunFileName(image.getOriginalUrl()))
            .append(this.getRepertoryImageAddressSuffix())
            .toString();
    image.setRepertoryUrl(repertoryImageUrl);
  }

  private String getRepertoryImageAddressPrefix() {
    return upyunAddress;
  }

  private String getRepertoryImageAddressSuffix() {
    return ".jpg";
  }

  public UpYun getUpyun() {
    return upyun;
  }

  public void setUpyun(UpYun upyun) {
    this.upyun = upyun;
  }

  public String getUpyunAddress() {
    return upyunAddress;
  }

  public void setUpyunAddress(String upyunAddress) {
    this.upyunAddress = upyunAddress;
  }
  
  private void sleep(long time) {
      try {
        Thread.sleep(time);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
}
