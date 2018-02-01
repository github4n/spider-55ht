package com.haitao55.spider.crawler.core.callable.custom.dinos;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

class DinosCallable implements Callable<JSONArray> {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BRAND_COLOR = "brand_color";
	private static final String BRAND_SIZE = "brand_size";
	private static final String BRAND_COLOR_SELECT = "brand_color_select";
	private static final String BRAND = "brand";
	private static final String COLOR = "color";
	private static final String COLOR_SELECT = "color_select";
	private JSONObject requestObject;
	private Url url;

	public DinosCallable(JSONObject requestObject, Url url) {
		this.requestObject = requestObject;
		this.url = url;
	}

	@Override
	public JSONArray call() {
		JSONArray resultArray = new JSONArray();
		try {

			String sku_category = requestObject.getString("category");
			if (BRAND_COLOR.equals(sku_category)) {// 商品 颜色 组合 sku
				brand_color_sku(resultArray, requestObject, url);
			} else if (BRAND_SIZE.equals(sku_category)) {
				brand_size＿sku(resultArray, requestObject, url);
			} else if (BRAND_COLOR_SELECT.equals(sku_category)) {
				brand_color_select_sku(resultArray, requestObject, url);
			}else if (BRAND.equals(sku_category)) {
				brand_sku(resultArray, requestObject, url);
			} else if (COLOR.equals(sku_category)) {
				color_sku(resultArray, requestObject, url);
			} else if (COLOR_SELECT.equals(sku_category)) {
				color_select_sku(resultArray, requestObject, url);
			}else { // 颜色 尺码组合 sku
				size_color_sku(resultArray, requestObject, url);
			}

		} catch (Exception e) {
			logger.error("DinosCallable get sku data error", e);
		}
		return resultArray;
	}
	
	
	/**
	 * 只有颜色下拉　　http://www.dinos.co.jp/p/1132400881/
	 * @param resultArray
	 * @param requestObject
	 * @param url
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws ClientProtocolException 
	 */
	private void color_select_sku(JSONArray resultArray, JSONObject requestObject, Url url) throws ClientProtocolException, HttpException, IOException {
		String path = "http://www.dinos.co.jp/defaultMall/sitemap/XHRGetZaikoInfo.jsp?CATNO=900&GOODS_NO=&MOSHBG=()&CLS1CD={}&CLS2CD=&DATEFLG=1";
		String param = URLEncoder.encode(URLEncoder.encode(requestObject.getString("param")));
		String moshbg=requestObject.getString("moshbg");
		// 封装goods_no
		path = path.replaceAll("\\(\\)", moshbg);
		path = path.replaceAll("\\{\\}", param);
		
		String  content=StringUtils.EMPTY;
		content=crawler_result(content,url,path);
		
		if (StringUtils.isNotBlank(content)) {
			String color_image = requestObject.getString("color_image");
			String switch_image = requestObject.getString("switch_image");
			String color_value = requestObject.getString("color_value");
			String cate_name1 = requestObject.getString("cate_name1");
			String cate_name2 = requestObject.getString("cate_name2");
			String brand_name = requestObject.getString("brand_name");

			JSONObject responseJson = JSONObject.parseObject(content);
			responseJson = responseJson.getJSONObject("Result");
			JSONArray responseArray = responseJson.getJSONArray("data");
			if (null != responseArray && responseArray.size() > 0) {
				for (Object object : responseArray) {
					JSONObject resultJson = (JSONObject) object;
					resultJson.put("color_image", color_image);
					resultJson.put("switch_image", switch_image);
					resultJson.put("color", color_value);
					resultJson.put("cate_name1", cate_name1);
					resultJson.put("cate_name2", cate_name2);
					resultJson.put("brand_name", brand_name);
					String stock = resultJson.getString("zaiko");
					int stock_status = 1;
					if (!"在庫あり".equals(stock)) {
						stock_status = 0;
					}
					resultJson.put("limited_cnt2", stock_status);
					resultArray.add(resultJson);
				}
			}
		}
	}
	
