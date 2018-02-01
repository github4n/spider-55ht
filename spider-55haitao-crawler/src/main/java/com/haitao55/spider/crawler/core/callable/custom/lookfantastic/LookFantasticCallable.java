package com.haitao55.spider.crawler.core.callable.custom.lookfantastic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

class LookFantasticCallable implements Callable<LookFantasticSkuBean> {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String imgprefix="http://s4.thcdn.com/";
	private Url skuUrl;
	
	public LookFantasticCallable(Url skuUrl){
		this.skuUrl = skuUrl;
	}
	

	@Override
	public LookFantasticSkuBean call() {
		LookFantasticSkuBean skuBean = new LookFantasticSkuBean();
		skuBean.setUrl(skuUrl.getValue());
		try{
//			String content = HttpUtils.get(skuUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES, false);
			String content = HttpUtils.get(skuUrl.getValue());
			
			
			JSONObject jsonObject=JSONObject.parseObject(content);
			String sku_Id = String.valueOf(jsonObject.get("productId"));
			String salePrice = StringEscapeUtils.unescapeHtml4(String.valueOf( jsonObject.get("price")));
			String origPrice = StringEscapeUtils.unescapeHtml4(String.valueOf(jsonObject.get("rrpDisplay")));
			Object saveObject = jsonObject.get("rrpSavingPercent");
			String save=StringUtils.EMPTY;
			if(null==saveObject){
				save="0";
			}
			String unit = getCurrencyValue(salePrice);//得到货币代码
			salePrice = salePrice.replaceAll("[£,]", "");
			origPrice = origPrice.replaceAll("[£,]", "");
			
			if(StringUtils.isBlank(replace(origPrice)) ){
				origPrice = salePrice;
			}
			if(StringUtils.isBlank(replace(salePrice)) ){
				salePrice = origPrice;
			}
			if(StringUtils.isBlank(replace(origPrice)) && StringUtils.isBlank(replace(salePrice))){
				logger.error("Error while crawling amazon price ,url {}",skuUrl);
				return null;
			}
			if(StringUtils.isBlank(origPrice) || Float.valueOf(replace(origPrice)) < Float.valueOf(replace(salePrice)) ){
				origPrice = salePrice;
			}
			if(StringUtils.isBlank(save)){
				save = Math.round((1 - Float.valueOf(replace(salePrice)) / Float.valueOf(replace(origPrice))) * 100)+"";// discount
			}
			JSONArray imageJsonArray=JSONArray.parseArray(jsonObject.getString("images"));
			if(null!=imageJsonArray&&imageJsonArray.size()>0){
				for (int i=0;i<imageJsonArray.size();i++) {
					JSONObject map = (JSONObject) imageJsonArray.get(i);
					if("carousel".equals(map.get("type"))&&0==map.getIntValue("index")){
						skuBean.setImageUrl(new ArrayList<Image>(){/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

						{
							add(new Image(imgprefix.concat(String.valueOf(map.get("name")))));
						}});
					}
				}	
			}
			skuBean.setSkuId(sku_Id);
			skuBean.setOrig(replace(origPrice));
			skuBean.setSale(replace(salePrice));
			skuBean.setSave(save);
			skuBean.setUnit(unit);
			
			List<Map<String, String>> selections=new ArrayList<Map<String,String>>();
			
			JSONArray jsonarray = (JSONArray)jsonObject.get("variations");
			JSONObject variations= (JSONObject) jsonarray.get(0);
			if(jsonarray.size()>1){
				for (int i =1;i<jsonarray.size();i++ ) {
					Map<String, String> map=new HashMap<String,String>();
					JSONObject object = (JSONObject) jsonarray.get(i);
					String select_name = (String) object.get("variation");
					String select_value=StringUtils.EMPTY;
					JSONArray variationJsonarray = (JSONArray)object.get("options");
					for (int j = 0; j < variationJsonarray.size(); j++) {//确认options
						JSONObject optionObject = (JSONObject) variationJsonarray.get(j);
						String optionId=String.valueOf(optionObject.get("id"));
						if(StringUtils.containsIgnoreCase(skuUrl.getValue(), optionId)){
							select_value=String.valueOf(optionObject.get("name"));
						}
					}
					map.put("select_name", select_name);
					map.put("select_value", select_value);
					selections.add(map);
				}
			}
			skuBean.setSelections(selections);
//			@SuppressWarnings("unchecked")
//			Map<String,Object> variations = (Map<String, Object>) object;
			String cate_name = (String) variations.get("variation");
			jsonarray = (JSONArray)variations.get("options");
			String style_id=StringUtils.EMPTY;
			Object skuRel= null;
			for (int j = 0; j < jsonarray.size(); j++) {//确认options
				JSONObject optionObject = (JSONObject) jsonarray.get(j);
				String optionId=String.valueOf(optionObject.get("id"));
				if(StringUtils.containsIgnoreCase(skuUrl.getValue(), optionId)){
					style_id=String.valueOf(optionObject.get("name"));
					skuRel=optionObject.get("id");
				}
			}
			skuBean.setCate_name(cate_name);
			skuBean.setStyleId(style_id);
			skuBean.setSkuRel(skuRel+"");
		}catch(HttpException e){
			e.printStackTrace();
		} catch(Exception e1){
			logger.error("lookfantastic  sku analysis error",e1);
			return null;
		}
		return skuBean;
	}
	

	/**
	 * get 货币
	 * @param val
	 * @return
	 */
	private String getCurrencyValue(String val){
	    String currency = StringUtils.substring(val, 0, 1);
	    String unit = StringUtils.EMPTY;
	    if("£".equals(currency)){
	    	unit = Currency.codeOf(currency).name();
	    }
	    return unit;
		
   }	
	
	public String getText(Elements es){
		if(es != null && es.size() > 0){
			return es.get(0).text();
		}
		return StringUtils.EMPTY;
	}
	
	public String getAttr(Elements es,String attrKey){
		if(es != null && es.size() > 0){
			return es.get(0).attr(attrKey);
		}
		return StringUtils.EMPTY;
	}

	private String replace(String dest){
		if(StringUtils.isBlank(dest)){
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));
		
	}

}

