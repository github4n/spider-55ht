package com.haitao55.spider.image.server.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.image.server.service.ImageService;
/**
 * @ClassName: ImageAction 
 * @Description: 图片服务
 *
 * @author songsong.xu
 * @date 2017年4月10日 下午6:23:46
 */
@Controller
@RequestMapping("/img")
public class ImageAction {

  private static final Logger logger = LoggerFactory.getLogger(ImageAction.class);
  @Autowired private ImageService imageService;

  
  @RequestMapping(path = "/get", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
  public @ResponseBody byte[] get(String url, HttpServletRequest request) {
    logger.info("request url [{}]", url);
    Pattern pattern = Pattern.compile("(http://|https://)");
    Matcher matcher = pattern.matcher(url);
    if (StringUtils.isBlank(url) || !matcher.find()) {
        return not_found(url);
    }
    byte[] bytes = imageService.getImg(new Image(url));
    if( null == bytes){
        return not_found(url); 
    }
    return bytes;
  }

  /**
   * @Title: not_found
   * @Description: 圖片找不到
   * @param @param url
   * @param @return    参数
   * @return byte[]    返回类型
   * @throws
    */
  private byte[] not_found(String url) {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream("notfound.jpg");
    try {
      return IOUtils.toByteArray(in);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(in);
    }
    return null;
  }
 
}
