package com.haitao55.spider.crawler.core.callable.custom.footlocker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * @author denghuan
 *
 */
public class Footlocker extends AbstractSelect{

	private static final String domain = "www.footlocker.com";
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private String imageApi = "https://images.footlocker.com/is/image/EBFL2/#skuId#?req=set,json&handler=s7ViewResponse";
	
	private static final String PREFIX_IMAGE ="https://images.footlocker.com/is/image/";
	
	private static Map<String, Object> getHeaders() {
		Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			headers.put("accept-encoding", "gzip, deflate, br");
			headers.put("accept-language", "en-US,en;q=0.9");
			headers.put("Cache-Control", "max-age=0");
			headers.put("Connection", "keep-alive");
			headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
		return headers;
	}
	
	
	@Override
	public void invoke(Context context) throws Exception {
		
		String url = context.getCurrentUrl();
		
		//String content = curlCrawler(url,context);
//        String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
        String content = StringUtils.EMPTY;
//        if (proxyRegionId != null) {
//            Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
//            String ip = proxy.getIp();
//            int port = proxy.getPort();
//            content = Crawler.create().timeOut(30000).retry(2).proxy(true).proxyAddress(ip).proxyPort(port).url(url).header(getHeaders()).resultAsString();
//            //content = CurlCrawlerUtil.get(url,20,ip,port);
//            logger.info("use proxy ip {}, port {},url {}",ip,port,url);
//        } else {
            content = Crawler.create().timeOut(30000).url(url).retry(2).header(getHeaders()).resultAsString();
//        	logger.info("do not use proxy url {}",url);
//        	LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US","55ht_zone_us");
//			content = luminatiHttpClient.request(context.getCurrentUrl(), getHeaders());
//        }
		RetBody rebody = new RetBody();
		
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);		
			String title  = doc.select("h1.product_title").text();
			String unit = StringUtils.substringBetween(content, "priceCurrency\": \"", "\"");
			String brand = StringUtils.substringBetween(content, "tagMgt.brand = \"", "\"");
			String productId = StringUtils.substringBetween(content, "product_model = \"", "\"");
			String gender = StringUtils.substringBetween(content, "dtm_gender = \"", "\"");
			
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			String defaultColor = doc.select("span.attType_color").text();
			String defaultWidth = doc.select("span.attType_width").text();
			
