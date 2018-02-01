package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.context.Context;


/**
 * 
  * @ClassName: AmazonUSParser
  * @Description: 美國亞馬遜的解析
  * @author songsong.xu
  * @date 2016年10月17日 下午7:15:25
  *
 */
public class AmazonUSParser extends DefaultAmazonParser {
	
	private ResultContent result;

	public AmazonUSParser(ResultContent result) {
		super(null);
		this.result = result;
	}

	@Override
	public Price price(Context context) {
		String content = result.getContent();
		String skuUrl = result.getSkuUrl();
		Pattern p = Pattern.compile("<span[^>]*id=[^>]*priceblock[^>]*>(.*?)<");
		Matcher m = p.matcher(content);
		String salePrice = StringUtils.EMPTY;
		String unit = StringUtils.EMPTY;
		if(m.find()){
			salePrice = m.group(1);
			unit = getCurrencyValue(salePrice);//得到货币代码
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
			origPrice = m.group(1);
			if(Currency.EUR.name().equals(unit)){
				origPrice = origPrice.replaceAll("[EUR]", "");
			}else{
				origPrice = origPrice.replaceAll("[\\s￥,EUR]", "");
		
			}
		}
		p = Pattern.compile("<tr[^>]*regularprice_savings[^>]*>[^<]*<td[^>]*>[^<]*<[^>]*td>[^<]*<td[^>]*>([^<]*)<");
		m = p.matcher(content);
		String save = StringUtils.EMPTY;
		if(m.find()){
			save = StringUtils.substringBetween(replace(m.group(1)), "(", "%");
		}
		if(StringUtils.containsIgnoreCase(skuUrl,"amazon.de")){
			origPrice=convertPrice(origPrice);
			salePrice=convertPrice(salePrice);
		}
		
		if(StringUtils.isBlank(origPrice) || Float.valueOf(origPrice) < Float.valueOf(salePrice) ){
			origPrice = salePrice;
		}
		
		if(StringUtils.isBlank(save)){
			save = Math.round((1 - Float.valueOf(replace(salePrice)) / Float.valueOf(replace(origPrice))) * 100)+"";// discount
		}
		return new Price(Float.valueOf(origPrice), Integer.valueOf(save), Float.valueOf(salePrice), unit);
	}

