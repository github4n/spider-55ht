package com.haitao55.spider.crawler.core.callable.custom.jomashop;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

public class Jomashop extends AbstractSelect {
//	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String SEX_WOMEN = "Ladies";
	private static final String SEX_MEN = "Men's";
	private static final String DOMAIN="www.jomashop.com";
	private static final String luminatiIpUrl="http://lumtest.com/myip.json";
	private static final Logger logger = LoggerFactory.getLogger(Jomashop.class);

	
	private String crawlerUrl(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
		    String luminatiIp = luminatiHttpClient.request(luminatiIpUrl, null);
		    logger.info("use luminati_ip {},url {}",luminatiIp,url);
			content = luminatiHttpClient.request(url, getHeaders(url));
			context.setHtmlPageSource(content);
		}else{
			String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
			if(StringUtils.isBlank(proxyRegionId)){
				content = Crawler.create().timeOut(60000).url(url).header(getHeaders(url)).method(HttpMethod.GET.getValue())
						.resultAsString();
			}else{
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String proxyAddress=proxy.getIp();
				int proxyPort=proxy.getPort();
				logger.info("use proxy ip {}, port {},url {}",proxyAddress,proxyPort,url);
				content = Crawler.create().timeOut(60000).url(url).header(getHeaders(url)).method(HttpMethod.GET.getValue())
						.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
			}
		}
		return content;
	}
	
	private static Map<String,Object> getHeaders(String url){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("user-agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
		headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("upgrade-insecure-requests", "1");
		headers.put(":authority", "www.jomashop.com");
		headers.put(":method", "GET");
		headers.put(":path", url.replace("https://www.jomashop.com", ""));
//		headers.put("referer", url);
		headers.put(":scheme", "https");
		headers.put("accept-encoding", "gzip, deflate, br");
		headers.put("accept-language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		headers.put("cache-control", "max-age=0");
		//headers.put("Cookie", "dw=1; dw=1; __cfduid=dd9aeb19ede1a78ab26e812725fb40d4d1501138055; cqcid=ab02eBCq22LWZLtlTfLbCbHTc1; dwanonymous_7254072e2668c23dc3bf6cca213a6657=ab02eBCq22LWZLtlTfLbCbHTc1; AMCVS_C2D31CFE5330AFE50A490D45%40AdobeOrg=1; TruView_visitor=ae697c6c-0b93-42e2-ad81-28d947452af0; TruView_uab=46; TruView_session=7641de39-5d60-4684-87ff-d9e3834ff8a8; pt_s_3cc35021=vt=1502864903926&cad=; pt_3cc35021=uid=bqMktSrlPWspsinhQo1wIA&nid=0&vid=ZdPJTPxH1oj1GPXYHtF3qA&vn=2&pvn=1&sact=1502864905644&to_flag=0&pl=GJNBbH9ugujz46Ywo1oJVQ*pt*1502864903926; TruView_tssession=1502867314006; dw=1; liveagent_oref=; liveagent_sid=5e3cc0b0-3e2c-48b7-9adc-1bd1598adf1c; liveagent_vc=2; liveagent_ptid=5e3cc0b0-3e2c-48b7-9adc-1bd1598adf1c; PopupFlag=Puma True; _blka_v=0ae705f1-a5fd-463a-8194-ec31c4097f00; _blka_uab=83; _CT_RS_=Recording; _blka_lpd=10; _blka_lt=y; _blka_t=y; _blka_pd=2; dwac_bc6ZEiaaieGqMaaaddtsaoIDP5=ZL0ztJXzJVkRLC5CFuP5M-bZmiMOsctHu80%3D|dw-only|||USD|false|US%2FEastern|true; sid=ZL0ztJXzJVkRLC5CFuP5M-bZmiMOsctHu80; dwsid=fD6xrcGWSaJAzu5bWlZFcP5aGDpa1Ke86i9-EnecAxgi6LmQNT058lX_sAYPiCCqEfsMrBsW29S4I-Usp5qblw==; AMCV_C2D31CFE5330AFE50A490D45%40AdobeOrg=-1176276602%7CMCIDTS%7C17429%7CMCMID%7C32783341557131236263529480666784243745%7CMCAAMLH-1506393649%7C9%7CMCAAMB-1506404397%7CNRX38WO0n5BH8Th-nqAG_A%7CMCOPTOUT-1505806797s%7CNONE%7CMCAID%7C2C3F736C0519519B-4000060D6006A946;");
		return headers;
	}
	
	@Override
	public void invoke(Context context) throws Exception {
		RetBody rebody=new RetBody();
		String sourceUrl = context.getCurrentUrl();
		String url = DetailUrlCleaningTool.getInstance().cleanDetailUrl(sourceUrl);
		if(StringUtils.isBlank(url)){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"Jomashop.com itemUrl: "+sourceUrl+" ,  url rule is error.");
		}
		String content = crawlerUrl(context,url);
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String data = StringUtils.substringBetween(content, "window.JomaRelatedItem.xItemArray = {", "};");
			String productId = StringUtils.substringBetween(content, "prod_id\":\"", "\",");
			if(StringUtils.isBlank(productId)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"Jomashop.com itemUrl: "+sourceUrl+" ,  url sourceUrl is error.");
			}
			String skuId=StringUtils.substringBetween(data, "\"primary_item\": \"", "\"");
			String skuData=StringUtils.substringBetween(data, "\"xitems\":", "\"primary_item\": \"");
			if(null!=skuData){
				skuData=skuData.substring(0, skuData.lastIndexOf(","));
				
				Type skuListType = new TypeToken<Map<String,Map<String,Object>>>(){}.getType();
				Map<String,Map<String,Object>> skuList = JsonUtils.json2bean(skuData, skuListType);
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				Sku sku=new Sku();
				//brand
				String brandName="";
				//title
				String title="";
				if(null!=skuList&&skuList.size()>0){
					skuId=StringEscapeUtils.unescapeJava(skuId);
					Map<String, Object> value = skuList.get(skuId);
					List<Image> pics = new ArrayList<Image>();
//					@SuppressWarnings("unchecked")
//					List<String> images = (List<String>) value.get("images");//images
//					for (String imageUrl : images) {
//						Image image = new Image(imageUrl);
//						pics.add(image);
//					}
					
					Elements es  = doc.select(".MagicToolboxSelectorsContainer a");
					for(Element e : es){
						String imageUrl = e.attr("href");
						if(StringUtils.isNotBlank(imageUrl)){
							Image image = new Image(imageUrl);
							pics.add(image);
						}
					}
					context.getUrl().getImages().put(skuId, pics);
					String salePrice = (String) value.get("price");//salePrice
					String orignPrice = (String) value.get("retail");//orignPrice
					String currency = StringUtils.substring(salePrice, 0, 1);
					if(null==orignPrice){
						orignPrice=salePrice;
					}
					orignPrice = orignPrice.replaceAll("[,]", "");
					salePrice = salePrice.replaceAll("[,]", "");
					Float orign_price=Float.parseFloat(orignPrice.replace(currency, ""));
					Float sale_price=Float.parseFloat(salePrice.replace(currency, ""));
					if(orign_price<sale_price){
						orign_price=sale_price;
					}
					String price_unit = Currency.codeOf(currency).name();//unit
					Double save = (Double) value.get("savings");//save
					Integer is_availible = Integer.parseInt((value.get("is_availible").toString().trim()));//0 out of stock  1,in stock
					Integer stock_status=0;
					if(is_availible>0){
						stock_status=1;
					}
					
					
					//封装spu属性
					//price
					rebody.setPrice(new Price(orign_price, save.intValue(), sale_price, price_unit));
					//stock
					rebody.setStock(new Stock(stock_status));
					//brand
					
					Pattern p = Pattern.compile("<span[^>]*itemprop=[^>]*brand manufacturer[^>]*>([^<]*)</");
					Matcher m = p.matcher(content);
					if(m.find()){
						brandName=replace(m.group(1));
					}
					rebody.setBrand(new Brand(brandName, "","",""));
					
					//title
					
					p = Pattern.compile("<span[^>]*class=[^>]*product-name[^>]*>([^<]*)</");
					m = p.matcher(content);
					if(m.find()){
						title=replace(m.group(1));
					}
				}
				//l_selection_list,l_style_list
				sku.setL_selection_list(l_selection_list);
				sku.setL_style_list(l_style_list);
				
				//sku
				rebody.setSku(sku);
				
				String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
				String url_no = SpiderStringUtil.md5Encode(DOMAIN + productId);
				rebody.setDOCID(docid);
				rebody.setSite(new Site(DOMAIN));
				rebody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
				
				String item=StringUtils.EMPTY;
				Elements elements=doc.select("span.product-ids");
				if(CollectionUtils.isNotEmpty(elements)){
					item=elements.attr("content");
				}
				rebody.setTitle(new Title(title, "","",""));
				
				// Category
				List<String> cats = new ArrayList<String>();
				List<String> breads = new ArrayList<String>();
			    elements = doc.select("div.breadcrumbs>ul>li:not(:first-child) a");
				if (elements != null && elements.size()>0) {
					for (Element c : elements) {
						String cat = Native2AsciiUtils.ascii2Native(c.text());
						cats.add(cat);
						breads.add(cat);
					}
				}
				rebody.setCategory(cats);
				// BreadCrumb
				breads.add(brandName);
				rebody.setBreadCrumb(breads);
				
				String gender=doc.select("span#Gender").text();
				
				// description
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				Document document = Jsoup.parse(content);
				Elements es = document.select("div.product-description > div");
				StringBuilder sb = new StringBuilder();
				if (es != null && es.size() > 0) {
					int count = 1;
					for (Element e : es) {
						featureMap.put("feature-" + count, e.text());
						count++;
						sb.append(e.text());
					}
				}
				rebody.setFeatureList(featureMap);
				descMap.put("en", sb.toString());
				rebody.setDescription(descMap);
				
				Map<String, Object> propMap = new HashMap<String, Object>();
				List<List<Object>> propattr=new ArrayList<List<Object>>();
				gender = getSex(gender);
				propMap.put("s_gender", gender);
				propMap.put("s_identifier", item);
				es = document.select("div.product-attributes>div.attribute-group");
				if (es != null && es.size() > 0) {
					for (Element e : es) {
						List<Object> proList=new ArrayList<Object>();
						List<List<String>> list=new ArrayList<List<String>>();
						String keyValue=StringUtils.EMPTY;
						Elements key = e.select("h3");
						if(CollectionUtils.isNotEmpty(key)){
							keyValue=key.get(0).text();
						}
						Elements valueList = e.select("ul li");
						if(CollectionUtils.isNotEmpty(valueList)){
							for (Element element : valueList) {
								List<String> tempList=new ArrayList<String>();
								String prokey = StringUtils.trim(StringUtils.substringBefore(element.text(), ":"));
								String value = StringUtils.trim(StringUtils.substringAfter(element.text(), ":"));
								if (StringUtils.isNotBlank(prokey) && StringUtils.isNotBlank(value)) {
									tempList.add(prokey);
									tempList.add(value);
								}
								list.add(tempList);
							}
						}
						proList.add(keyValue);
						proList.add(list);
						propattr.add(proList);
					}
				}
				propMap.put("attr", propattr);
				rebody.setProperties(propMap);
			}
		}
		setOutput(context, rebody);
	}
	
	private static  String replace(String dest){
		if(StringUtils.isBlank(dest)){
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));
		
	}
	
	
	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN)){
			gender = "women";
		} else if(StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		} 
		return gender;
	}

}