	/**
	 * brand_size 组合　http://www.dinos.co.jp/p/1330801387/
	 * @param resultArray
	 * @param requestObject
	 * @param url
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws ClientProtocolException 
	 */
	private void brand_size＿sku(JSONArray resultArray, JSONObject requestObject, Url url) throws ClientProtocolException, HttpException, IOException {
		String path = "http://www.dinos.co.jp/defaultMall/sitemap/XHRGetGoodsCls2.jsp?CATNO=900&MOSHBG=";
		path = path.concat(requestObject.getString("moshbg")).concat("&CLS1CD=");
		
		String  content=StringUtils.EMPTY;
		content=crawler_result(content,url,path);
		
		if (StringUtils.isNotBlank(content)) {
			String color_image = requestObject.getString("color_image");
			String switch_image = requestObject.getString("switch_image");
			String color_value = requestObject.getString("color_value");
			String cate_name1 = requestObject.getString("cate_name1");
			String cate_name2 = requestObject.getString("cate_name2");
			String brand_name = requestObject.getString("brand_name");

			JSONObject responseJson = JSONObject.parseObject(content);
			responseJson = responseJson.getJSONObject("Result");
			JSONArray responseArray = responseJson.getJSONArray("cls2");
			if (null != responseArray && responseArray.size() > 0) {
				for (Object object : responseArray) {
					JSONObject resultJson = (JSONObject) object;
					resultJson.put("color_image", color_image);
					resultJson.put("switch_image", switch_image);
					resultJson.put("color", color_value);
					resultJson.put("cate_name1", cate_name1);
					resultJson.put("cate_name2", cate_name2);
					resultJson.put("brand_name", brand_name);
					resultArray.add(resultJson);
				}
			}
		}
	}

	
	/**
	 * 商品　颜色下拉框　　形式　http://www.dinos.co.jp/p/1293900351/
	 * @param resultArray
	 * @param requestObject
	 * @param url
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws ClientProtocolException 
	 */
	private void brand_color_select_sku(JSONArray resultArray, JSONObject requestObject, Url url) throws ClientProtocolException, HttpException, IOException {
		String path = "http://www.dinos.co.jp/defaultMall/sitemap/XHRGetZaikoInfo.jsp?CATNO=900&GOODS_NO={}&MOSHBG=&CLS1CD=()&CLS2CD=&DATEFLG=1";

		// 封装goods_no
		path = path.replaceAll("\\{\\}", requestObject.getString("goods_no"));
		String param = URLEncoder.encode(URLEncoder.encode(requestObject.getString("param")));
		// 封装param
		path = path.replaceAll("\\(\\)", param);
		String  content=StringUtils.EMPTY;
		content=crawler_result(content,url,path);
		
		if (StringUtils.isNotBlank(content)) {
			String color_image = requestObject.getString("color_image");
			String switch_image = requestObject.getString("switch_image");
			String color_value = requestObject.getString("color_value");
			String cate_name1 = requestObject.getString("cate_name1");
			String cate_name2 = requestObject.getString("cate_name2");
			String brand_name = requestObject.getString("brand_name");

			JSONObject responseJson = JSONObject.parseObject(content);
			responseJson = responseJson.getJSONObject("Result");
			JSONArray responseArray = responseJson.getJSONArray("data");
			if (null != responseArray && responseArray.size() > 0) {
				for (Object object : responseArray) {
					JSONObject resultJson = (JSONObject) object;
					resultJson.put("color_image", color_image);
					resultJson.put("switch_image", switch_image);
					resultJson.put("color", color_value);
					resultJson.put("cate_name1", cate_name2);
					resultJson.put("cate_name2", cate_name1);
					resultJson.put("brand_name", brand_name);
					resultJson.put("namec2", brand_name);
					resultJson.put("valuec2", brand_name);
					String stock = resultJson.getString("zaiko");
					int stock_status = 1;
					if (!"在庫あり".equals(stock)) {
						stock_status = 0;
					}
					resultJson.put("limited_cnt2", stock_status);
					resultArray.add(resultJson);
				}
			}
		}
	}
	
