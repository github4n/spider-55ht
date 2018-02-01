package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;

/**
 * 
  * @ClassName: DefaultAmazonParser
  * @Description: 亞馬遜的默認解析器
  * @author songsong.xu
  * @date 2016年10月14日 下午2:56:55
  *
 */
public class DefaultAmazonParser extends AbstractAmazonParser{
	
	
	public Map<String,SkuBean> skuResult;//sku
	public DefaultAmazonParser(Map<String, SkuBean> skuResult) {
		super();
		this.skuResult = skuResult;
	}

	@Override
	public Price price(Context context) {
		String skuId = StringUtils.substringBetween(context.getCurrentUrl(), "/dp/", "/");
		SkuBean skuBean = skuResult.get(skuId);
		if(skuBean != null){
			return new Price(Float.valueOf(skuBean.getOrig()), Integer.valueOf(skuBean.getSave()), Float.valueOf(skuBean.getSale()), skuBean.getUnit());
		}
		return null;
	}

	@Override
	public Stock stock(Context context) {
		String skuId = StringUtils.substringBetween(context.getCurrentUrl(), "/dp/", "/");
		SkuBean skuBean = skuResult.get(skuId);
		if(skuBean != null){
			return new Stock(skuBean.getStockStatus());
		}
		return null;
	}

