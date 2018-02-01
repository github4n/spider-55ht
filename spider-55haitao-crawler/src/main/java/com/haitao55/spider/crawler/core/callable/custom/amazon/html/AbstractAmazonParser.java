package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

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
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: AbstractAmazonParser
  * @Description: 亞馬遜的解析器的默認實現
  * @author songsong.xu
  * @date 2016年10月14日 下午2:58:33
  *
 */
public abstract class AbstractAmazonParser implements  ItemParser{
	
	static String ITEM_URL_TEMPLATE = "https://www.amazon.com/dp/#itemId#/";
	static String DOMAIN = "www.amazon.com";
	static final String SEX_WOMEN = "women";
	static final String SEX_MEN = "men";
	@Override
	public Brand brand(Context context) {
		Document document = context.getCurrentDoc().getDoc();
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
		}
		if(StringUtils.containsIgnoreCase(context.getUrl().getValue(),"amazon.com")){
			return new Brand(brand, "","","");
		}else if (StringUtils.containsIgnoreCase(context.getUrl().getValue(),"amazon.co.jp")) {
			
			return new Brand("", "",brand,"");
		}else if(StringUtils.containsIgnoreCase(context.getUrl().getValue(),"amazon.de")){
			return new Brand("", "","",brand);
		}
		
		return null;
	}
	@Override
	public List<String> category(Context context) {
		List<String> cats = new ArrayList<String>();
		String cat=null;
		Document document = context.getCurrentDoc().getDoc();
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
		Document document = context.getCurrentDoc().getDoc();
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
		Document document = context.getCurrentDoc().getDoc();
		String content = context.getCurrentHtml();
		Elements es = document.select("div#productDescription > p");
		StringBuilder sb = new StringBuilder();
	    getDescription(es,sb);
		
		if(sb.length() < 1){
			es = document.select("#productDescription .one-col");
			getDescription(es,sb);
		}
		getDescription(content, sb);
		descMap.put("en", sb.toString());
		return descMap;
	}
	private void getDescription(String content, StringBuilder sb) {
		if(StringUtils.isBlank(sb.toString())){
			 String iframeContent = StringUtils.substringBetween(content, "var iframeContent = \"", "\";");
		        if(StringUtils.isNotBlank(iframeContent)){
		        	try {
						String desc = URLDecoder.decode(iframeContent,"UTF-8");
						 Document d = JsoupUtils.parse(desc);
				         String descContent = getText(d.select("div.productDescriptionWrapper"));
				         sb.append(descContent);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
		        }
		}
	}
	
	public void getDescription(Elements es,StringBuilder sb){
		if (es != null && es.size() > 0) {
			for (Element e : es) {
				sb.append(e.text());
			}
		}
	}
	
	@Override
	public String docID(Context context) {
		String itemUrl = context.getUrl().getValue();
		return SpiderStringUtil.md5Encode(itemUrl);
	}
	@Override
	public Map<String, Object> featureList(Context context) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Document document = context.getCurrentDoc().getDoc();
		String content = context.getCurrentHtml();
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
	
	
	@Override
	public LImageList image(Context context) {
		// TODO Auto-generated method stub
		return null;
	}
	/*@Override
	public Price price(String content) {
		// TODO Auto-generated method stub
		return null;
	}*/
	@Override
	public ProdUrl prodUrl(Context context) {
		String itemUrl = context.getUrl().getValue();
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
	@Override
	public Site site(Context context) {
		if (StringUtils.containsIgnoreCase(context.getUrl().getValue(),"amazon.co.jp")) {
			DOMAIN="www.amazon.co.jp";
		}else if(StringUtils.containsIgnoreCase(context.getUrl().getValue(),"amazon.de")){
			DOMAIN="www.amazon.de";
		}
		return new Site(DOMAIN);
	}
	/*@Override
	public Sku sku(String content) {
		// TODO Auto-generated method stub
		return null;
	}*/
	/*@Override
	public Stock stock(String content) {
		// TODO Auto-generated method stub
		return null;
	}*/
	@Override
	public Title title(Context context) {
		Document document = context.getCurrentDoc().getDoc();
		Elements es = document.select("span#productTitle");
		String title = getText(es);
		if(StringUtils.containsIgnoreCase(context.getUrl().getValue(),"amazon.com")){
			return new Title(title, "","","");
		}else if (StringUtils.containsIgnoreCase(context.getUrl().getValue(),"amazon.co.jp")) {
			return new Title("", "",title,"");
		}else if(StringUtils.containsIgnoreCase(context.getUrl().getValue(),"amazon.de")){
			return new Title("", "","",title);
		}
		return null;
	}
	
	public String getText(Elements es){
		if(es != null && es.size() > 0){
			return es.get(0).text();
		}
		return StringUtils.EMPTY;
	}
	
	public String getAttr(Elements es,String attrKey){
		if(es != null && es.size() > 0){
			return es.get(0).attr(attrKey);
		}
		return StringUtils.EMPTY;
	}

	public String replace(String dest){
		if(StringUtils.isBlank(dest)){
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));
		
	}

	@SuppressWarnings("unused")
	private boolean setStock(String stockStr,SkuBean skuBean) {
		Pattern pattern = Pattern.compile("Only (\\d+) left in stock",Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(stockStr);
		if(matcher.find()){
			int stockNum = Integer.valueOf(replace(matcher.group(1)));
			if(stockNum > 0 ){
				skuBean.setStockStatus(2);
				skuBean.setStockNum(stockNum);
				return true;
			}
		}
		pattern = Pattern.compile("in stock|Usually ships in|Want it",Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(stockStr);
		if(matcher.find()){
			skuBean.setStockStatus(1);
			skuBean.setStockNum(0);
			return true;
		}
		pattern = Pattern.compile("Out of Stock|Not yet published|Not yet released|Available for Pre-order|Currently unavailable",Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(stockStr);
		if(matcher.find()){
			skuBean.setStockStatus(0);
			skuBean.setStockNum(0);
			return true;
		}
		return false;
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
	public static void main(String[] args) {
		try {
			String url = HttpUtils.get("https://www.amazon.com/dp/B00DFFT5HG/?th=1");	
            String iframeContent = StringUtils.substringBetween(url, "var iframeContent = \"", "\";");
            String en = URLDecoder.decode(iframeContent,"UTF-8");
            Document d = JsoupUtils.parse(en);
            Elements es = d.select("div.productDescriptionWrapper");
            System.out.println(es.text());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