	@Override
	public Stock stock(Context context) {
		String url = result.getSkuUrl();
		String content = result.getContent();
		int stockStatus = 0;
		int stockNum = 0;
		if(StringUtils.containsIgnoreCase(url,"amazon.com")){//检查US是否自营
			Pattern p = Pattern.compile("<div[^>]*id=[^>]*availability[^>]*>[^<]*<span[^>]*>([^<]*)<");
			Matcher m = p.matcher(content);
			if(m.find()){
				Pattern pattern = Pattern.compile("Only (\\d+) left in stock",Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(content);
				if(matcher.find()){
					int num = Integer.valueOf(replace(matcher.group(1)));
					if(stockNum > 0 ){
						stockStatus = 2;
						stockNum = num;
					}
				}
				pattern = Pattern.compile("in stock|Usually ships|Want it",Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(content);
				if(matcher.find()){
					stockStatus = 1;
					stockNum = 0;
				}
				pattern = Pattern.compile("Out of Stock|Not yet published|Not yet released|Available for Pre-order|Currently unavailable",Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(content);
				if(matcher.find()){
					stockStatus = 0;
					stockNum = 0;
				}
			}
		}else if(StringUtils.containsIgnoreCase(url,"amazon.co.jp")){//JP
			Document document = Jsoup.parse(content);
			String stock = document.select("div#availability span.a-size-medium").text();
			if(!StringUtils.containsIgnoreCase(stock,"り扱いできません") &&
					!StringUtils.containsIgnoreCase(stock,"入荷時期は未定です")){
				stockStatus = 1;
				stockNum = 0;
			}else if(StringUtils.containsIgnoreCase(stock,"り扱いできません") ||
					StringUtils.containsIgnoreCase(stock,"入荷時期は未定です")){
				stockStatus = 0;
				stockNum = 0;
			}else{
				stockStatus = 1;
				stockNum = 0;
			}
		}else if(StringUtils.containsIgnoreCase(url,"amazon.de")){//德亚
			//Pattern pShips = Pattern.compile("Versand durch Amazon",Pattern.CASE_INSENSITIVE);
			//Matcher mShips = pShips.matcher(content);
			Document document = Jsoup.parse(content);
			String stock =document.select("div#ddmDeliveryMessage").text();
			if(!StringUtils.containsIgnoreCase(stock,"nicht nach")){
				stockStatus = 1;
				stockNum = 0;
			}else if(StringUtils.containsIgnoreCase(stock,"nicht nach")){
				stockStatus = 0;
				stockNum = 0;
			}else{
				//没取到库存标识
				stockStatus = 1;
				stockNum = 0;
			}
		}
		
		return new Stock(stockStatus, stockNum);
	}

	@Override
	public Sku sku(Context context) {
		return super.sku(context);
	}

	@Override
	public Brand brand(Context context) {
		Document document = result.getDocument();
		Elements es = document.select("a#brand");
		String brand = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(context.getCurrentUrl(),"amazon.com")){
			String href = getAttr(es, "href");
			if(StringUtils.isNotBlank(href)){
				brand = StringUtils.substringBetween(href, "/", "/");
				if(StringUtils.isNotBlank(brand) && StringUtils.contains(brand, "-")){
					brand = brand.replace("-", " ");
				}
			}
			return new Brand(brand, "");
		}else if (StringUtils.containsIgnoreCase(context.getCurrentUrl(),"amazon.co.jp")) {
			brand = es.text();
			return new Brand("", "",brand,"");
		}else if(StringUtils.containsIgnoreCase(context.getCurrentUrl(),"amazon.de")){
			brand = es.text();
			return new Brand("", "","",brand);
		}
		return null;
	}

	@Override
	public List<String> category(Context context) {
		List<String> cats = new ArrayList<String>();
		String cat=null;
		Document document = result.getDocument();
		Elements es = document.select("div#wayfinding-breadcrumbs_feature_div > ul > li > span > a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				cat = e.text();
				cats.add(cat);
			}
		}
		return cats;
	}

	@Override
	public List<String> breadCrumb(Context context) {
		List<String> breads = new ArrayList<String>();
		String bread=null;
		Document document = result.getDocument();
		Elements es = document.select("div#wayfinding-breadcrumbs_feature_div > ul > li > span > a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				bread = e.text();
				breads.add(bread);
			}
		}
		return breads;
	}

	@Override
	public Map<String, Object> description(Context context) {
		Map<String, Object> descMap = new HashMap<String, Object>();
		Document document = result.getDocument();
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

	@Override
	public String docID(Context context) {
		String itemUrl = result.getSkuUrl();
		String content = result.getContent();
		String parent_asin = StringUtils.substringBetween(content, "\"parent_asin\":\"", "\",");
		if(StringUtils.isNotBlank(parent_asin)){
			itemUrl = ITEM_URL_TEMPLATE.replace("#itemId#", parent_asin);
		}
		return SpiderStringUtil.md5Encode(itemUrl);
	}

	@Override
	public Map<String, Object> featureList(Context context) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Document document = result.getDocument();
		Elements es = document.select("div#productDescription > p");
		getFeature(es,featureMap);
		
		if(MapUtils.isEmpty(featureMap)){
			es = document.select("#productDescription .one-col");
			getFeature(es,featureMap);
		}
		
		return featureMap;
	}

	@Override
	public LImageList image(Context context) {
		return super.image(context);
	}

	@Override
	public ProdUrl prodUrl(Context context) {
		String itemUrl = result.getSkuUrl();
		String docid = SpiderStringUtil.md5Encode(itemUrl);
		return new ProdUrl(itemUrl, System.currentTimeMillis(), docid);
	}

	@Override
	public Map<String, Object> properties(Context context) {
		String gender = StringUtils.EMPTY;
		Document document = context.getCurrentDoc().getDoc();
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

	@Override
	public Site site(Context context) {
		return super.site(context);
	}

	@Override
	public Title title(Context context) {
		return super.title(context);
	}
	
	
	/*public DOC DOC(){
		
	}*/

	
	/**
	 * get 货币
	 * @param val
	 * @return
	 */
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
	
	/**
	 * 德亚 金额需要转换
	 * @param origPrice
	 * @param salePrice
	 */
	private String convertPrice(String price) {
		if(StringUtils.isNotBlank(price)){
			price=price.trim().replace(".", "").replace(",", ".");
			return price;
		}
		return "";
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
	
}
