package com.haitao55.spider.crawler.core.callable.custom.sephora;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
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
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * Sephora/丝芙兰
 * @author denghuan
 *
 */
public class Sephora extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.sephora.cn";
	private String SEPHORA_INTERFACE="http://www.sephora.cn/webapp/wcs/stores/servlet/RefreshProductDetailUp?";

	@Override
	public void invoke(Context context) throws Exception {
		try {
			String content = super.getInputString(context);
			RetBody rebody = new RetBody();
			if (StringUtils.isNotBlank(content)) {
				Document document = Jsoup.parse(content);
				String pSkuId = document.select("#mySelCurrentSKUID").attr("value");
				String productId = document.select("#productId").attr("value");
				String brandOrTile = document.select(".popProDet h1").text();
				String salePrice = getSalePrcie(document);
				int  stock = getStock(document);
				String storeId = StringUtils.substringBetween(content, "param.storeId=\"", "\";");
				String styleId = document.select("#defineAttrValue").attr("value");
				String skuAttr = document.select("#defineAttr").attr("value");
				String unit = Currency.codeOf("￥").name();
				String origPirce = "";
				String[] hPrice = handlePrice(salePrice);
				if(hPrice != null){
					origPirce = hPrice[0];
					salePrice = hPrice[1];
				}
				
				Sku sku = new Sku();
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				Elements itemEs = document.select(".proSelectItems ul#dAttr li");
				Map<String,String> skuMap = new HashMap<>();
				Map<String,String> imgMap = new HashMap<>();
				for(Element e : itemEs){
					String sid = e.attr("id");
					String style_id = e.text();
					String src = e.select("img").attr("src");
					if(StringUtils.isNotBlank(src)){
						imgMap.put(sid, src);
					}
					if(StringUtils.isNotBlank(sid)){
						skuMap.put(sid, style_id);
					}
				}
				if(MapUtils.isNotEmpty(skuMap)){
				   Iterator<Entry<String,String>> iterator = skuMap.entrySet().iterator();
				   while (iterator.hasNext()) {
					   Entry<String, String> entry = iterator.next();  
					   String key = entry.getKey();
					   String value = entry.getValue();
					   if(StringUtils.containsIgnoreCase(key, "sku_")){
						  String skuId = key.replaceAll("sku_", "");
						  if(skuId.equals(pSkuId)){
							  String attrUnit = "";//sku单位，比如 ml g等单位属性 
							  if(StringUtils.containsIgnoreCase(value, " ")){
								  attrUnit = value.substring(value.indexOf(" "));
							  }
							  LSelectionList lSelectionList = wrapLSelectionList(styleId+attrUnit,skuId,salePrice,origPirce,unit,stock);
							  l_selection_list.add(lSelectionList);
							  LStyleList lStyle = wrapLStyleList(true,skuId,styleId+attrUnit,skuAttr,imgMap.get(key));
							  //List<Picture> styeImgList = getImgList(document);
							 // lStyle.setStyle_images(styeImgList);
							  List<Image> imgList = getImgList(document);
							  context.getUrl().getImages().put(skuId + "", imgList);// picture
							  l_style_list.add(lStyle);
						  }else{
							  String parm = "storeId="+storeId+"&skuId="+skuId+"&proId="+productId;
							  String html = HttpUtils.get(SEPHORA_INTERFACE+parm);
							  Document doc = Jsoup.parse(html);
							  if(doc != null){
								  String skuOrigPirce = "";
								  String skuSalePrice = getSalePrcie(doc);
								  int skuStock = getStock(doc);
								  String[] skuPrice = handlePrice(skuSalePrice);
									if(hPrice != null){
										skuOrigPirce = skuPrice[0];
										skuSalePrice = skuPrice[1];
									}
								   if(StringUtils.isBlank(value)){
									   value = getSkuAttrValue(doc);
								   }
								  LSelectionList lSelectionList = wrapLSelectionList(value,skuId,skuSalePrice,skuOrigPirce,unit,skuStock);
								  l_selection_list.add(lSelectionList);
								  LStyleList lStyle = wrapLStyleList(false,skuId,value,skuAttr,imgMap.get(key));
								 // List<Picture> styeImgList = getImgList(doc);
								  List<Image> imgList = getImgList(document);
								  context.getUrl().getImages().put(skuId + "", imgList);// picture
								  //lStyle.setStyle_images(styeImgList);
								  l_style_list.add(lStyle);
							  }
						  }
					   }
				   }
				}
				sku.setL_selection_list(l_selection_list);
				sku.setL_style_list(l_style_list);
				
				String docid = SpiderStringUtil.md5Encode(domain + productId);
				String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
				rebody.setDOCID(docid);
				rebody.setSite(new Site(domain));
				rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
				
				if(StringUtils.isNotBlank(brandOrTile)){
					if(StringUtils.containsIgnoreCase(brandOrTile, " ")){
						String[] sp = brandOrTile.split(" ");
						String brand = sp[0];
						String title = sp[1];
						rebody.setTitle(new Title(title, ""));
						rebody.setBrand(new Brand(brand, ""));
					}
				}
				
				if(StringUtils.isBlank(origPirce)){
					rebody.setPrice(new Price(Float.parseFloat(salePrice), 0, Float.parseFloat(salePrice), unit));
				}else{
					int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPirce)) * 100);// discount
					rebody.setPrice(new Price(Float.parseFloat(origPirce), save, Float.parseFloat(salePrice), unit));
				}
				rebody.setStock(new Stock(stock));
				
				List<String> cats = new ArrayList<String>();
				List<String> breads = new ArrayList<String>();
				Elements  es = document.select("#widget_breadcrumb ul a");
				for(int i = 1;i < es.size()-1;i++){
					String cat = es.get(i).text();
					if(StringUtils.isNotBlank(cat)){
						cats.add(cat);
						breads.add(cat);
					}
				}
				rebody.setCategory(cats);
				rebody.setBreadCrumb(breads);
				
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				String ds = document.select(".proInfoTxt").text();
				StringBuilder sb = new StringBuilder();
				if(StringUtils.isNotBlank(ds)){
					int count = 1;
					featureMap.put("feature-" + count, ds);
					sb.append(ds);
				}
				rebody.setFeatureList(featureMap);
				descMap.put("en", sb.toString());
				rebody.setDescription(descMap);
	 			
				List<Image> imgList = getImgList(document);
				//rebody.setImage(new LImageList(imgList));
				context.getUrl().getImages().put(pSkuId + "", imgList);// picture
				
				
				Map<String, Object> propMap = new HashMap<String, Object>();
				es = document.select("#skuInfo li p");
				if (es != null && es.size() > 0) {
					for (Element e : es) {
						String key = StringUtils.trim(StringUtils.substringBefore(e.text(), "："));
						String value = StringUtils.trim(StringUtils.substringAfter(e.text(), "："));
						if(StringUtils.containsIgnoreCase(key, "适用人群")){
							propMap.put("s_gender", value);
						}
						if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
							propMap.put(key, value);
						}
					}
				}
				rebody.setProperties(propMap);
				rebody.setSku(sku);
			}
			setOutput(context, rebody);
			//System.out.println(rebody.parseTo());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while crawling url {} ,exception {}", context.getCurrentUrl(),e);
		}
	}
	
	public String[] handlePrice(String price){
		String[] sprice = null;
		if(StringUtils.isNotBlank(price)){
			if(StringUtils.containsIgnoreCase(price, "¥")){
				price = price.replace("¥", "");
				if(StringUtils.containsIgnoreCase(price, " ")){
					sprice = price.split(" ");
				}
			}
		}
		return sprice;
	}
	
	
	private List<Image> getImgList(Document document){
		List<Image> picList = new ArrayList<Image>();
		Elements es = document.select(".skuImgItems ul li img");//图片
		if(es != null && es.size() > 0){
			for(Element e : es){
				String img = e.attr("data-super");
				picList.add(new Image(img));
			}
		}
		return picList;
	}
	
	private String getSkuAttrValue(Document document){
		String pro = document.select(".promotion-container p.proItem").text();
		if(StringUtils.isNotBlank(pro)){
			String skuAttrValue = StringUtils.substringBetween(pro, "货号：", "<");
			return skuAttrValue;
		}
		return null;
	}
	
	
	private String getSalePrcie(Document document){
		String salePrice = document.select("p.proPrice span").text();
		if(StringUtils.isNotBlank(salePrice)){
			return salePrice;
		}
		return null;
	}
	
	private int getStock(Document document){
		 String stock = document.select(".proSelect p.proTip").text();
		 int stockStatus = 0;
		 if(StringUtils.containsIgnoreCase(stock, "暂时无货")){
			 stockStatus = 0;
		 }else if(!StringUtils.containsIgnoreCase(stock, "暂时无货")){
			 stockStatus = 1;
		 }else{
			 stockStatus = 1;
		 }
		 return stockStatus;
	}
	
	private LSelectionList wrapLSelectionList(String styleId,String skuId,String salePrice
			,String origPirce,String unit,int stock){
		  LSelectionList lSelectionList = new LSelectionList();
		  lSelectionList.setStyle_id(styleId);
		  lSelectionList.setGoods_id(skuId);
		  if(StringUtils.isNotBlank(origPirce)){
			  lSelectionList.setOrig_price(Float.parseFloat(origPirce));
		  }else{
			  lSelectionList.setOrig_price(Float.parseFloat(salePrice));
		  }
		  lSelectionList.setSale_price(Float.parseFloat(salePrice));
		  lSelectionList.setPrice_unit(unit);
		  lSelectionList.setStock_status(stock);
		return lSelectionList;
	}
	private LStyleList wrapLStyleList(boolean display,String skuId,String styleId,String skuAttr
			,String styleImg){
		  LStyleList lStyleList = new LStyleList();
		  lStyleList.setStyle_switch_img(styleImg);
		  lStyleList.setDisplay(display);
		  lStyleList.setGood_id(skuId);
		  lStyleList.setStyle_cate_id(0);
		  lStyleList.setStyle_id(styleId);
		  lStyleList.setStyle_name(styleId);
		  lStyleList.setStyle_cate_name(skuAttr);
		return lStyleList;
	}
	
	public static void main(String[] args) {
		  String str ="ab cml";
		  if(StringUtils.containsIgnoreCase(str, " ")){
			  System.out.println("====="+ str.substring(str.indexOf(" ")));
		  }else{
			  System.out.println("没有空格");
		  }
		 
//		  String html = HttpUtils.get("http://www.sephora.cn/webapp/wcs/stores/servlet/RefreshProductDetailUp?storeId=10001&skuId=12000022&proId=153501");	
//	       System.out.println(html);
	}
	
}
