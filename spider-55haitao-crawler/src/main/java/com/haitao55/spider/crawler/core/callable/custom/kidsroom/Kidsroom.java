package com.haitao55.spider.crawler.core.callable.custom.kidsroom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
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
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Constants;

public class Kidsroom extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.kidsroom.de/en/";
	@Override
	public void invoke(Context context) throws Exception{
		try{
			String content = super.getInputString(context);
			RetBody retbody = new RetBody();
			if (StringUtils.isNotBlank(content)){
				String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
				String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
				Document document = Jsoup.parse(content);
				//BS Data
				Elements basic_info = document.select("div[class=Layout1 Middle]");
				Elements stock_info = document.select("input[value=Add to basket]");
				if(!stock_info.isEmpty()){
					retbody.setStock(new Stock(1,0));
				}
				else{
				    retbody.setStock(new Stock(0,0));
				}
				Elements bread_crumbs_ret = basic_info.select("div[class=BreadCrumbInnerDiv]");
				List<String> bread_list = new ArrayList<String>();
				if(!bread_crumbs_ret.isEmpty()){
					for(Element bc:bread_crumbs_ret.select("span[class=BreadCrumbWrapper]")){
					    bread_list.add(bc.text());	
					}
				    Elements bread_crumbs_ret_last = bread_crumbs_ret.select("span[class=BreadcrumbLastItem]");
				    bread_list.add(bread_crumbs_ret_last.text());
				}
			String sku_id = document.select("input[name=ViewObjectID]").attr("value");	
			String brand_info = document.select("head").select("title").text();
			String brand = brand_info.split(" ")[0];
			String title = brand_info.split(" -")[0];
			Elements pic_box = basic_info.select("div[class=ICMaqicZoom]").select("a");
			List<Image> img_list = new ArrayList<Image>();
			if(pic_box.size()>1){
				for(int i=1;i < pic_box.size();i++){
					Element pic = pic_box.get(i);
					img_list.add(new Image("http://www.kidsroom.de"+pic.attr("href")));
				}
			}
			else{
				img_list.add(new Image("http://www.kidsroom.de"+pic_box.get(0).attr("href")));
			}
			Elements description_info = basic_info.select("div[id=DynamicDescriptionBox]");
			String description= "";
			if(!description_info.isEmpty()){
				description = description_info.text().replace("\n", "");			    
			}
			Map<String, Object> descMap = new HashMap<String, Object>();
			descMap.put("en", description);
			Elements price_info = basic_info.select("div[class=Price]");
			Elements price_sale = price_info.select("span[id=ProductPrice]");
			Elements price_original = price_info.select("span[class=LineThrough]");
			String unit = "EUR";
			float sale_price = 0,origin_price = 0;
			if(!price_original.isEmpty()){
				sale_price = Float.valueOf(price_sale.text().replace("\n", "").replace("€", "").replace(",", ""));
				origin_price = Float.valueOf(price_original.text().replace("\n", "").replace("€", "").replace(",", ""));
			}
			else{
				sale_price=origin_price = Float.valueOf(price_sale.text().replace("\n", "").replace("€", "").replace(",", ""));
			}
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Sku sku = new Sku();
			int stock_flag = 1;
			String style_id = "One Color";
			Elements selection_info = basic_info.select("select[id=VariationSelect_Groesse]");
			Elements selection_info_second = basic_info.select("span[class=FloatLeft n4]");
			if(!selection_info.isEmpty()){
				Elements option = selection_info.select("option");
				for(Element e:option){
					LSelectionList lSelectionList = new LSelectionList();
					lSelectionList.setGoods_id(sku_id);
					lSelectionList.setStyle_id(style_id);
					lSelectionList.setSale_price(sale_price);
					lSelectionList.setOrig_price(origin_price);
					lSelectionList.setPrice_unit(unit);
					if(e.attr("class")=="disabled"){
						lSelectionList.setStock_status(0);
						lSelectionList.setStock_number(0);
						stock_flag *=1;
					}
					else{
						lSelectionList.setStock_status(1);
						lSelectionList.setStock_number(0);
						stock_flag *=0;
					}
					Selection selection_size = new Selection();
					selection_size.setSelect_id(0);
					selection_size.setSelect_name("Size");
					selection_size.setSelect_value(e.text());
					l_selection_list.add(lSelectionList);
				}
			}
			else if(selection_info_second.size() > 1){
				 for(int i=1;i<selection_info_second.size();i++){
					 LSelectionList lSelectionList = new LSelectionList();
					 lSelectionList.setStyle_id(style_id);
					 lSelectionList.setGoods_id(sku_id);
					 lSelectionList.setSale_price(sale_price);
					 lSelectionList.setOrig_price(origin_price);
					 lSelectionList.setPrice_unit(unit);
					 Element size = selection_info_second.get(i);
					 if(size.attr("class")=="disabled"){
							lSelectionList.setStock_status(0);
							lSelectionList.setStock_number(0);
							stock_flag *=1;
						}
						else{
							lSelectionList.setStock_status(1);
							lSelectionList.setStock_number(0);
							stock_flag *=0;
						}
					 Selection selection_size = new Selection();
					 selection_size.setSelect_id(0);
					 selection_size.setSelect_name("Size");
					 selection_size.setSelect_value(size.text());
					 List<Selection> selections = new LinkedList<Selection>();
					 selections.add(selection_size);
					 lSelectionList.setSelections(selections);
					 l_selection_list.add(lSelectionList);
				 }
			}
			if(stock_flag == 0){
                retbody.setStock(new Stock(1, 0));
				}
			else if(stock_flag == 1){
				retbody.setStock(new Stock(0,0));
				}
			if(!l_selection_list.isEmpty()){
				LStyleList lStyleList = new LStyleList();
				lStyleList.setStyle_cate_id(0);
				lStyleList.setGood_id(sku_id);
				lStyleList.setStyle_cate_name("color");
				lStyleList.setStyle_id(style_id);
				lStyleList.setStyle_name(style_id);
				lStyleList.setStyle_switch_img("");
				lStyleList.setDisplay(true);
				context.getUrl().getImages().put(sku_id, img_list);
				l_style_list.add(lStyleList);
			}
			int save = Math.round((1 - sale_price / origin_price) * 100);
			retbody.setBrand(new Brand(brand, ""));
			retbody.setDescription(descMap);
			retbody.setTitle(new Title(title,""));
			retbody.setDOCID(docid);
			retbody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			retbody.setPrice(new Price(origin_price, save, sale_price, unit));
			retbody.setSite(new Site(domain));
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
	        retbody.setSku(sku);
	        retbody.setBreadCrumb(bread_list);
		    retbody.setCategory(bread_list);
		    setOutput(context,retbody);	
			}			
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("Error while crawling url {} ,exception {}", context.getCurrentUrl(),e);
			
		}
		
	}
}
