package com.haitao55.spider.crawler.core.callable.custom.nordstrom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * 
  * @ClassName: Nordstrom
  * @Description: nordstrom的
  * @author songsong.xu
  * @date 2016年10月19日 下午2:19:57
  *
 */
public class NordstromSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private int pageSize = 66;
	private final String BASEURL = "http://shop.nordstrom.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String res = super.getInputString(context);
		List<String> urls = new ArrayList<String>();
		String navNodes = StringUtils.substringBetween(res, "\"PrimaryNavigationNodes\":", ",\"Urls\"");
		if(StringUtils.isNotBlank(navNodes)){
			JsonArray arr = JsonUtils.json2bean(navNodes, JsonArray.class);
			if(arr != null && arr.size() > 0){
				for(int i =0 ; i < arr.size(); i++){
					JsonObject obj = arr.get(i).getAsJsonObject();
					JsonObject pageUrl = obj.getAsJsonObject("PageUrl");
					if(pageUrl != null){
						JsonArray children = obj.getAsJsonArray("Children");
						if(children != null && children.size() > 0){
							for(int j = 0; j < children.size();j++){
								JsonObject childObj = children.get(j).getAsJsonObject();
								if(childObj != null && !childObj.get("PageUrl").isJsonNull()){
									JsonObject childPageUrl = childObj.getAsJsonObject("PageUrl");
									if(childPageUrl != null){
										String url = childPageUrl.getAsJsonPrimitive("Url").getAsString();
										urls.add(BASEURL+url);
									}
								}
							}
						}
					}
				}
			}
		}
		
		List<String> newUrlValues = new ArrayList<String>();
		for(String url : urls){
			try{
				Url currentUrl = new Url(url);
				currentUrl.setTask(context.getUrl().getTask());
				String content = HttpUtils.get(currentUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES);
				Pattern p = Pattern.compile("All Items \\((.*?)\\)");
				Matcher m = p.matcher(content);
				int totalPage = 1;
				if(m.find()){
					String totalCount = m.group(1);
					if(StringUtils.isNotBlank(totalCount)){
						totalPage = (Integer.valueOf(totalCount)+pageSize -1)/pageSize;
					}
				}
				for(int i = 1; i <= totalPage;i++){
					String apiUrl = url.replace("/c/", "/api/c/");
					newUrlValues.add(apiUrl+"?top="+pageSize+"&page="+i);
				}
				logger.info("nordstrom seed url: {},totalPage: {}",url,totalPage);
				Thread.sleep(500);
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
		Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);
		context.getUrl().getNewUrls().addAll(value);
	}
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}

	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String url = "http://shop.nordstrom.com/c/sale-womens-clothing";
		/*Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("Referer", "http://shop.nordstrom.com/c/sale-home-gifts?origin=leftnav&cm_sp=Top%20Navigation-_-Home&page=2&top=66");
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36");*/
		String content = Crawler.create().timeOut(30000).url(url).retry(3).proxy(true).proxyAddress("127.0.0.1").proxyPort(1080).resultAsString();
		//198.11.178.141 3128
		Pattern p = Pattern.compile("All Items \\((.*?)\\)");
		Matcher m = p.matcher(content);
		while(m.find()){
			System.out.println(m.group(1));
		}
		//System.out.println(content);
	}

}
