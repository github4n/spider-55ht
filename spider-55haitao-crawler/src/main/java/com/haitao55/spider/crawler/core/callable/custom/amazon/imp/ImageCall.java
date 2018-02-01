package com.haitao55.spider.crawler.core.callable.custom.amazon.imp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import main.java.com.UpYun;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.haitao55.spider.common.dos.ImageDO;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.SpringUtils;
import com.haitao55.spider.image.service.ImageService;

public class ImageCall implements Callable<Map<String, List<Image>>>{
	
	private static final String UPYUN_DIR = "/prodimage/";
	private UpYun upyun = SpringUtils.getBean("upyun");
	private ImageService imageService = SpringUtils.getBean("imageService");
	private static final String upyunAddress = "http://st-prod.b0.upaiyun.com";
	private static Long taskId = 1482470886504l;
	private String url ;
	private Map<String, List<Image>> images;
	private ProxyPool pool;
	
	public ImageCall(String url,Map<String, List<Image>> images,ProxyPool pool){
		this.url = url;
		this.images =images;
		this.pool = pool;
	}
	@Override
	public Map<String, List<Image>> call() throws Exception {
		for (Map.Entry<String, List<Image>> entry : images.entrySet()) {
			List<Image> imageSet = entry.getValue();
			// String skuId = entry.getKey();
			List<ImageDO> imageDoList = new ArrayList<ImageDO>();
			for (Image image : imageSet) {
				createImageRepertoryUrl(image);
				//if (!outputService.existInRepertory(image)) {// 图片在图片库中不存在,才执行实际的下载和上传过程
				boolean srcExist = true;
				try {
					srcExist = imageService.isSrcExist(taskId, image.getOriginalUrl());
				} catch (Throwable e) {
					e.printStackTrace();
					srcExist = existInRepertory(image);
				}
				if(!srcExist){
					for (int i = 0; i < 5; i++) {
						long begin = System.currentTimeMillis();
						this.downloadImageData(image);// 下载图片的逻辑都是一样的
						System.out.println("image:"+image.getOriginalUrl()+",consume time:"+(System.currentTimeMillis()-begin));
						if (StringUtils.isNotBlank(image.getOriginalUrl()) && ArrayUtils.isNotEmpty(image.getData())) {
							break;
						}
						try {
							Thread.sleep(i*1000);//downloading picture
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					uploadImage(image);
					ImageDO imageDO = new ImageDO();
					imageDO.setDoc_id(SpiderStringUtil.md5Encode(url));
					imageDO.setSrc(image.getOriginalUrl());
					imageDO.setSrc_key(SpiderStringUtil.md5Encode(image.getOriginalUrl()));
					imageDO.setCdn(image.getRepertoryUrl());
					imageDO.setCdn_key(StringUtils.substringBetween(image.getRepertoryUrl(), "/prodimage/", ".jpg"));
					imageDO.setStatus("1");//已下载
					imageDO.setCreate_time(System.currentTimeMillis());
					imageDoList.add(imageDO);
				}
			}
			
			try {
				//save images
				imageService.saveImages(taskId, imageDoList);
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return images;
	}
	
	public void createImageRepertoryUrl(Image image) {
		if (image == null) {
			return;
		}
		String repertoryImageUrl = (new StringBuilder())
				.append(upyunAddress)
				.append(UPYUN_DIR)
				.append(SpiderStringUtil.upYunFileName(image.getOriginalUrl()))
				.append(this.getRepertoryImageAddressSuffix()).toString();
		image.setRepertoryUrl(repertoryImageUrl);
	}
	
	
	private void downloadImageData(Image image) {
		try {
			byte[] imageData = null;
			Map<String,Object> headers = new HashMap<String,Object>();
			headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
			headers.put("Upgrade-Insecure-Requests", "1");
			headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			headers.put("Host", "images-na.ssl-images-amazon.com");
			headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
			headers.put("Cache-Control", "max-age=0");
			headers.put("Connection", "keep-alive");
			for(int i = 0 ; i < 20; i++){
				try {
					ProxyPool.ProxyHost host = pool.pollHost();
					imageData = Crawler.create().retry(1).timeOut(30000)
							.url(image.getOriginalUrl()).header(headers)
							.method(HttpMethod.GET.getValue()).proxy(true).proxyAddress(host.getIp()).proxyPort(host.getPort())
							.resultAsBytes();
					if (imageData != null) {
						break;
					}
					Thread.sleep(1000);
				} catch (IOException e) {
					e.printStackTrace();
					Thread.sleep(1000);
				}
			}
			image.setData(imageData);
		} catch (Throwable e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public boolean existInRepertory(Image image) {
		String path=StringUtils.substring(image.getRepertoryUrl(), StringUtils.indexOf(image.getRepertoryUrl(), UPYUN_DIR));
		Map<String, String> fileInfo = upyun.getFileInfo(path);
		if(null==fileInfo||fileInfo.isEmpty()){
			return false;//不存在
		}
		return true;//存在
	}
	public void uploadImage(Image image) {
		upyun.setTimeout(30);
		upyun.setApiDomain(UpYun.ED_AUTO);
		String path=StringUtils.substring(image.getRepertoryUrl(), StringUtils.indexOf(image.getRepertoryUrl(), UPYUN_DIR));
		boolean  result = false;
		for(int i =0; i < 5; i++){
			try{
				result = this.upyun.writeFile(path, image.getData());
			}catch(Throwable e){
				e.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			if(result){
				image.setData(null);
				break;
			}
		}
	}
	private String getRepertoryImageAddressSuffix() {
		return ".jpg";
	}
	
}