	/**
	 * ｏｎｌｙ　ｃｏｌｏｒ
	 * @param resultArray
	 * @param requestObject
	 * @param url
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws ClientProtocolException 
	 */
	private void color_sku(JSONArray resultArray, JSONObject requestObject, Url url) throws ClientProtocolException, HttpException, IOException {
		String path = "http://www.dinos.co.jp/defaultMall/sitemap/XHRGetZaikoInfo.jsp?CATNO=900&GOODS_NO=&MOSHBG=()&CLS1CD={}&CLS2CD=&DATEFLG=1";
		@SuppressWarnings("deprecation")
		String param = URLEncoder.encode(URLEncoder.encode(requestObject.getString("param")));
		String moshbg=requestObject.getString("moshbg");
		// 封装goods_no
		path = path.replaceAll("\\(\\)", moshbg);
		path = path.replaceAll("\\{\\}", param);
		
		String  content=StringUtils.EMPTY;
		content=crawler_result(content,url,path);
		
		if (StringUtils.isNotBlank(content)) {
			String color_image = requestObject.getString("color_image");
			String switch_image = requestObject.getString("switch_image");
			String color_value = requestObject.getString("color_value");
			String cate_name1 = requestObject.getString("cate_name1");
			String cate_name2 = requestObject.getString("cate_name2");
			String brand_name = requestObject.getString("brand_name");

			JSONObject responseJson = JSONObject.parseObject(content);
			responseJson = responseJson.getJSONObject("Result");
			JSONArray responseArray = responseJson.getJSONArray("data");
			if (null != responseArray && responseArray.size() > 0) {
				for (Object object : responseArray) {
					JSONObject resultJson = (JSONObject) object;
					resultJson.put("color_image", color_image);
					resultJson.put("switch_image", switch_image);
					resultJson.put("color", color_value);
					resultJson.put("cate_name1", cate_name1);
					resultJson.put("cate_name2", cate_name2);
					resultJson.put("brand_name", brand_name);
					String stock = resultJson.getString("zaiko");
					int stock_status = 1;
					if (!"在庫あり".equals(stock)) {
						stock_status = 0;
					}
					resultJson.put("limited_cnt2", stock_status);
					resultArray.add(resultJson);
				}
			}
		}
	}


	/**
	 * 只有brand sku 组合   http://www.dinos.co.jp/p/1366400071/
	 * 
	 * @param resultArray
	 * @param requestObject
	 * @param url
	 * @throws IOException
	 * @throws HttpException
	 * @throws ClientProtocolException
	 */
	private void brand_sku(JSONArray resultArray, JSONObject requestObject, Url url)
			throws ClientProtocolException, HttpException, IOException {
		String path = "http://www.dinos.co.jp/defaultMall/sitemap/XHRGetZaikoInfo.jsp?CATNO=900&GOODS_NO={}&MOSHBG=&CLS1CD=&CLS2CD=&DATEFLG=1";
		
		// 封装goods_no
		path = path.replaceAll("\\{\\}", requestObject.getString("goods_no"));
		
		String content=StringUtils.EMPTY;
		
		content=crawler_result(content, url, path);
		
		if (StringUtils.isNotBlank(content)) {
			String color_image = requestObject.getString("color_image");
			String switch_image = requestObject.getString("switch_image");
			String color_value = requestObject.getString("color_value");
			String cate_name1 = requestObject.getString("cate_name1");
			String cate_name2 = requestObject.getString("cate_name2");
			String brand_name = requestObject.getString("brand_name");

			JSONObject responseJson = JSONObject.parseObject(content);
			responseJson = responseJson.getJSONObject("Result");
			JSONArray responseArray = responseJson.getJSONArray("data");
			if (null != responseArray && responseArray.size() > 0) {
				for (Object object : responseArray) {
					JSONObject resultJson = (JSONObject) object;
					resultJson.put("color_image", color_image);
					resultJson.put("switch_image", switch_image);
					resultJson.put("color", color_value);
					resultJson.put("cate_name1", cate_name1);
					resultJson.put("cate_name2", cate_name2);
					resultJson.put("brand_name", brand_name);
					resultJson.put("namec2", brand_name);
					resultJson.put("valuec2", brand_name);
					String stock = resultJson.getString("zaiko");
					int stock_status = 1;
					if (!"在庫あり".equals(stock)) {
						stock_status = 0;
					}
					resultJson.put("limited_cnt2", stock_status);
					resultArray.add(resultJson);
				}
			}
		}
	}

