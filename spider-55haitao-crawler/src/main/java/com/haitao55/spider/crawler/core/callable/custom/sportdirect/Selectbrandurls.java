package com.haitao55.spider.crawler.core.callable.custom.sportdirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class Selectbrandurls extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document document = Jsoup.parse(content);
		Elements grid = document.select("div .lettersBoxesColumn").select("div");
		for(Element brand:grid){
			String brand_name = brand.select("a").text().replace(" ", "").toUpperCase().split("\\(")[0];
			String brand_path = brand.select("a").attr("href");
			String url = "http://www.sportsdirect.com/DesktopModules/BrowseV2/API/BrowseV2Serv"+
				         "ice/GetProductsInformation?categoryName=SD_BRA"+brand_name+"&currentPage=1&productsP"+
				         "erPage=100&sortOption=rank&selectedFilters=&isSearch=false&descriptionFil"+
				         "ter=&columns=5&mobileColumns=2&clearFilters=false&pathName="+brand_path+"&searchTermCa"+
				         "tegory=&selectedCurrency=GBP";
			newUrlValues.add(url);
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Sportsdirect item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}

}
