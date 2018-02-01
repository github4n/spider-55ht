package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;

/**
 * 
  * @ClassName: Site
  * @Description: json中的host节点
  * @author songsong.xu
  * @date 2016年9月20日 下午2:32:22
  *
 */
public class Site implements Serializable{
	
	private static final long serialVersionUID = 3934016757323543456L;
	private String host;
	
	public Site(String host) {
		super();
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public String toString() {
		return "Site [host=" + host + "]";
	}
}