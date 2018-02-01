package com.test.rebeccaminkoff;

import java.util.List;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月22日 上午9:53:56  
 */
public class Size {
	
	private String id;
	private String value;
	private List<String> skuids;
	
	public String getId() {
		return id;
	}
	public String getValue() {
		return value;
	}
	public List<String> getSkuids() {
		return skuids;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setSkuids(List<String> skuids) {
		this.skuids = skuids;
	}
	
}
