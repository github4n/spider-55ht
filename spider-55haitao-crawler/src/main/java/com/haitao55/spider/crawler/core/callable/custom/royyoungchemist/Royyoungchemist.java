package com.haitao55.spider.crawler.core.callable.custom.royyoungchemist;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

public class Royyoungchemist extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain="cn.royyoungchemist.com.au"; 
	private static final String SEX_WOMEN = "妇";
	private static final String SEX_WOMEN2 = "女";
	private static final String SEX_MEN = "男";
	private static final Double CARRY = 0.01d;
	@SuppressWarnings("serial")
	@Override
	public void invoke(Context context) throws Exception {
		try {
			String content = this.getInputString(context);
			RetBody rebody=new RetBody();
			Elements es =null;
			if (StringUtils.isNotBlank(content)) {
				Document doc=context.getCurrentDoc().getDoc();
				//price
				String orignPrice=StringUtils.EMPTY;
				String salePrice = doc.select("div.detail-price-contain>span.detail-last-price").text();//salePrice
				String save = doc.select("div.detail-price-contain>span.detail-discount").text();//salePrice
				String currency = StringUtils.substring(salePrice, 0, 3);
				orignPrice=salePrice;
				Float orign_price=Float.parseFloat(orignPrice.replace(currency, ""));
				Float sale_price=Float.parseFloat(salePrice.replace(currency, ""));
				
				es=doc.select("div.BundleProductContain.clearfix a div.BundleSingleRight div.BundleSingleQty");
				Float tota_price=0f;
				if(null!=es&&es.size()>0){
					for (int i=0; i<es.size();i++) {
						tota_price+=operating(currency,es.get(i).text());
					}
				}
				
				if(tota_price.intValue()!=0){
					orign_price=tota_price;
				}
				
				if(StringUtils.isNotBlank(save)){//有折扣
					save=save.replaceAll("[SAVE %]", "");
					orign_price=(float) (sale_price/(1-Double.parseDouble(save)*CARRY));
					int   scale  =   2;
					int   roundingMode  =  4;
					BigDecimal   bd  =   new  BigDecimal((double)orign_price);
					bd   =  bd.setScale(scale,roundingMode); 
					orign_price   =  bd.floatValue();  
				}
				
				if(orign_price<sale_price){
					orign_price=sale_price;
				}
				String price_unit = Currency.codeOf(currency).name();//unit
				
				if(null!=es&&es.size()>0&&StringUtils.isBlank(save)){
					save = String.valueOf(Math.round((1 - sale_price / orign_price) * 100));
				}
				
				rebody.setPrice(new Price(orign_price, StringUtils.isBlank(save)?0:Integer.parseInt(save), sale_price, price_unit));
				
				//stock
				int stock_status=1;
				String attr = doc.select("div.add-to-box button").attr("onclick");
				if(StringUtils.isBlank(attr)){//不能加入购物车
					stock_status=0;
				}
				
				rebody.setStock(new Stock(stock_status));
				
				//sku
				Sku sku=new Sku();
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				sku.setL_selection_list(l_selection_list);
				sku.setL_style_list(l_style_list);
				rebody.setSku(sku);
				
				
				//productId
				String productId=doc.select("div.infor>p:first-child").text();
				//title
				String title=doc.select("div.product-name").text();
				
				//brand
				String brandName=doc.select("td.col_1.brand+td.col_2").text();
				
				//image
				es=doc.select("p.product-image>a>img");
				String image = es.attr("abs:src");
				context.getUrl().getImages().put(productId, new ArrayList<Image>(){{
					add(new Image(image));
				}});
				
				// Category
				List<String> cats = new ArrayList<String>();
				List<String> breads = new ArrayList<String>();
				es = doc.select("div.breadcrumbs>ul>li:not(:first-child)");
				if (es != null && es.size()>0) {
					for (Element c : es) {
						String cat = Native2AsciiUtils.ascii2Native(c.text()).replaceAll("[>]", "");
						cats.add(cat);
						breads.add(cat);
					}
				}
				rebody.setCategory(cats);
				// BreadCrumb
				breads.add(brandName);
				rebody.setBreadCrumb(breads);
				
				
				String gender="";
				gender = getSex(title);
				// description
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
//				Document document = Jsoup.parse(content);
			    es = doc.select("div.tab_wrap div.std p");
			    if(null==es||es.size()==0){
			    	es=doc.select("div.tab_wrap div.std");
			    }
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
//				gender = getSex(sb.toString());
				propMap.put("s_gender", gender);
				es = doc.select("div.tab_content table td");
				if (es != null && es.size() > 0) {
					for (int i=0; i<es.size();i++) {
						if(i<es.size()-1){
							if(i%2==0){
								if (StringUtils.isNotBlank(es.get(i).text()) && StringUtils.isNotBlank(es.get(i+1).text())) {
									propMap.put(es.get(i).text(), es.get(i+1).text());
								}
							}
						}
					}
				}
				rebody.setProperties(propMap);
				
				
				String docid = SpiderStringUtil.md5Encode(domain + productId);
				rebody.setDOCID(docid);
				rebody.setSite(new Site(domain));
				rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), docid));
				rebody.setTitle(new Title("",title,"", ""));
				
				rebody.setBrand(new Brand("",brandName,"", ""));
				
			}
			setOutput(context, rebody);
		} catch (Throwable e) {
			e.printStackTrace();
			logger.error("Error while crawling url {} ,exception {}", context.getCurrentUrl(),e);
		}
	}
	
	/**
	 * 套装商品,价格运算
	 * @param text
	 * @return
	 */
	private static Float operating(String currency,String text) {
		String[] split = text.split(" ");
		if(null==split||split.length==0){
			return 0f;
		}
		Float qua=Float.parseFloat(split[0].split(":")[1]);
		Float orign_price=Float.parseFloat(split[1].replace(currency, ""));
		return qua*orign_price;
	}
	private static String getSex(String str) {
		String gender = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(str, SEX_WOMEN)){
			gender = "women";
		} else if(StringUtils.containsIgnoreCase(str, SEX_WOMEN2)) {
			gender = "women";
		} else if(StringUtils.containsIgnoreCase(str, SEX_MEN)) {
			gender = "men";
		} 
		return gender;
	}
	
	private static  String replace(String dest){
		if(StringUtils.isBlank(dest)){
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));
		
	}
	
}
