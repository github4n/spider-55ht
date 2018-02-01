package com.haitao55.spider.crawler.core.callable.custom.lookfantastic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

public class LookFantastic extends AbstractSelect{
	private static String requestUrl="http://www.lookfantastic.com/variations.json?productId=";
	private static String requestParam="&selected=";
	private static String variation1="&variation1=";
	private static String variation2="&variation2=";
	private static String variation3="&variation3=";
	private static String stockFlag="In stock";
	private static String domain="www.lookfantastic.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	@Override
	public void invoke(Context context) {
		String content = super.getInputString(context);
		Document doc=super.getDocument(context);
		RetBody rebody=new RetBody();
		List<Url> skuUrlsList = new ArrayList<Url>();
		String brand=StringUtils.substringBetween(content, "productBrand: \"", "\",");
		String title=StringEscapeUtils.unescapeHtml4(StringUtils.substringBetween(content, "productTitle: \"", "\","));
		String productId=StringUtils.substringBetween(content, "productID: \"", "\",");
		String salePrice=StringUtils.EMPTY;
		String origPrice=StringUtils.EMPTY;
		String save=StringUtils.EMPTY;
		boolean skuFlag=false;
		List<String> dimensionsList=new ArrayList<String>();
		
		/*sku url封装*/
		skuUrlsPackage(doc,productId,skuUrlsList,context);
		
		Elements elements=doc.select("form.field.showHint legend");
		if(CollectionUtils.isNotEmpty(elements)){
			for (Element element : elements) {
				dimensionsList.add(element.text());
			}
		}
		elements=doc.select("p.availability span");
		int stock_status=0;
		if(CollectionUtils.isNotEmpty(elements)){
			String stockStatus=elements.get(0).text();
			if(stockFlag.equals(stockStatus)){
				stock_status=1;
			}
			
		}
		Map<String,LookFantasticSkuBean> skuResult = new LookFantasticHandler().process(skuUrlsList);
		//sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		if(null!=skuResult&&skuResult.size()>0){
			for(Entry<String, LookFantasticSkuBean> entry : skuResult.entrySet()){
				String skuId = entry.getKey();
				LookFantasticSkuBean skuBean = entry.getValue();
				//selectlist
				LSelectionList lselectlist = new LSelectionList();
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(Float.valueOf(skuBean.getOrig()));
				lselectlist.setPrice_unit(skuBean.getUnit());
				lselectlist.setSale_price(Float.valueOf(skuBean.getSale()));
				//lookfantastic  not find number
				lselectlist.setStock_number(0);
				lselectlist.setStock_status(stock_status);
				List<Selection> selections = new ArrayList<Selection>();
				//stylelist
				LStyleList lStyleList = new LStyleList();
				if(!skuFlag){
					rebody.setPrice(new Price(Float.valueOf(skuBean.getOrig()), Integer.parseInt(skuBean.getSave()), Float.valueOf(skuBean.getSale()), skuBean.getUnit()));
					lStyleList.setDisplay(true);
				}
				skuFlag=true;
				//selectlist
				lselectlist.setStyle_id(skuBean.getStyleId());
				
				//selections
				List<Map<String, String>> selections2 = skuBean.getSelections();
				if(null!=selections2&&selections2.size()>0){
					for (Map<String, String> map : selections2) {
						Selection selection=new Selection();
						String select_name = map.get("select_name");
						String select_value = map.get("select_value");
						selection.setSelect_id(0);
						selection.setSelect_name(select_name);
						selection.setSelect_value(select_value);
						selections.add(selection);
					}
				}
				
				lselectlist.setSelections(selections);
				l_selection_list.add(lselectlist);
				//stylelist
				lStyleList.setGood_id(skuId);
				lStyleList.setStyle_switch_img("");
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_id(skuBean.getStyleId());
				lStyleList.setStyle_cate_name(skuBean.getCate_name());
				lStyleList.setStyle_name(skuBean.getStyleId());
				l_style_list.add(lStyleList);
				context.getUrl().getImages().put(skuId, skuBean.getImageUrl());
			}
		}
		
