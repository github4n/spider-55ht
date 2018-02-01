package com.haitao55.spider.crawler.core.callable.custom.macys;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.reflect.TypeToken;
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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
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

public class Macys extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
//	private static final String MACYS_PICTURE_DOMAIN = "http://macys-o.scene7.com/is/image/MCY/products/";
	private static final String MACYS_PICTURE_DOMAIN = "https://slimages.macysassets.com/is/image/MCY//products/";
	private static final String DOMAIN = "www.macys.com";
	private static final String image_suffix="?op_sharpen=1&wid=400&hei=489&fit=fit,1&$filterlrg$";
	private static final String CURRENCY = "$";
	private static final String INSTOCK = "true";
	
	private	static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	@Override
	public void invoke(Context context) throws Exception {
		RetBody rebody=new RetBody();
		String url = context.getUrl().getValue();
		String referer = context.getUrl().getParentUrl();
		String content = "";
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(url, getHeaders(referer));
			context.setHtmlPageSource(content);
		}else{
			return;
		}
		Document document=Jsoup.parse(content);
		String offline = document.select("ul.similarItems li span").text();
		if(StringUtils.isNotBlank(offline) && 
				StringUtils.containsIgnoreCase(offline, "currently unavailable")){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"macys.com itemUrl:" + context.getUrl().toString() + "  this item is offline....");
		}
		Elements elements = null;
		String data = StringUtils.substringBetween(content, "id=\"productMainData\" type=\"application/json\">","</script>");
		JSONObject productJsonObject = JSONObject.parseObject(data);
		if(StringUtils.isNotBlank(data)){
			String productId = StringUtils.substringBetween(data, "\"id\": \"", "\",").trim();
			String title = StringUtils.substringBetween(data, "\"title\": \"", "\",");
			if(StringUtils.isBlank(title)){
				elements = document.select("h1.productName");
				if(CollectionUtils.isNotEmpty(elements)){
					title = elements.get(0).text();
				}
			}
			String breadCrumbCategory = StringUtils.substringBetween(data, "\"breadCrumbCategory\": \"", "\",").trim();
			String selectedColor = StringUtils.substringBetween(data, "\"selectedColor\": \"", "\",").trim();
			String colorwayPrimaryImages = StringUtils.substringBetween(data, "\"colorwayPrimaryImages\": ", "},").concat("}").trim();
			String colorwayAdditionalImages = StringUtils.substringBetween(data, "\"colorwayAdditionalImages\": ", "},").trim();
			String colorSwatch = StringUtils.substringBetween(data, "\"colorSwatchMap\": ", "},").concat("}").trim();
			String imageSource = StringUtils.substringBetween(data, "\"imageSource\": \"", "\",").trim();
			String additionalImages =  StringUtils.substringBetween(data, "\"additionalImages\": ", "],").concat("]").trim();
			String sizes_list = StringUtils.substringBetween(data, "\"sizesList\": ", "],").concat("]").trim();
			String skuData = StringUtils.substringBetween(data, "\""+productId+"\": ", "]");
			JSONObject skuJsonObject = null;
			if(StringUtils.isBlank(skuData)){
				skuJsonObject = productJsonObject.getJSONObject("upcMap");
			}else{
				skuData = skuData.concat("]").trim();
			}
			String brandName = StringUtils.substringBetween(data, "\"brandName\": \"", "\",").trim();
			String inStock = StringUtils.substringBetween(data, "\"inStock\": \"", "\",").trim();

			// color_primary
			Type colorPrimaryType = new TypeToken<Map<String, String>>() {}.getType();
			Map<String, String> colorPrimaryMap = JsonUtils.json2bean(colorwayPrimaryImages, colorPrimaryType);

			// color_additional
			// {Mushroom=4/optimized/3820104_fpx.tif,5/optimized/3820105_fpx.tif}
			Type colorAdditionalType = new TypeToken<Map<String, String>>() {}.getType();
			Map<String, String> colorAdditionalMap = JsonUtils.json2bean(colorwayAdditionalImages, colorAdditionalType);

			
			// additionalImages
			Type additionalImagesType = new TypeToken<List<String>>() {}.getType();
			List<String> additionalImagesList = JsonUtils.json2bean(additionalImages, additionalImagesType);
			
			// color_Swatch
			// styleList-->style-->style_switch_img
			Type colorSwatchType = new TypeToken<Map<String, String>>() {}.getType();
			Map<String, String> colorSwatchMap = JsonUtils.json2bean(colorSwatch, colorSwatchType);
			Set<String> colorkeys=null;
			
			if(colorSwatchMap.size()>0){
				colorkeys = colorSwatchMap.keySet();
			}

			// sizes_list
			Type sizesListType = new TypeToken<List<String>>() {}.getType();
			List<String> sizesList = JsonUtils.json2bean(sizes_list, sizesListType);

			// skuData
//			Type skuDataType = new TypeToken<List<Map<String,String>>>() {}.getType();
//			List<Map<String,String>> skuDataList = JsonUtils.json2bean(skuData, skuDataType);

			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Sku sku=new Sku();
			boolean displayFlag=true;
			boolean colorFlag=true;
			boolean sizeFlag=true;
			String defaltSku=StringUtils.EMPTY;
			Set<String> keySet = skuJsonObject.keySet();
			Map<String,String> colorMap = new HashMap<>();
//			if(null!=skuDataList&&skuDataList.size()>0){
			for (Map.Entry<String,Object> entry : skuJsonObject.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					JSONObject skuDataMap = (JSONObject)value;
					List<Image> pics = new ArrayList<Image>();
					LSelectionList lselectlist=new LSelectionList();
					LStyleList style=new LStyleList();
					String skuId = skuDataMap.getString("upcID");
					String color = skuDataMap.getString("color");
					String size = skuDataMap.getString("size");
					String isAvailable = skuDataMap.getString("isAvailable");//stock
					String salePrice = skuDataMap.getString("retailPrice");//sale_price
					String orignPrice = skuDataMap.getString("originalPrice");//orign_price
					if(StringUtils.isBlank(orignPrice)){
						orignPrice=salePrice;
					}
					Float orign_price=Float.parseFloat(orignPrice);
					Float sale_price=Float.parseFloat(salePrice);
					if(orign_price<sale_price){
						orign_price=sale_price;
					}
					String price_unit = Currency.codeOf(CURRENCY).name();//unit
					int save = Math.round((1 - sale_price / orign_price) * 100);
					String primaryImage = colorPrimaryMap.get(color);
					if(StringUtils.isNotBlank(primaryImage)){
						String address = MACYS_PICTURE_DOMAIN.concat(primaryImage);
						if(!address.contains(image_suffix)){
							address=address.concat(image_suffix);
						}
						Image image = new Image(address);
						pics.add(image);
					}else{
						if(StringUtils.isNotBlank(imageSource)){
							String address = MACYS_PICTURE_DOMAIN.concat(imageSource);
							if(!address.contains(image_suffix)){
								address=address.concat(image_suffix);
							}
							Image image = new Image(address);
							pics.add(image);
						}
					}
					
					String additionaImage = colorAdditionalMap.get(color);
					if(StringUtils.isNotBlank(additionaImage)){
						String[] additionaImages = additionaImage.split(",");
						for (String additionaImageurl : additionaImages) {
							String address = MACYS_PICTURE_DOMAIN.concat(additionaImageurl);
							if(!address.contains(image_suffix)){
								address=address.concat(image_suffix);
							}
							Image image = new Image(address);
							pics.add(image);
						}
					}else{
						if(null!=additionalImagesList&&additionalImagesList.size()>0){
							for (String imageUrl : additionalImagesList) {
								String address = MACYS_PICTURE_DOMAIN.concat(imageUrl);
								if(!address.contains(image_suffix)){
									address=address.concat(image_suffix);
								}
								Image image = new Image(address);
								pics.add(image);
							}
						}
					}
					context.getUrl().getImages().put(skuId, pics);
					String thumb=StringUtils.EMPTY;//style style_switch_img
					if(StringUtils.isNotBlank(colorSwatchMap.get(color))){
						thumb=MACYS_PICTURE_DOMAIN.concat(colorSwatchMap.get(color));
					}
					
					Integer stock_status=0;
					if(INSTOCK.equals(isAvailable)){
						stock_status=1;
					}
					if(null==colorkeys&&"No Color".equals(color)&&StringUtils.isBlank(color)){
						colorFlag=false;
					}
					if(sizesList.size()==0||(sizesList.size()==1&&sizesList.get(0).equals("No Size"))){
						sizeFlag=false;
					}
					if(keySet.size()>=1){
						String style_cate_name=StringUtils.EMPTY;
						String style_id=StringUtils.EMPTY;
						List<Selection> selections = new ArrayList<Selection>();
						//l_selection_list
						lselectlist.setGoods_id(skuId);
						if(sizeFlag){
							style_cate_name="Size";
							style_id=size;
						}
						if(colorFlag){
							style_cate_name="Color";
							style_id=color;
						}
						//针对只有一个颜色 没有尺码
						if(!(colorFlag || sizeFlag)&&StringUtils.isNotBlank(selectedColor)){
							style_cate_name="Color";
							style_id=color;
						}
						if("No Color".equals(color)&&"No Size".equals(size)){
							style_cate_name="Size";
							style_id="No Size";
						}
						
						if(colorFlag && sizeFlag){
							Selection selection=new Selection();
							selection.setSelect_id(0);
							selection.setSelect_name("Size");
							selection.setSelect_value(size);
							selections.add(selection);
						}
						lselectlist.setStyle_id(style_id);
						lselectlist.setOrig_price(orign_price);
						lselectlist.setSale_price(sale_price);
						lselectlist.setPrice_unit(price_unit);
						lselectlist.setStock_number(0);
						lselectlist.setStock_status(stock_status);
						
						lselectlist.setSelections(selections);
						
						//l_style_list
						style.setStyle_switch_img(thumb);
						style.setStyle_id(style_id);
						style.setStyle_cate_id(0l);
						style.setStyle_cate_name(style_cate_name);
						style.setStyle_name(style_id);
						// style.setStyle_images(picsPerSku);
						style.setGood_id(skuId);
						style.setStyle_switch_img(thumb);
						if(displayFlag){
							if (StringUtils.isNotBlank(selectedColor) && selectedColor.equals(color)) {
								style.setDisplay(true);
								displayFlag=false;
								defaltSku=skuId;
							}
						}
						l_selection_list.add(lselectlist);
						l_style_list.add(style);
					}
					
					if(keySet.size()==1){
						//price
						rebody.setPrice(new Price(orign_price, save, sale_price, price_unit));
					}
					if(defaltSku.equals(skuId)){
						rebody.setPrice(new Price(orign_price, save, sale_price, price_unit));
					}
						
				}
//			}
			
			//设置默认sku
			if(CollectionUtils.isNotEmpty(l_style_list)&&l_style_list.size()==1){
				List<LStyleList> l_style_list_new = new ArrayList<LStyleList>();
				LStyleList lStyleList = l_style_list.get(0);
				if(!lStyleList.isDisplay()){
					lStyleList.setDisplay(true);
				}
				l_style_list_new.add(lStyleList);
				l_style_list=l_style_list_new;
			}
			//l_selection_list,l_style_list
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			
			//sku
			rebody.setSku(sku);
			
			Integer stock_status=0;
			if(INSTOCK.equals(inStock)){
				stock_status=1;
			}
			//stock
			rebody.setStock(new Stock(stock_status));
			
			//brand
			rebody.setBrand(new Brand(brandName, "","",""));
			
			//title
			rebody.setTitle(new Title(title, "","",""));
			
			String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
//				String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(DOMAIN));
//				rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			
			// Category
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			if(StringUtils.isNotBlank(breadCrumbCategory)){
				String[] breadsSplit = breadCrumbCategory.split("-");
				for (String bread : breadsSplit) {
					bread=StringEscapeUtils.unescapeHtml4(bread);
					breads.add(bread);
					cats.add(bread);
				}
			}
			if(cats.size()==0){
				cats.add(brandName);
			}
			rebody.setCategory(cats);
			// BreadCrumb
			breads.add(brandName);
			rebody.setBreadCrumb(breads);
			
			// description
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Elements es = document.select("section>ul#bullets li");
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
			
			String gender=StringUtils.EMPTY;
			
			Map<String, Object> propMap = new HashMap<String, Object>();
			gender = getSex(breads.toString());
			propMap.put("s_gender", gender);
			rebody.setProperties(propMap);
			
		}
		setOutput(context, rebody);
		
	}
	
	private String getContent(Context context,String referer) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		String url = context.getCurrentUrl();
		if(!StringUtils.containsIgnoreCase(url, "https")){
			url = url.replace("http:", "https:");
		}
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders(referer)).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders(referer)).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	private static Map<String,Object> getHeaders(String referer){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		 headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Host", "www.macys.com");
		 headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
		 headers.put("Cache-Control", "max-age=0");
		 headers.put("referer", referer);
		 headers.put("Cookie", "RTD=f137b60f1371b0f136190f136a70f137760f139b20f13b8d0f1310e0; SEED=-1644804040075046425; _4c_mc_=56a7587f488d43c55b2e25732abb14a4; BVBRANDID=50b96188-5292-4f10-b930-2cb959066b87; MISCGCs=USERPC1_92_850013_87_USERLL1_92_33.4486%2C-112.07333_87_USERST1_92_AZ3_87_USERDMA1_92_7533_87_DT1_92_PC3_87_DSW1_92_2803_87_DSH1_92_1753_87_DBN1_92_Chrome3_87_DMN1_92_613_87_rvi1_92_2219254%2C45349593_87_BOPSPICKUPSTORE1_92_5154; shippingCountry=US; SignedIn=0; ak_bmsc=A8057EDE59CFBEE3BF9AC14538FF4CE8170AF83E8A400000EAA0F2596BA06016~plsH9CntSIuzn5hKYFOx0plJkEuhRFSeFu/EgNJFkuHtEY0Gf9rs9D1kAk7Yymr1HC5EcOazwdWbEymVaqDYI8Q7GtBI4BaT8QL3V4nYOpQfvNEp9NxYPVuEL8znesqNaNvLRICkDKkt0yGMCr+k2rU1CbqU5ZbhKrBGhnv/bZ6LL71LrBDB0Eu7dSvzAy1Z92+GiVRjU8hjwHvrH83Ek+anabtG3hF6pX5+qxCyUW82DKqSAn8g/Z4K5Z/1L6bu5j0zlhjg1Fw/d5xN+pgEgkwg2RafeEXA4PR3MxayBDcSX2r1hHvy4FmstqTI+2aOwcJBv0+sgSBnOsbHf+HZO4kg==; AMCVS_8D0867C25245AE650A490D4C%2540AdobeOrg%40AdobeOrg=1; AMCV_8D0867C25245AE650A490D4C%2540AdobeOrg%40AdobeOrg=-1891778711%7CMCIDTS%7C17467%7CMCMID%7C17728691128096297962306680657653504299%7CMCOPTOUT-1509080358s%7CNONE%7CMCAID%7C2C3F736C0519519B-4000060D6006A946%7CvVersion%7C2.4.0; CMAVID=none; AMCVS_8D0867C25245AE650A490D4C%40AdobeOrg=1; AMCV_8D0867C25245AE650A490D4C%40AdobeOrg=-1891778711%7CMCIDTS%7C17467%7CMCMID%7C28687526477529657654200199467711982478%7CMCAAMLH-1509677961%7C9%7CMCAAMB-1509677961%7CRAK0UQ-l7kNyPJ-DNxndOHc89GgT8iL8XTPST5iyfKwJ5YU%7CMCOPTOUT-1509080361s%7CNONE%7CMCAID%7C2C3F736C0519519B-4000060D6006A946%7CMCSYNCSOP%7C411-17474%7CvVersion%7C2.4.0; cmTPSet=Y; TLTSID=00004981774928548761780166648598; xdVisitorId=1201kGM3S6YCYu1gKzvW2LYfPSQ2bRB3mto2Fbt_B0CbPTwA739; s_fid=6460250DA675A6A3-246440BE26B35F56; TS0132ea28=0112b7dea0ebc30b7f815848b3dd1061fde516589c89144ede616cd0216626585abb43b1d6; akavpau_www_www1_macys=1509073705~id=0a21fe6b296ca61c0a34a79936c7dc04; CoreM_State=58~-1~-1~-1~-1~3~3~5~3~3~7~7~|~5E2AEC43~DAB1F120~|~~|~~|~0~1||||||~|~1507252653111~1507252583824~|~~|~~|~~|~~|~~|~~|~; CoreM_State_Content=6~|~D14767D88F82214F~50FAA048893D91A3~002C13E5E8B266DF~EB3DAC925C0631B7~C385AFD78F8453D6~|~0~1~2~3~4; s_sq=%5B%5BB%5D%5D; GCs=CartItem1_92_03_87_UserName1_92_4_02_; bm_sv=5F6BAE224FCC70CA4C117D3944A5B963~50aX68CImY7FcMgbBgtBliVZXJV+l7SEOP1lNwgkNbEndYC9AI8tXcY/47Pm+ImOz71sq85YnF0Lwz7cfShznEsv93VXDgzz2SAMGrnj8/5FAQayvOIhnNrTlPywcYUxwVsMMBv8DjnYyWMFyQs9W5OKhsxecZj2PoxYvQY3/pg=; utag_main=v_id:015eef40efc0008f09ea3244c5a002085001607d0086e$_sn:2$_ss:0$_st:1509075222520$vapi_domain:macys.com$ses_id:1509073161194%3Bexp-session$_pn:6%3Bexp-session; FORWARDPAGE_KEY=https%3A%2F%2Fwww.macys.com%2Fshop%2Fproduct%2Fnew-balance-boys-247-casual-sneakers-from-finish-line%3FID%3D4574735%26CategoryID%3D63270; c29=mcom%3Apdp%3Asingle%3A4574735%3Anew%20balance%20boys%27%20247%20casual%20sneakers%20from%20finish%20line; v30=product; s_cc=true; _uetsid=_uetbeca2df1; _ga=GA1.2.1499405418.1507252565; _gid=GA1.2.652425111.1509073247; mbox=PC#2b42560db55944938d39532dcf15ffdb.20_22#1572318223|session#57a6a0ee123444bd809694d037dee4b6#1509075287; smtrrmkr=636446702257414513%5Eba81b9eb-33aa-e711-8165-0a56d4370718%5Ea070916d-c3ba-e711-815b-0aec35f1f106%5E0%5E192.243.119.27; _loop_ga=GA1.2.f9dfe13d-4296-4328-aea6-409db179dbdb; _loop_ga_gid=GA1.2.1892402297.1509073430; BVBRANDSID=0fada1ab-8c92-4a31-b49a-e9bacd72d2a1; mercury=true; atgRecVisitorId=1201kGM3S6YCYu1gKzvW2LYfPSQ2bRB3mto2Fbt_B0CbPTwA739;");
		 return headers;
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
	
	public static void main(String[] args) throws Exception {
		Macys mk = new Macys();
		Context con = new Context();
		con.setRunInRealTime(true);
		con.setUrl(new Url(
				"https://www.macys.com/shop/product/reebok-girls-classic-metallic-casual-sneakers-from-finish-line?ID=5490880"));
		mk.invoke(con);
	}
}
