package com.haitao55.spider.crawler.core.callable.custom.marcjacobs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.haitao55.spider.common.gson.JsonUtils;
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
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: Marcjacobs
  * @Description: detail page parser
  * @author songsong.xu
  * @date 2016年11月23日 下午2:19:08
  *
 */
public class Marcjacobs extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.marcjacobs.com";
	private final String SKU_URL_PREFIX = "https://www.marcjacobs.com/on/demandware.store/Sites-marcjacobs-Site/default/Product-Variation?Quantity=1&format=ajax";
	//pid=M0010070&dwvar_M0010070_color=453&dwvar_M0010070_size=1SZ
	private static final int nThreads = 3;
	private boolean debug = false;
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getUrl().getValue();
		String content =  StringUtils.EMPTY;
		if(debug){
			content = Crawler.create().timeOut(10000).retry(3).proxy(true).proxyAddress("47.88.25.82").proxyPort(3128).url(context.getUrl().toString()).resultAsString();
		} else {
		    String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
	        if(StringUtils.isNotBlank(proxyRegionId)){
	            Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
	            String proxyAddress=proxy.getIp();
	            int proxyPort=proxy.getPort();
	            content = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
	                    .proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
	        } else {
	            content = super.getInputString(context);
	        }
		}
		RetBody rebody = new RetBody();
		//<a class="availability check-in-store" href="/on/demandware.store/Sites-marcjacobs-Site/default/StoreInventory-FindInStore?pid=889732791147&amp;mid=M0010070&amp;clid=509" title="Check In Store Availability" data-dlg-options="{&quot;dialogClass&quot;:&quot;in-store-availability-dialog&quot;}" data-productid="889732791147" data-target="/on/demandware.store/Sites-marcjacobs-Site/default/StoreInventory-Inventory">Check In Store Availability</a>
		String itemId = StringUtils.EMPTY;
		Pattern pattern = Pattern.compile("<a[^>]*class=\"availability check-in-store\"[^>]*mid=(.*?)&");
		Matcher matcher = pattern.matcher(content);
		if(matcher.find()){
			itemId = matcher.group(1);
		} else  {
			logger.error("Error while fetching itemId from marcjacobs.com's detail page,url {}",context.getUrl().toString());
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"marcjacobs.com item url :"+context.getUrl().toString()+" itemId is not found.");
		}
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("input[type=hidden]#pid");
		//String pid = JsoupUtils.attr(es, "value");
		es = d.select("input[type=hidden]#name");
		String title = JsoupUtils.attr(es, "value");
		es = d.select("input[type=hidden]#brand");
		String brand = JsoupUtils.attr(es, "value");
		//default sku
		es = d.select("div.product-variations");
		String defaultColorValue = StringUtils.EMPTY;
		String defaultSizeValue = StringUtils.EMPTY;
		String displaySizeName = StringUtils.EMPTY;
		if(es != null && es.size() > 0 ){
			String data = JsoupUtils.attr(es, "data-current");
			Type type = new TypeToken<JsonObject>() {private static final long serialVersionUID = 1L;}.getType();
			JsonObject defObj = JsonUtils.json2bean(data, type);
			if(defObj != null){
				JsonObject color = defObj.getAsJsonObject("color");
				JsonObject size = defObj.getAsJsonObject("size");
				if(color != null){
					defaultColorValue = color.getAsJsonPrimitive("value").getAsString();
					//color.getAsJsonPrimitive("displayValue").getAsString();
				}
				if(size != null){
					defaultSizeValue = size.getAsJsonPrimitive("value").getAsString();
					displaySizeName = size.getAsJsonPrimitive("displayName").getAsString();
				}
			}
		}
		es = d.select("div.product-variations > ul > li.attribute");
		List<Image> swatchList = new ArrayList<Image>();
		List<String> colorSelect = new ArrayList<String>();
		List<String> sizeSelect = new ArrayList<String>();
		boolean isSizeDefault = true;
		if(es != null && es.size() > 0 ){
			for(Element e : es){
				String claszz = JsoupUtils.attr(e, "class");
				Elements elements = e.select("div.value > p");
				boolean imageBased = false;
				if(elements != null && elements.size() > 0 ){
					if(StringUtils.contains(JsoupUtils.attr(elements, "class"), "selected-value")){
						imageBased = true;
					}
				}
				//default selected color
				Elements eles = e.select("div.value > ul > li");
				String key = StringUtils.EMPTY;
				String value = StringUtils.EMPTY;
				String name = StringUtils.EMPTY;
				String skuParamUrl = StringUtils.EMPTY;
				if(eles != null && eles.size() > 0){
					//div.value > p > span.label
					String label = JsoupUtils.text(e.select("div.value > p > span.label"));
					if(StringUtils.contains(label, ":")){
						label = StringUtils.substringBefore(label, ":");
					}
					for(Element ele : eles){
						name = JsoupUtils.text(ele.select("a"));
						
						skuParamUrl = JsoupUtils.attr(ele.select("a"), "href");
						if(StringUtils.isNotBlank(skuParamUrl)){
							skuParamUrl = skuParamUrl.replace("&amp;", "&");
						}
						String[] skuPrampArr = StringUtils.split(StringUtils.substringAfter(skuParamUrl, "?"), "&");
						if(skuPrampArr != null && skuPrampArr.length > 0){
							for(String skuParam : skuPrampArr){
								if(StringUtils.containsIgnoreCase(skuParam, label)){
									key = StringUtils.substringBefore(skuParam, "=");
									value = StringUtils.substringAfter(skuParam, "=");
								}
							}
						}
						String style = JsoupUtils.attr(ele.select("a"), "style");
						String swatchUrl = StringUtils.EMPTY;
						if(StringUtils.isNotBlank(style)){
							style = StringUtils.substringBetween(style, "(", ")");
							if(!StringUtils.startsWith(style, "https")){
								swatchUrl = "https:"+style;
							}else {
								swatchUrl = style;
							}
						}
						swatchList.add(new Image(swatchUrl));
						colorSelect.add(value+";"+label+";"+name+";"+swatchUrl+";"+imageBased+";"+key);
					}
				}
				//default size
				if(StringUtils.contains(claszz, "variant")){
					String label = JsoupUtils.text(e.select("label"));
					if(StringUtils.contains(label, ":")){
						label = StringUtils.substringBefore(label, ":");
					}
					eles = e.select("div.styled-select > select.variation-select");
					key = JsoupUtils.attr(eles, "name");
					
					eles = e.select("div.styled-select > select.variation-select > option");
					for(Element element : eles){
						String option = JsoupUtils.text(element);
						if(StringUtils.contains(option, "Select Size")){
							continue;
						}
						value =  option;
						name = value;
						sizeSelect.add(value+";"+label+";"+name+";;"+imageBased+";"+key);
					}
				} else {
				    if(isSizeDefault && StringUtils.isNotBlank(displaySizeName) && StringUtils.isNotBlank(skuParamUrl)){
				        skuParamUrl = skuParamUrl.replace("&amp;", "&");
                        String[] skuPrampArr = StringUtils.split(StringUtils.substringAfter(skuParamUrl, "?"), "&");
                        if(skuPrampArr != null && skuPrampArr.length > 0){
                            for(String skuParam : skuPrampArr){
                                if(StringUtils.containsIgnoreCase(skuParam, displaySizeName)){
                                    key = StringUtils.substringBefore(skuParam, "=");
                                    value = StringUtils.substringAfter(skuParam, "=");
                                }
                            }
                        }
                        imageBased = false;
				        name = value;
				        sizeSelect.add(value+";"+displaySizeName+";"+name+";;"+imageBased+";"+key);
				        isSizeDefault = false;
				    }
				}
			}
		}
		//switch image
		context.getUrl().getImages().put(System.currentTimeMillis()+"", swatchList);
		//all skus
		List<String> skuList = combinate(colorSelect,sizeSelect);
		//组装所有的skuurl
		Map<String,Url> skuUrlMap = new HashMap<String,Url>();
		for(String item : skuList){
			String[] skuProp  = StringUtils.split(item, "||");
			StringBuilder skuUrlBuilder = new StringBuilder(SKU_URL_PREFIX);;
			skuUrlBuilder.append("&pid=").append(itemId);
			StringBuilder  skuKey = new StringBuilder();
			if(skuProp != null && skuProp.length > 0){
				for(String prop : skuProp){
					String key = StringUtils.substringAfterLast(prop, ";");
					String value = StringUtils.substringBefore(prop, ";");
					skuUrlBuilder.append("&"+key).append("="+value);
					skuKey.append(value).append(";");
				}
				if(skuKey.length() > 0){
					skuKey.deleteCharAt(skuKey.length() -1);
				}
			}
			Url skuUrl = new Url(skuUrlBuilder.toString());
			skuUrl.setTask(context.getUrl().getTask());
			skuUrlMap.put(skuKey.toString(), skuUrl);
		}
		//multi threads request for price and stock
		Map<String,String> priceStockMap = getPriceStock(skuUrlMap);
		
		//sku 组装
		boolean stockFlag = false;
		Sku sku = new Sku();
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		Map<String,String> style_map = new HashMap<String,String>();
		boolean defaultDisplay = true;
		float defaultOrig = 0;
		float defaultSale = 0;
		String defaultUnit = StringUtils.EMPTY;
		for(String itemSkus : skuList){
			String[] itemSkuArr = StringUtils.split(itemSkus, "||");
			LSelectionList lSelectionList = new LSelectionList();
			List<Selection> selections = new ArrayList<Selection>();
			StringBuilder priceKey = new StringBuilder();
			String skuId = StringUtils.EMPTY;
			for(String item : itemSkuArr){
				String[] itemArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(item, ";");
				String optionKey = itemArr[1];
				String optionValue = itemArr[2];
				String imageBase = itemArr[4];
				if(StringUtils.equals(imageBase, "true")){
					skuId = itemArr[0];
					priceKey.append(skuId).append(";");
					String swatchUrl = itemArr[3];
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setStyle_id(optionValue);
					String sku_id = style_map.get(skuId);
					if(StringUtils.isBlank(sku_id)){
						LStyleList lStyleList = new LStyleList();
						lStyleList.setStyle_id(optionValue);
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(swatchUrl);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_cate_name(optionKey);
						lStyleList.setStyle_name(optionValue);
						if(defaultDisplay){//
							lStyleList.setDisplay(true);
						}
						l_style_list.add(lStyleList);
						style_map.put(skuId, skuId);
					}
				} else {
					String value = itemArr[0];
					if(StringUtils.isNotBlank(optionValue)){
						Selection selection = new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name(optionKey);
						selection.setSelect_value(optionValue);
						selections.add(selection);
					}
					priceKey.append(value);
				}
			}
			if(priceKey.length() > 0 && StringUtils.endsWith(priceKey.toString(), ";")){
				priceKey.delete(priceKey.length()-1, priceKey.length());
			}
			//sku price stock
			String priceStock = priceStockMap.get(priceKey.toString());
			if(priceStock == null){
				continue;
			}
			float sale = 0;
			float orig = 0;
			String unit = StringUtils.EMPTY;
			int stockStatus = 0;
			int stockNum = 0;
			String[] arr = StringUtils.splitByWholeSeparatorPreserveAllTokens(priceStock, ";");
			if(arr != null && arr.length > 0 ){
				String salePrice =  arr[0];
				String origPrice = arr[1];
				String stock = arr[2];
				String[] imageArr = StringUtils.split(arr[3], "|");
				if(StringUtils.isNotBlank(salePrice)){
					String currency = StringUtils.substring(salePrice, 0, 1);
					unit = Currency.codeOf(currency).name();
					sale = Float.valueOf(StringUtils.substring(salePrice, 1));
				}
				if(StringUtils.isNotBlank(origPrice)){
					orig = Float.valueOf(StringUtils.substring(origPrice, 1));
				}
				if(orig == 0){
					orig = sale;
				}
				if(defaultDisplay){
					defaultOrig = orig;
					defaultSale = sale;
					defaultUnit = unit;
					defaultDisplay = false;
				}
				if(StringUtils.isNotBlank(stock)){
					stockNum = Integer.valueOf(stock);
					stockStatus = 2;
					stockFlag = stockFlag || true;
				} else {
					stockFlag = stockFlag || false;
				}
				
				if(imageArr != null && imageArr.length > 0 ){
					List<Image> pics = new ArrayList<Image>();
					for(String imageUrl : imageArr){
						if (StringUtils.isNotBlank(imageUrl)) {
							Image image = new Image(imageUrl);
							pics.add(image);
						}
					}
					context.getUrl().getImages().put(skuId, pics);// picture download
				}
			}
			
			lSelectionList.setSale_price(sale);
			lSelectionList.setOrig_price(orig);
			lSelectionList.setPrice_unit(unit);
			lSelectionList.setStock_status(stockStatus);
			lSelectionList.setStock_number(stockNum);
			lSelectionList.setSelections(selections);
			l_selection_list.add(lSelectionList);
		}
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		// full doc info
		String docid = SpiderStringUtil.md5Encode(url);
		String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
		rebody.setDOCID(docid);
		rebody.setSite(new Site(domain));
		rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
		//title 
		rebody.setTitle(new Title(title, "", "", ""));
		// price 
		if(defaultOrig == 0 || defaultSale == 0){
			logger.error("Error while fetching spuprice from marcjacobs.com's detail page,url {}",context.getUrl().toString());
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"marcjacobs.com category url :"+context.getUrl().toString()+" spuprice is null.");
		}
		int save = Math.round((1 - defaultSale / defaultOrig) * 100);// discount
		rebody.setPrice(new Price(defaultOrig, save, defaultSale, defaultUnit));
		// stock
		int stockStatus = 0;
		if(stockFlag){
			stockStatus = 2;
		}
		rebody.setStock(new Stock(stockStatus));
		// images l_image_list
		// rebody.setImage(new LImageList(pics));
		// brand
		rebody.setBrand(new Brand(brand , "","",""));
		// Category
		//#main > div.container > div.row > div > ul > li > a
		es = d.select("#main > div.container > div.row > div > ul > li > a");
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if(es != null && es.size() > 0){
			for(Element ele :es ){
				String text = ele.text();
				if(StringUtils.isBlank(text)){
					continue;
				}
				cats.add(text);
				breads.add(text);
			}
		}
		rebody.setCategory(cats);
		// BreadCrumb
		rebody.setBreadCrumb(breads);
		// description
		//#pdpMain > div.product-detail.col-xs-12.col-sm-12.col-md-4.col-lg-4 > div.product-tabs > ul > li.active > div
		Map<String, Object> descMap = new HashMap<String, Object>();
		es = d.select("div.product-details-copy");
		StringBuilder sb = new StringBuilder();
		if(es != null && es.size() > 0){
		    sb.append(es.get(0).text());
		}
		descMap.put("en", sb.toString());
		rebody.setDescription(descMap);
		//feature
		Map<String, Object> featureMap = new HashMap<String, Object>();
		es = d.select("div.product-attributes.hidden > ul > li");
		if (es != null && es.size() > 0) {
            int count = 1;
            for (Element e : es) {
                featureMap.put("feature-" + count, e.text());
                count++;
                sb.append(e.text());
            }
        }
		rebody.setFeatureList(featureMap);

		Map<String, Object> propMap = new HashMap<String, Object>();
		if(cats.contains("WOMEN")){
			propMap.put("s_gender", "women");
		} else if(cats.contains("MEN")){
			propMap.put("s_gender", "men");
		} else {
			propMap.put("s_gender", "all");
		}
		rebody.setProperties(propMap);
		rebody.setSku(sku);
		setOutput(context, rebody);
		if(debug){
			System.out.println(rebody.parseTo());
		}
	}

	private Map<String,String> getPriceStock(Map<String, Url> skuUrlMap) {
		ExecutorService service = Executors.newFixedThreadPool(nThreads);
		Map<String,String> result = new HashMap<String,String>();
		try {
			List<PriceStockCall> calls = new ArrayList<PriceStockCall>();
			for(Map.Entry<String, Url> entry: skuUrlMap.entrySet()){
				String skuId = entry.getKey();
				Url skuUrl = entry.getValue();
				calls.add(new PriceStockCall(skuId, skuUrl));
			}
			List<Future<Map<String,String>>> futures = service.invokeAll(calls);
			for(Future<Map<String,String>> f : futures){
				try {
					Map<String,String> map = f.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					result.putAll(map);
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			service.shutdownNow();
		}
		return result;
	}
	
	public List<String> combinate(List<String> l1,List<String> l2){
		List<String> newList = new ArrayList<String>();
		if(l1 == null || l2 == null){
			return new ArrayList<String>();
		}
		if(l1.size() == 0 && l2.size() > 0){
			newList.addAll(l2);
			return newList;
		}
		if(l1.size() > 0 && l2.size() == 0){
			newList.addAll(l1);
			return newList;
		}
		for(int i=0; i < l1.size(); i++){
			for(int j=0; j<l2.size();j++){
				newList.add(l1.get(i)+"||"+l2.get(j));
			}
		}
		return newList;
	}

	class PriceStockCall implements Callable<Map<String,String>> {
		
		private String IMAGE_TMP = "https://i1.adis.ws/s/Marc_Jacobs/#mid#_#colorValue#_SET.js?func=app.mjiProduct.handleJSON&protocol=https";
		private String skuId;
		private Url skuUrl;
		
		private PriceStockCall(String skuId,Url skuUrl){
			this.skuId = skuId;
			this.skuUrl = skuUrl;
		}

		@Override
		public Map<String,String> call() throws Exception {
			Map<String,String> result = new HashMap<String,String>();
			try{
				String content = StringUtils.EMPTY;
				if(debug){
					content = Crawler.create().timeOut(10000).retry(3).proxy(true).proxyAddress("47.88.25.82").proxyPort(3128).url(skuUrl.getValue()).resultAsString();
				} else {
					content = HttpUtils.get(skuUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES, true);
				}
				//<span class="price-standard">$429.00</span>
				//<span class="price-sales">$129.00</span>
				if(StringUtils.isNotBlank(content)){
					content = StringUtils.replacePattern(content, "\\\r|\\\n|\\\t", "");
				}
				//sale
				Pattern p = Pattern.compile("<span[^>]*class=\"price-sales\">(.*?)</span>");
				Matcher m = p.matcher(content);
				String salePrice = StringUtils.EMPTY;
				if(m.find()){
					salePrice = StringUtils.replace(m.group(1), ",", "");
				}
				//orig
				p = Pattern.compile("<span[^>]*class=\"price-standard\">(.*?)<span[^>]*class=[^>]*strike[^>]*>[^<]*</span>");
				m = p.matcher(content);
				String origPrice = StringUtils.EMPTY;
				if(m.find()){
					origPrice = StringUtils.replace(m.group(1), ",", "");
				}
				//stock
				p = Pattern.compile("<select[^>]*class=\"input-text\"[^>]*name=\"Quantity\"[^>]*id=\"Quantity\"[^>]*data-available=\"(.*?)\"[^>]*>");
				m = p.matcher(content);
				String stockNum = "0";
				if(m.find()){
					stockNum = m.group(1);
				}
				logger.info("url {}, orgi {},sale {},stock {}",skuUrl.getValue(),origPrice,salePrice,stockNum);
				String imageReqUrl = StringUtils.EMPTY;
				p = Pattern.compile("<div[^>]*class=\"product-images\"[^>]*data-images=\"(.*?)\"[^>]*>");
				m = p.matcher(content);
				if(m.find()){
					imageReqUrl = StringUtils.replace(m.group(1), "&amp;", "&");
				}
				StringBuilder sb = new StringBuilder();
				if(StringUtils.isNotBlank(imageReqUrl)){
					for(int i = 0 ; i < 3; i++){
						try{
							Thread.sleep(1000);
							Url imgUrl = new Url(imageReqUrl);
							imgUrl.setTask(skuUrl.getTask());
							String imageData = StringUtils.EMPTY;
							if(debug){
								imageData = Crawler.create().timeOut(10000).retry(3).proxy(true).proxyAddress("47.88.25.82").proxyPort(3128).url(imgUrl.getValue()).resultAsString();
							} else {
								imageData = HttpUtils.get(imgUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES, false);
							}
							if(StringUtils.isNotBlank(imageData)){
								p = Pattern.compile("\"src\":\"(.*?)\",\"width\"");
								m = p.matcher(imageData);
								while(m.find()){
									String imageUrl = m.group(1);
									if(StringUtils.contains(imageUrl, "\\")){
										imageUrl = imageUrl.replace("\\", "");
									}
									sb.append(imageUrl).append("|");
								}
								if(sb.length() > 0){
									sb.deleteCharAt(sb.length()-1);
									break;
								}
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
				result.put(skuId, salePrice+";"+origPrice+";"+stockNum+";"+sb.toString());
			}catch(Throwable e){
				e.printStackTrace();
			}
			return result;
		}
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public static void main(String[] args) throws Exception {
		//https://www.marcjacobs.com/snapshot-small-camera-bag/889732791147.html
		//https://www.marcjacobs.com/overalls-and-cardigan-set/LM000519.html
		Marcjacobs shan = new Marcjacobs();
		shan.setDebug(true);
		Context context = new Context();
		context.setUrl(new Url("https://www.marcjacobs.com/the-grind-shopper-tote-bag/191267169293.html"));
		shan.invoke(context);
		
		
		/*String html = Crawler.create().timeOut(10000).retry(3).url("https://www.marcjacobs.com/snapshot-small-camera-bag/889732791147.html").resultAsString();
		Document d = JsoupUtils.parse(html);
		Elements es = d.select("div.product-tabs > ul > li");
		StringBuilder sb = new StringBuilder();
		if(es != null && es.size() > 0){
			for(Element ele : es){
				String text = JsoupUtils.text(ele.select("h2 > a"));
				if(StringUtils.contains(text, "Description")){
					sb.append(JsoupUtils.text(ele.select("div.tab-content")));
				}
			}
		}*/
		/*String url = "https://www.marcjacobs.com/on/demandware.store/Sites-marcjacobs-Site/default/Product-Variation?pid=LM000396&dwvar_LM000396_size=6A&dwvar_LM000396_color=601&source=detail&uuid=&Quantity=1&format=ajax";
		String content = Crawler.create().method("get").timeOut(60000).url(url).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).resultAsString();
		if(StringUtils.isNotBlank(content)){
			content = StringUtils.replacePattern(content, "\\\r|\\\n|\\\t", "");
		}
		Pattern p = Pattern.compile("<span[^>]*class=\"price-sales\">(.*?)</span>");
		Matcher m = p.matcher(content);
		String salePrice = StringUtils.EMPTY;
		if(m.find()){
			salePrice = StringUtils.replace(m.group(1), ",", "");
			System.out.println(salePrice);
		}
		
		p = Pattern.compile("<select[^>]*class=\"input-text\"[^>]*name=\"Quantity\"[^>]*id=\"Quantity\"[^>]*data-available=\"(.*?)\"[^>]*>");
		m = p.matcher(content);
		String stockNum = "0";
		if(m.find()){
			stockNum = m.group(1);
			System.out.println("stockNum:"+stockNum);
		}
		p = Pattern.compile("<span[^>]*class=\"price-standard\">(.*?)<span[^>]*class=[^>]*strike[^>]*>[^<]*</span>");
		m = p.matcher(content);
		String origPrice = StringUtils.EMPTY;
		if(m.find()){
			origPrice = StringUtils.replace(m.group(1), ",", "");
			System.out.println(origPrice);
		}
		//<div class="product-images" data-images="https://i1.adis.ws/s/Marc_Jacobs/LM000396_601_SET.js?func=app.mjiProduct.handleJSON&amp;protocol=https">
		p = Pattern.compile("<div[^>]*class=\"product-images\"[^>]*data-images=\"(.*?)\">");
		m = p.matcher(content);
		if(m.find()){
			System.out.println(m.group(1));
		}*/
		
	}
}