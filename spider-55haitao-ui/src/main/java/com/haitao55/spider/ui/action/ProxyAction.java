package com.haitao55.spider.ui.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.pagehelper.PageInfo;
import com.haitao55.spider.ui.service.DictionaryService;
import com.haitao55.spider.ui.service.ProxyService;
import com.haitao55.spider.ui.view.DictionaryView;
import com.haitao55.spider.ui.view.ProxyView;

/**
 * 
 * 功能：任务管理的Action
 * 
 * @author Arthur.Liu
 * @time 2016年8月11日 下午4:05:20
 * @version 1.0
 */
@Controller
@RequestMapping("/proxy")
public class ProxyAction extends BaseAction {

	private static final Logger logger = LoggerFactory.getLogger(ProxyAction.class);
	@Value("#{configProperties['region']}")
	String region;
	
	public String getRegion() {
		return region;
	}

	@Autowired
	private ProxyService proxyService;
	

	@Autowired
	private DictionaryService dictionaryService;
	/**
	 * 获取所有代理  分页
	 * @return
	 */
	@RequestMapping("/getAllProxies")
	public String getAllProxies(HttpServletRequest request,HttpServletResponse response,Model model,ProxyView proxyView) {
		int page=getPage(request);
		int pageSize=getPageSize(request);
		List<ProxyView> proxyViewList = this.proxyService.getAllProxies(proxyView,page,pageSize);
//		this.getRequest().setAttribute("pageInfo", new PageInfo<ProxyView>(proxyViewList));
		model.addAttribute("pageInfo", new PageInfo<ProxyView>(proxyViewList));
		model.addAttribute("page", page);
		return "proxy/proxy-home";
	}

	/**
	 * 跳转到添加页面
	 * @return
	 */
	@RequestMapping("gotoCreateProxyPage")
	public String gotoCreateProxyPage(HttpServletRequest request,HttpServletResponse response,Model model,ProxyView proxyView) {
		DictionaryView dictionaryView=new DictionaryView();
		dictionaryView.setType(region);
		List<DictionaryView> dictionaryDetails = dictionaryService.getDictionaryDetailsNoPage(dictionaryView);
		model.addAttribute("proxyRegionList", dictionaryDetails);
		return "proxy/proxy-create";
	}

	/**
	 * 执行添加操作
	 * @return
	 */
	@RequestMapping("insertProxy")
	public String insertProxy(HttpServletRequest request,HttpServletResponse response,Model model,ProxyView proxyView) {
		this.proxyService.insertProxy(proxyView);
		return "redirect:getAllProxies.action";
	}

	/**
	 * 跳转到编辑页面
	 * @return
	 */
	@RequestMapping("gotoEditProxyPage")
	public String gotoEditProxyPage(HttpServletRequest request,HttpServletResponse response,Model model,ProxyView proxyView)  throws Exception{
		if (StringUtils.isBlank(proxyView.getId()+"")) {
			return "login";
		}
		ProxyView view=null;
		List<DictionaryView> dictionaryDetails=null;
		try {
			view=this.proxyService.selectProxyByMapper(proxyView);
			DictionaryView dictionaryView=new DictionaryView();
			dictionaryView.setType(region);
			dictionaryDetails = dictionaryService.getDictionaryDetailsNoPage(dictionaryView);
		} catch (Exception e) {
			logger.error("查询代理异常",e);
		}
		model.addAttribute("proxyRegionList", dictionaryDetails);
		model.addAttribute("proxyView", view);

		return "proxy/proxy-edit";
	}

	/**
	 * 执行编辑操作
	 * @return
	 */
	@RequestMapping("editProxy")
	public String editProxy(HttpServletRequest request,HttpServletResponse response,Model model,ProxyView proxyView) {
		this.proxyService.updateProxy(proxyView);
		return "redirect:getAllProxies.action";
	}
	
	
	/**
	 * 跳转到view页面，查看
	 * @return
	 */
	@RequestMapping("viewProxy")
	public String viewProxy(HttpServletRequest request,HttpServletResponse response,Model model,ProxyView proxyView)  throws Exception{
		if (StringUtils.isBlank(proxyView.getId()+"")) {
			return "login";
		}
		ProxyView view=null;
		List<DictionaryView> dictionaryDetails=null;
		try {
			view=this.proxyService.selectProxyByMapper(proxyView);
			DictionaryView dictionaryView=new DictionaryView();
			dictionaryView.setType(region);
			dictionaryDetails = dictionaryService.getDictionaryDetailsNoPage(dictionaryView);
		} catch (Exception e) {
			logger.error("查询代理异常",e);
		}
		model.addAttribute("proxyRegionList", dictionaryDetails);
		model.addAttribute("proxyView", view);

		return "proxy/proxy-view";
	}
	
	/**
	 * 删除
	 * @return
	 */
	@RequestMapping("doDeleteProxy")
	public String doDeleteProxy(HttpServletRequest request,HttpServletResponse response,Model model,ProxyView proxyView){
		if (StringUtils.isBlank(proxyView.getId()+"")) {
			return "login";
		}
		
		this.proxyService.delete(proxyView.getId());
		return "redirect:getAllProxies.action";
	}
}