		//单品
		else{
			List<Image> pics=new ArrayList<Image>();
			elements = doc.select("div.container ul li a");
			if (CollectionUtils.isNotEmpty(elements)) {
				for (Element element : elements) {
					String imageUrl = element.attr("href");
					if (StringUtils.isNotBlank(imageUrl)) {
						imageUrl = StringUtils.trim(imageUrl);
					}
					Image image = new Image(imageUrl);
					pics.add(image);
				}
			}
			context.getUrl().getImages().put(productId,pics);
			
			elements=doc.select("p.product-price span");
			if(CollectionUtils.isNotEmpty(elements)){
				salePrice=elements.get(0).text();
			}
			elements=doc.select("p.price-rrp span");
			if(CollectionUtils.isNotEmpty(elements)){
				origPrice=elements.get(0).text();
			}
			
			String unit = getCurrencyValue(salePrice);//得到货币代码
			salePrice = salePrice.replaceAll("[£,]", "");
			origPrice = origPrice.replaceAll("[£,]", "");
			
			if(StringUtils.isBlank(replace(origPrice)) ){
				origPrice = salePrice;
			}
			if(StringUtils.isBlank(replace(salePrice)) ){
				salePrice = origPrice;
			}
			if(StringUtils.isBlank(origPrice) || Float.valueOf(replace(origPrice)) < Float.valueOf(replace(salePrice)) ){
				origPrice = salePrice;
			}
			if(StringUtils.isBlank(save)){
				save = Math.round((1 - Float.valueOf(replace(salePrice)) / Float.valueOf(replace(origPrice))) * 100)+"";// discount
			}
			
			rebody.setPrice(new Price(Float.valueOf(origPrice), Integer.parseInt(save), Float.valueOf(salePrice), unit));

		}
		Sku sku=new Sku();
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		
		
		//stock
		rebody.setStock(new Stock(stock_status));
		
		//brand
		rebody.setBrand(new Brand(brand, "","",""));
		
		//title
		rebody.setTitle(new Title(title, "","",""));
		
		// full doc info
		String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
		String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
		rebody.setDOCID(docid);
		rebody.setSite(new Site(domain));
		rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
		
		
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements cateElements = doc.select("div.breadcrumbs ul li");
		if (CollectionUtils.isNotEmpty(cateElements)) {
			for (Element element : cateElements) {
				String cat = Native2AsciiUtils.ascii2Native(element.text());
				if(StringUtils.isNotBlank(cat)){
					cats.add(cat);
					breads.add(cat);
				}
			}
		}
		rebody.setCategory(cats);
		// BreadCrumb
		breads.add(brand);
		rebody.setBreadCrumb(breads);
		// description
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		Document document = Jsoup.parse(content);
		Elements es = document.select("div[itemprop=description]");
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
		
		String gender = StringUtils.EMPTY;
		Map<String, Object> propMap = new HashMap<String, Object>();
		List<List<Object>> propattr=new ArrayList<List<Object>>();
		gender = getSex(sb.toString());
		gender = getSex(title);
		gender = getSex(cats.toString());
		propMap.put("s_gender", gender);
		es = document.select("div#technicaldetails");
		if (es != null && es.size() > 0) {
			for (Element e : es) {
				List<Object> proList=new ArrayList<Object>();
				List<List<String>> list=new ArrayList<List<String>>();
				String keyValue=StringUtils.EMPTY;
				Elements key = e.select("h3");
				if(CollectionUtils.isNotEmpty(key)){
					keyValue=key.get(0).text();
				}
				Elements valueList = e.select("tr");
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
		
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		rebody.setSku(sku);
		setOutput(context, rebody);
	}
	/**
	 * get 货币
	 * @param val
	 * @return
	 */
	private String getCurrencyValue(String val){
	    String currency = StringUtils.substring(val, 0, 1);
	    String unit = StringUtils.EMPTY;
	    if("£".equals(currency)){
	    	unit = Currency.codeOf(currency).name();
	    }
	    return unit;
		
   }	
	private  String replace(String dest){
		if(StringUtils.isBlank(dest)){
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));
		
	}
	
