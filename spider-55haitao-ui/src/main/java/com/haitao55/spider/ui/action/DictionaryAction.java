package com.haitao55.spider.ui.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.pagehelper.PageInfo;
import com.haitao55.spider.ui.service.DictionaryService;
import com.haitao55.spider.ui.view.DictionaryView;

/**
 * 
* Title: 系统字典action
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年9月1日 上午9:56:30
* @version 1.0
 */
@Controller
@RequestMapping("/dictionary")
public class DictionaryAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(DictionaryAction.class);
	@Autowired
	private DictionaryService dictionaryService;
	
	@RequestMapping("/getDictionaries")
	public String getDictionaries(HttpServletRequest request,HttpServletResponse response,DictionaryView dictionaryView,Model model ){
		int page=getPage(request);
		int pageSize=getPageSize(request);
		List<DictionaryView> list=dictionaryService.getDictionaries(dictionaryView,page,pageSize);
		model.addAttribute("pageInfo", new PageInfo<DictionaryView>(list));
		model.addAttribute("page", page);
		return "/dictionary/dictionary-home";
	}
	
	@RequestMapping(value="/gotoCreateDictionaryPage",method=RequestMethod.GET)
	public String gotoCreateDictionaryPage(HttpServletRequest request,HttpServletResponse response ){
		return "/dictionary/dictionary-create";
	}
	@RequestMapping(value="/insertDictionary",method=RequestMethod.POST)
	public String insertDictionary(HttpServletRequest request,HttpServletResponse response,DictionaryView dictionaryView,Model model ){
		dictionaryService.insertDictionary(dictionaryView);
		return "redirect:getDictionaries.action";
	}
	@RequestMapping(value="/gotoEditDictionaryPage",method=RequestMethod.GET)
	public String gotoEditDictionaryPage(HttpServletRequest request,HttpServletResponse response,DictionaryView dictionaryView,Model model ){
		if (StringUtils.isBlank(dictionaryView.getId()+"")) {
			return "login";
		}
		DictionaryView view=null;
		try {
			view=this.dictionaryService.selectDictionaryByMapper(dictionaryView);
		} catch (Exception e) {
			logger.error("查询代理异常",e);
		}
		model.addAttribute("dictionaryView", view);
		return "/dictionary/dictionary-edit";
	}
	@RequestMapping(value="/editDictionary",method=RequestMethod.POST)
	public String editDictionary(HttpServletRequest request,HttpServletResponse response,DictionaryView dictionaryView,Model model ){
		dictionaryService.updateDictionary(dictionaryView);
		return "redirect:getDictionaries.action";
	}
	@RequestMapping(value="/doDeleteDictionary")
	public String doDeleteDictionary(HttpServletRequest request,HttpServletResponse response,DictionaryView dictionaryView,Model model ){
		dictionaryService.deleteDictionary(dictionaryView);
		return "redirect:getDictionaries.action";
	}
	@RequestMapping("/getDictionaryDetails")
	public String getDictionaryDetails(HttpServletRequest request,HttpServletResponse response,DictionaryView dictionaryView,Model model ){
		int page=getPage(request);
		int pageSize=getPageSize(request);
		List<DictionaryView> list=dictionaryService.getDictionaryDetails(dictionaryView,page,pageSize);
		model.addAttribute("pageInfo", new PageInfo<DictionaryView>(list));
		model.addAttribute("page", page);
		model.addAttribute("type", dictionaryView.getType());
		return "/dictionary/detail/dictionary-detail-home";
	}
	@RequestMapping(value="/gotoCreateDictionaryDetailPage",method=RequestMethod.GET)
	public String gotoCreateDictionaryDetailPage(HttpServletRequest request,HttpServletResponse response ,DictionaryView dictionaryView,Model model ){
		model.addAttribute("dictionaryView", dictionaryView);
		return "/dictionary/detail/dictionary-detail-create";
	}
	@RequestMapping(value="/insertDictionaryDetail",method=RequestMethod.POST)
	public String insertDictionaryDetail(HttpServletRequest request,HttpServletResponse response,DictionaryView dictionaryView,RedirectAttributes attr){
		dictionaryService.insertDictionary(dictionaryView);
		attr.addAttribute("type",dictionaryView.getType());
		return "redirect:getDictionaryDetails.action";
	}
	@RequestMapping(value="/gotoEditDictionaryDetailPage",method=RequestMethod.GET)
	public String gotoEditDictionaryDetailPage(HttpServletRequest request,HttpServletResponse response,DictionaryView dictionaryView,Model model ){
		if (StringUtils.isBlank(dictionaryView.getId()+"")) {
			return "login";
		}
		DictionaryView view=null;
		try {
			view=this.dictionaryService.selectDictionaryByMapper(dictionaryView);
		} catch (Exception e) {
			logger.error("查询代理异常",e);
		}
		model.addAttribute("dictionaryView", view);
		return "/dictionary/detail/dictionary-detail-edit";
	}
	@RequestMapping(value="/editDictionaryDetail",method=RequestMethod.POST)
	public String editDictionaryDetail(HttpServletRequest request,HttpServletResponse response,DictionaryView dictionaryView,RedirectAttributes attr ){
		dictionaryService.updateDictionaryDetail(dictionaryView);
		attr.addAttribute("type",dictionaryView.getType());
		return "redirect:getDictionaryDetails.action";
	}
	@RequestMapping(value="/doDeleteDictionaryDetail")
	public String doDeleteDictionaryDetail(HttpServletRequest request,HttpServletResponse response,DictionaryView dictionaryView,RedirectAttributes attr){
		dictionaryService.doDeleteDictionaryDetail(dictionaryView);
		attr.addAttribute("type",dictionaryView.getType());
		return "redirect:getDictionaryDetails.action";
	}
}
