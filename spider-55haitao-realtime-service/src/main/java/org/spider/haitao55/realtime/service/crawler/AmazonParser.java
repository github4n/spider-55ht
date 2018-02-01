package org.spider.haitao55.realtime.service.crawler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class AmazonParser {
	
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	private String url ;
	private Document document;
	
	public AmazonParser(String url,Document document){
		this.url = url;
		this.document = document;
	}
	
	public RetBody retboby(){
		RetBody ret = new RetBody();
		ret.setDOCID(docID());
		ret.setSite(site());
		ret.setProdUrl(prodUrl());
		ret.setTitle(title());
		ret.setPrice(price());
		ret.setStock(stock());
		ret.setBrand(brand());
		ret.setBreadCrumb(breadCrumb());
		ret.setCategory(category());
		ret.setImage(image());
		ret.setProperties(properties());
		ret.setFeatureList(featureList());
		ret.setDescription(description());
		ret.setSku(sku());
		return ret;
	}
	

	public String docID() {
		String docid = SpiderStringUtil.md5Encode(url);
		return docid;
	}
	public Site site() {
		String domain = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(url,"amazon.com")){
			domain="www.amazon.com";
		} else if (StringUtils.containsIgnoreCase(url,"amazon.co.jp")) {
			domain="www.amazon.co.jp";
		}else if(StringUtils.containsIgnoreCase(url,"amazon.de")){
			domain="www.amazon.de";
		}  
		return new Site(domain);
	}
	
	public ProdUrl prodUrl() {
		String docid = SpiderStringUtil.md5Encode(url);
		return new ProdUrl(url, System.currentTimeMillis(), docid);
	}
	
	public Title title() {
		Elements es = document.select("span#productTitle");
		String title = JsoupUtils.text(es);
		if(StringUtils.containsIgnoreCase(url,"amazon.com")){
			return new Title(title, "","","");
		}else if (StringUtils.containsIgnoreCase(url,"amazon.co.jp")) {
			return new Title("", "",title,"");
		}else if(StringUtils.containsIgnoreCase(url,"amazon.de")){
			return new Title("", "","",title);
		}
		return null;
	}
	
	public Price price() {
		String content = document.html();
		Pattern p = Pattern.compile("<span[^>]*id=[^>]*priceblock[^>]*>(.*?)<");
		Matcher m = p.matcher(content);
		String salePrice = StringUtils.EMPTY;
		String unit = StringUtils.EMPTY;
		if(m.find()){
			String unitPrice = m.group(1);
			unit = getCurrencyValue(unitPrice);//得到货币代码
			salePrice = replace(unitPrice);
			if(Currency.EUR.name().equals(unit)){
				salePrice = salePrice.replaceAll("[EUR]", "");
			}else{
				salePrice = salePrice.replaceAll("[\\s￥,EUR]", "");
			}
		}
		p = Pattern.compile("<span[^>]*a-text-strike[^>]*>(.*?)<");
		m = p.matcher(content);
		String origPrice = StringUtils.EMPTY;
		if(m.find()){
			String unitPrice = m.group(1);
			origPrice = replace(unitPrice);
			if(Currency.EUR.name().equals(unit)){
				origPrice = origPrice.replaceAll("[EUR]", "");
			}else{
				origPrice = origPrice.replaceAll("[\\s￥,EUR]", "");
			}
		}
		p = Pattern.compile("<tr[^>]*regularprice_savings[^>]*>[^<]*<td[^>]*>[^<]*<[^>]*td>[^<]*<td[^>]*>([^<]*)<");
		m = p.matcher(content);
		String saveStr = StringUtils.EMPTY;
		if(m.find()){
			saveStr = StringUtils.substringBetween(replace(m.group(1)), "(", "%");
		}
		if(StringUtils.containsIgnoreCase(url,"amazon.de")){
			origPrice=convertPrice(origPrice);
			salePrice=convertPrice(salePrice);
		}
		if(StringUtils.isBlank(origPrice) ){
			origPrice = salePrice;
		}
		if(StringUtils.isBlank(salePrice) ){
			salePrice = origPrice;
		}
		if(StringUtils.isBlank(origPrice) || Float.valueOf(origPrice) < Float.valueOf(salePrice) ){
			origPrice = salePrice;
		}
		try{
			float orig = Float.valueOf(origPrice);
			float sale = Float.valueOf(salePrice);
			int save = 0;
			if(StringUtils.isBlank(saveStr)){
				save = Math.round((1 - sale /orig) * 100);// discount
			} else {
				save = Integer.valueOf(saveStr);
			}
			return new Price(orig, save, sale, unit);
		}catch(Exception e){
			//e.printStackTrace();
		}
		return null;
		
		
	}
	
	public Stock stock() {
		String content = document.html();
		if(StringUtils.containsIgnoreCase(url,"amazon.com")){//检查US是否自营
			Pattern p = Pattern.compile("<div[^>]*id=[^>]*availability[^>]*>[^<]*<span[^>]*>([^<]*)<");
			Matcher m = p.matcher(content);
			if(m.find()){
				String stockStr = replace(m.group(1));
				Pattern pattern = Pattern.compile("Only (\\d+) left in stock",Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(stockStr);
				if(matcher.find()){
					int stockNum = Integer.valueOf(replace(matcher.group(1)));
					if(stockNum > 0 ){
						return new Stock(2, stockNum);
					}
				}
				pattern = Pattern.compile("in stock|Usually ships|Want it",Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(stockStr);
				if(matcher.find()){
					return new Stock(1, 0);
				}
				pattern = Pattern.compile("Out of Stock|Not yet published|Not yet released|Available for Pre-order|Currently unavailable",Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(stockStr);
				if(matcher.find()){
					return new Stock(0, 0);
				}
			}
		}else if(StringUtils.containsIgnoreCase(url,"amazon.co.jp")){//JP
			String isSelf = document.select("#merchant-info a").text();
			if(StringUtils.isNotBlank(isSelf)){
				String stock = document.select("div#availability span.a-size-medium").text();
				if(!StringUtils.containsIgnoreCase(stock,"り扱いできません") &&
						!StringUtils.containsIgnoreCase(stock,"入荷時期は未定です")){
					return new Stock(1, 0);
				}else if(StringUtils.containsIgnoreCase(stock,"り扱いできません") ||
						StringUtils.containsIgnoreCase(stock,"入荷時期は未定です")){
					return new Stock(0, 0);
				}else{
					return new Stock(1, 0);
				}
			}
		}else if(StringUtils.containsIgnoreCase(url,"amazon.de")){//德亚
			String stock =document.select("div#ddmDeliveryMessage").text();
			if(!StringUtils.containsIgnoreCase(stock,"nicht nach")){
				return new Stock(1, 0);
			}else if(StringUtils.containsIgnoreCase(stock,"nicht nach")){
				return new Stock(0, 0);
			}else{
				return new Stock(1, 0);
			}
		}
		return null;
	}
	
	public Brand brand() {
		Elements es = document.select("a#brand");
		String brand = StringUtils.EMPTY;
		String href = getAttr(es, "href");
		if(StringUtils.isNotBlank(href)){
			brand = StringUtils.substringBetween(href, "/", "/");
			if(StringUtils.isNotBlank(brand) && StringUtils.contains(brand, "-")){
				brand = brand.replace("-", " ");
			}
			if(StringUtils.contains(href, "field-brandtextbin=")){
				try {
					brand = URLDecoder.decode(StringUtils.substringAfter(href, "field-brandtextbin="), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if(StringUtils.equals(brand, "s")){
				brand = StringUtils.substringAfter(href, "field-keywords=");
			}
			
		}
		if(StringUtils.containsIgnoreCase(url,"amazon.com")){
			return new Brand(brand, "","","");
		}else if (StringUtils.containsIgnoreCase(url,"amazon.co.jp")) {
			return new Brand("", "",brand,"");
		}else if(StringUtils.containsIgnoreCase(url,"amazon.de")){
			return new Brand("", "","",brand);
		}
		return null;
	}
	
	public List<String> breadCrumb() {
		List<String> breads = new ArrayList<String>();
		Elements es = document.select("div#wayfinding-breadcrumbs_feature_div > ul > li > span > a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				String bread = e.text();
				if(StringUtils.isBlank(bread)){
					continue;
				}
				breads.add(bread);
			}
		}
		return breads;
	}
	
	public List<String> category() {
		List<String> cats = new ArrayList<String>();
		Elements es = document.select("div#wayfinding-breadcrumbs_feature_div > ul > li > span > a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				String cat = e.text();
				if(StringUtils.isBlank(cat)){
					continue;
				}
				cats.add(cat);
			}
		}
		return cats;
	}
	
	public LImageList image() {
		return null;
	}
	
	public Map<String, Object> properties() {
		String gender = StringUtils.EMPTY;
		Elements es = document.select("div#wayfinding-breadcrumbs_feature_div > ul > li > span > a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				String cat = e.text();
				gender = getSex(cat);
			}
		}
		Map<String, Object> propMap = new HashMap<String, Object>();
		es = document.select("span#productTitle");
		String title = getText(es);
		if(StringUtils.isBlank(gender)){
			gender = getSex(title);
		}
		propMap.put("s_gender", gender);
		
		es = document.select("div#detailBullets_feature_div > ul > li > span");
		extractTextToMap(propMap, es);
		//div#detail-bullets > table > tbody > tr > td > div > ul > li:nth-child(1)
		es = document.select("div#detail-bullets > table > tbody > tr > td > div > ul > li");
		extractTextToMap(propMap, es);
		
		es = document.select("#productDetails_techSpec_section_1 > tbody > tr");
		for(Element e : es){
			String key = getText(e.select("th"));
			String value = getText(e.select("td"));
			propMap.put(key, value);
		}
		//#productDetails_detailBullets_sections1 > tbody > tr:nth-child(1)
		es = document.select("#productDetails_detailBullets_sections1 > tbody > tr");
		for(Element e : es){
			String key = getText(e.select("th"));
			String value = getText(e.select("td"));
			propMap.put(key, value);
		}
		return propMap;
	}
	
	
	public Map<String, Object> featureList() {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		String content = document.html();
		Elements es = document.select("div#productDescription > p");
		getFeature(es,featureMap);
		
		if(MapUtils.isEmpty(featureMap)){
			es = document.select("#productDescription .one-col");
			getFeature(es,featureMap);
		}
		if(MapUtils.isEmpty(featureMap)){
			 String iframeContent = StringUtils.substringBetween(content, "var iframeContent = \"", "\";");
		        if(StringUtils.isNotBlank(iframeContent)){
		        	try {
						String desc = URLDecoder.decode(iframeContent,"UTF-8");
						 Document d = JsoupUtils.parse(desc);
				         String descContent = getText(d.select("div.productDescriptionWrapper"));
				         featureMap.put("feature-1", descContent);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
		        }
		}
		
		return featureMap;
	}
	
	public Map<String, Object> description() {
		Map<String, Object> descMap = new HashMap<String, Object>();
		Elements es = document.select("div#productDescription > p");
		StringBuilder sb = new StringBuilder();
	    getDescription(es,sb);
		
		if(sb.length() < 1){
			es = document.select("#productDescription .one-col");
			getDescription(es,sb);
		}
		
		descMap.put("en", sb.toString());
		return descMap;
	}
	
	public Sku sku() {
		return null;
	}
	
	
	public void getDescription(Elements es,StringBuilder sb){
		if (es != null && es.size() > 0) {
			for (Element e : es) {
				sb.append(e.text());
			}
		}
	}
	public void getFeature(Elements es,Map<String, Object> featureMap){
		if (es != null && es.size() > 0) {
			int count = 1;
			for (Element e : es) {
				String feature = e.text();
				if(StringUtils.isNotBlank(feature)){
					featureMap.put("feature-" + count, feature);
					count++;
				}
			}
		}
	}
	
	
	public String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN)){
			gender = SEX_WOMEN;
		} else if(StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = SEX_MEN;
		} 
		return gender;
	}
	private void extractTextToMap(Map<String, Object> propMap, Elements es) {
		if (es != null && es.size() > 0) {
			for (Element e : es) {
				String key = StringUtils.trim(StringUtils.substringBefore(e.text(), ":"));
				String value = StringUtils.trim(StringUtils.substringAfter(e.text(), ":"));
				if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
					propMap.put(key, value);
				}
			}
		}
	}
	
	
	private String getText(Elements es) {
		if (es != null && es.size() > 0) {
			return es.get(0).text();
		}
		return StringUtils.EMPTY;
	}

	private String getAttr(Elements es, String attrKey) {
		if (es != null && es.size() > 0) {
			return es.get(0).attr(attrKey);
		}
		return StringUtils.EMPTY;
	}

	private static String replace(String dest) {
		if (StringUtils.isBlank(dest)) {
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$|,", ""));
	}
	
	private String getCurrencyValue(String val){
	    String currency = StringUtils.substring(val, 0, 1);
	    String unit = StringUtils.EMPTY;
	    if("￥".equals(currency)){
	    	unit = Currency.codeOf("J"+currency).name();//添加一个对日本货币 J前缀
	    }else if("E".equals(currency)){
	    	unit = Currency.codeOf("€").name();//EUR
	    }else{
	    	unit = Currency.codeOf(currency).name();
	    }
	    return unit;
   }
	
	private String convertPrice(String price) {
		if(StringUtils.isNotBlank(price)){
			price=price.trim().replace(".", "").replace(",", ".");
			return price;
		}
		return "";
	}
	
	public static void main(String[] args) {
		System.out.println(replace("$109,757.40"));
	}

}
