package com.haitao55.spider.crawler.core.callable.custom.levis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class Levis_bak extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.levi.com";
	@Override
	public void invoke(Context context) throws Exception {
		try{
			String content = super.getInputString(context);
			RetBody retbody = new RetBody();
			if (StringUtils.isNotBlank(content)) {
				Document document = Jsoup.parse(content);	
				String pid = document.select("meta[itemprop=model]").attr("content");
				String docid = SpiderStringUtil.md5Encode(domain+pid);
				String url_no = SpiderStringUtil.md5Encode(domain+pid);
				String title = document.select("h1[class=title]").text();
				String brandString = "Levis";
				String description = document.select("div[class=pdp-description]").select("p").text();
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				String first_key = "var buyStackJSON = '";
				String last_key  = "';";
				String jsonString  = StringUtils.substringBetween(content,first_key, last_key);
				JSONObject json = JSONObject.parseObject(jsonString.replace("\\'", "\\\\'").replace("\\\"", "\\\\\""));
				JSONObject style_list = json.getJSONObject("colorid");
				for(Entry<String, Object> e: style_list.entrySet()){
					LStyleList lStyleList = new LStyleList();
					JSONObject Style_Map = (JSONObject)e.getValue();
					lStyleList.setStyle_cate_id(01);
					lStyleList.setStyle_cate_name("color");
					lStyleList.setStyle_id(Style_Map.getJSONObject("finish").getString("title"));
					lStyleList.setStyle_name(Style_Map.getJSONObject("finish").getString("title"));
					lStyleList.setStyle_switch_img("");
					lStyleList.setGood_id(Style_Map.getString("colorid"));
					if(pid.equals(Style_Map.getString("colorid"))){
					lStyleList.setDisplay(true);
					}
					List<Image> img_list = new ArrayList<Image>();
					img_list.add((new Image(Style_Map.getString("imageURL")+"front-pdp.jpg")));
					context.getUrl().getImages().put(lStyleList.getGood_id(),img_list);
					l_style_list.add(lStyleList);	
				}
				JSONObject selection_list = json.getJSONObject("sku");
				int flag = 1;
				for(Entry<String, Object> e:selection_list.entrySet()){
					LSelectionList lSelectionList = new LSelectionList();
					JSONObject Selectioin_Map = (JSONObject)e.getValue();
					lSelectionList.setGoods_id(Selectioin_Map.getString("skuid"));
					if(!Selectioin_Map.getString("colorid").isEmpty()){
					lSelectionList.setStyle_id(style_list.getJSONObject(Selectioin_Map.getString("colorid")).getJSONObject("finish").getString("title"));
					}
					else {
						lSelectionList.setStyle_id("OneColor");
					}
					lSelectionList.setPrice_unit("USD");
					if(Selectioin_Map.getJSONArray("price").size()>1){
						lSelectionList.setOrig_price(Float.valueOf((Selectioin_Map.getJSONArray("price").getJSONObject(0).getString("amount").replace("$", ""))));
						lSelectionList.setSale_price(Float.valueOf((Selectioin_Map.getJSONArray("price").getJSONObject(1).getString("amount").replace("$", ""))));
					}
					else if(Selectioin_Map.getJSONArray("price").size() ==1){
						lSelectionList.setOrig_price(Float.valueOf((Selectioin_Map.getJSONArray("price").getJSONObject(0).getString("amount").replace("$", ""))));
						lSelectionList.setSale_price(Float.valueOf((Selectioin_Map.getJSONArray("price").getJSONObject(0).getString("amount").replace("$", ""))));
					}
					if(Selectioin_Map.getInteger("stock") >0){
					lSelectionList.setStock_status(1);
					flag *=0;
					}
					else{
					lSelectionList.setStock_status(0);
					flag *=1;
					}
					lSelectionList.setStock_number(0);
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("size");
					if(Selectioin_Map.getString("size") != null){
					selection.setSelect_value(Selectioin_Map.getString("size"));
					}
					else{
						selection.setSelect_value("OneSize");
					}
					 List<Selection> selections = new LinkedList<Selection>();
					 selections.add(selection);
					 lSelectionList.setSelections(selections);
					 l_selection_list.add(lSelectionList);
				}
				float sale_price = l_selection_list.get(0).getSale_price();
				float orig_price = l_selection_list.get(0).getOrig_price();
				int save = Math.round((1 - sale_price/orig_price) * 100);
				if(flag == 0){
					retbody.setStock(new Stock(1,0));
				}
				else{
					retbody.setStock(new Stock(0,0));
				}
				retbody.setBrand(new Brand(brandString, ""));
				ArrayList<String> noArrayList = new ArrayList<String>();
				noArrayList.add("No");
				retbody.setBreadCrumb(noArrayList);
				retbody.setCategory(noArrayList);
				HashMap<String, Object> desc_map = new HashMap<String, Object>();
				desc_map.put("en", description);
				retbody.setDescription(desc_map);
				retbody.setDOCID(docid);
				String unit = "USD";
				retbody.setPrice(new Price(orig_price, save, sale_price, unit));
				Sku sku_object = new Sku();
				sku_object.setL_selection_list(l_selection_list);
				sku_object.setL_style_list(l_style_list);
				retbody.setSite(new Site(domain));
				retbody.setTitle(new Title(title, ""));
				retbody.setSku(sku_object);
				retbody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
				setOutput(context,retbody);			
			}
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while crawling url {} ,exception {}", context.getCurrentUrl(),e);
		}
	}

}
