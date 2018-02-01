package org.spider.haitao55.realtime.service.crawler;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.ws.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.custom.amazon.api.AWSKey;
import com.haitao55.spider.crawler.utils.JsoupUtils;

import am.ik.aws.apa.AwsApaRequester;
import am.ik.aws.apa.AwsApaRequesterImpl;
import am.ik.aws.apa.jaxws.BrowseNode;
import am.ik.aws.apa.jaxws.BrowseNode.Ancestors;
import am.ik.aws.apa.jaxws.BrowseNodes;
import am.ik.aws.apa.jaxws.EditorialReview;
import am.ik.aws.apa.jaxws.EditorialReviews;
import am.ik.aws.apa.jaxws.ImageSet;
import am.ik.aws.apa.jaxws.Item;
import am.ik.aws.apa.jaxws.Item.ImageSets;
import am.ik.aws.apa.jaxws.Item.VariationAttributes;
import am.ik.aws.apa.jaxws.ItemAttributes;
import am.ik.aws.apa.jaxws.ItemLookupRequest;
import am.ik.aws.apa.jaxws.ItemLookupResponse;
import am.ik.aws.apa.jaxws.Items;
import am.ik.aws.apa.jaxws.Offer;
import am.ik.aws.apa.jaxws.Offers;
import am.ik.aws.apa.jaxws.Price;
import am.ik.aws.apa.jaxws.VariationAttribute;
import am.ik.aws.apa.jaxws.VariationDimensions;
import am.ik.aws.apa.jaxws.Variations;

public class AmazonHtmlApiCrawler extends AmazonCrawler{
	
	private final static Logger logger = LoggerFactory.getLogger(AmazonHtmlApiCrawler.class);
	
	private static final String ENDPOINT = "https://ecs.amazonaws.com";
	private static final String IDTYPE = "ASIN";
	private static AmazonAWSKeyPool awsKeyPool;
	//private static final String defaultKeyId = "AKIAI77X7X5JVEZ52ZCA";
	//private static final String defaultSecretKey = "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";
	//private static final String defaultAssociateTag = "55haitao";
	private final String ITEM_URL_SUFFIX = "?th=1&psc=1";
	private GenericObjectPool pool;
	
