package com.haitao55.spider.ui.service.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.haitao55.spider.common.dao.CurrentItemDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.ui.common.util.ItemsCrawlerTool;
import com.haitao55.spider.ui.service.ActivityItemsService;

/**
 * Title: 活动商品查询　service　实现类 利用其他dao实现的功能进行业务实现 Description: Company: 55海淘
 *
 * @author zhaoxl
 * @date 2017年1月17日 下午2:58:47
 * @version 1.0
 */
@Service("activityItemsService")
public class ActivityItemsServiceImpl implements ActivityItemsService {
  private static final Logger logger = LoggerFactory.getLogger(ActivityItemsServiceImpl.class);
  private static final String items = "items";
  private static final String docIds = "docIds";
  private static final String urls = "urls";
  private static final String QUERY_ITEM_ADDRESS =
      "http://114.55.61.171/spider-55haitao-data-service/items/queryItem.action?url=";
  
  private String cn_feelunique_interface ="http://cn.feelunique.com/searchapi/v1/lists?callback=jQuery1704368382751904454&page=1&pageSize=72&listCid={lid}&cid={cid}&sort=0";
 
  @Autowired private CurrentItemDAO currentItemDAO;
  
  private ExecutorService service = Executors.newFixedThreadPool(10);

  @Override
  public JSONObject getItemsAndUrls(
      JSONObject cssJsonObject, String taskId, String url, String website_preffix, Task task) {
	  List<String> urlList = null;
	  if(StringUtils.containsIgnoreCase(url, "cn.feelunique.com")){
		  urlList = crawlerCnFeeluniqueListInfo(url);
	  }else{
		  String item_css = cssJsonObject.getString(items);
		  if(StringUtils.isNotBlank(item_css)){
			  urlList = getUrlsByItemCss(item_css, url, website_preffix, task);
		  }
	  }
    
    if (CollectionUtils.isEmpty(urlList)) {
      return null;
    }
    JSONObject resultJsonObject = new JSONObject();

    //存在的商品的docid
    List<String> docIdList = new ArrayList<String>();
    //没有收录的商品url
    List<String> notIncludedUrlList = new ArrayList<String>();
    List<Future<Entry<String,Boolean>>> result = new ArrayList<>();
    for (String string : urlList) {
      Future<Entry<String,Boolean>> f =
          service.submit(
              () -> {
                int retry = 0;
                do {
                  try {
                    //String md5Url = SpiderStringUtil.md5Encode(string);
                    //ItemDO itemDo = currentItemDAO.queryMd5UrlLastItem(Long.parseLong(taskId), md5Url);
                    String jsonResult = HttpUtils.get(QUERY_ITEM_ADDRESS + string);
                    logger.info(
                        "getItemsAndUrls queryItems param {}, result  {}", string, jsonResult);
                    //CrawlerJSONResult crawlerJSONResult = JsonUtils.json2bean(jsonResult, CrawlerJSONResult.class);
                    if (StringUtils.isNotBlank(jsonResult)) {
                      Pattern pattern = Pattern.compile("\"DOCID\":\"(.*?)\"");
                      Matcher matcher = pattern.matcher(jsonResult);
                      if (matcher.find()) {
                        String docId = matcher.group(1);
                        docIdList.add(docId);
                      }
                    } else {
                      notIncludedUrlList.add(string);
                    }
                    return Maps.immutableEntry(string, true);
                  } catch (Throwable e) {
                    e.printStackTrace();
                    retry++;
                  }
                } while (retry < 3);
                return Maps.immutableEntry(string, false);
              });
      result.add(f);
    }
    result.forEach(
        f -> {
          try {
            Entry<String,Boolean> res = f.get();
            logger.info("multi-threads request url {},result {}",res.getKey(),res.getValue());
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (ExecutionException e) {
            e.printStackTrace();
          }
        });
    resultJsonObject.put(docIds, docIdList);
    resultJsonObject.put(urls, notIncludedUrlList);
    return resultJsonObject;
  }

  private List<String> crawlerCnFeeluniqueListInfo(String url){
	  List<String> urlList = new ArrayList<>();
	  try {
		String html = Crawler.create().timeOut(20000).retry(3).url(url).resultAsString();
		if(StringUtils.isNotBlank(html)){
			String cateId = StringUtils.substringBetween(html, "CATEGORYID = \"", "\"");
			String listId = StringUtils.substringBetween(html, "LISTCID = \"", "\"");
			if(StringUtils.isNotBlank(cateId) && StringUtils.isNotBlank(listId)){
				String cnUrl = cn_feelunique_interface.replace("{cid}", cateId).replace("{lid}", listId);
				String rs = Crawler.create().timeOut(20000).retry(3).url(cnUrl).resultAsString();
				if(StringUtils.isNotBlank(rs)){
					String jsonRs = StringUtils.substringBetween(rs, "(", ")");
					JSONObject jsonObject = JSONObject.parseObject(jsonRs);
					JSONArray jsonArray = jsonObject.getJSONArray("data");
					for(int i = 0; i < jsonArray.size();i++){
						JSONObject proJson = jsonArray.getJSONObject(i);
						urlList.add(proJson.getString("url_path"));
					}
				}
			}
		}
	} catch (HttpException | IOException e) {
		e.printStackTrace();
	}
	  return urlList;
  }
  
  private List<String> getUrlsByItemCss(
      String item_css, String url, String website_preffix, Task task) {
    List<String> urls = new ArrayList<String>();

    ItemsCrawlerTool itemCrawlerTool = ItemsCrawlerTool.getInstance();
    List<String> newUrlValues =
        itemCrawlerTool.itemsCrawler(item_css, url, website_preffix, task.getProxyRegionId());
    logger.info("itemCrawlerTool taskId :{} , item_css : {} , url : {} , website_preffix : {}",task.getTaskId(),item_css,url,website_preffix);
    //之后会有根据pages查询，统一urls接收
    if (CollectionUtils.isNotEmpty(newUrlValues)) {
      urls.addAll(newUrlValues);
    }
    return urls;
  }

  public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
//    Pattern pattern = Pattern.compile("\"DOCID\":\"(.*?)\"");
//    Matcher matcher =
//        pattern.matcher("\"retbody\":{\"DOCID\":\"b021c70b71a3a7d69e28ed25ab33d8e5\",\"Site\"");
//    if (matcher.find()) {
//      System.out.println(matcher.group(1));
//    }
//	  String url = "http://shop.nordstrom.com/c/sexy-summer-must-haves?campaign=0515SexySummer4001&mcamp=4001&cm_sp=merch-_-beauty_0515SexySummer4001-_-landingpage_shop";
//			  Pattern p = Pattern.compile("^(http://|https://)?[^//]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE); 
//			  Matcher matcher = p.matcher(url);  
//		      if(matcher.find()){
//		    	  System.out.println(matcher.group());
//		      }  
	  
	  String url1 ="https://www.macys.com/shop/mens-clothing/mens-calvin-klein/Mens_product_type/Socks%7cUnderwear?id=28169";
	  System.out.println(url1);
	  String html = Crawler.create().timeOut(20000).retry(3).url(url1).resultAsString();
	  System.out.println(html);
	  Document doc = Jsoup.parse(html);
	  int count = 0;
	  Elements es = doc.select("div.product-wrapper div.product-name>a");
	  for(Element e : es){
		  String url = e.attr("href");
		  if(StringUtils.isNotBlank(url)){
			  count++;
			  System.out.println(url);
		  }
	  }
	  System.out.println(count);
  }
}
