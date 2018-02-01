package com.haitao55.spider.disposable.kimiss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

import main.java.com.UpYun;

/**
 * 
* Title: 根据url  抓取　home.kimiss.com　　马甲帐号
* Description: 
* Company: 55海淘
* @author zhaoxl 
* @date 2016年12月20日 下午4:10:20
* @version 1.0
 */
public class KimissCrawlerMain {
	private static final String  bucketName="spider-prerelease";
	private static final String  userName="shantao";
	private static final String  password="qDnnAdOTfpoWc";
	private static final String PLACEHOLDER = "\\{\\}";
	public static void main(String[] args) {
		UpYun upyun = new UpYun(bucketName, userName, password);
		List<String> urlList = new ArrayList<String>();
		try {
			url_package(urlList);
		} catch (ClassNotFoundException e) {
		}
		
		List<String> page_list = url_page_package(urlList);
		urlList.addAll(page_list);
//		String url="http://home.kimiss.com/home.php?mod=space&uid=2434057&do=friend&view=me&from=space";
//		urlList.add(url);
		new KimissHandler().process(urlList, upyun);
	}
	
	/**
	 * url 种子　迭代  分页url
	 * @param urlList
	 */
	private static List<String> url_page_package(List<String> urlList) {
		List<String> list = new ArrayList<String>();
		if(CollectionUtils.isEmpty(urlList)){
			return list;
		}
		String content = StringUtils.EMPTY;
		Document doc = null;
		Elements elements = null;
		int pages =1;
		String templateUrl = StringUtils.EMPTY;
		for (String url : urlList) {
			content = HttpUtils.get(url);
			doc = JsoupUtils.parse(content);
			elements = doc.select("div.pg a.last");
			if(CollectionUtils.isEmpty(elements)){
				pages =1;
			}else{
				String href = elements.attr("href");
				pages = Integer.parseInt(StringUtils.substringAfter(href, "page="));
			}
			if(pages==1){
				continue;
			}
			templateUrl = url.concat("&page={}");
			List<String> newUrlValues = new ArrayList<String>();
			for (int i = 2; i <= pages; i++) {
				String pageUrl = templateUrl.replaceAll(PLACEHOLDER, String.valueOf(i));
				newUrlValues.add(pageUrl);
			}
			list.addAll(newUrlValues);
		}
		return list;
	}

	/**
	 * 封装所有链接种子
	 * @param urlList
	 * @throws ClassNotFoundException 
	 */
	private static void url_package(List<String> urlList) throws ClassNotFoundException {
		// 加载 配置域名,访问google 云
		// WEB-INF/classes
		URL url = KimissCrawlerMain.class.getProtectionDomain().getCodeSource().getLocation();
		File jarFile = new File(url.getFile());
		String filepath = jarFile.getParent();
//		String filepath = Class.forName("com.haitao55.spider.disposable.kimiss.KimissCrawlerMain").getClassLoader().getResource("").getPath();
		String fileName = "kimiss";
		try {
			InputStream in = new FileInputStream(new File(filepath + "/" + fileName));
			String line = "";
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			line = buffer.readLine();
			while (line != null) {
				urlList.add(line);
				line = buffer.readLine();
			}
			buffer.close();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
