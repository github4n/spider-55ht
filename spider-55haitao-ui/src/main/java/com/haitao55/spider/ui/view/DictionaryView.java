package com.haitao55.spider.ui.view;

public class DictionaryView {
	private Integer id;
	private String type;
	private String typeParam;
	private String nameParam;
	private String name;
	private String key;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
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
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public DictionaryView() {
		super();
	}
	
	public DictionaryView(Integer id, String type, String key, String value) {
		super();
		this.id = id;
		this.type = type;
		this.key = key;
		this.value = value;
	}
	@Override
	public String toString() {
		return "DictionaryView [id=" + id + ", type=" + type + ", key=" + key + ", value=" + value + "]";
	}
	
}
