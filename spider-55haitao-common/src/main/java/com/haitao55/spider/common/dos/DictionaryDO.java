package com.haitao55.spider.common.dos;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 
* Title:数据字典  do
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年9月1日 上午10:07:06
* @version 1.0
 */
@Table(name="dictionary")
public class DictionaryDO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Transient
	private String typeParam;
	@Column
	private String type;
	@Transient
	private String nameParam;
	@Column
	private String name;
	@Column
	private String value;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTypeParam() {
		return typeParam;
	}
	public void setTypeParam(String typeParam) {
		this.typeParam = typeParam;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getNameParam() {
		return nameParam;
	}
	public void setNameParam(String nameParam) {
		this.nameParam = nameParam;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public DictionaryDO(Integer id, String type, String value) {
		super();
		this.id = id;
		this.type = type;
		this.value = value;
	}
	
	public DictionaryDO(Integer id, String type) {
		super();
		this.id = id;
		this.type = type;
	}
	public DictionaryDO() {
		super();
	}
	@Override
	public String toString() {
		return "DictionaryView [id=" + id + ", type=" + type + ", value=" + value + "]";
	}
	
}
