package com.haitao55.spider.common.gson.bean.competitor;

import java.io.Serializable;
import java.util.List;

/**
 * 
* Title: Tag
* Description:json中的商品优惠码节点
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月21日 上午11:32:24
* @version 1.0
 */
public class PromoCode  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8668180341803874173L;
	/**
	 * 
	 */

	private List<String> codes;

	public PromoCode() {
		super();
	}

	public PromoCode(List<String> codes) {
		super();
		this.codes = codes;
	}



	public List<String> getCodes() {
		return codes;
	}

	public void setCodes(List<String> codes) {
		this.codes = codes;
	}

}
