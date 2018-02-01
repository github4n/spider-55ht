package com.haitao55.spider.crawler.core.callable;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * Title: Description: 用来处理 性别 Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2016年11月25日 上午10:39:34
 * @version 1.0
 */
public class Gender extends AbstractSelect {
	private static String SEX_WOMEN = "women";
	private static String SEX_WOMEN2 = "female";
	private static String SEX_WOMEN3 = "ladies";
	private static String SEX_MEN = "men";
	private static String SEX_MEN2 = "male";
	// css 选择器
	private String css;

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	@Override
	public void invoke(Context context) throws Exception {
		String gender = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(css)) {
			Document document = this.getDocument(context);
			gender = getSex(document.select(css).text());
		} else {
			String inputParam = this.getInputString(context);
			gender = getSex(inputParam);
		}
		setOutput(context,gender);
	}

	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN)) {
			gender = "women";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN2)) {
			gender = "women";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN3)) {
			gender = "women";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_MEN2)) {
			gender = "men";
		}
		return gender;
	}

}
