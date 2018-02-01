package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;

/**
 * 
  * @ClassName: Stock
  * @Description: json中的库存节点
  * @author songsong.xu
  * @date 2016年9月20日 下午2:10:41
  *
 */
public class Stock implements Serializable{
	
	private static final long serialVersionUID = -5109644065272348453L;
	private int status;
	private Integer number;
	public Stock(int status) {
		super();
		this.status = status;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public Stock(int status, Integer number) {
		super();
		this.status = status;
		this.number = number;
	}
	@Override
	public String toString() {
		return "Stock [status=" + status + ", number=" + number + "]";
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stock other = (Stock) obj;
		if(this.getStatus() != other.getStatus()){
			return false;
		}
		return true;
	}
	
}