package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @ClassName: LSelectionList
 * @Description: sku的属性引用
 * @author songsong.xu
 * @date 2016年9月20日 下午5:13:16
 *
 */
public class LSelectionList implements Serializable, Comparable<Object> {

	private static final long serialVersionUID = 797246454932650503L;
	private String style_id;
	private String goods_id;
	private String sku_id;
	private String price_unit;
	private int stock_status;
	private float orig_price;
	private float sale_price;
	private int stock_number;
	private List<Selection> selections;

	public LSelectionList() {
	};

	public String getStyle_id() {
		return style_id;
	}

	public void setStyle_id(String style_id) {
		this.style_id = style_id;
	}

	public String getGoods_id() {
		return goods_id;
	}

	public void setGoods_id(String goods_id) {
		this.goods_id = goods_id;
	}

	public String getSku_id() {
		return sku_id;
	}

	public void setSku_id(String sku_id) {
		this.sku_id = sku_id;
	}

	public String getPrice_unit() {
		return price_unit;
	}

	public void setPrice_unit(String price_unit) {
		this.price_unit = price_unit;
	}

	public int getStock_status() {
		return stock_status;
	}

	public void setStock_status(int stock_status) {
		this.stock_status = stock_status;
	}

	public float getOrig_price() {
		return orig_price;
	}

	public void setOrig_price(float orig_price) {
		this.orig_price = orig_price;
	}

	public float getSale_price() {
		return sale_price;
	}

	public void setSale_price(float sale_price) {
		this.sale_price = sale_price;
	}

	public int getStock_number() {
		return stock_number;
	}

	public void setStock_number(int stock_number) {
		this.stock_number = stock_number;
	}

	public List<Selection> getSelections() {
		return selections;
	}

	public void setSelections(List<Selection> selections) {
		this.selections = selections;
	}

	@Override
	public String toString() {
		return "LSelectionList [style_id=" + style_id + ", goods_id=" + goods_id + ", price_unit=" + price_unit
				+ ", stock_status=" + stock_status + ", orig_price=" + orig_price + ", sale_price=" + sale_price
				+ ", stock_number=" + stock_number + ", selections=" + selections + "]";
	}

	@Override
	public int compareTo(Object o) {
		return this.getGoods_id().compareTo(((LSelectionList) o).getGoods_id());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		LSelectionList other = (LSelectionList) obj;
		
		if (this.getStyle_id() == null && other.getStyle_id() != null) {
			return false;
		} else if (this.getStyle_id() != null && other.getStyle_id() == null) {
			return false;
		} else if (!this.getStyle_id().equals(other.getStyle_id()))
			return false;

		if(StringUtils.isBlank(other.getSku_id())){//mongo sku_id字段为空 return false;
			return false;
		}
		
		if (this.selections == null && other.selections == null) {
			return true;
		} else if (this.selections == null && other.selections != null) {
			return false;
		} else if (this.selections != null && other.selections == null) {
			return false;
		} else if (this.selections.size() != other.selections.size()) {
			return false;
		}

		Collections.sort(this.selections, new Comparator<Selection>() {
			@Override
			public int compare(Selection o1, Selection o2) {
				return o1.compareTo(o2);
			}
		});

		Collections.sort(other.getSelections(), new Comparator<Selection>() {
			@Override
			public int compare(Selection o1, Selection o2) {
				return o1.compareTo(o2);
			}
		});

		boolean flag = this.selections.equals(other.getSelections());
		if (flag) {
			if (this.getOrig_price() != other.getOrig_price() || this.getSale_price() != other.getSale_price()
					|| this.getStock_status() != other.getStock_status()
					|| this.getStock_number() != other.getStock_number()) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}
}
