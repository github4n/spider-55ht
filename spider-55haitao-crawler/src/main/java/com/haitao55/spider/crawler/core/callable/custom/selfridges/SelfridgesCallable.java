package com.haitao55.spider.crawler.core.callable.custom.selfridges;

import java.util.concurrent.Callable;

import com.haitao55.spider.crawler.utils.HttpUtils;

public class SelfridgesCallable implements Callable<String> {
	private String url;

	private String split;
	
	private boolean imageurlflag;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSplit() {
		return split;
	}

	public void setSplit(String split) {
		this.split = split;
	}

	public boolean isImageurlflag() {
		return imageurlflag;
	}

	public void setImageurlflag(boolean imageurlflag) {
		this.imageurlflag = imageurlflag;
	}

	public SelfridgesCallable(String url) {
		this.url = url;
	}
	public SelfridgesCallable(String url,String split,boolean imageurlflag) {
		this.url = url;
		this.split=split;
		this.imageurlflag=imageurlflag;
	}

	@Override
	public String call() throws Exception {
		String resultJson = HttpUtils.get(url, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES);
		if(imageurlflag){
			resultJson=resultJson.concat(split).concat(url);
		}
		return resultJson;
	}

}
