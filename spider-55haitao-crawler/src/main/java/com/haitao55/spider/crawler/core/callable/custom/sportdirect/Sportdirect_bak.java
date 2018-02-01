/**
 * 
 */
package com.haitao55.spider.crawler.core.callable.custom.sportdirect;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.mashape.unirest.http.Unirest;

/**
 * @author YY
 * @date2017年3月20日下午6:20:46x
 * @Description 
 */
public class Sportdirect_bak extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.sportsdirect.com";
	
	@Override
	public void invoke(Context context) throws Exception{
		try{
//			String content = super.getInputString(context);
			String content = Unirest.get(context.getCurrentUrl()).asString().getBody();
			RetBody retbody = new RetBody();
			if (StringUtils.isNotBlank(content)) {
				String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
				String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
				Document document = Jsoup.parse(content);
				//BS Data
				Elements main_body = document.select("section[class=mainBody clearfix]");
				Elements basic_body = main_body.select("div[id=mainDetails]");
				Elements image_info = basic_body.select("div[id=productImages]");
				Elements main_info = basic_body.select("div[id=productDetails]");
				Elements desc_info = basic_body.select("div[class=ProdDetRight col-xs-12 col-sm-6 col-md-4]");
				//Common Json Data
				String common_first_key = "dataLayer.push(";
				String common_last_key = ");";
				String common_json_string = StringUtils.substringBetween(content,common_first_key, common_last_key);
				JSONObject common_json = JSONObject.parseObject(common_json_string);
				//Size Json Data
				String size_first_key = "data-variants=\"";
				String size_last_key = "\">";
				String size_json_string = StringUtils.substringBetween(content,size_first_key, size_last_key).replace("&quot;", "'");
				HashMap<String, HashMap<String, JSONObject>> size_price_dict = new HashMap<String, HashMap<String,JSONObject>>();
				JSONArray size_json = JSONObject.parseArray(size_json_string);
				for(Object item:size_json){
					JSONObject item_converted = (JSONObject)item;
					JSONArray size_variants = item_converted.getJSONArray("SizeVariants");
					HashMap<String, JSONObject> size_price_info = new HashMap<String, JSONObject>();
					for(Object size_price:size_variants){
						JSONObject size_price_converted = (JSONObject)size_price;
						JSONObject prodsizeprices = size_price_converted.getJSONObject("ProdSizePrices");
						String sizename = size_price_converted.getString("SizeName");
						size_price_info.put(sizename, prodsizeprices);
					}
					String colvarid = item_converted.getString("ColVarId");
					size_price_dict.put(colvarid, size_price_info);
				}				
				String sku_id =common_json.getString("productId");
				String brand = common_json.getString("productBrand");
				String title = main_info.select("span[id=ProductName]").text();
				String breadcrumb = main_body.select("span[id=dnn_dnnBreadcrumb_siteMap]").text();
				List<String> bread_list = Arrays.asList(breadcrumb.split("/"));
				Elements img_info = image_info.select("ul[id=piThumbList]");
				List<Image> img_list = new ArrayList<Image>();
				if(!img_info.isEmpty()){
					for(Element e:img_info.select("li")){
						img_list.add(new Image(e.select("a").attr("srczoom")));
					}					
				}
				String description = desc_info.select("span[itemprop]").text().split(">")[0];
				Elements price_info = main_info.select("div .pdpPrice");
				float price = 0,origin_price = 0;
				String unit = "";
				if(!price_info.isEmpty()){
					if(price_info.text().indexOf("£")!= -1){
						unit = "GBP";
					}
					else {
						unit = "USD";
					}
					price = Float.parseFloat(price_info.text().split("£")[1]);
					Elements origin_price_info = main_info.select("div[class=originalprice]");
					if(!origin_price_info.isEmpty()){
					    origin_price = Float.parseFloat(origin_price_info.text().split("£")[1]);
					}
					else{
						origin_price = price;
					}
					
				}
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				Sku sku = new Sku();
				int stock_flag = 1;
				String style_id = "";
				Elements selection_info = main_info.select("div[class=col-xs-12 no-padding-all productVariantContainer]");
				if(!selection_info.isEmpty()){
					HashMap<String, JSONObject> _size_price_dict = new HashMap<String, JSONObject>();
					Elements selected_var = selection_info.select("option[selected]");
					if(!selected_var.isEmpty()){
						String selected_colvar = selected_var.attr("value");
						_size_price_dict = size_price_dict.get(selected_colvar);
					}
				    style_id = selection_info.select("div[class=s-productextras-column-2-3]").select("span").text();
					Elements size_info = selection_info.select("div[id=productVariantAndPrice]");
					for(Element size: size_info.select("ul[data-clicktoselecttext=Click to select]").select("li")){
						LSelectionList lSelectionList = new LSelectionList();
						lSelectionList.setStyle_id(style_id);
						lSelectionList.setGoods_id(sku_id);
						if(_size_price_dict.containsKey(size.text())){
						lSelectionList.setOrig_price(origin_price);
						lSelectionList.setSale_price(price);}
						lSelectionList.setPrice_unit(unit);
						if(size.attr("class").indexOf("greyOut") != -1){
							lSelectionList.setStock_status(0);
							lSelectionList.setStock_number(0);
							stock_flag *= 1;
						}
						else{
							lSelectionList.setStock_status(1);
							lSelectionList.setStock_number(0);
							stock_flag *= 0;
						}
						Selection selection_size = new Selection();
						selection_size.setSelect_id(0);
						selection_size.setSelect_name("Size");
						if(size.hasText()){
							selection_size.setSelect_value(size.text());
						}
						else{
							selection_size.setSelect_value("");	
						}
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
				if(!selection_info.isEmpty()){
					LStyleList lStyleList = new LStyleList();
					lStyleList.setStyle_cate_id(01);
					lStyleList.setStyle_cate_name("color");
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_name(style_id);
					lStyleList.setGood_id(sku_id);
					lStyleList.setStyle_switch_img("");
					lStyleList.setDisplay(true);
//					context.getUrl().getImages().put(sku_id, img_list);
					l_style_list.add(lStyleList);
				}
				int save = Math.round((1 - price / origin_price) * 100);
				retbody.setBrand(new Brand(brand, ""));
				Map<String, Object> descMap = new HashMap<String, Object>();
				descMap.put("en", description);
				retbody.setDescription(descMap);
				retbody.setTitle(new Title(title,""));
				retbody.setDOCID(docid);
				retbody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
				retbody.setPrice(new Price(origin_price, save, price, unit));
			    retbody.setSite(new Site(domain));
			    sku.setL_selection_list(l_selection_list);
				sku.setL_style_list(l_style_list);
	            retbody.setSku(sku);
	            retbody.setBreadCrumb(bread_list);
				retbody.setCategory(bread_list);
				System.out.println(retbody.parseTo());
//				setOutput(context,retbody);
			}
			
			
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("Error while crawling url {} ,exception {}", context.getCurrentUrl(),e);
			
		}
		
		
	}
}
