package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;

/**
 * 
  * @ClassName: Price
  * @Description: 爬取结果json中的价格节点
  * @author songsong.xu
  * @date 2016年9月20日 下午2:10:12
  *
 */
public class Price implements Serializable{

	private static final long serialVersionUID = 8743844709665950936L;
	
	private float orig;
	private int save;
	private float sale;
	private String unit;
	
	public Price(float orig, int save, float sale, String unit) {
		super();
		this.orig = orig;
		this.save = save;
		this.sale = sale;
		this.unit = unit;
	}
	
	
	public float getOrig() {
		return orig;
	}
	public void setOrig(float orig) {
		this.orig = orig;
	}
	public int getSave() {
		return save;
	}
	public void setSave(int save) {
		this.save = save;
	}
	public float getSale() {
		return sale;
	}
	public void setSale(float sale) {
		this.sale = sale;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return "Price [orig=" + orig + ", save=" + save + ", sale=" + sale
				+ ", unit=" + unit + "]";
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Price other = (Price) obj;
		
		if(this.getSale() != other.getSale() ||
				this.getOrig() != other.getOrig() || 
				!this.getUnit().equals(other.getUnit())){
			return false;
		}
		return true;
	}
}
