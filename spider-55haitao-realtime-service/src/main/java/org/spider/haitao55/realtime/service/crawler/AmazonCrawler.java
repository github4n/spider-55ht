package org.spider.haitao55.realtime.service.crawler;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.crawler.core.callable.custom.amazon.api.AmazonAWSKeyPool;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;

public abstract class AmazonCrawler {
	
	public static AmazonAWSKeyPool awsKeyPool;
	
	static {
		try {
			awsKeyPool = new AmazonAWSKeyPool();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ChromeDriver getDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("no-sandbox");
		options.addArguments("start-maximized");
		options.setBinary("/usr/bin/google-chrome-stable");
		options.addExtensions(new File(System.getProperty("user.dir")+"/config/block-image_1_1.crx"));
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		ChromeDriver dr = new ChromeDriver(capabilities);
		dr.manage().window().maximize();
		return dr;
	}
	
	protected int waitForComplete(WebDriver webDriver) {
		String stateJs = " return document.readyState; ";
		JavascriptExecutor js = ((JavascriptExecutor) webDriver);
		String onload = js.executeScript(stateJs).toString();
		int ii = 10;
		int statu = 0;
		while (!"complete".equalsIgnoreCase(onload) && ii >= 0) {
			onload = js.executeScript(stateJs).toString();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("firefox url >>>" + webDriver.getCurrentUrl()
					+ " >>>statu>>>>>>>>>>>" + onload);
			ii--;
			// 浏览器如果超过时间限制就刷新,webDriver.navigate().refresh();
			if (ii % 5 == 0) {// 刷新浏览器
				webDriver.navigate().refresh();
			}
			if (ii == 0) {
				statu = -1;
			}
		}
		try {
			statu = 1;
		} catch (Exception e) {
			e.printStackTrace();
			return statu;
		}
		return statu;
	}
	

	protected RetBody check(RetBody retBody) {
		if(retBody.getSku() == null){
			return retBody;
		}
		List<LSelectionList> l_selection_list = retBody.getSku().getL_selection_list();
		List<LStyleList> l_style_list = retBody.getSku().getL_style_list();
		LStyleList defaultLStyleList = null;
		// find display = true
		for (LStyleList lStyleList : l_style_list) {
			boolean display = lStyleList.isDisplay();
			if (display) {
				defaultLStyleList = lStyleList;
				break;
			}
		}
		// 可能无默认的 LStyleList 全部都是 display = false
		if (l_style_list.size() > 0 && defaultLStyleList == null) {
			defaultLStyleList = l_style_list.get(0);
			defaultLStyleList.setDisplay(true);
		}
		// check stock is or not
		boolean stockFlag = false;
		if (defaultLStyleList != null) {
			for (LSelectionList selectionList : l_selection_list) {
				if (StringUtils.equals(selectionList.getStyle_id(),defaultLStyleList.getStyle_id())) {
					int status = selectionList.getStock_status();
					if (status > 0) {
						stockFlag = stockFlag || true;
						// 虽然默认sku有库存，仍然检查spu属性是否符合条件 不符合条件 主动修正
						if (retBody.getStock() == null
								|| retBody.getStock().getStatus() == 0) {
							retBody.setStock(new Stock(selectionList
									.getStock_status()));
							float orig = selectionList.getOrig_price();
							float sale = selectionList.getSale_price();
							String unit = selectionList.getPrice_unit();
							int save = Math.round((1 - sale / orig) * 100);// discount
							retBody.setPrice(new Price(orig, save, sale, unit));
						}
						if (retBody.getPrice() == null) {
							float orig = selectionList.getOrig_price();
							float sale = selectionList.getSale_price();
							String unit = selectionList.getPrice_unit();
							int save = Math.round((1 - sale / orig) * 100);// discount
							retBody.setPrice(new Price(orig, save, sale, unit));
						}
						if(retBody.getImage() == null){
							retBody.setImage(new LImageList(defaultLStyleList.getStyle_images()));
						}
						break;
					} else {
						stockFlag = stockFlag || false;
					}
				}
			}
		}
		// adjust display sku while stock is not
		if (!stockFlag && defaultLStyleList != null) {
			for (LStyleList lStyleList : l_style_list) {
				boolean stock = false;
				LSelectionList lSelectionList = null;
				for (LSelectionList selectionList : l_selection_list) {
					if (StringUtils.equals(selectionList.getStyle_id(),lStyleList.getStyle_id())) {
						int status = selectionList.getStock_status();
						if (status > 0) {
							stock = stock || true;
							lSelectionList = selectionList;
							break;
						} else {
							stock = stock || false;
						}
					}
				}
				if (stock) {
					float orig = lSelectionList.getOrig_price();
					float sale = lSelectionList.getSale_price();
					String unit = lSelectionList.getPrice_unit();
					int save = Math.round((1 - sale / orig) * 100);// discount
					retBody.setPrice(new Price(orig, save, sale, unit));
					lStyleList.setDisplay(true);
					defaultLStyleList.setDisplay(false);
					retBody.setStock(new Stock(lSelectionList.getStock_status()));
					retBody.setImage(new LImageList(lStyleList.getStyle_images()));
					break;
				}
			}
		}
		if (retBody.getStock() != null && retBody.getStock().getStatus() == 0) {// 下架
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"url is offline...");
		}
		return retBody;
	}

}
