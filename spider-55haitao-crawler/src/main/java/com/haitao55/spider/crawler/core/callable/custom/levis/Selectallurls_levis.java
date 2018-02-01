package com.haitao55.spider.crawler.core.callable.custom.levis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class Selectallurls_levis extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		Document document = Jsoup.parse(content);
		int p_num = Integer.valueOf(document.select("span[class=productCount]").text());
		int page_num = p_num/12;
		String[] urlStrings  = new String[]{"%2FUS%2Fen_US%2Fcategory%2Fmen%2Fclothing%2Fall%2F_%2FN-2sZ1z13"
				                            + "wzsZ8azZ1z13x71Z1z140oj%3Fab%3DHome_carousel_prom"
				                            + "o_springpromo_30ff150_shopm_041817",
				                            "%2FUS%2Fen_US%2Fcategory%2Fwomen%2Fclothing%2Fall%2F"
				                            + "_%2FN-2sZ1z13wzsZ8bgZ1z13x71Z1z140oj%3Fab"+
                                            "%3DHome_carousel_promo_springpromo_30ff150_shopw_041817",
                                            "%2FUS%2Fen_US%2Fcategory%2Fkids%2Fall%2F_%2F"
                                            + "N-2sZ1z13wzsZ8alZ1z13x71Z1z140oj%3F"
                                            + "ab%3DHome_carousel_promo_springprom"
                                            + "o_30ff150_shopk_041817"
		                                    };
		String current_urlString = "";
		if(context.getCurrentUrl().contains("women")){
			current_urlString = urlStrings[0];
		}
		else if(context.getCurrentUrl().contains("men")){
			current_urlString = urlStrings[1];
		}
		else if(context.getCurrentUrl().contains("kids")){
			current_urlString = urlStrings[2];
		}
		List<String> newUrlValues = new ArrayList<String>();
		for(int i=1;i<=page_num;++i){
			String _url =  "http://www.levi.com/US/en_US/includes/searchResultsScroll/?nao="+String.valueOf(i*12)+"&url="+current_urlString;
			newUrlValues.add(_url);
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Levis list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}

}