	/**
	 * brand size 组合 实例链接 http://www.dinos.co.jp/p/1367900245/
	 * 
	 * @param resultArray
	 * @param requestObject
	 * @param url
	 * @throws IOException
	 * @throws HttpException
	 * @throws ClientProtocolException
	 */
	private void brand_color_sku(JSONArray resultArray, JSONObject requestObject, Url url)
			throws ClientProtocolException, HttpException, IOException {
		String path = "http://www.dinos.co.jp/defaultMall/sitemap/XHRGetGoodsCls1.jsp?CATNO=900&MOSHBG=";
		@SuppressWarnings("deprecation")
		String param = URLEncoder.encode(URLEncoder.encode(requestObject.getString("param")));
		path = path.concat(requestObject.getString("moshbg")).concat("&CLS1CD=" + param);
		
		String content=StringUtils.EMPTY;
		
		content=crawler_result(content, url, path);
	
		if (StringUtils.isNotBlank(content)) {
			String color_image = requestObject.getString("color_image");
			String switch_image = requestObject.getString("switch_image");
			String color_value = requestObject.getString("color_value");
			String cate_name1 = requestObject.getString("cate_name1");
			String cate_name2 = requestObject.getString("cate_name2");
			String brand_name = requestObject.getString("brand_name");

			JSONObject responseJson = JSONObject.parseObject(content);
			responseJson = responseJson.getJSONObject("Result");
			JSONArray responseArray = responseJson.getJSONArray("cls1");
			if (null != responseArray && responseArray.size() > 0) {
				for (Object object : responseArray) {
					JSONObject resultJson = (JSONObject) object;
					resultJson.put("color_image", color_image);
					resultJson.put("switch_image", switch_image);
					resultJson.put("color", color_value);
					resultJson.put("cate_name1", cate_name1);
					resultJson.put("cate_name2", cate_name2);
					resultJson.put("brand_name", brand_name);
					resultJson.put("namec2", brand_name);
					resultJson.put("valuec2", brand_name);
					resultJson.put("limited_cnt2", resultJson.getString("limited_cnt1"));
					if (color_value.contains(resultJson.getString("valuec1"))) {
						resultArray.add(resultJson);
					}
				}
			}
		}
	}

	/**
	 * 颜色 size 组合 ｓｋｕ 获取
	 * 
	 * @param resultArray
	 * @param requestObject
	 * @param url
	 * @throws IOException
	 * @throws HttpException
	 * @throws ClientProtocolException
	 */
	private void size_color_sku(JSONArray resultArray, JSONObject requestObject, Url url)
			throws ClientProtocolException, HttpException, IOException {
		String path = "http://www.dinos.co.jp/defaultMall/sitemap/XHRGetGoodsCls2.jsp?CATNO=900&MOSHBG=";
		@SuppressWarnings("deprecation")
		String param = URLEncoder.encode(URLEncoder.encode(requestObject.getString("param")));
		path = path.concat(requestObject.getString("moshbg")).concat("&CLS1CD=" + param);
		
		String content=StringUtils.EMPTY;
		
		content=crawler_result(content, url, path);
		 
		if (StringUtils.isNotBlank(content)) {
			String color_image = requestObject.getString("color_image");
			String switch_image = requestObject.getString("switch_image");
			String color_value = requestObject.getString("color_value");
			String cate_name1 = requestObject.getString("cate_name1");
			String cate_name2 = requestObject.getString("cate_name2");
			String brand_name = requestObject.getString("brand_name");

			JSONObject responseJson = JSONObject.parseObject(content);
			responseJson = responseJson.getJSONObject("Result");
			JSONArray responseArray = responseJson.getJSONArray("cls2");
			if (null != responseArray && responseArray.size() > 0) {
				for (Object object : responseArray) {
					JSONObject resultJson = (JSONObject) object;
					resultJson.put("color_image", color_image);
					resultJson.put("switch_image", switch_image);
					resultJson.put("color", color_value);
					resultJson.put("cate_name1", cate_name1);
					resultJson.put("cate_name2", cate_name2);
					resultJson.put("brand_name", brand_name);
					resultArray.add(resultJson);
				}
			}
		}
	}


	
	/**
	 * 线上爬取
	 * @param content
	 * @param url
	 * @param path
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	private String crawler_result(String content, Url url, String path) throws ClientProtocolException, HttpException, IOException {
		String proxyRegionId = url.getTask().getProxyRegionId();
		 if(StringUtils.isBlank(proxyRegionId)){
			 content = Crawler.create().timeOut(15000).url(path).proxy(false).resultAsString();
		 }else{
			 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
					 true);
			 String proxyAddress=proxy.getIp();
			 int proxyPort=proxy.getPort();
			 content = Crawler.create().timeOut(15000).url(path).proxy(true).proxyAddress(proxyAddress)
					 .proxyPort(proxyPort).resultAsString();
		 }
		 return content;
	}
	
}
