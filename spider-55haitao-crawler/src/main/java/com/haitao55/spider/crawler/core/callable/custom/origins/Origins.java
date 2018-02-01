package com.haitao55.spider.crawler.core.callable.custom.origins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

public class Origins extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.origins.com";
	
	@Override
	public void invoke(Context context) throws Exception{
		try {
			String content = super.getInputString(context);
			RetBody retbody = new RetBody();
			if (StringUtils.isNotBlank(content)) {
				String productId = StringUtils.substringBetween(content, "PRODUCT_ID = \"", "\"");
				String docid = SpiderStringUtil.md5Encode(domain+productId);
				String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
				String first_key = "<script>var page_data = ";
				String second_key = "</script>";
				String jsonString  = StringUtils.substringBetween(content,first_key, second_key);
				JSONObject json = JSONObject.parseObject(jsonString);
				JSONObject product_detail = json.getJSONObject("catalog-spp").getJSONArray("products").getJSONObject(0);
				String url = product_detail.getJSONObject("category").getString("SEO_URL");
				int len = url.split("/").length;
                List<String> bread_list = new LinkedList<String>();
                String category_name = product_detail.getJSONObject("category").getString("CATEGORY_NAME");
                for(int i = 4; i <= (len-1); i++){
                	if(url.split("/")[i] != category_name){
                		bread_list.add(url.split("/")[i]);
                	}
                	else if(url.split("/")[i] == category_name){
                		break;
                	}
                	
                }
                String title = product_detail.getString("PROD_RGN_SUBHEADING");
                String description = product_detail.getString("DESCRIPTION");
                String image_url = "http://www.origins.com"+product_detail.getJSONArray("IMAGE_L").getString(0);
                List<Image> image_list = new ArrayList<Image>();
                image_list.add(new Image(image_url));
                List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				Sku sku = new Sku();
				int stock_flag = 1;
				List<String> color_history = new ArrayList<String>();
				String first_color = product_detail.getJSONArray("skus").getJSONObject(0).getString("SHADENAME");
				String default_sku = product_detail.getJSONObject("defaultSku").getString("SKU_BASE_ID");
				for(Object selection :product_detail.getJSONArray("skus")){
					LStyleList lStyleList = new LStyleList();
					JSONObject selection_converted =   (JSONObject)selection;
					String color_name = "OneColor";
					if(first_color != null){
					color_name = selection_converted.getString("SHADENAME");}
					String sku_id = selection_converted.getString("SKU_BASE_ID");
					String size = selection_converted.getString("PRODUCT_SIZE");
					int stock_status = selection_converted.getIntValue("INVENTORY_STATUS");
					if(color_history.indexOf(color_name) == -1){
						lStyleList.setStyle_cate_id(01);
						lStyleList.setStyle_cate_name("color");
						lStyleList.setStyle_id(color_name);
						lStyleList.setStyle_name(color_name);
						lStyleList.setGood_id(sku_id);
						lStyleList.setStyle_switch_img("");
						if(sku_id.equals(default_sku)){
							lStyleList.setDisplay(true);
						}
						context.getUrl().getImages().put(sku_id, image_list);
						l_style_list.add(lStyleList);
						color_history.add(color_name);
						}
						LSelectionList lSelectionList = new LSelectionList();
						lSelectionList.setStyle_id(color_name);
						lSelectionList.setGoods_id(sku_id);
						float origPrice = Float.parseFloat(selection_converted.getString("formattedPrice").replace("$", ""));
						float salePrice = Float.parseFloat(selection_converted.getString("formattedPrice").replace("$", ""));
						lSelectionList.setOrig_price(origPrice);
						lSelectionList.setSale_price(salePrice);
						lSelectionList.setPrice_unit("USD");
						lSelectionList.setStock_status(stock_status);
						lSelectionList.setStock_number(0);
						if(stock_status == 1){
							stock_flag *=0;
						}
						else if (stock_status == 0){
							stock_flag *= 1;	
						}
						Selection selection_size = new Selection();
						selection_size.setSelect_id(0);
						selection_size.setSelect_name("Size");
						if(size != null){
						selection_size.setSelect_value(size);}
						else{
						selection_size.setSelect_value("default");	
						}
				        List<Selection> selections = new LinkedList<Selection>();
				        selections.add(selection_size);
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
				}
				sku.setL_selection_list(l_selection_list);
				sku.setL_style_list(l_style_list);
				if(stock_flag == 0){
                retbody.setStock(new Stock(1, 0));
				}
				else if(stock_flag == 1){
				retbody.setStock(new Stock(0,0));
				}
				float orig_price = Float.parseFloat(product_detail.getJSONArray("skus")
						.getJSONObject(0).getString("formattedPrice").replace("$",""));
				float sale_price = Float.parseFloat(product_detail.getJSONArray("skus").getJSONObject(0)
						.getString("formattedPrice").replace("$",""));
				int save = Math.round((1 - sale_price / orig_price) * 100);
				String unit = "USD";
				retbody.setBrand(new Brand("ORIGINS",""));
				Map<String, Object> descMap = new HashMap<String, Object>();
				Map<String, Object> featureMap = new HashMap<String, Object>();
				featureMap.put("feature-1", description);
				descMap.put("en", description);
				retbody.setFeatureList(featureMap);
				retbody.setDescription(descMap);
				retbody.setTitle(new Title(title,""));
				retbody.setDOCID(docid);
				retbody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
				retbody.setPrice(new Price(orig_price, save, sale_price, unit));
                retbody.setSite(new Site(domain));
                retbody.setSku(sku);
				retbody.setBreadCrumb(bread_list);
				retbody.setCategory(bread_list);
				setOutput(context,retbody);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while crawling url {} ,exception {}", context.getCurrentUrl(),e);
		}
		
	}
	public static void main(String[] args) throws Exception {
		Context cont = new Context();
		cont.setCurrentUrl("http://www.origins.com/product/15370/11482/makeup/face-makeup/foundations/silk-screen/refining-powder-makeup");
		Origins o =new Origins();
		o.invoke(cont);
	}
	
}




