package com.haitao55.spider.crawler.core.callable.custom.sierratradingpost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;

/**
 * SierratradingPost收录
 * date:2017.2.24
 * @author denghuan
 *
 */
public class SierratradingPost extends AbstractSelect{

	private static final String domain = "www.sierratradingpost.com";
	private static final String IMAGE_SUFFIX = "~1500.2.jpg";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String brand = doc.select(".productDetails-title h1 a").text();
			String title = doc.select(".productDetails-title h1").text();
			String productId = StringUtils.substringBetween(content, "prodid: '", "'");
			String unit = StringUtils.substringBetween(content, "priceCurrency\">", "<");
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			String defalutImage = StringUtils.substringBetween(content, "image\" content=\"", "\"");
			String imagePrefix = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(defalutImage)){
				imagePrefix = defalutImage.substring(0, defalutImage.indexOf("_")+1);
			}
			
			String origPrice = StringUtils.EMPTY;
			String ourPrice = doc.select("span.ourPrice").text();
			if(StringUtils.isBlank(ourPrice)){
				String retailPrice = doc.select("span.retailPrice").text();
				if(StringUtils.isNotBlank(retailPrice)){
					origPrice = patternPrice(retailPrice);
				}
			}else{
				origPrice = patternPrice(ourPrice);
			}
			
		
			List<Image> imageList = new ArrayList<>();
			Elements imageEs = doc.select("a.altImage");
			for(Element es : imageEs){
				String image = es.attr("href");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image));
				}
			}
			
			String sizeKey = doc.select("#property2Label").attr("title");
			String widthKey = doc.select("#property3Label").attr("title");
			String skus = StringUtils.substringBetween(content, "var skus = new TAFFY(", ");");
			
			Map<String,String> colorMap = new HashMap<String,String>();
			Elements es = doc.select("#selectedProperty1 option");
			String defalutColor = StringUtils.EMPTY;
			for(int i = 0; i < es.size(); i++){
				if(i == 1){
					defalutColor = es.get(i).text();
				}
				String key = es.get(i).attr("value");
				String value = es.get(i).text();
				if(StringUtils.isNotBlank(key)){
					colorMap.put(key, value);
				}
			}
			if(StringUtils.isBlank(defalutColor)){
				defalutColor = doc.select("#selectedLocation").attr("value");
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Map<String,String> styleMap = new HashMap<>();
			if(StringUtils.isNotBlank(skus)){
				List<SierratradingPostVO> skuList = pattern(skus);
				if(CollectionUtils.isNotEmpty(skuList)){
					for(SierratradingPostVO postVo : skuList){
						LSelectionList lSelectionList = new LSelectionList();
						String color = postVo.getColor();
						String sizeVal = postVo.getSize();
						String width = postVo.getWdith();
						String salePrice = postVo.getPrice();
						String colorVal = StringUtils.EMPTY;
						if(StringUtils.isNotBlank(color)){
							colorVal = colorMap.get(color);
						}
						lSelectionList.setGoods_id(colorVal+sizeVal);
						lSelectionList.setPrice_unit(unit);
						lSelectionList.setSale_price(Float.parseFloat(salePrice));
						if(StringUtils.isBlank(origPrice)){
							origPrice = salePrice;
						}
						lSelectionList.setOrig_price(Float.parseFloat(origPrice));
						lSelectionList.setStock_status(1);
						lSelectionList.setStyle_id(colorVal);
						List<Selection> selections = new ArrayList<>();
						if(StringUtils.isNotBlank(sizeVal)){
							Selection selection = new Selection();
							selection.setSelect_name("size");
							selection.setSelect_value(sizeVal);
							selections.add(selection);
						}
						if(StringUtils.isNotBlank(width)){
							Selection selection = new Selection();
							selection.setSelect_name("width");
							selection.setSelect_value(width);
							selections.add(selection);
						}
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
						
						if(!styleMap.containsKey(colorVal)){
							List<Image> skuImageList = new ArrayList<>();
							LStyleList lStyleList = new LStyleList();
							if(StringUtils.containsIgnoreCase(colorVal, defalutColor)){
								lStyleList.setDisplay(true);
								if(StringUtils.isNotBlank(origPrice) && 
										StringUtils.isNotBlank(salePrice)){
									 int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
									 rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
								}
								if(CollectionUtils.isEmpty(imageList)){
									String image = imagePrefix+color+IMAGE_SUFFIX;
									skuImageList.add(new Image(image));
									context.getUrl().getImages().put(colorVal+sizeVal, skuImageList);// picture
								}else{
									context.getUrl().getImages().put(colorVal+sizeVal, imageList);// picture
								}
							}else{
								if(StringUtils.isNotBlank(imagePrefix)){
									String image = imagePrefix+color+IMAGE_SUFFIX;
									skuImageList.add(new Image(image));
									skuImageList.addAll(imageList);
									context.getUrl().getImages().put(colorVal+sizeVal, skuImageList);
								}
							}
							lStyleList.setGood_id(colorVal+sizeVal);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("color");
							lStyleList.setStyle_id(colorVal);
							lStyleList.setStyle_name(colorVal);
							lStyleList.setStyle_switch_img("");
							styleMap.put(colorVal, colorVal);
							l_style_list.add(lStyleList);
						}
					}
				}
			}
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			
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
				rebody.setStock(new Stock(spuStock));
			}
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String categorys = StringUtils.substringBetween(content, "content_category: '", "',");
			if(StringUtils.isNotBlank(categorys)){
				if(StringUtils.containsIgnoreCase(categorys, ">")){
					String[] cates = categorys.split(">");
					for(String cate : cates){
						cats.add(cate);
						breads.add(cate);
					}
				}else{
					cats.add(categorys);
					breads.add(categorys);
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			
			String description = doc.select("#overviewSection").text();
			
			Elements featureEs = doc.select("#specsSection ul.list li");
			int count = 0;
			if(featureEs != null &&  featureEs.size() > 0){
				for(Element e : featureEs){
					String text = e.text();
					if(StringUtils.isNotBlank(text)){
						 count ++;
						 featureMap.put("feature-"+count, text);
					}
				}
			}
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			String gender = StringUtils.EMPTY;
			if(StringUtils.containsIgnoreCase(title, "women")){
				gender = "women";
			}else if(StringUtils.containsIgnoreCase(title, "men")){
				gender = "men";
			}
			propMap.put("s_gender", gender);
			rebody.setProperties(propMap);
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}
	
	private  List<SierratradingPostVO>  pattern(String skus){
		List<SierratradingPostVO> list = new ArrayList<>();
		Pattern pattern = Pattern.compile("property1: \"(.*?)\", property2: \"(.*?)\", property3: \"(.*?)\", price: \"(.*?)\"");
		Matcher matcher = pattern.matcher(skus);
		while(matcher.find()){
			SierratradingPostVO sierratVo = new SierratradingPostVO();
			String color = matcher.group(1);
			String size = matcher.group(2);
			String width = matcher.group(3);
			String price = matcher.group(4);
			if(StringUtils.isNotBlank(color)){
				sierratVo.setColor(color);
			}
			if(StringUtils.isNotBlank(size)){
				sierratVo.setSize(size);
			}
			if(StringUtils.isNotBlank(width)){
				String wt = width.substring(0, width.indexOf("\""));
				sierratVo.setWdith(wt);
			}
			if(StringUtils.isNotBlank(price)){
				sierratVo.setPrice(price);
			}
			list.add(sierratVo);
		}
		return list;
	}
	
	private String patternPrice(String proPrice){
		Pattern pattern = Pattern.compile("(\\d+.?\\d+)");
		Matcher matcher = pattern.matcher(proPrice);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	public static void main(String[] args) {
		String st = "{ property1: \"01\", property2: \"\", property3: \"\", in stock: \"true\",finalPromoPrice: \"89.99\", price: \"89.99\"}";
		//pattern(st);
	}
}
