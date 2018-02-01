package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 
  * @ClassName: AmazonCreateSeeds
  * @Description: 生成列表的種子
  * @author songsong.xu
  * @date 2016年10月13日 下午1:41:39
  *
 */
public class AmazonCreateSeeds  {
	
	public static String BASE_URL = "https://www.amazon.com/mn/search/ajax/?";
	//private static final String KEYWORD_URL = "https://www.#domain#/mn/search/ajax/?rh=n%3A3760901%2Cn%3A3777371%2Ck%3ALotions_Lubes&page=1&keywords=Lotions_Lubes&ie=UTF8&qid=1476273606&fromHash=&fromRH=n%3A3760901%2Cn%3A3777371%2Ck%3ALotions_Lubes&section=BTF&fromApp=gp%2Fsearch&fromPage=results&fromPageConstruction=auisearch&version=2";
	public static String url(String origUrl,int page){
		Map<String,String> params = paramsFromOrigUrl(origUrl);
		if(params.get("rh") == null){
			return keywordUrl(origUrl,params,page);
		} else {
			return catUrl(origUrl,params,page);
		}
	}
	
	private static String keywordUrl(String origUrl,Map<String,String> params,int page){
		String	node = params.get("node");
		if(StringUtils.isBlank(node)){
			 node = StringUtils.substringBetween(origUrl, "node=", "/");
		}
		StringBuilder rh = new StringBuilder();
		if(StringUtils.isNotBlank(node)){
			if(StringUtils.contains(node, ",")){
				String[] arr = StringUtils.split(node, ",");
				for(String cat : arr){
					rh.append("n:").append(cat).append(",");
				}
				if(rh.length() > 0){
					rh.deleteCharAt(rh.length() - 1);
				}
			} else {
				rh.append("n:").append(node);
			}
		}
		
		String keywords = params.get("field-keywords");
		if(StringUtils.isNotBlank(keywords)){
			rh.append(",k:").append(keywords);
		}
		if(StringUtils.isBlank(node) && StringUtils.isBlank(keywords)){
			return origUrl;
		}
		
		String section = "BTF";
		if(page == 0 ){
			section = "ATF";
			page = 1;
		}
		String qid = params.get("qid");
		StringBuilder url = new StringBuilder(BASE_URL);
		try {
			String rhEncode = URLEncoder.encode(rh.toString(), "UTF-8");
			url.append("rh=").append(rhEncode);
			url.append("&page=").append(page);
			if(StringUtils.isNotBlank(keywords)){
				url.append("&keywords=").append(keywords);
			}
			url.append("&ie=UTF8");
			if(StringUtils.isNotBlank(qid)){
				url.append("&qid=").append(params.get("qid"));
			}
			url.append("&fromHash=");
			url.append("&fromRH=").append(rhEncode);
			url.append("&section=").append(section);
			url.append("&fromApp=gp%2Fsearch");
			url.append("&fromPage=results");
			url.append("&fromPageConstruction=auisearch");
			url.append("&version=2");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url.toString();
	}
	
	
	private static String catUrl(String origUrl,Map<String,String> params,int page){
		
		String section = "BTF";
		if(page == 0 ){
			section = "ATF";
			page = 1;
		}
		String rh = params.get("rh");
		String qid = params.get("qid");
		if(StringUtils.isBlank(rh)){
			return origUrl;
		}
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append("rh=").append(rh);
		url.append("&page=").append(page);
		url.append("&ie=UTF8");
		if(StringUtils.isNotBlank(qid)){
			url.append("&qid=").append(qid);
		}
		url.append("&fromHash=");
		url.append("&fromRH=").append(rh);
		url.append("&section=").append(section);
		url.append("&fromApp=gp%2Fsearch");
		url.append("&fromPage=results");
		url.append("&fromPageConstruction=auisearch");
		url.append("&version=2");
		return url.toString();
	}
	
	private static Map<String,String> paramsFromOrigUrl(String origUrl){
		Map<String,String> params = new HashMap<String,String>();
		if(StringUtils.isBlank(origUrl)){
			return params;
		}
		String[] paramArr = StringUtils.split(StringUtils.substringAfter(origUrl, "?"), "&");
		for(String param : paramArr){
			String key = StringUtils.substringBefore(param, "=");
			String value = StringUtils.substringAfter(param, "=");
			params.put(key, value);
		}
		return params;
	}
	
	public static void main(String[] args) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/data/amazon_cleaned.txt")));
		List<String> list = FileUtils.readLines(new File("/data/amazon.com.txt"));
		for(String item : list){
			String url = StringUtils.trim(item);
			if(!StringUtils.startsWith(url, "http://") && StringUtils.startsWith(url, "www")){
				url = "https://"+url;
			} else if(StringUtils.startsWith(url, "http://")){
				url = url.replace("http://", "https://");
			}
			//String url0 = url(url, 0);
			String url1 = url(url, 1);
			/*if(!StringUtils.contains(url0, "/mn/search/ajax/")){
				System.out.println(url0);
				continue;
			}*/
			if(!StringUtils.contains(url1, "/mn/search/ajax/")){
				System.out.println(url1);
				continue;
			}
			//bw.write(url0+"\n");
			bw.write(url1+"\n");
			bw.flush();
		}
		if(bw != null ){
			bw.close();
		}
	}

}