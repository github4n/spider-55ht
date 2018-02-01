package com.haitao55.spider.ui.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
/**
* @ClassName: UrlRuleAction
* @Description: url地址规则
* @author jerome
* @date 2017年4月13日
 */
@Controller
@RequestMapping("/urlRule")
public class UrlRuleAction extends BaseAction{
    
    @RequestMapping("/clean")
    public @ResponseBody String clean(String url) {
        Pattern pattern = Pattern.compile("(http://|https://)");
        Matcher matcher = pattern.matcher(url);
        if (StringUtils.isBlank(url) || !matcher.find()) {
            return "please type the valid url.";
        }
        return DetailUrlCleaningTool.getInstance().cleanDetailUrl(url);
    }
    
}
