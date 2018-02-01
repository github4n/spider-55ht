package com.haitao55.spider.view;

public class DomainCounter implements Comparable<DomainCounter>{

	private String domain;
	
	private long count;
	
	public DomainCounter(){
		
	}
	
	public DomainCounter(String domain,Long count){
		this.domain = domain;
		this.count = count;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	@Override
	public int compareTo(DomainCounter o) {
		if(this.count > o.getCount()){
			return (int) (this.count - o.getCount());
		}
		if (this.count < o.getCount()) {
			return (int)(this.count - o.getCount());
		}
		return 0;
	}
}
