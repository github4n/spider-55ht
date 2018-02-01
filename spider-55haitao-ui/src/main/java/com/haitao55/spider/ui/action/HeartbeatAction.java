/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: HeartbeatAction.java 
 * @Prject: spider-55haitao-ui
 * @Package: com.haitao55.spider.ui.action 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年9月18日 上午10:54:38 
 * @version: V1.0   
 */
package com.haitao55.spider.ui.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.pagehelper.PageInfo;
import com.haitao55.spider.ui.service.HeartbeatService;
import com.haitao55.spider.ui.view.HeartbeatView;

/**
 * @ClassName: HeartbeatAction
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年9月18日 上午10:54:38
 */
@Controller
@RequestMapping("/heartbeat")
public class HeartbeatAction extends BaseAction {

	@Autowired
	private HeartbeatService heartbeatService;

	@RequestMapping("getAllLatestHeartbeat")
	public String getAllLatestHeartbeat(HttpServletRequest request, HttpServletResponse response, Model model) {
		List<HeartbeatView> listView = this.heartbeatService.getAllLatestHeartbeat(getPage(request),
				getPageSize(request));

		if (CollectionUtils.isNotEmpty(listView)) {
			for (HeartbeatView view : listView) {
				long time = System.currentTimeMillis() - view.getAccurateTime();
				if (time < 1000 * 60 * 3) {// 3分钟
					view.setVitalSign("alive");
				} else {
					view.setVitalSign("dead");
				}
			}
		}

		model.addAttribute("pageInfo", new PageInfo<HeartbeatView>(listView));
		return "heartbeat/heartbeat-home";
	}
}