	@Override
	public Sku sku(Context context) {
		Sku sku = new Sku();
		String content = context.getCurrentHtml();
		String skuData = StringUtils.substringBetween(content, "window.DetailPage", "</script>");
		if(StringUtils.isNotBlank(skuData)){
			String num_variation_dimensions = StringUtils.substringBetween(skuData, "\"num_variation_dimensions\":", ",");
			String dimensions = StringUtils.substringBetween(skuData, "\"dimensions\":", ",\"prioritizeReqPrefetch\"");
			String variation_values = StringUtils.substringBetween(skuData, "\"variation_values\":", ",\"deviceType\"");
			String variationDisplayLabels = StringUtils.substringBetween(skuData, "\"variationDisplayLabels\":", ",\"twisterInitPrefetchMode\"");
			String asin_variation_values = StringUtils.substringBetween(skuData, "\"asin_variation_values\":", ",\"contextMetaData\"");
			String imageData = StringUtils.substringBetween(content, "\"indexToColor\"", "data[\"heroImage\"]");
			String images = StringUtils.EMPTY;
			String visualDimensions = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(imageData)){
				visualDimensions = StringUtils.substringBetween(imageData, "\"visualDimensions\":", ",\"productGroupID\"");
				images = StringUtils.substringBetween(imageData, "data[\"colorImages\"] = ", ";");
			}
			String current_asin = StringUtils.substringBetween(skuData, "\"current_asin\":\"", "\",");
			Type typeList = new TypeToken<List<String>>(){}.getType();
			List<String> dimensionsList = new ArrayList<String>();
			if(StringUtils.isNotBlank(dimensions)){
				dimensionsList = JsonUtils.json2bean(dimensions, typeList);
			}
			List<String> visualDimensionsList = new ArrayList<String>();
			if(StringUtils.isNotBlank(visualDimensions)){
				visualDimensionsList =  JsonUtils.json2bean(visualDimensions, typeList);
			}
			Type typeMap = new TypeToken<Map<String,String>>(){}.getType();
			Type typeMapList = new TypeToken<Map<String,List<String>>>(){}.getType();
			Map<String,List<String>> variation_valuesMapList = new HashMap<String,List<String>>();
			if(StringUtils.isNotBlank(variation_values)){
				variation_valuesMapList = JsonUtils.json2bean(variation_values, typeMapList);
			}
			Map<String,String> variationDisplayLabelsMap = new HashMap<String,String>();
			try{
				if(StringUtils.isNotBlank(variationDisplayLabels)){
					variationDisplayLabelsMap = JsonUtils.json2bean(variationDisplayLabels, typeMap);
				}
			}catch(Throwable e){
				//e.printStackTrace();
				System.out.println("itemUrl:"+context.getUrl().getValue()+",variationDisplayLabels:"+variationDisplayLabels);
				variationDisplayLabels = StringUtils.substringBetween(skuData, "\"variationDisplayLabels\":", ",\"productTypeName\"");
				//,"productTypeName"
				if(StringUtils.isNotBlank(variationDisplayLabels)){
					variationDisplayLabelsMap = JsonUtils.json2bean(variationDisplayLabels, typeMap);
				}
			}
			
			
			Type typeMapMap = new TypeToken<Map<String,Map<String,String>>>(){}.getType();
			Map<String,Map<String,String>> asin_variation_valuesMapMap = new HashMap<String,Map<String,String>>();
			if(StringUtils.isNotBlank(asin_variation_values)){
				asin_variation_valuesMapMap =  JsonUtils.json2bean(asin_variation_values, typeMapMap);
			}
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			if(asin_variation_valuesMapMap != null && asin_variation_valuesMapMap.size() > 0 ){
				for(Map.Entry<String,Map<String,String>> entry : asin_variation_valuesMapMap.entrySet()){
					String skuId = entry.getKey();
					SkuBean skuBean = skuResult.get(skuId);
					if(null ==  skuBean){
						continue;//失敗或者非自營
					}
					Map<String,String> valueMap = entry.getValue();
					//selectlist
					LSelectionList lselectlist = new LSelectionList();
					lselectlist.setGoods_id(skuId);
					lselectlist.setOrig_price(Float.valueOf(skuBean.getOrig()));
					lselectlist.setPrice_unit(skuBean.getUnit());
					lselectlist.setSale_price(Float.valueOf(skuBean.getSale()));
					lselectlist.setStock_number(skuBean.getStockNum());
					lselectlist.setStock_status(skuBean.getStockStatus());
					List<Selection> selections = new ArrayList<Selection>();
					//stylelist
					LStyleList lStyleList = new LStyleList();
					if(skuId.equals(current_asin)){
						lStyleList.setDisplay(true);
					}
					
					//dimensions
					boolean colorFlag = false;
					boolean major = true;
					if(StringUtils.containsIgnoreCase(dimensions, "color")){
						colorFlag = true;
						major = false;
					}
					
					int numDim = Integer.valueOf(num_variation_dimensions);
					for(String dim : dimensionsList){
						//索引
						String index = valueMap.get(dim);
						//select labels
						String label = variationDisplayLabelsMap.get(dim);
						
						//select values
						List<String> variations = variation_valuesMapList.get(dim);
						String value = variations.get(Integer.valueOf(index));
						
						if(numDim > 1){
							if((colorFlag && StringUtils.containsIgnoreCase(dim, "color")) || major){
								//selectlist
								lselectlist.setStyle_id(value);
								//stylelist
								lStyleList.setGood_id(skuId);
								lStyleList.setStyle_switch_img("");
								lStyleList.setStyle_cate_id(0);
								lStyleList.setStyle_id(value);
								lStyleList.setStyle_cate_name(label);
								lStyleList.setStyle_name(value);
								l_style_list.add(lStyleList);
								colorFlag = false;
								major = false;
								continue;
							}
							Selection selection = new Selection();
							selection.setSelect_id(0);
							selection.setSelect_name(label);
							selection.setSelect_value(value);
							selections.add(selection);
						} else {
							//selectlist
							lselectlist.setStyle_id(value);
							//stylelist
							lStyleList.setGood_id(skuId);
							lStyleList.setStyle_switch_img("");
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_id(value);
							lStyleList.setStyle_cate_name(label);
							lStyleList.setStyle_name(value);
							l_style_list.add(lStyleList);
						}
					}
					// picture download
					StringBuilder imagekey = new StringBuilder();
					for(String imageKey : visualDimensionsList){
						String index = valueMap.get(imageKey);
						List<String> variations = variation_valuesMapList.get(imageKey);
						String value = variations.get(Integer.valueOf(index));
						imagekey.append(value).append(" ");
					}
					if(imagekey.length() > 0){
						imagekey.delete(imagekey.length()-1, imagekey.length());
					}
					if(StringUtils.isNotBlank(StringUtils.trim(imagekey.toString()))){
						List<Image> picsPerSku = getPicsByStyleId(images, StringUtils.trim(imagekey.toString()));
						context.getUrl().getImages().put(skuId, picsPerSku);
					}
					lselectlist.setSelections(selections);
					l_selection_list.add(lselectlist);
				}
			}
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
		} else {
			sku.setL_selection_list(new ArrayList<LSelectionList>());
			sku.setL_style_list(new ArrayList<LStyleList>());
			String skuId = StringUtils.substringBetween(context.getCurrentUrl(), "/dp/", "/");
			String imageData = StringUtils.substringBetween(content, "'colorImages':", "</script>");
			String imagesStr = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(imageData)){
				imagesStr = StringUtils.substringBetween(imageData, "'initial': ", "'colorToAsin':");
				if(StringUtils.isNotBlank(imagesStr)){
					imagesStr = StringUtils.substringBeforeLast(imagesStr, ",");
				}
				List<Image> picsPerSku = getPics(skuId,StringUtils.trim(imagesStr));
				context.getUrl().getImages().put(skuId, picsPerSku);
			}
		}
		
		return sku;
	}
	
	public List<Image> getPics(String skuId,String content) {
		List<Image> images = new ArrayList<Image>();
		//Type type = new TypeToken<List<Map<String,String>>>(){}.getType();
		//List<Map<String,String>> arr  = JsonUtils.json2bean(content, type);
		String[] arr = StringUtils.substringsBetween(content, "{", "\"variant\":");
		if(arr != null && arr.length > 0){
			//boolean thumb = true;
			for(String item : arr){
				String imgUrl = StringUtils.substringBetween(item, "\"large\":\"", "\",");
				if(StringUtils.isNotBlank(imgUrl)){
					Image image = new Image(imgUrl);
					images.add(image);
					System.out.println("skuId: "+skuId+",imgUrl: "+imgUrl);
				}
				/*if(thumb){
					image = new Image(StringUtils.substringBetween(item, "\"thumb\":\"", "\","));
					images.add(image);
					thumb = false;
				}*/
			}
		}
		return images;
	}

	public List<Image> getPicsByStyleId(String content, String imagekey) {
		List<Image> images = new ArrayList<Image>();
		JsonObject obj = JsonUtils.json2bean(content, JsonObject.class);
		if(obj != null){
			JsonArray arr = obj.getAsJsonArray(imagekey);
			if(arr != null && arr.size() > 0){
				//boolean thumb = true;
				for(int i =0 ; i < arr.size(); i++){
					JsonObject jsonObject = arr.get(i).getAsJsonObject();
					Image image = new Image(StringUtils.replace(jsonObject.get("large").toString(), "\"", ""));
					images.add(image);
					/*if(thumb){
						image = new Image(StringUtils.replace(jsonObject.get("thumb").toString(), "\"", ""));
						images.add(image);
						thumb = false;
					}*/
				}
			}
		}
		return images;
	}

	public RetBody retboby(Context context){
		RetBody ret = new RetBody();
		ret.setDOCID(docID(context));
		ret.setSite(site(context));
		ret.setProdUrl(prodUrl(context));
		ret.setTitle(title(context));
		ret.setPrice(price(context));
		ret.setStock(stock(context));
		ret.setBrand(brand(context));
		ret.setBreadCrumb(breadCrumb(context));
		ret.setCategory(category(context));
		ret.setImage(image(context));
		ret.setProperties(properties(context));
		ret.setFeatureList(featureList(context));
		ret.setDescription(description(context));
		ret.setSku(sku(context));
		return ret;
	}

}