	static {
		try {
			awsKeyPool = new AmazonAWSKeyPool();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public AmazonHtmlApiCrawler(GenericObjectPool pool){
		this.pool = pool;
	}
	
  public String crawleByBrowser(String url) {
    long start = System.currentTimeMillis();
    ChromeDriver dr = null;
    String title = StringUtils.EMPTY;
    String content = StringUtils.EMPTY;
    try {
      dr = (ChromeDriver) pool.borrowObject();
      dr.get(url + ITEM_URL_SUFFIX);
      dr.manage().addCookie(new Cookie("test", "test", ".amazon.com", "/", new Date()));
      waitForComplete(dr);
      title = dr.getTitle();
      content = dr.getPageSource();
    } catch (Throwable e) {
      e.printStackTrace();
      dr = getDriver();
    } finally {
      try {
        pool.returnObject(dr);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    long end = System.currentTimeMillis();
    logger.info("url {} from amazon.com by browers,title {} , consume time : {}",url,title,(end - start));
    return content;
  }
	
	public String nordstromByBrowser(String url){
		long start = System.currentTimeMillis();
		ChromeDriver dr = null;
		String title = StringUtils.EMPTY;
		String content = StringUtils.EMPTY;
		try{
			dr = (ChromeDriver) pool.borrowObject();
			String domain = SpiderStringUtil.getAmazonDomain(url);
			dr.get(url);
			dr.manage().addCookie(new Cookie("test", "test", domain, "/", new Date()));
			waitForComplete(dr);
			title = dr.getTitle();
			content = dr.getPageSource();
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
              try {
                pool.returnObject(dr);
              } catch (Exception e) {
                e.printStackTrace();
              }
		}
		long end = System.currentTimeMillis();
		logger.info(" url {} from amazon.com by browers,title {} , consume time : {}", url, title,(end-start));
		return content;
	}
	
	public RetBody getResult(String url){
		String html = crawleByBrowser(url);
		RetBody retbody = parse(url,html);
		return check(retbody);
	}
	
	public RetBody parse(String url,String content){
		RetBody ret = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			boolean hasSku = true;
			String skuData = StringUtils.substringBetween(content, "twister-js-init-dpx-data", "</script>");
			String parent_asin = StringUtils.EMPTY;
			String current_asin = StringUtils.EMPTY;
			List<String> skuIds = new ArrayList<String>();
			if(StringUtils.isNotBlank(skuData)){
				parent_asin = StringUtils.substringBetween(content, "\"parent_asin\":\"", "\",");
				current_asin = StringUtils.substringBetween(content, "\"current_asin\":\"", "\",");
				String asin_variation_values = StringUtils.substringBeforeLast(StringUtils.substringBetween(skuData, "\"asinVariationValues\" : ", "\"dimensionValuesData\""), ",");
				
				Type typeMapMap = new TypeToken<Map<String,Map<String,String>>>(){}.getType();
				Map<String,Map<String,String>> asin_variation_valuesMapMap = new HashMap<String,Map<String,String>>();
				if(StringUtils.isNotBlank(asin_variation_values)){
					asin_variation_valuesMapMap =  JsonUtils.json2bean(asin_variation_values, typeMapMap);
				}
				if(asin_variation_valuesMapMap != null && asin_variation_valuesMapMap.size() > 0 ){
					for(Map.Entry<String,Map<String,String>> entry : asin_variation_valuesMapMap.entrySet()){
						String skuId = entry.getKey();
						skuIds.add(skuId);
					}
				}
				
			} else {//no sku
				
				hasSku = false;
				parent_asin = StringUtils.substringBetween(content, "parentASIN=", "&amp;");
				if(StringUtils.isNotBlank(parent_asin)){
					current_asin = parent_asin;
					skuIds.add(parent_asin);
				} else {
					parent_asin = StringUtils.substringBetween(url, "/dp/", "/");
					current_asin = parent_asin;
					skuIds.add(parent_asin);
				}
				//assemble orignal images
				String imageData = StringUtils.substringBetween(content, "'colorImages':", "</script>");
				if(StringUtils.isNotBlank(imageData)){
					String imagesStr = StringUtils.substringBetween(imageData, "'initial': ", "'colorToAsin':");
					if(StringUtils.isNotBlank(imagesStr)){
						imagesStr = StringUtils.substringBeforeLast(imagesStr, ",");
						List<Picture> images = new ArrayList<Picture>();
						String[] arr = StringUtils.substringsBetween(imagesStr, "\"large\":\"", "\",\"");
						if(arr != null && arr.length > 0){
							for(String item : arr){
								if(StringUtils.isBlank(item)){
									continue;
								}
								Picture image = new Picture(item);
								images.add(image);
							}
						}
						LImageList lImageList = new LImageList(images);
						ret.setImage(lImageList);
					}
				}
			}
			
			//parse from html content
			Document document = JsoupUtils.parse(content);
			AmazonParser parser = new AmazonParser(url,document);
			ret.setDOCID(parser.docID());
			ret.setSite(parser.site());
			ret.setProdUrl(parser.prodUrl());
			ret.setProperties(parser.properties());
			if(!hasSku){
				ret.setPrice(parser.price());
				ret.setStock(parser.stock());
				Sku sku = new Sku();
				sku.setL_selection_list(new ArrayList<LSelectionList>() );
				sku.setL_style_list(new ArrayList<LStyleList>());
				ret.setSku(sku);
			}
			//send api request
			apiService(ret, url, parent_asin, current_asin, skuIds);
			
		}
		return ret;
	}
	
	
	
	
	private void apiService(RetBody ret, String url, String parent_asin,String current_asin,List<String> skuIds) {
		Sku sku = new Sku();
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		if(StringUtils.isNotBlank(parent_asin)){
			for(int i =0 ; i < 2; i++){
				AWSKey key = null;
				try{
					key = awsKeyPool.pollKey();
					//key = new AWSKey(defaultKeyId,defaultSecretKey,defaultAssociateTag);
					AwsApaRequester requester = new AwsApaRequesterImpl(ENDPOINT,key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag());
					ItemLookupRequest lookup = new ItemLookupRequest();
					lookup.setIdType(IDTYPE);
					lookup.getItemId().add(parent_asin);//B01LZYTSFP
					lookup.setMerchantId("Amazon");
					addReturnKeys(lookup);
					logger.info("staturl {} AccessKeyId {},callTime {}",url,key.getAccessKeyId(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					Response<ItemLookupResponse> itemLookupResponse = requester.itemLookupAsync(lookup);
					ItemLookupResponse response = itemLookupResponse.get(60000, TimeUnit.MILLISECONDS);
					if(response == null){
						continue;
					}
					List<Items> itemsList = response.getItems();
					if(itemsList != null ){
						Items items = itemsList.get(0);
						if(items != null){
							List<Item> itemList = items.getItem();
							if(itemList != null && itemList.size() > 0 ){
								Item item = itemList.get(0);
								if(item != null){
									Variations variations =item.getVariations();
									if(variations != null){
										
										VariationDimensions vardims = variations.getVariationDimensions();
										List<String> dims = new ArrayList<String>();
										boolean hasPrimary = false;
										if(vardims != null){
											dims = vardims.getVariationDimension();
											for(String dim : dims ){
												if(StringUtils.containsIgnoreCase(dim, "Color")){
													hasPrimary = true;
													break;
												}
											}
										}
										List<VariationAttribute> vas = new ArrayList<VariationAttribute>();
										List<Item> varItemList = variations.getItem();
										if(varItemList != null && varItemList.size() > 0){
											Map<String,String> styleMap = new HashMap<String,String>();
											for(Item varItem : varItemList){
												String skuAsin = varItem.getASIN();
												
												if(skuIds.size() > 0 && !skuIds.contains(skuAsin)){
													logger.info("parentAsin {}, skuIds {}, skuAsin {}",parent_asin,Arrays.toString(skuIds.toArray()),skuAsin);
													continue;
												}
												LSelectionList lselectlist = new LSelectionList();
												LStyleList lStyleList = new LStyleList();
												
												//offers
												Offers offers = varItem.getOffers();
												if(offers != null){
													List<Offer> offerList = offers.getOffer();
													if(offerList != null && offerList.size() > 0 ){
														Offer offer = offerList.get(0);
														if(offer != null){
															//price
															Price price = offer.getOfferListing().get(0).getPrice();
															if(price != null){
																String unit = price.getCurrencyCode();
																String salePrice = price.getFormattedPrice();
																float sale = 0;
																float orig = 0;
																if(StringUtils.isNotBlank(salePrice)){
																	salePrice = StringUtils.replace(salePrice, "$", "");
																	sale = Float.valueOf(salePrice);
																	orig = sale;
																}
																lselectlist.setOrig_price(orig);
																lselectlist.setSale_price(sale);
																lselectlist.setPrice_unit(unit);
															}
															//stock
															String stockStr = offer.getOfferListing().get(0).getAvailability();
															int stockStatus = 0;
															int num = 0;
															if(StringUtils.isNotBlank(stockStr)){
																Pattern pattern = Pattern.compile("Only (\\d+) left in stock",Pattern.CASE_INSENSITIVE);
																Matcher matcher = pattern.matcher(stockStr);
																if(matcher.find()){
																	stockStatus = 2;
																	num = Integer.valueOf(matcher.group(1));
																}
																pattern = Pattern.compile("in stock|Usually ships|Want it",Pattern.CASE_INSENSITIVE);
																matcher = pattern.matcher(stockStr);
																if(matcher.find()){
																	stockStatus = 1;
																}
																pattern = Pattern.compile("Out of Stock|Not yet published|Not yet released|Available for Pre-order|Currently unavailable",Pattern.CASE_INSENSITIVE);
																matcher = pattern.matcher(stockStr);
																if(matcher.find()){
																	stockStatus = 0;
																}
																lselectlist.setStock_status(stockStatus);
																lselectlist.setStock_number(num);
															}
															
															if(StringUtils.equals(current_asin, skuAsin)){
																int save = Math.round((1 - lselectlist.getSale_price() / lselectlist.getOrig_price()) * 100);// discount
																ret.setPrice(new com.haitao55.spider.common.gson.bean.Price(lselectlist.getOrig_price(), save, lselectlist.getSale_price(), lselectlist.getPrice_unit()));
																ret.setStock(new Stock(lselectlist.getStock_status()));
															}
														}
													} else {
														continue;
													}
												}
												String swatchUrl = StringUtils.EMPTY;
												if(varItem.getMediumImage() != null){
													swatchUrl = varItem.getMediumImage().getURL();
												}
												
												//image
												List<ImageSets> imageSetsList = varItem.getImageSets();
												List<Picture> imageList = new ArrayList<Picture>();
												if(imageSetsList != null && imageSetsList.size() > 0){
													for(ImageSets imageSets : imageSetsList){
														List<ImageSet> imageSetList = imageSets.getImageSet();
														if(imageSetList != null && imageSetList.size() > 0){
															for(ImageSet imageSet : imageSetList){
																String category = imageSet.getCategory();
																if(StringUtils.containsIgnoreCase(category, "variant")){
																	imageList.add(new Picture(imageSet.getLargeImage().getURL()));
																} else if(StringUtils.containsIgnoreCase(category, "primary")){
																	imageList.add(0, new Picture(imageSet.getLargeImage().getURL()));
																}
															}
														}
													}
												}
												//spuimage
												if(StringUtils.equals(current_asin, skuAsin)){
													ret.setImage(new LImageList(imageList));
												}
												//sku
												String styleId = StringUtils.EMPTY;
												VariationAttributes vatrr = varItem.getVariationAttributes();
												if(vatrr != null){
													vas = vatrr.getVariationAttribute();
													List<Selection> selections = new ArrayList<Selection>();
													for(VariationAttribute va : vas){
														if(!hasPrimary){
															styleId = "default";
															lselectlist.setStyle_id(styleId);
															lselectlist.setGoods_id(skuAsin);
															
															lStyleList.setStyle_switch_img((swatchUrl == null?"":swatchUrl));
															lStyleList.setStyle_id(styleId);
															lStyleList.setGood_id(skuAsin);
															lStyleList.setStyle_cate_id(0);
															lStyleList.setStyle_cate_name(va.getName());
															lStyleList.setStyle_name(styleId);
															if(StringUtils.equals(current_asin, skuAsin)){
																lStyleList.setDisplay(true);
															}
															lStyleList.setStyle_images(imageList);
														}
														if(StringUtils.containsIgnoreCase(va.getName(), "Color")){
															
															styleId = va.getValue().get(0);
															lselectlist.setStyle_id(styleId);
															lselectlist.setGoods_id(skuAsin);
															
															lStyleList.setStyle_switch_img((swatchUrl == null?"":swatchUrl));
															lStyleList.setStyle_id(styleId);
															lStyleList.setGood_id(skuAsin);
															lStyleList.setStyle_cate_id(0);
															lStyleList.setStyle_cate_name(va.getName());
															lStyleList.setStyle_name(styleId);
															if(StringUtils.equals(current_asin, skuAsin)){
																lStyleList.setDisplay(true);
															}
															lStyleList.setStyle_images(imageList);
														} else {
															Selection selection = new Selection();
															selection.setSelect_id(0);
															selection.setSelect_name(va.getName());
															selection.setSelect_value(va.getValue().get(0));
															selections.add(selection);
														}
														lselectlist.setSelections(selections);
													}
												}
												l_selection_list.add(lselectlist);
												if(styleMap.get(styleId) == null){
													l_style_list.add(lStyleList);
													styleMap.put(styleId, styleId);
												}
											}
										}
										
									}
									
									ItemAttributes itemAttributes = item.getItemAttributes();
									
									if(itemAttributes != null){
										//feature
										List<String> features = itemAttributes.getFeature();
										Map<String, Object> featureMap = new HashMap<String, Object>();
										if(features != null && features.size() > 0 ){
											int count = 1;
											for(String feature : features){
												featureMap.put("feature-" + count, feature);
												count++;
											}
										}
										ret.setFeatureList(featureMap);
										
										//title
										ret.setTitle(new Title(itemAttributes.getTitle(), "", "", ""));
										//brand
										ret.setBrand(new Brand(itemAttributes.getBrand(), "", "", ""));
										
									}
									//description
									EditorialReviews reviews = item.getEditorialReviews();
									if(reviews != null && (ret.getDescription() == null || StringUtils.isBlank((String)ret.getDescription().get("en"))  )){
										Map<String, Object> descMap = new HashMap<String, Object>();
										List<EditorialReview> reviewList = reviews.getEditorialReview();
										if(reviewList != null && reviewList.size() > 0 ){
											for(EditorialReview review : reviewList){
												if(StringUtils.containsIgnoreCase(review.getSource(), "Description")){
													String text = delHTMLTag(review.getContent());
													descMap.put("en", text);
													break;
												}
											}
										}
										ret.setDescription(descMap);
									}
									//category
									BrowseNodes browseNodes = item.getBrowseNodes();
									if(browseNodes != null && (ret.getCategory() == null || ret.getCategory().size() == 0) ){
										List<BrowseNode> browseNodesList = browseNodes.getBrowseNode();
										if(browseNodesList != null){
											List<String> cats = new ArrayList<String>();
											categories(browseNodesList,cats);
											Collections.reverse(cats);
											ret.setCategory(cats);
											ret.setBreadCrumb(cats);
										}
									}
									break;
								}
							}
						}
					}
				}catch(Throwable e){
					e.printStackTrace();
					logger.error("AccessKeyId {}, SecretKey {}, AssociateTag {},ItemId {},error count {}",key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag(),parent_asin,i);
				}
			}
			
		}
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		ret.setSku(sku);
	}
	
	private void categories(List<BrowseNode> browNodeList,List<String> cats){
		if(browNodeList == null || browNodeList.size() == 0){
			return ;
		}
		for(BrowseNode browNode : browNodeList){
			String name = browNode.getName();
			if(browNode.isIsCategoryRoot() == null){
				cats.add(name);
			}
			Ancestors ancestors =  browNode.getAncestors();//.getBrowseNode();
			if(ancestors != null){
				categories(ancestors.getBrowseNode(),cats);
			}
			break;
		}
	}
	
	
	private void addReturnKeys(ItemLookupRequest request) {
		String keys = "Accessories,AlternateVersions,BrowseNodes,EditorialReview,Images,ItemAttributes,ItemIds,Large,Medium,OfferFull,OfferListings,Offers,OfferSummary,PromotionSummary,Reviews,SalesRank,Similarities,Small,Tracks,Variations,VariationImages,VariationMatrix,VariationOffers,VariationSummary";
		for(String key : StringUtils.split(keys, ",")){
			request.getResponseGroup().add(key);
		}
	}
	private String delHTMLTag(String htmlStr){ 
         String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式 
         String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式 
         String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式 
         
         Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE); 
         Matcher m_script=p_script.matcher(htmlStr); 
         htmlStr=m_script.replaceAll(""); //过滤script标签 
         
         Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE); 
         Matcher m_style=p_style.matcher(htmlStr); 
         htmlStr=m_style.replaceAll(""); //过滤style标签 
         
         Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE); 
         Matcher m_html=p_html.matcher(htmlStr); 
         htmlStr=m_html.replaceAll(""); //过滤html标签 

        return htmlStr.trim(); //返回文本字符串 
     } 
	 
	 public static void main(String[] args) {
		 List<String> list = new ArrayList<String>();
		 list.add("B00I3N6RDU");
		 new AmazonHtmlApiCrawler(null).apiService(new RetBody(),"https://www.amazon.com/dp/B00I3N6RDU/","B00I3N6RDU","B00I3N6RDU",list);
		/*String txt = delHTMLTag("<div class=\"aplus\"> <div class='leftImage' style='width:360px'><img src='https://images-na.ssl-images-amazon.com/images/G/01/aplus/detail-page/B00I3N6RDU_1._CB340853920_.jpg' alt='' ></div> <h5>Moist Collection</h5> <p>Moisturize. Soften. Tame. Dry Hair? Not here.</p> <h5>Available In:</h5> <ul><li>Shampoo</li><li>Conditioner</li><li>2 In 1</li><li>3 Minute Miracle Moist</li></ul> <div class=\"break\"></div> <h4>Check Out Our Other Collections</h4> <div class=\"half-col\"><div class='leftImage' style='width:300px'><img src='https://images-na.ssl-images-amazon.com/images/G/01/aplus/detail-page/B00I3N6RDU_2._CB340853922_.jpg' alt='' ></div> ");
		System.out.println(txt);*/
	 }
	

}