			String styles = StringUtils.substringBetween(content, "var styles = ", "};");
			if(StringUtils.isBlank(styles)){
				logger.info("Footlocker crawler styles isBlank #### url :{},title :{},brand :{}",url,title,brand);
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean display = false;
			
			boolean stockFlag = false;
			if(StringUtils.isNotBlank(styles)){
				styles = styles.concat("}");
				JSONObject jsonObject = JSONObject.parseObject(styles);
				Set<String> set = jsonObject.keySet();
				Iterator<String> it = set.iterator();
				while(it.hasNext()){
					String key = it.next();
					JSONArray jsonArray = jsonObject.getJSONArray(key);
					
					String colorVal = StringUtils.substringBetween(jsonArray.toString(), "attType_color'>", "</span>");

					String skuId = StringUtils.EMPTY;
					LStyleList lStyleList = new LStyleList();
					
					
					//modified by xusongsong
					JSONArray items = jsonArray.getJSONArray(7);
					/*String[] skus = StringUtils.substringsBetween(jsonArray.toString(), "[\" ", "\",\"Y\"");
					
					if (skus == null) {
						skus = StringUtils.substringsBetween(jsonArray.toString(), "[\" ", "In");
					}
					if(skus == null){
						skus = StringUtils.substringsBetween(jsonArray.toString(), "[\" ", "Only");
						
					}*/
					if (items != null) {
						for (int i = 0 ; i < items.size(); i++) {
							LSelectionList lSelectionList = new LSelectionList();
							//String[] sp = attr.split("\",\"");
							JSONArray item = items.getJSONArray(i);
							if (item != null) {
								String sizeVal = item.getString(0);//sp[0];
								String origPice = item.getString(1);//sp[1];
								String salePice = item.getString(2);//sp[2];
								String statusString = item.getString(3);
								String availability = item.getString(4);
								int sotckStatus = 1;
								if(StringUtils.containsIgnoreCase(statusString, "Only In Store") || StringUtils.equalsIgnoreCase(availability, "N")){
									sotckStatus = 0;
									stockFlag = stockFlag || false;
								} else {
									stockFlag = stockFlag || true;
								}
								skuId = key + sizeVal;
								lSelectionList.setGoods_id(skuId);
								lSelectionList.setOrig_price(Float.parseFloat(origPice));
								lSelectionList.setSale_price(Float.parseFloat(salePice));
								lSelectionList.setPrice_unit(unit);
								lSelectionList.setStock_status(sotckStatus);
								lSelectionList.setStyle_id(colorVal);
								List<Selection> selections = new ArrayList<>();
								if (StringUtils.isNotBlank(defaultWidth)) {
									Selection selection = new Selection();
									selection.setSelect_name("width");
									selection.setSelect_value(defaultWidth);
									selections.add(selection);
								}
								if (sizeVal != null) {
									Selection selection = new Selection();
									selection.setSelect_name("size");
									selection.setSelect_value(sizeVal);
									selections.add(selection);
								}
								lSelectionList.setSelections(selections);

								if (StringUtils.isNotBlank(colorVal) && colorVal.equals(defaultColor) && !display) {
									lStyleList.setDisplay(true);
									display = true;
									int save = Math
											.round((1 - Float.parseFloat(salePice) / Float.parseFloat(origPice)) * 100);// discount
									rebody.setPrice(new Price(Float.parseFloat(origPice), save,
											Float.parseFloat(salePice), unit));
								}
								l_selection_list.add(lSelectionList);
							}
						}
					}

					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_cate_name("color");
					lStyleList.setStyle_id(colorVal);
					lStyleList.setStyle_switch_img("");
					lStyleList.setStyle_name(colorVal);
					lStyleList.setGood_id(skuId);
					List<Image> imageList = new ArrayList<>();
					String imageUrl = imageApi.replace("#skuId#", key);
					//String imageRs = curlCrawler(imageUrl, context);
					String imageRs = "";
//					proxyRegionId = context.getUrl().getTask().getProxyRegionId();
//					if (proxyRegionId != null) {
//			            Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
//			            String ip = proxy.getIp();
//			            int port = proxy.getPort();
//			            imageRs = Crawler.create().timeOut(30000).retry(2).proxy(true).proxyAddress(ip).proxyPort(port).url(imageUrl).header(getHeaders()).resultAsString();
//			            logger.info("use proxy ip {}, port {},url {}",ip,port,url);
//			        } else {
			        	imageRs = Crawler.create().timeOut(30000).url(imageUrl).retry(2).header(getHeaders()).resultAsString();
//			        	logger.info("do not use proxy url {}",url);
//			        }
//			        	imageRs = luminatiHttpClient.request(imageUrl,null);
					//String imageRs = Crawler.create().timeOut(30000).retry(2)/*.proxy(true).proxyAddress(ip).proxyPort(port)*/.url(imageUrl).header(getHeaders()).resultAsString();
					if (StringUtils.isNotBlank(imageRs)) {
						String[] images = StringUtils.substringsBetween(imageRs, "i\":{\"n\":\"", "\"}");
						if (images != null) {
							for (String img : images) {
								imageList.add(new Image(PREFIX_IMAGE + img));
							}
						}
					}

					if (CollectionUtils.isEmpty(imageList)) {
						imageList.add(new Image(PREFIX_IMAGE + "EBFL2/" + key));
					}
					context.getUrl().getImages().put(skuId, imageList);// picture
					l_style_list.add(lStyleList);
				}
			}
			
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			
			
			int spuStock = 0;
			/*if(l_selection_list != null 
					&& l_selection_list.size() > 0){
				for(LSelectionList ll : l_selection_list){
					int sku_stock = ll.getStock_status();
					if (sku_stock == 1) {
						spuStock = 1;
						break;
					}
					if (sku_stock == 2){
						spuStock = 2;
					}
				}
			}*/
			if(stockFlag){
				spuStock = 1;
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			
			cats.add(title);
			breads.add(title);
			
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			
			String description = doc.select("#pdp_description").text();
			
			featureMap.put("feature-1", description);
			
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", gender);
			rebody.setProperties(propMap);
			rebody.setSku(sku);
		}
		
		setOutput(context, rebody);
		//System.out.println(rebody.parseTo());
		
	}
	
	private String curlCrawler(String url,Context context){
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isNotBlank(proxyRegionId)) {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
//			String ip = "114.55.10.105";
//			int port = 3128;
			String ip=proxy.getIp();
			int port=proxy.getPort();
			content = CurlCrawlerUtil.get(url,20,ip,port);
			logger.info("Footlocker use proxy ip {}, port {},url {}",ip,port,url);
		} else {
			content = CurlCrawlerUtil.get(url);
		}
		return content;
	}
	
	public static void main(String[] args) throws Exception {
		Context context = new Context();
		Footlocker fo = new Footlocker();
		context.setCurrentUrl("https://www.footlocker.com/product/model:131259/sku:53265005/jordan-retro-12-boys-grade-school/grey/grey/");
	    fo.invoke(context);
	}
	

}
