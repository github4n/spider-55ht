package com.haitao55.spider.crawler.core.callable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * Title: Description: 用于封装featurelist Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2016年11月25日 上午11:18:29
 * @version 1.0
 */
public class FeatureList extends AbstractSelect {
	// css选择器
	private String css;

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	@Override
	public void invoke(Context context) throws Exception {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Document document = this.getDocument(context);
		Elements es = document.select(css);
		if (es != null && es.size() > 0) {
			int count = 1;
			for (Element e : es) {
				featureMap.put("feature-" + count, StringEscapeUtils.unescapeHtml(e.text().replaceAll("<[^>]*>", "")));
				count++;
			}
		}
		setOutput(context,featureMap);
	}

}
