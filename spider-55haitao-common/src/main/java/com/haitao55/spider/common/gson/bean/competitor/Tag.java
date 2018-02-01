package com.haitao55.spider.common.gson.bean.competitor;

import java.io.Serializable;

/**
 * 
* Title: Tag
* Description:json中的商品标签节点
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月21日 上午11:32:24
* @version 1.0
 */
public class Tag  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4472359833604917672L;
	/**
	 * 
	 */

	private String tag;

	public Tag() {
		super();
	}

	public Tag(String tag) {
		super();
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	public String toString() {
		return "Tag [tag=" + tag + "]";
	}
	
}
