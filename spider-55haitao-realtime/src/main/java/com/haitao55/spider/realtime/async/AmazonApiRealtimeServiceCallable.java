package com.haitao55.spider.realtime.async;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.amazon.api.AWSKey;
import com.haitao55.spider.crawler.core.callable.custom.amazon.api.AmazonRealTimePriceAWSKeyPool;
import com.haitao55.spider.crawler.core.callable.custom.amazon.api.AmazonUtils;
import com.haitao55.spider.crawler.core.callable.custom.amazon.api.ParentResult;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.core.model.OutputChannel;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.UrlStatus;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.CompareSort;
import com.haitao55.spider.crawler.exception.ParseException;

/**
 * amazon api 核价callable Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月20日 下午2:41:00
 * @version 1.0
 */
public class AmazonApiRealtimeServiceCallable implements java.util.concurrent.Callable<OutputObject> {
	private static final Logger logger = LoggerFactory.getLogger(RealtimeCrawlerCallable.class);
	private static final String JSON_BODY_KEY = "retbody";
	private String channel = "kafka";
	private Set<String> fields = new HashSet<String>(){/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		add(JSON_BODY_KEY);
	}};
	private static long CRAWLING_WAIT_TIME = 7 * 1000;// 在线抓取最多等待时间
	private AmazonRealTimePriceAWSKeyPool awsKeyPool;
	private Context context;
	private Long taskId;
	private static final String defaultKeyId = "AKIAI77X7X5JVEZ52ZCA";
    private static final String defaultSecretKey = "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";
    private static final String defaultAssociateTag = "55haitao";
	

	public AmazonApiRealtimeServiceCallable(AmazonRealTimePriceAWSKeyPool awsKeyPool, Context context, Long taskId) {
		this.awsKeyPool = awsKeyPool;
		this.context = context;
		this.taskId = taskId;
	}

	@Override
	public OutputObject call() throws Exception {
		ParentResult result = null;
		AWSKey awsKey = null;
		for (int i = 0; i < 5; i++) {
		    try{
		        if(i < 2){
		            awsKey = awsKeyPool.pollKey();
                } else {
                    awsKey = new AWSKey(defaultKeyId, defaultSecretKey, defaultAssociateTag);
                }
	            result = AmazonUtils.getParentAsin(context, awsKey, CRAWLING_WAIT_TIME);
	            if (result != null) {
	                break;
	            }
		    }catch(Exception e){
		        e.printStackTrace();
		    }
		}
		RetBody rebody = null;
		if (result != null && StringUtils.isNotBlank(result.getParentAsin())) {
			for (int i = 0; i < 5; i++) {
				try{
				    if(i < 2){
	                    awsKey = awsKeyPool.pollKey();
	                } else {
	                    awsKey = new AWSKey(defaultKeyId, defaultSecretKey, defaultAssociateTag);
	                }
	                rebody = AmazonUtils.getRebody(result, awsKey, CRAWLING_WAIT_TIME);
	                if (rebody != null) {
	                    break;
	                }
				}catch(Exception e){
				    e.printStackTrace();
				}
			}

		}

		setOutput(context, rebody);

		OutputObject oo = new OutputObject();
		oo.setTaskId(String.valueOf(taskId));
		oo.setOutputChannel(OutputChannel.codeOf(this.channel));
		oo.setImages(context.getUrl().getImages());
		oo.setUrl(context.getUrl());
		oo.setDocType(DocType.INSERT);

		Map<String, Object> map = context.getAll();// 到目前为止context中存放的所有键-值对数据
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();// 首字母不需要一定大写
			Object value = entry.getValue();
			if (fields.contains(key)) {// 只有配置文件中列举的字段，才会被输出
				oo.putItemField(key, Objects.toString(value, ""));
			}
		}

		context.getUrl().setOutputObject(oo);// 最后将需要输出的数据对象放置到Url对象中
		context.getUrl().setUrlStatus(UrlStatus.CRAWLED_OK);
		
		logger.info("Set output object successfully, url:{}", context.getUrl().getValue());
		return oo;
	}
	
	private boolean checkSalePriceIsDifferent(List<LSelectionList> l_selection_list){
		Float firstSalePrice = 0f;
		boolean different = false;
		
		for(int i = 0; i < l_selection_list.size(); i++){
			LSelectionList lSelectionList = l_selection_list.get(i);
			Float salePrice = lSelectionList.getSale_price();
			if(i == 0){
				firstSalePrice = salePrice;
				continue;
			}
			BigDecimal saleBig = new BigDecimal(salePrice);
			BigDecimal tempPriceBig = new BigDecimal(firstSalePrice);
			if(firstSalePrice != 0 && saleBig.compareTo(tempPriceBig) != 0){
				different = true;
				break;
			}
		}
		return different;
	}

	private LSelectionList getTargetlSelectionListLowSalePrice(List<LSelectionList> l_selection_list){
		List<LSelectionList> all_l_selection_list = l_selection_list;
		LSelectionList lSelectionList = null;
		if(all_l_selection_list != null){
			CompareSort<LSelectionList> allLselectionCompare = new CompareSort<LSelectionList>();
			allLselectionCompare.sort(all_l_selection_list);//按最低价格排序
			
			for(LSelectionList lselection : all_l_selection_list){
				if(lselection.getStock_status() > 0){
					lSelectionList = lselection;
					break;
				}
			}
		}
		return lSelectionList;
	}
	
	private LSelectionList getDefaultLselectionListLowSalePrice(List<LSelectionList> l_selection_list,List<LStyleList> l_style_list){
		LStyleList oldDefaultStyle = null;
		LSelectionList defaultLSelection = null;
		
		for(LStyleList lStyleList : l_style_list){
			boolean display = lStyleList.isDisplay();
			if(display){
				oldDefaultStyle = lStyleList;
				break;
			}
		}
		
		if(l_style_list.size() > 0 && oldDefaultStyle == null){
			oldDefaultStyle = l_style_list.get(0);
			oldDefaultStyle.setDisplay(true);
		}
		
		List<LSelectionList> defaultSelectionList = new ArrayList<>();
		for(LSelectionList lSelectionList : l_selection_list){
			if(StringUtils.equals(lSelectionList.getStyle_id(), oldDefaultStyle.getStyle_id()) && 
					lSelectionList.getStock_status() > 0){
				defaultSelectionList.add(lSelectionList);
			}
		}
		
		if(defaultSelectionList != null){
			CompareSort<LSelectionList> oldLslectionCompare = new CompareSort<LSelectionList>();
			oldLslectionCompare.sort(defaultSelectionList);//按最低价格排序
			defaultLSelection = defaultSelectionList.get(0);//之前已经判断库存了,这段代码get(0)肯定是有库存,而且价格最低
		}
	
		return defaultLSelection;
	}
	
	private void updateSpuDefaultPrice(RetBody retBody,LSelectionList lselectList,
			List<LStyleList> l_style_list){
		retBody.setStock(new Stock(lselectList.getStock_status()));
		float orig =  lselectList.getOrig_price();
		float sale =  lselectList.getSale_price();
		String unit = lselectList.getPrice_unit();
		int save = Math.round((1 - sale / orig) * 100);// discount
		retBody.setPrice(new Price(orig, save, sale, unit));
		for(LStyleList lStyleList : l_style_list){
			String lstyleId = lStyleList.getStyle_id();
			boolean display = lStyleList.isDisplay();
			if(StringUtils.equals(lselectList.getStyle_id(), lstyleId)){
				lStyleList.setDisplay(true);
				retBody.setImage(new LImageList(lStyleList.getStyle_images()));
			}else{
				if(display){
					lStyleList.setDisplay(false);
				}
			}
		}
	}
	
	protected void setAmazonOutput(Context context, RetBody retBody) {
		
		List<LSelectionList> l_selection_list = retBody.getSku().getL_selection_list();
		List<LStyleList> l_style_list = retBody.getSku().getL_style_list();
		if(null == l_selection_list || null == l_style_list){
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, context.getUrl().toString()+" parser error..."); 
		}
		handleAmazonDefaultStyle_v1(retBody, l_selection_list, l_style_list);
		
		boolean priceFlag = checkPriceIsZero(retBody);//检查价格是否为0.00
		if(priceFlag){
			throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" price is zero");
		}
		if (retBody.getStock() != null && retBody.getStock().getStatus() == 0) {// 下架
			throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString() + " is offline...");
		}
		context.put(getOutput(), retBody.parseTo());
	}
	
	protected boolean checkPriceIsZero(RetBody retBody){
		boolean priceFlag = false;
		Price price = retBody.getPrice();
		if(!Objects.isNull(price)){
			float sale = price.getSale();
			float orig = price.getOrig();
			if(sale == 0.00 || orig == 0.00){
				priceFlag = true;
			}
		}
		List<LSelectionList> lselectionList = retBody.getSku().getL_selection_list();
		if(CollectionUtils.isNotEmpty(lselectionList)){
			for(LSelectionList selection : lselectionList){
				if(selection.getSale_price() == 0.00 || 
						selection.getOrig_price() == 0.00){
					priceFlag = true;
					break;
				}
			}
		}
		return priceFlag;
	}
	
	
	// 针对返回rebody 作进一步的库存检查
	protected void setOutput(Context context, RetBody retBody) {
		List<LSelectionList> l_selection_list = retBody.getSku().getL_selection_list();
		List<LStyleList> l_style_list = retBody.getSku().getL_style_list();
		if (null == l_selection_list || null == l_style_list) {
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
					context.getUrl().toString() + " parser error...");
		}
		
		boolean currentDefaultSkuFlag = false;
		boolean isDifferent = this.checkSalePriceIsDifferent(l_selection_list);//check lselectionList object salePrice isSame
		if(isDifferent){
			//获取lselection_list 有库存 && 价格最低
			LSelectionList targetSelection = this.getTargetlSelectionListLowSalePrice(l_selection_list);
			if(!Objects.isNull(targetSelection)){
				LSelectionList defaultLselection = this.getDefaultLselectionListLowSalePrice(l_selection_list,l_style_list);
				if(!Objects.isNull(defaultLselection)){
					Float targetSalePrice= targetSelection.getSale_price();
					Float defaultSalePrice= defaultLselection.getSale_price();
					if(targetSalePrice <= defaultSalePrice){//默认价格大于target对象价格
						this.updateSpuDefaultPrice(retBody,targetSelection,l_style_list);
						currentDefaultSkuFlag = true;
					}
				}
			}
		}
		
		if(!currentDefaultSkuFlag){
			this.handleAmazonDefaultStyle_v1(retBody, l_selection_list, l_style_list);
		}
		
		if (retBody.getStock() != null && retBody.getStock().getStatus() == 0) {// 下架
			throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString() + " is offline...");
		}
		
		context.put(getOutput(), retBody.parseTo());
	}

	private void handleAmazonDefaultStyle_v1(RetBody retBody,List<LSelectionList> l_selection_list,
			List<LStyleList> l_style_list){
		
		LStyleList defaultLStyleList = null;
		// find display = true
		for (LStyleList lStyleList : l_style_list) {
			boolean display = lStyleList.isDisplay();
			if (display) {
				defaultLStyleList = lStyleList;
				break;
			}
		}
		// 可能无默认的 LStyleList 全部都是 display = false
		if (l_style_list.size() > 0 && defaultLStyleList == null) {
			defaultLStyleList = l_style_list.get(0);
			defaultLStyleList.setDisplay(true);
		}
		// check stock is or not
		boolean stockFlag = false;
		if (defaultLStyleList != null) {
			for (LSelectionList selectionList : l_selection_list) {
				if (StringUtils.equals(selectionList.getStyle_id(), defaultLStyleList.getStyle_id())) {
					int status = selectionList.getStock_status();
					if (status > 0) {
						stockFlag = stockFlag || true;
						// 虽然默认sku有库存，仍然检查spu属性是否符合条件 不符合条件 主动修正
						float orig =  selectionList.getOrig_price();
						float sale =  selectionList.getSale_price();
						
						if(retBody.getStock() == null || retBody.getStock().getStatus() == 0 ){
							retBody.setStock(new Stock(selectionList.getStock_status()));
						} else {
							if(retBody.getStock().getStatus() != selectionList.getStock_status()){
								retBody.setStock(new Stock(selectionList.getStock_status()));
							}
						}
						if(retBody.getPrice() == null ){
							String unit = selectionList.getPrice_unit();
							int save = Math.round((1 - sale / orig) * 100);// discount
							retBody.setPrice(new Price(orig, save, sale, unit));
						} else {
							if(orig != retBody.getPrice().getOrig() || sale != retBody.getPrice().getSale()){
								String unit = selectionList.getPrice_unit();
								int save = Math.round((1 - sale / orig) * 100);// discount
								retBody.setPrice(new Price(orig, save, sale, unit));
							}
						}
						break;
					} else {
						stockFlag = stockFlag || false;
					}
				}
			}
		}
		// adjust display sku while stock is not
		if (!stockFlag && defaultLStyleList != null) {
			for (LStyleList lStyleList : l_style_list) {
				boolean stock = false;
				LSelectionList lSelectionList = null;
				for (LSelectionList selectionList : l_selection_list) {
					if (StringUtils.equals(selectionList.getStyle_id(), lStyleList.getStyle_id())) {
						int status = selectionList.getStock_status();
						if (status > 0) {
							stock = stock || true;
							lSelectionList = selectionList;
							break;
						} else {
							stock = stock || false;
						}
					}
				}
				if (stock) {
					float orig = lSelectionList.getOrig_price();
					float sale = lSelectionList.getSale_price();
					String unit = lSelectionList.getPrice_unit();
					int save = Math.round((1 - sale / orig) * 100);// discount
					retBody.setPrice(new Price(orig, save, sale, unit));
					lStyleList.setDisplay(true);
					defaultLStyleList.setDisplay(false);
					retBody.setStock(new Stock(lSelectionList.getStock_status()));
					retBody.setImage(new LImageList(lStyleList.getStyle_images()));
					break;
				}
			}
		}
	}
	
	private String getOutput() {
		return JSON_BODY_KEY;
	}
}