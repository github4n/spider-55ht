package com.haitao55.spider.crawler.core.callable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.base.RetbodyFieldConstants;
import com.haitao55.spider.crawler.core.callable.context.Context;
/**
 * retbody 数据封装
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月25日 下午2:36:03
* @version 1.0
 */
public class Retbody extends AbstractSelect implements RetbodyFieldConstants{

	@Override
	public void invoke(Context context) throws Exception {
		RetBody retbody=new RetBody();
		buildDOCID(retbody,context);
		buildSite(retbody,context);
		buildProdUrl(retbody,context);
		buildTitle(retbody,context);
		buildPrice(retbody,context);
		buildStock(retbody,context);
		buildBrand(retbody,context);
		buildBreadCrumb(retbody,context);
		buildCategory(retbody,context);
		buildProperties(retbody,context);
		buildFeatureList(retbody,context);
		buildDescription(retbody,context);
		buildSku(retbody,context);
		setOutput(context,retbody);
	}

	/**
	 * sku 封装
	 * @param retbody
	 * @param context
	 */
	private void buildSku(RetBody retbody, Context context) {
		retbody.setSku((Sku) context.get(SKU));
	}
	
	/**
	 * Description
	 * @param retbody
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	private void buildDescription(RetBody retbody, Context context) {
		retbody.setDescription((Map<String, Object>) context.get(DESCRIPTION_EN));
	}

	/**
	 * FeatureList
	 * @param retbody
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	private void buildFeatureList(RetBody retbody, Context context) {
		retbody.setFeatureList((Map<String, Object>) context.get(FEATURELIST));
	}

	/**
	 * Properties
	 * @param retbody
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	private void buildProperties(RetBody retbody, Context context) {
		retbody.setProperties((Map<String, Object>) context.get(PROPERTIES));
	}

	/**
	 * Category 
	 * @param retbody
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	private void buildCategory(RetBody retbody, Context context) {
		List<String> list=null;
		try {
			list=(List<String>)context.get(CATEGORY);
		} catch (ClassCastException e) {
			list=new ArrayList<String>(){
				{
					add((String)context.get(CATEGORY));
				}
			};
		}
		retbody.setCategory(list);
	}

	/**
	 * BreadCrumb
	 * @param retbody
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	private void buildBreadCrumb(RetBody retbody, Context context) {
		List<String> list=null;
		try {
			list=(List<String>)context.get(BREADCRUMB);
		} catch (ClassCastException e) {
			list=new ArrayList<String>(){
				{
					add((String)context.get(BREADCRUMB));
				}
			};
		}
		retbody.setBreadCrumb(list);
	}

	/**
	 * Brand
	 * @param retbody
	 * @param context
	 */
	private void buildBrand(RetBody retbody, Context context) {
		String en=(String) context.get(BRAND_EN);
		String cn=(String) context.get(BRAND_CN);
		String de=(String) context.get(BRAND_DE);
		String jp=(String) context.get(BRAND_JP);
		retbody.setBrand(new Brand(null==en?"":en, null==cn?"":cn, null==jp?"":jp, null==de?"":de));
	}

	/**
	 * Stock
	 * @param retbody
	 * @param context
	 */
	private void buildStock(RetBody retbody, Context context) {
		retbody.setStock((Stock)context.get(STOCK));
	}

	/**
	 * Price
	 * @param retbody
	 * @param context
	 */
	private void buildPrice(RetBody retbody, Context context) {
		retbody.setPrice((Price)context.get(PRICE));
	}

	/**
	 * Title
	 * @param retbody
	 * @param context
	 */
	private void buildTitle(RetBody retbody, Context context) {
		String en=(String) context.get(TITLE_EN);
		String cn=(String) context.get(TITLE_CN);
		String de=(String) context.get(TITLE_DE);
		String jp=(String) context.get(TITLE_JP);
		retbody.setTitle(new Title(null==en?"":en, null==cn?"":cn, null==jp?"":jp, null==de?"":de));
	}

	/**
	 * ProdUrl
	 * @param retbody
	 * @param context
	 */
	private void buildProdUrl(RetBody retbody, Context context) {
		String url = (String) context.get(PRODURL_URL);
		long discovery_time = Long.parseLong((String) context.get(PRODURL_DISCOVERY_TIME));
		String url_no = (String) context.get(PRODURL_URL_NO);
		retbody.setProdUrl(new ProdUrl(url,discovery_time,url_no));
	}

	/**
	 * Site
	 * @param retbody
	 * @param context
	 */
	private void buildSite(RetBody retbody, Context context) {
		retbody.setSite(new Site(String.valueOf(context.get(SITE_HOST))));
	}

	/**
	 * DOCID
	 * @param retbody
	 * @param context
	 */
	private void buildDOCID(RetBody retbody, Context context) {
		retbody.setDOCID(String.valueOf(context.get(DOCID)));
	}

}
