package com.haitao55.spider.crawler.core.callable.custom.superdrug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * Superdrug 网站收录
 * @author denghuan
 *
 */
public class Superdrug extends AbstractSelect{

	private static final String domain = "www.superdrug.com";
	
	private static final String BASE_URL = "http://www.superdrug.com";
	
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document docment = Jsoup.parse(content);
			String title = docment.select(".pad10top .lead").text();
			String brand = docment.select(".panel-body a.ViewAll span").text();
			String salePrice = docment.select(".pricing span.pricing__now").text();
			String origPrice = docment.select(".pricing span.pricing__was span").text();
			Elements skus = docment.select(".colour-content ul a");
			String pskuId = StringUtils.substringBetween(content, "product_ean': '", "',");
			String productStock = StringUtils.substringBetween(content, "product_stock': '", "',");
			String stockNumber = StringUtils.substringBetween(content, "product_stock_level_indicator': '", "',");
			
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(SpiderStringUtil.md5Encode(context.getCurrentUrl()));
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			if(StringUtils.isBlank(salePrice)){
				throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "Error while crawling superdrug saleprice error ,url"+context.getUrl().toString());
			}
			
			String currency = StringUtils.substring(salePrice, 0, 1);
			String unit = Currency.codeOf(currency).name();
			if(StringUtils.containsIgnoreCase(salePrice, currency)){
				salePrice = salePrice.replace(currency, "");
			}
			if(StringUtils.isNotBlank(origPrice) && StringUtils.containsIgnoreCase(origPrice, currency)){
				origPrice = origPrice.replace(currency, "");
			}
			
			if(StringUtils.isNotBlank(salePrice)
					&& StringUtils.isNotBlank(origPrice)){
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
			}else if(StringUtils.isNotBlank(salePrice)
					&& StringUtils.isBlank(origPrice)){
				rebody.setPrice(new Price(Float.parseFloat(salePrice), 0, Float.parseFloat(salePrice), unit));
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			for(Element es : skus){
				String skuUrl = es.attr("href");
				if(StringUtils.isNotBlank(skuUrl)){
					if(!StringUtils.containsIgnoreCase(skuUrl, domain)){
						skuUrl = BASE_URL + skuUrl;
					}
					Url currentUrl = new Url(skuUrl);
					currentUrl.setTask(context.getUrl().getTask());
					String html = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
					getLSelectList(l_selection_list,l_style_list,unit,pskuId,html,context);
				}
			}
			
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			
			int spuStock = 0;
			int stockCount = 0;
			if(l_selection_list != null 
					&& l_selection_list.size() > 0){
				for(LSelectionList ll : l_selection_list){
					int sku_stock = ll.getStock_status();
					int stockNb = ll.getStock_number();
					if (sku_stock == 2){
						spuStock = 2;
						stockCount = stockNb;
						break;
					}
					if (sku_stock == 1) {
						spuStock = 1;
					}
					
				}
			}else{
				if(StringUtils.isNotBlank(productStock) && !"outOfStock".equals(productStock)
						&& Integer.parseInt(stockNumber) > 0){
					spuStock = 2;
					stockCount = Integer.parseInt(stockNumber);
				}else if(StringUtils.isBlank(productStock) && 
						StringUtils.isNotBlank(stockNumber) && Integer.parseInt(stockNumber) > 0){
					spuStock = 1;
				}
			}
			rebody.setStock(new Stock(spuStock,stockCount));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements attrCat = docment.select("#breadcrumb a");
			if(attrCat != null && attrCat.size() > 0){
				for(int i = 0;i < attrCat.size()-1;i++){
					String cat  = attrCat.get(i).text();
					if(StringUtils.isNotBlank(cat) && !"Home".equals(cat)){
						cats.add(cat);
						breads.add(cat);
					}
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			List<Image> imgList = new ArrayList<>();
			getImageList(imgList,docment);
			context.getUrl().getImages().put(pskuId, imgList);// picture
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			String description = docment.select("#collapseOne .panel-body").text();
			StringBuilder sb = new StringBuilder();
			if(StringUtils.isNotBlank(description)){
				featureMap.put("feature-1", description);
				sb.append(description);
			}
			rebody.setProperties(propMap);
			rebody.setFeatureList(featureMap);
			descMap.put("en", sb.toString());
			rebody.setDescription(descMap);
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
		
	}
	
	
	private void getLSelectList(List<LSelectionList> l_selection_list,List<LStyleList> l_style_list,
			String unit,String pskuId,String content,Context context){
		LSelectionList lSelection = new LSelectionList();
		LStyleList lStyleList = new LStyleList();
		Document docment = Jsoup.parse(content);
		String salePrice = docment.select(".pricing span.pricing__now").text();
		String origPrice = docment.select(".pricing span.pricing__was span").text();
		String colorValue = docment.select(".colour-content ul a li.active").attr("title");
		if(StringUtils.isBlank(colorValue)){
		   colorValue = docment.select(".colour-content p.colour-name strong").text().trim();
		}
		docment.select(".colour-content p.colour-name strong").remove();
		String color = docment.select(".colour-content p.colour-name").text();
		String skuId = StringUtils.substringBetween(content, "product_ean': '", "',");
		String productStock = StringUtils.substringBetween(content, "product_stock': '", "',");
		String stockNumber = StringUtils.substringBetween(content, "product_stock_level_indicator': '", "',");
		
		lSelection.setGoods_id(skuId);
		lStyleList.setStyle_cate_id(0);
		lSelection.setStyle_id(colorValue);
		lStyleList.setStyle_cate_name(color);
		lStyleList.setStyle_id(colorValue);
		lStyleList.setStyle_name(colorValue);
		lStyleList.setStyle_switch_img("");
		
		List<Image> imgList = new ArrayList<>();
		getImageList(imgList,docment);
		
		
		if(pskuId.equals(skuId)){
			lStyleList.setDisplay(true);
			context.getUrl().getImages().put(pskuId, imgList);// picture
		}else{
			context.getUrl().getImages().put(skuId, imgList);// picture
		}
		lStyleList.setGood_id(skuId);
		
		int stock_Status = 0;
		if(StringUtils.isNotBlank(productStock) && !"outOfStock".equals(productStock)
				&& Integer.parseInt(stockNumber) > 0){
			stock_Status = 2;
		}else if(StringUtils.isNotBlank(productStock) && 
				StringUtils.isNotBlank(stockNumber) && Integer.parseInt(stockNumber) > 0){
			stock_Status = 1;
		}
		
		lSelection.setStock_status(stock_Status);
		
		if(StringUtils.isNotBlank(stockNumber) && Integer.parseInt(stockNumber) > 0){
			lSelection.setStock_number(Integer.parseInt(stockNumber));
		}
		
		if(StringUtils.isBlank(salePrice)){
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "Error while crawling superdrug saleprice error ,url"+context.getUrl().toString());
		}
		if(StringUtils.containsIgnoreCase(salePrice, "£")){
			salePrice = salePrice.replace("£", "");
		}
		if(StringUtils.isNotBlank(origPrice) 
				&& StringUtils.containsIgnoreCase(origPrice, "£")){
			origPrice = origPrice.replace("£", "");
		}
		
		if(StringUtils.isNotBlank(salePrice) && StringUtils.isNotBlank(origPrice)){
			lSelection.setOrig_price(Float.parseFloat(origPrice));
			lSelection.setSale_price(Float.parseFloat(salePrice));
		}else if(StringUtils.isNotBlank(salePrice) && StringUtils.isBlank(origPrice)){
			lSelection.setOrig_price(Float.parseFloat(salePrice));
			lSelection.setSale_price(Float.parseFloat(salePrice));
		}
		lSelection.setPrice_unit(unit);
		
		List<Selection> selections = new ArrayList<Selection>();
		lSelection.setSelections(selections);
		l_selection_list.add(lSelection);
		l_style_list.add(lStyleList);
		
	}
	
   private void getImageList(List<Image> imgList,Document docment){
	   Elements images = docment.select(".thumbnail ul#links li a");
		for(Element e :images){
			String image  = e.attr("href");
			if(StringUtils.isNotBlank(image) && !image.equals("#")){
				if(!StringUtils.containsIgnoreCase(image, domain)){
					image = BASE_URL+image;
				}
				imgList.add(new Image(image));
			}
		}
   }
	
}
