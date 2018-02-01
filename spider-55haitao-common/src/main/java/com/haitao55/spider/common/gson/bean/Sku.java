package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sku implements Serializable {

	private static final long serialVersionUID = 5415816294465125697L;

	private List<LSelectionList> l_selection_list;
	private List<LStyleList> l_style_list;

	public Sku() {
	}

	public Sku(List<LSelectionList> l_selection_list, List<LStyleList> l_style_list) {
		super();
		this.l_selection_list = l_selection_list;
		this.l_style_list = l_style_list;
	}

	public List<LSelectionList> getL_selection_list() {
		return l_selection_list;
	}

	public void setL_selection_list(List<LSelectionList> l_selection_list) {
		this.l_selection_list = l_selection_list;
	}

	public List<LStyleList> getL_style_list() {
		return l_style_list;
	}

	public void setL_style_list(List<LStyleList> l_style_list) {
		this.l_style_list = l_style_list;
	}

	@Override
	public String toString() {
		return "Sku [l_selection_list=" + l_selection_list + ", l_style_list=" + l_style_list + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((l_selection_list == null) ? 0 : l_selection_list.hashCode());
		result = prime * result + ((l_style_list == null) ? 0 : l_style_list.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		Sku other = (Sku) obj;

		if (this.l_selection_list == null && other.l_selection_list == null) {
			return true;
		} else if (this.l_selection_list == null && other.l_selection_list != null) {
			return false;
		} else if (this.l_selection_list != null && other.l_selection_list == null) {
			return false;
		} else if (this.l_selection_list.size() != other.l_selection_list.size()) {
			return false;
		}else if(this.l_selection_list.size() == 1 && other.l_selection_list.contains(this.l_selection_list.get(0))){
			return true;
		}
		

		Collections.sort(this.l_selection_list, new Comparator<LSelectionList>() {
			@Override
			public int compare(LSelectionList o1, LSelectionList o2) {
				return o1.compareTo(o2);
			}
		});
		
		Collections.sort(other.l_selection_list, new Comparator<LSelectionList>() {
			@Override
			public int compare(LSelectionList o1, LSelectionList o2) {
				return o1.compareTo(o2);
			}
		});

		return this.l_selection_list.equals(other.l_selection_list);
	}
}