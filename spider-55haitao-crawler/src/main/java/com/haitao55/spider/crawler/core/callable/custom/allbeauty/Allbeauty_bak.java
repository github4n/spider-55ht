package com.haitao55.spider.crawler.core.callable.custom.allbeauty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;

/**
 * Allbeauty 网站收录
 * 2016-11-26
 * @author denghuan
 *
 */
public class Allbeauty_bak extends AbstractSelect{
	
	private static final String domain = "www.allbeauty.com";

	@Override
	public void invoke(Context context) throws Exception {
		//String content = this.getInputString(context);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Cookie", setCookie());
		String content = Crawler.create().timeOut(60000).header(headers).url(context.getCurrentUrl())
		.resultAsString();
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			String origPirce = StringUtils.substringBetween(content, "class=\"productRRP\">RRP","</span>");
			String salePirce = StringUtils.substringBetween(content, "class=\"ourPrice\">Our Price","</span>");
			//String brand = StringUtils.substringBetween(content, "brand_name\":\"","\"");
			String skuId = StringUtils.substringBetween(content, "product\":{\"sku\":",",");
			Document doc = Jsoup.parse(content);
			String brand = doc.select(".productDescription h2").text();
			
			String title = doc.select(".productDescription h4").text();
			if(StringUtils.isBlank(title)){
				title = doc.select(".productDescription h3").text();
			}
			
			String isStock = doc.select(".productDescription form p.mt5 input.btn").attr("value");
			
			String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			String unit = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(salePirce) && 
					(StringUtils.containsIgnoreCase(salePirce, "&#163;") ||
							StringUtils.containsIgnoreCase(salePirce, "£"))){
				salePirce = salePirce.replace("&#163;", "");
				salePirce = salePirce.replace("£", "");
				unit = Currency.codeOf("£").name();
			}
			if(StringUtils.isNotBlank(origPirce) && 
					(StringUtils.containsIgnoreCase(origPirce, "&#163;") || 
							StringUtils.containsIgnoreCase(origPirce, "£"))){
				origPirce = origPirce.replace("&#163;", "");
				origPirce = origPirce.replace("£", "");
			}
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements cates = doc.select("span#breadcrumb a");
			for(int i = 0; i < cates.size(); i++){
				String cat = cates.get(i).text();
				if(!"Home".equals(cat) && !"Brands".equals(cat)){
					cats.add(cat);
					breads.add(cat);
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			List<Image> imageList = new ArrayList<Image>();
			Elements imagesEs = doc.select(".productSummary .productImageContainer img.productImage");
			for(Element e : imagesEs){
				String image = e.attr("src");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image));
				}
			}
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select(".productDescription .descriptionText").text();
			featureMap.put("feature-1", description);
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			rebody.setProperties(propMap);
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean display = true;
			Elements es = doc.select(".rangeTable .rangeProduct");
			if(es != null && es.size() > 0){
				for(Element e : es){
					LSelectionList lSelectionList = new LSelectionList();
					LStyleList  lStyleList = new LStyleList();
					String whichImage = e.select(".rangeImage .productImageContainer img").attr("src");
					String colorValue = e.select(".productName span.productType").text().trim();
					String skuOrigPrice = e.select("p.productPrice span.productRRP").text();
					String skuSalePrice = e.select("p.productPrice span.ourPrice").text();
					String isBuy = e.select(".buyButtons form input.btn").attr("value");
					String sOrigPrice = pattern(skuOrigPrice);
					String sSalePrice = pattern(skuSalePrice);
					
					if(StringUtils.isNotBlank(sOrigPrice)){
						lSelectionList.setOrig_price(Float.parseFloat(sOrigPrice));
					}else{
						lSelectionList.setOrig_price(Float.parseFloat(sSalePrice));
					}
					
					lSelectionList.setSale_price(Float.parseFloat(sSalePrice));
					
					List<Selection> selections = new ArrayList<>();
					//Selection selection = new Selection();
					lSelectionList.setGoods_id(colorValue);
					lSelectionList.setStyle_id(colorValue);
					lSelectionList.setPrice_unit(unit);
					//selections.add(selection);
					lSelectionList.setSelections(selections);
					int stockStatus = 0;
					if(StringUtils.isNotBlank(isBuy) && 
							StringUtils.containsIgnoreCase(isBuy, "Add to Bag")){
						stockStatus = 1;
					}
					
					if(display){
						lStyleList.setDisplay(true);
						display = false;
						if(StringUtils.isNotBlank(sOrigPrice) 
								&& StringUtils.isNotBlank(sSalePrice)){
							int save = Math.round((1 - Float.parseFloat(sSalePrice) / Float.parseFloat(sOrigPrice)) * 100);// discount
							rebody.setPrice(new Price(Float.parseFloat(sOrigPrice), 
									save, Float.parseFloat(sSalePrice), unit));
						}else if(StringUtils.isBlank(sOrigPrice) 
								&& StringUtils.isNotBlank(sSalePrice)){
							rebody.setPrice(new Price(Float.parseFloat(salePirce), 
									0, Float.parseFloat(salePirce), unit));
						}
						
					}
					lStyleList.setGood_id(colorValue);
					lStyleList.setStyle_cate_name("color");
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_switch_img(whichImage);
					lStyleList.setStyle_id(colorValue);
					lStyleList.setStyle_name(colorValue);
					context.getUrl().getImages().put(colorValue, imageList);// picture
					lSelectionList.setStock_status(stockStatus);
					l_selection_list.add(lSelectionList);
					l_style_list.add(lStyleList);
				}
			}
			
			int spuStock = 0;
			if(l_selection_list != null 
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
			}else{
				if(StringUtils.isNotBlank(isStock) && 
						StringUtils.containsIgnoreCase(isStock, "Add to Bag")){
					spuStock = 1;
				}
				if(StringUtils.isNotBlank(origPirce) 
						&& StringUtils.isNotBlank(salePirce)){
					int save = Math.round((1 - Float.parseFloat(salePirce) / Float.parseFloat(origPirce)) * 100);// discount
					rebody.setPrice(new Price(Float.parseFloat(origPirce), 
							save, Float.parseFloat(salePirce), unit));
				}else if(StringUtils.isBlank(origPirce) 
						&& StringUtils.isNotBlank(salePirce)){
					rebody.setPrice(new Price(Float.parseFloat(salePirce), 
							0, Float.parseFloat(salePirce), unit));
				}
				
				context.getUrl().getImages().put(skuId, imageList);// picture
				
			}
			
			rebody.setStock(new Stock(spuStock));
			
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			rebody.setSku(sku);
		}
		setOutput(context, rebody.parseTo());
		
	}
	
	private static String pattern(String price){
		Pattern pattern = Pattern.compile("(\\d+.\\d+)");
		Matcher matcher = pattern.matcher(price);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	private static String setCookie(){
		String cookie ="locale=GBP%2C48%2C0%2CEN; awinsource=uk; basket=%7B%226013%22%3A3%7D; PHPSESSID=gants1lospoum1mllfnn5aag82; SERVERID=server153; _dc_gtm_UA-200096-1=1; _ga=GA1.2.1990824704.1479874547; __atuvc=52%7C51%2C78%7C6%2C0%7C7%2C0%7C8%2C3%7C9; __atuvs=58b38bf35cd2779a002; __asc=05686bec15a7d5ab4d594d1318c; __auc=34761af61588f6592d528bb8291; __btr_id=f70422c1-c1fa-4b9f-81b9-a96935198be1; frontend=1; __zlcmid=dkg1Ovs22NfWKW";
		return cookie;
	}
}