	/***
	 * sku请求url封装
	 * @param doc
	 * @param productId
	 * @param skuUrlsList
	 * @param context 
	 */
	private void skuUrlsPackage(Document doc, String productId, List<Url> skuUrlsList, Context context) {
		List<Map<String,Map<String,String>>> skuParam=new ArrayList<Map<String,Map<String,String>>>();
		int countvalue=0;
		//封装variation和option
		Elements elements = doc.select("div.variation-dropdowns form");
		if(CollectionUtils.isNotEmpty(elements)){
			for (Element element : elements) {
				countvalue++;
				Elements options = element.select("option:not(:first-child)");
				
				Elements variations =element.select("input[name=variation]");

				if(CollectionUtils.isNotEmpty(variations)){//多select参数封装
					for (Element variation : variations) {
						if(CollectionUtils.isNotEmpty(options)){
							for (Element option : options) {
								Map<String,Map<String,String>> map=new HashMap<String,Map<String,String>>();
								Map<String,String> map2=new HashMap<String,String>();
								String vari=variation.attr("value");
								String opt = option.attr("value");
								map2.put(vari,"variation"+countvalue);
								map2.put(opt,"option"+countvalue);
								map.put(countvalue+"", map2);
								skuParam.add(map);
							}
						}
					}
				}
			}
		}
		//[{1={5=variation1, 10499=option1}},{1={5=variation1, 10501=option1}}, {2={6=variation2, 2539=option2}}, {3={7=variation3, 5875=option3}}]
		if(null!=skuParam&&skuParam.size()>0){
			
			//实在没想到太好的办法...
			
			List<Map<String,String>> form1=new ArrayList<Map<String,String>>();
			List<Map<String,String>> form2=new ArrayList<Map<String,String>>();
			List<Map<String,String>> form3=new ArrayList<Map<String,String>>();
			List<Map<String,String>> form4=new ArrayList<Map<String,String>>();
			for (int i=0;i<skuParam.size();i++) {
				Map<String, Map<String, String>> map = skuParam.get(i);
				for(Map.Entry<String, Map<String, String>> entry: map.entrySet()){
					if("1".equals(entry.getKey())){//取出第一个selected
						Map<String, String> value = entry.getValue();
						form1.add(value);
					}
					if("2".equals(entry.getKey())){//取出第二个selected
						Map<String, String> value = entry.getValue();
						form2.add(value);
					}
					if("3".equals(entry.getKey())){//取出第三个selected
						Map<String, String> value = entry.getValue();
						form3.add(value);
					}
					if("4".equals(entry.getKey())){//取出第四个selected
						Map<String, String> value = entry.getValue();
						form4.add(value);
					}
				}
			}
			//[{1={5=variation1, 10499=option1}},{1={5=variation1, 10501=option1}}, {2={6=variation2, 2539=option2}}, {3={7=variation3, 5875=option3}}]
			if(null!=form1&&form1.size()>0){
//				String sku_url=requestUrl.concat(productId).concat(requestParam);
				String urltemp=StringUtils.EMPTY;
				StringBuffer param=null;
				StringBuffer param2=null;
				StringBuffer param3=null;
				for (Map<String, String> map : form1) {
					param=new StringBuffer();
					String urltemp1=urltemp;
					for (Map.Entry<String, String> entry : map.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();
						param.append("&").append(value).append("=").append(key);
					}
					urltemp1=urltemp1.concat(param.toString());
					if(null==form2||form2.size()==0){
						skuUrlsListPackage(productId,skuUrlsList,urltemp1,context);
					}
					if(null!=form2&&form2.size()>0){
						for (Map<String, String> map2 : form2) {
							param2=new StringBuffer();
							String urltemp2=urltemp1;
							for (Map.Entry<String, String> entry2 : map2.entrySet()) {
								String key = entry2.getKey();
								String value = entry2.getValue();
								param2.append("&").append(value).append("=").append(key);
							}
							urltemp2=urltemp2.concat(param2.toString());
							if(null==form3||form2.size()==0){
								skuUrlsListPackage(productId,skuUrlsList,urltemp2,context);
							}
							if(null!=form3&&form3.size()>0){
								for (Map<String, String> map3 : form3) {
									param3=new StringBuffer();
									String urltemp3=urltemp2;
									for (Map.Entry<String, String> entry3 : map3.entrySet()) {
										String key = entry3.getKey();
										String value = entry3.getValue();
										param3.append("&").append(value).append("=").append(key);
									}
									urltemp3=urltemp3.concat(param3.toString());
									skuUrlsListPackage(productId,skuUrlsList,urltemp3,context);
								}
							}
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @param productId
	 * @param skuUrlsList
	 * @param urltemp
	 * @param context 
	 */
	private static void skuUrlsListPackage(String productId,List<Url> skuUrlsList, String urltemp, Context context) {
		String selected=StringUtils.EMPTY;
		
		if(urltemp.contains(variation1)){
			selected="1";
		}
		
		if(urltemp.contains(variation2)){
			selected="2";
		}
		
		if(urltemp.contains(variation3)){
			selected="3";
		}
		
		String sku_url=requestUrl.concat(productId).concat(requestParam).concat(selected).concat(urltemp);
		Url skuUrl = new Url(sku_url);
		skuUrl.setTask(context.getUrl().getTask());
		skuUrlsList.add(skuUrl);
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
