package com.haitao55.spider.crawler.core.callable.custom.amazon.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.ws.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import am.ik.aws.apa.AwsApaRequester;
import am.ik.aws.apa.AwsApaRequesterImpl;
import am.ik.aws.apa.jaxws.BrowseNode;
import am.ik.aws.apa.jaxws.BrowseNode.Ancestors;
import am.ik.aws.apa.jaxws.BrowseNodes;
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

import com.google.common.collect.Maps;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;

public class AmazonUtils {

	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String ENDPOINT = "https://ecs.amazonaws.com";
	private static final String HOST = "www.amazon.com";
	private static final String IDTYPE = "ASIN";
	
	public static ParentResult getParentAsin(Context context , AWSKey key,long timeout) throws Exception{
		String url =    context.getUrl().getValue();
		//String domain = SpiderStringUtil.getAmazonDomain(url);
		String itemId = SpiderStringUtil.getAmazonItemId(url);
		RetBody rebody = new RetBody();
		String parentAsin = StringUtils.EMPTY;
		try{
			
			AwsApaRequester requester = new AwsApaRequesterImpl(ENDPOINT,key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag());
			ItemLookupRequest lookup = new ItemLookupRequest();
			lookup.setIdType(IDTYPE);
			lookup.getItemId().add(itemId);//B01LZYTSFP
			addReturnKeys(lookup);
			logger.info("AccessKeyId {}, SecretKey {}, AssociateTag {},ItemId {}",key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag(),itemId);
			Response<ItemLookupResponse> itemLookupResponse = requester.itemLookupAsync(lookup);
			ItemLookupResponse response = itemLookupResponse.get(timeout, TimeUnit.MILLISECONDS);
			//String resultJson = JsonUtils.bean2json(response);
			String docid = SpiderStringUtil.md5Encode(url);
			rebody.setDOCID(docid);
			List<Items> itemsList = response.getItems();
			if(itemsList != null ){
				Items items = itemsList.get(0);
				if(items != null){
					List<Item> itemList = items.getItem();
					if(itemList != null && itemList.size() > 0){
						Item item = itemList.get(0);
						if(item != null){
							//parentAsin
							parentAsin = item.getParentASIN();
							if(StringUtils.isBlank(parentAsin)){
								parentAsin = item.getASIN();
							}
							//offers
							Offers offers = item.getOffers();
							if(offers != null){
								List<Offer> offerList = offers.getOffer();
								if(offerList != null && offerList.size() > 0 ){
									Offer offer = offerList.get(0);
									if(offer != null){
									    
									    float sale = 0;
                                        float orig = 0;
                                        //salePrice
                                        Price salePrice = offer.getOfferListing().get(0).getSalePrice();
                                        String unit = StringUtils.EMPTY;
                                        if(salePrice != null){
                                            unit = salePrice.getCurrencyCode();
                                            String formatSalePrice = salePrice.getFormattedPrice();
                                            if(StringUtils.isNotBlank(formatSalePrice)){
                                                String formatSale = StringUtils.replace(StringUtils.replace(formatSalePrice, "$", ""), ",", "");
                                                sale = Float.valueOf(formatSale);
                                            }
                                        }
                                        //price
                                        Price origPrice = offer.getOfferListing().get(0).getPrice();
                                        if(origPrice != null){
                                            if(StringUtils.isBlank(unit)){
                                                unit = origPrice.getCurrencyCode();
                                            }
                                            String formatOrigPrice = origPrice.getFormattedPrice();
                                            if(StringUtils.isNotBlank(formatOrigPrice)){
                                                String formatOrig = StringUtils.replace(StringUtils.replace(formatOrigPrice, "$", ""), ",", "");
                                                orig = Float.valueOf(formatOrig);
                                            }
                                        }
                                        if(sale == 0){
                                            sale = orig;
                                        }
                                        
										int save = Math.round((1 - sale / orig) * 100);// discount
										rebody.setPrice(new com.haitao55.spider.common.gson.bean.Price(orig, save, sale, unit));
										//stock
										String stockStr = offer.getOfferListing().get(0).getAvailability();
										int stockStatus = 0;
										if(StringUtils.isNotBlank(stockStr)){
											Pattern pattern = Pattern.compile("Only (\\d+) left in stock",Pattern.CASE_INSENSITIVE);
											Matcher matcher = pattern.matcher(stockStr);
											if(matcher.find()){
												stockStatus = 2;
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
											rebody.setStock(new Stock(stockStatus));
										}
									}
								} else {
									logger.info("url {} offerlist is null and it will be offline.",url);
									//throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" is offline...");
								}
							}
							//ItemAttributes
							ItemAttributes attr = item.getItemAttributes();
							if(attr != null){
								String title = attr.getTitle();
								String brand = attr.getBrand();
								List<String> features = attr.getFeature();
								
								//site
								rebody.setSite(new Site(HOST));
								//produrl
								rebody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), docid));
								//title
								rebody.setTitle(new Title(title, "", "", ""));
								//brand
								rebody.setBrand(new Brand(brand, "", "", ""));
								
								Map<String, Object> properties = new HashMap<String,Object>();
								String department = attr.getDepartment();
								String s_gender = StringUtils.EMPTY;
								if(StringUtils.isNotBlank(department) && StringUtils.containsIgnoreCase(department, "women")){
									s_gender = "women";
								} else if (StringUtils.isNotBlank(department) && StringUtils.containsIgnoreCase(department, "men")){
									s_gender = "men";
								} else {
									s_gender = "all";
								}
								properties.put("s_gender", s_gender);
								rebody.setProperties(properties);
								
								Map<String, Object> featureMap = new HashMap<String, Object>();
								Map<String, Object> descMap = new HashMap<String, Object>();
								StringBuilder sb = new StringBuilder();
								if(features != null && features.size() > 0 ){
									int count = 1;
									for(String feature : features){
										featureMap.put("feature-" + count, feature);
										count++;
										sb.append(feature);
									}
								}
								//feature
								rebody.setFeatureList(featureMap);
								descMap.put("en", sb.toString());
								//description
								rebody.setDescription(descMap);
								
							}
							
							//category
							BrowseNodes browseNodes = item.getBrowseNodes();
							if(browseNodes != null){
								List<BrowseNode> browseNodesList = browseNodes.getBrowseNode();
								if(browseNodesList != null){
									List<String> cats = new ArrayList<String>();
									categories(browseNodesList,cats);
									Collections.reverse(cats);
									rebody.setCategory(cats);
									rebody.setBreadCrumb(cats);
								}
							}
							
							//image
							List<ImageSets> imageSetsList = item.getImageSets();
							if(imageSetsList != null && imageSetsList.size() > 0){
								for(ImageSets imageSets : imageSetsList){
									List<ImageSet> imageSetList = imageSets.getImageSet();
									if(imageSetList != null && imageSetList.size() > 0){
										List<Image> imageList = new ArrayList<Image>();
										for(ImageSet imageSet : imageSetList){
											String category = imageSet.getCategory();
											if(StringUtils.containsIgnoreCase(category, "swatch")){
												List<Image> swatchList = new ArrayList<Image>();
												swatchList.add(new Image(imageSet.getMediumImage().getURL()));
												context.getUrl().getImages().put(System.currentTimeMillis()+"", swatchList);
											} else if(StringUtils.containsIgnoreCase(category, "variant")){
												imageList.add(new Image(imageSet.getLargeImage().getURL()));
											} else if(StringUtils.containsIgnoreCase(category, "primary")){
												imageList.add(0, new Image(imageSet.getLargeImage().getURL()));
											}
										}
										context.getUrl().getImages().put(itemId, imageList);
									}
								}
							}
							
						}
					} else {
						logger.info("url {} item list  is null and it will be offline.",url);
						throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" is offline...");
					}
				}
			}
		}catch(ParseException e1){
			e1.printStackTrace();
			logger.error("AccessKeyId {}, SecretKey {}, AssociateTag {},ItemId {},ParseException error msg {}",key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag(),itemId,e1.getMessage());
			throw e1;
		}catch(ExecutionException | InterruptedException e2){
			e2.printStackTrace();
			logger.error("AccessKeyId {}, SecretKey {}, AssociateTag {},ItemId {},ExecutionException | InterruptedException error msg {}",key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag(),itemId,e2.getMessage());
			throw e2;
		} catch (TimeoutException e3) {
			e3.printStackTrace();
			logger.error("AccessKeyId {}, SecretKey {}, AssociateTag {},ItemId {},TimeoutException error msg {}",key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag(),itemId,e3.getMessage());
			throw e3;
		}
		return new ParentResult(parentAsin,itemId,rebody,context);
	
	}
	
	
	
	public static RetBody getRebody(ParentResult parentResult, AWSKey key,long timeout) throws Exception{
		
		Sku sku = new Sku();
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		try{
			AwsApaRequester requester = new AwsApaRequesterImpl(ENDPOINT,key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag());
			ItemLookupRequest lookup = new ItemLookupRequest();
			lookup.setIdType(IDTYPE);
			lookup.getItemId().add(parentResult.getParentAsin());//B01LZYTSFP
			lookup.setMerchantId("Amazon");
			addReturnKeys(lookup);
			logger.info("AccessKeyId {}, SecretKey {}, AssociateTag {},ItemId {}",key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag(),parentResult.getItemId());
			Response<ItemLookupResponse> itemLookupResponse = requester.itemLookupAsync(lookup);
			ItemLookupResponse response = itemLookupResponse.get(timeout, TimeUnit.MILLISECONDS);
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
									if(dims != null && dims.size() > 0){
									    for(String dim : dims ){
	                                        if(StringUtils.containsIgnoreCase(dim, "Color")){
	                                            hasPrimary = true;
	                                            break;
	                                        }
	                                    }
									}
									
								}
								List<VariationAttribute> vas = new ArrayList<VariationAttribute>();
								List<Item> varItemList = variations.getItem();
								if(varItemList != null && varItemList.size() > 0){
									Map<String,String> styles = Maps.newHashMap();
									//StringBuilder skuAsins = new StringBuilder();
									for(Item varItem : varItemList){
										
										String skuAsin = varItem.getASIN();
										
										LSelectionList lselectlist = new LSelectionList();
										LStyleList lStyleList = new LStyleList();
										
										//offers
										Offers offers = varItem.getOffers();
										if(offers != null){
											List<Offer> offerList = offers.getOffer();
											if(offerList != null && offerList.size() > 0 ){
												Offer offer = offerList.get(0);
												if(offer != null){
												    
												    //salePrice
												    Price salePrice = offer.getOfferListing().get(0).getSalePrice();
												    float sale = 0;
                                                    float orig = 0;
                                                    String unit = StringUtils.EMPTY;
												    if(salePrice != null){
												        unit = salePrice.getCurrencyCode();
												        String formatSalePrice = salePrice.getFormattedPrice();
												        if(StringUtils.isNotBlank(formatSalePrice)){
												            String formatSale = StringUtils.replace(StringUtils.replace(formatSalePrice, "$", ""), ",", "");
                                                            sale = Float.valueOf(formatSale);
												        }
												    }
													//price
													Price origPrice = offer.getOfferListing().get(0).getPrice();
													if(origPrice != null){
													    if(StringUtils.isBlank(unit)){
													        unit = origPrice.getCurrencyCode();
													    }
														String formatOrigPrice = origPrice.getFormattedPrice();
														if(StringUtils.isNotBlank(formatOrigPrice)){
														    String formatOrig = StringUtils.replace(StringUtils.replace(formatOrigPrice, "$", ""), ",", "");
														    orig = Float.valueOf(formatOrig);
														}
													}
													if(sale == 0){
													    sale = orig;
													}
													lselectlist.setOrig_price(orig);
													lselectlist.setSale_price(sale);
													lselectlist.setPrice_unit(unit);
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
												}
											} else {
												continue;
											}
										}
										
										String swatchUrl = StringUtils.EMPTY;
										if(varItem.getMediumImage() != null){
											swatchUrl = varItem.getMediumImage().getURL();
											if(StringUtils.isNotBlank(swatchUrl)){
												List<Image> swatchList = new ArrayList<Image>();
												swatchList.add(new Image(swatchUrl));
												parentResult.getContext().getUrl().getImages().put(System.currentTimeMillis()+"", swatchList);
											}
										}
										
										//image
										List<ImageSets> imageSetsList = varItem.getImageSets();
										List<Image> imageList = new ArrayList<Image>();
										if(imageSetsList != null && imageSetsList.size() > 0){
											for(ImageSets imageSets : imageSetsList){
												List<ImageSet> imageSetList = imageSets.getImageSet();
												if(imageSetList != null && imageSetList.size() > 0){
													for(ImageSet imageSet : imageSetList){
														String category = imageSet.getCategory();
														if(StringUtils.containsIgnoreCase(category, "variant")){
															imageList.add(new Image(imageSet.getLargeImage().getURL()));
														} else if(StringUtils.containsIgnoreCase(category, "primary")){
															imageList.add(0, new Image(imageSet.getLargeImage().getURL()));
														}
													}
												}
											}
										}
										//sku
										
										String styleId = StringUtils.EMPTY;
										
										VariationAttributes vatrr = varItem.getVariationAttributes();
										if(vatrr != null){
											vas = vatrr.getVariationAttribute();
											List<Selection> selections = new ArrayList<Selection>();
											boolean isMajor = true;
											for(VariationAttribute va : vas){
												 
												
											    if(!hasPrimary){
											    	 if(isMajor){
											    		 styleId = va.getValue().get(0);;
		                                                 lselectlist.setStyle_id(styleId);
		                                                 lselectlist.setGoods_id(skuAsin);
		                                                 
		                                                 lStyleList.setStyle_switch_img((swatchUrl == null?"":swatchUrl));
		                                                 lStyleList.setStyle_id(styleId);
		                                                 lStyleList.setGood_id(skuAsin);
		                                                 lStyleList.setStyle_cate_id(0);
		                                                 lStyleList.setStyle_cate_name(va.getName());
		                                                 lStyleList.setStyle_name(styleId);
		                                                 if(StringUtils.contains(parentResult.getItemId(), skuAsin)){
		                                                     lStyleList.setDisplay(true);
		                                                 }
		                                                 isMajor = false;
											    	 } else {
														Selection selection = new Selection();
														selection.setSelect_id(0);
														selection.setSelect_name(va.getName());
														selection.setSelect_value(va.getValue().get(0));
														selections.add(selection);
											    	 }
                                                   
                                                } else {
                                                	
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
                                                        if(StringUtils.contains(parentResult.getItemId(), skuAsin)){
                                                            lStyleList.setDisplay(true);
                                                        }
                                                    } else {
                                                        Selection selection = new Selection();
                                                        selection.setSelect_id(0);
                                                        selection.setSelect_name(va.getName());
                                                        selection.setSelect_value(va.getValue().get(0));
                                                        selections.add(selection);
                                                    }
                                                	
                                                }
                                                
											}
											lselectlist.setSelections(selections);
										}
										//download pics
										parentResult.getContext().getUrl().getImages().put(skuAsin, imageList);
										
										l_selection_list.add(lselectlist);
										if(StringUtils.isNotBlank(lStyleList.getStyle_id()) && styles.get(lStyleList.getStyle_id()) == null
										        && imageList.size() > 0 ){
										    l_style_list.add(lStyleList);
										    styles.put(lStyleList.getStyle_id(), lStyleList.getStyle_id());
										}
										
									}
								}
								
							}
							
						}
					}
				}
			}
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			parentResult.getRebody().setSku(sku);
		}catch(ParseException e1){
            e1.printStackTrace();
            logger.error("AccessKeyId {}, SecretKey {}, AssociateTag {},ItemId {},ParseException error msg {}",key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag(),parentResult.getItemId(),e1.getMessage());
            throw e1;
        }catch(ExecutionException | InterruptedException e2){
            e2.printStackTrace();
            logger.error("AccessKeyId {}, SecretKey {}, AssociateTag {},ItemId {},ExecutionException | InterruptedException error msg {}",key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag(),parentResult.getItemId(),e2.getMessage());
            throw e2;
        } catch (TimeoutException e3) {
            e3.printStackTrace();
            logger.error("AccessKeyId {}, SecretKey {}, AssociateTag {},ItemId {},TimeoutException error msg {}",key.getAccessKeyId(),key.getSecretKey(),key.getAssociateTag(),parentResult.getItemId(),e3.getMessage());
            throw e3;
        }
		return parentResult.getRebody();
	}
	
	
	
	public static void addReturnKeys(ItemLookupRequest request) {
		String keys = "Accessories,AlternateVersions,BrowseNodes,EditorialReview,Images,ItemAttributes,ItemIds,Large,Medium,OfferFull,OfferListings,Offers,OfferSummary,PromotionSummary,Reviews,SalesRank,Similarities,Small,Tracks,Variations,VariationImages,VariationMatrix,VariationOffers,VariationSummary";
		for(String key : StringUtils.split(keys, ",")){
			request.getResponseGroup().add(key);
		}
	}
	
	
	public static void categories(List<BrowseNode> browNodeList,List<String> cats){
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
}
