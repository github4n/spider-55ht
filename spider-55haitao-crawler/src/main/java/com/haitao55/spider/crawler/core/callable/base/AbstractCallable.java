package com.haitao55.spider.crawler.core.callable.base;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.RequiredException;
import com.haitao55.spider.crawler.utils.CompareSort;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：Callable接口的抽象实现，为子类提供一些基础功能
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 上午10:29:32
 * @version 1.0
 */
public abstract class AbstractCallable implements Callable {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_PARSER);
	
	protected String input;
	protected String output;

	protected boolean required = false;// 默认为false；如果必须,则需要在任务的配置文件中明确指出

	protected Object getInputObject(Context context) {
		return context.get(getInput());
	}
	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

	protected String getInputString(Context context) {
		return Objects.toString(getInputObject(context), "");
	}

	@SuppressWarnings("rawtypes")
	protected void setOutput(Context context, Object value) {
		checkRequired(context, value);// 所有处理器,在设置输出值之前,都统一检查一下是否必须
		if (value instanceof Collection) {
			Collection collection = (Collection) value;
			setOutput(context, collection);
		} else {
			context.put(getOutput(), value);
		}
	}
	
	/**
	 * 检查l_selection_list价格是否一样
	 * @param l_selection_list
	 * @return
	 */
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
	
	/**
	 * update spu price or stock or image
	 * @param retBody
	 * @param lselectList
	 * @param l_style_list
	 */
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
	
	/**
	 * 获取LSelectionList 最低价格 && 有库存
	 * @param l_selection_list
	 * @return
	 */
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
	
	/**
	 * 获取默认的defulat l_selection_list
	 * @param l_selection_list
	 * @param l_style_list
	 * @return
	 */
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
		
		if(CollectionUtils.isNotEmpty(defaultSelectionList)){
			CompareSort<LSelectionList> oldLslectionCompare = new CompareSort<LSelectionList>();
			oldLslectionCompare.sort(defaultSelectionList);//按最低价格排序
			defaultLSelection = defaultSelectionList.get(0);//之前已经判断库存了,这段代码get(0)肯定是有库存,而且价格最低
		}
	
		return defaultLSelection;
	}
	
	protected void setAmazonOutput(Context context, RetBody retBody) {
		
		List<LSelectionList> l_selection_list = retBody.getSku().getL_selection_list();
		List<LStyleList> l_style_list = retBody.getSku().getL_style_list();
		if(null == l_selection_list || null == l_style_list){
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, context.getUrl().toString()+" parser error..."); 
		}
		
		handleDefaultStyle_v1(retBody, l_selection_list, l_style_list);
		
		boolean priceFlag = checkPriceIsZero(retBody);//检查价格是否为0.00
		if(priceFlag){
			throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" price is zero");
		}
		this.setOutput(context, retBody.parseTo());
	}
	
	//针对返回rebody 作进一步的库存检查
	protected void setOutput(Context context, RetBody retBody) {
		List<LSelectionList> l_selection_list = retBody.getSku().getL_selection_list();
		List<LStyleList> l_style_list = retBody.getSku().getL_style_list();
		if(null == l_selection_list || null == l_style_list){
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, context.getUrl().toString()+" parser error..."); 
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
			this.handleDefaultStyle_v1(retBody, l_selection_list, l_style_list);
		}	
		
		// 将以下spu stock status 逻辑判断代码注释，不需要抛OFFLINE异常，按原有逻辑走
//		if(retBody.getStock() != null && retBody.getStock().getStatus() == 0){//下架
//			throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" is offline...");
//		}
		
		boolean priceFlag = checkPriceIsZero(retBody);//检查价格是否为0.00
		if(priceFlag){
			throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" price is zero");
		}
		
		this.setOutput(context, retBody.parseTo());
		//context.put(getOutput(), retBody.parseTo());
	}
	
	private void handleDefaultStyle_v1(RetBody retBody,List<LSelectionList> l_selection_list,
			List<LStyleList> l_style_list){

		LStyleList defaultLStyleList = null;
		//find display = true
		for(LStyleList lStyleList : l_style_list){
			boolean display = lStyleList.isDisplay();
			if(display){
				defaultLStyleList = lStyleList;
				break;
			}
		}
		//能无默认的 LStyleList 全部都是 display = false可 
		if(l_style_list.size() > 0 && defaultLStyleList == null){
			defaultLStyleList = l_style_list.get(0);
			defaultLStyleList.setDisplay(true);
		}
		
		//check stock is or not
		boolean stockFlag = false;
		if(defaultLStyleList != null){
			for(LSelectionList selectionList : l_selection_list){
				if(StringUtils.equals(selectionList.getStyle_id(), defaultLStyleList.getStyle_id())){
					int status = selectionList.getStock_status();
					if(status > 0){
						stockFlag = stockFlag || true;
						//虽然默认sku有库存，仍然检查spu属性是否符合条件 不符合条件 主动修正 
						
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
		//adjust display sku while stock is not
		if(!stockFlag && defaultLStyleList != null){
			for(LStyleList lStyleList : l_style_list){
				boolean stock = false;
				LSelectionList lSelectionList = null;
				for(LSelectionList selectionList : l_selection_list){
					if(StringUtils.equals(selectionList.getStyle_id(), lStyleList.getStyle_id())){
						int status = selectionList.getStock_status();
						if(status > 0){
							stock = stock || true;
							lSelectionList = selectionList;
							break;
						} else {
							stock = stock || false;
						}
					}
				}
				if(stock){
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
	
	@SuppressWarnings("rawtypes")
	protected void setOutput(Context context, Collection value) {
		if (CollectionUtils.isNotEmpty(value)) {
			if (value.size() == 1) {// 如果集合中只有一个元素，则取出这个唯一的元素放到context中，供以后使用
				context.put(getOutput(), value.iterator().next());
			} else {
				context.put(getOutput(), value);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected void checkRequired(Context context, Object outputValue) {
		if (!required) {// 如果当前字段并非必须,则不用再进行后续的验证,直接返回
			return;
		}

		if (outputValue == null) {
			logger.error("Required field is null! url:{}, field:{}", context.getCurrentUrl(), this.getOutput());
			throw new RequiredException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED, getOutput() + " is null");
		}

		if (outputValue instanceof String) {
			String str = (String) outputValue;
			if (StringUtils.isBlank(str)) {
				logger.error("Required field is blank! url:{}, field:{}", context.getCurrentUrl(), this.getOutput());
				throw new RequiredException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED, getOutput() + " is blank");
			}
		}

		if (outputValue instanceof Collection) {
			Collection collection = (Collection) outputValue;
			if (CollectionUtils.isEmpty(collection)) {
				logger.error("Required field is empty! url:{}, field:{}", context.getCurrentUrl(), this.getOutput());
				throw new RequiredException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED, getOutput() + " is empty");
			}
		}
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}