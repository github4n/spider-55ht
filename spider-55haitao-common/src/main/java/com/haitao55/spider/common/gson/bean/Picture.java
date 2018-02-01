package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;

/**
 * 
  * @ClassName: Picture
  * @Description: json中的图片节点
  * @author songsong.xu
  * @date 2016年9月20日 下午2:06:09
  *
 */
public class Picture  implements Serializable{

	private static final long serialVersionUID = 895219187681661669L;
	private String src;
	private String cdn;
	private String Order;
	
	public Picture(String src) {
		super();
		this.src = src;
	}
	
	public Picture(String src, String cdn) {
		super();
		this.src = src;
		this.cdn = cdn;
	}
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getCdn() {
		return cdn;
	}
	public void setCdn(String cdn) {
		this.cdn = cdn;
	}
	
	
	public String getOrder() {
		return Order;
	}
	public void setOrder(String order) {
		Order = order;
	}
	@Override
	public String toString() {
		return "Picture [src=" + src + ", cdn=" + cdn + ", Order=" + Order
				+ "]";
	}
	
	
	
	
}
