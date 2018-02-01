package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;


public class Selection implements Serializable, Comparable<Object> {

	private static final long serialVersionUID = -4861860627233362570L;
	
	private long select_id;
	private String select_value;
	private String select_name;
	
	
	public Selection(){}
	
	public Selection(long select_id, String select_value, String select_name) {
		super();
		this.select_id = select_id;
		this.select_value = select_value;
		this.select_name = select_name;
	}
	public long getSelect_id() {
		return select_id;
	}
	public void setSelect_id(long select_id) {
		this.select_id = select_id;
	}
	public String getSelect_value() {
		return select_value;
	}
	public void setSelect_value(String select_value) {
		this.select_value = select_value;
	}
	public String getSelect_name() {
		return select_name;
	}
	public void setSelect_name(String select_name) {
		this.select_name = select_name;
	}
	@Override
	public String toString() {
		return "selection [select_id=" + select_id + ", select_value="
				+ select_value + ", select_name=" + select_name + "]";
	}
	
	@Override
	public int compareTo(Object o) {
		return Long.valueOf(this.getSelect_id()-((Selection)o).getSelect_id()).intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Selection other = (Selection) obj;
		
		if((this.getSelect_name() == null && other.getSelect_name() == null) && 
				(this.getSelect_value() == null && other.getSelect_value() == null)){
			return true;
		}
		
		if(this.getSelect_name() == null && other.getSelect_name() == null){
			return false;
		}else if(this.getSelect_name() == null && other.getSelect_name() != null) {
			return false;
		}else if(this.getSelect_name() != null && other.getSelect_name() == null){
			return false;
		}else if (!this.getSelect_name().equals(other.getSelect_name()))
			return false;
		
		
		if(this.getSelect_value() == null && other.getSelect_value() == null){
			return false;
		}else if (this.getSelect_value() == null && other.getSelect_value() != null) {
			return false;
		}else if(this.getSelect_value() != null && other.getSelect_value() == null){
			return false;
		}else if (!this.getSelect_value().equals(other.getSelect_value()))
			return false;
		
		
		return true;
	}
}
