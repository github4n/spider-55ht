package com.haitao55.spider.ui.action;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.haitao55.spider.common.dao.HaiTunCunItemDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.HaiTunCunRetBody;
import com.haitao55.spider.crawler.common.cache.TaskCache;
import com.haitao55.spider.crawler.core.model.Rule;
import com.haitao55.spider.crawler.core.model.Task;

/**
 * @Description:海豚村接口
 * @author: zhoushuo
 * @date: 2017年3月6日 上午10:35:40
 */
@Controller
@RequestMapping("/haituncun")
public class HaiTunCunAction extends BaseAction {
	
	private static final Logger logger = LoggerFactory.getLogger(HaiTunCunAction.class);
	
	private int page = 1;
	private int pageSize = 100;
	
	
	@Resource
	private HaiTunCunItemDAO haituncunDao;

	@RequestMapping(method=RequestMethod.POST, value="/getData.action")
	public void getData(String user, String password, HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");//设置字符流编码格式
		try {
			PrintWriter pw = response.getWriter();
			BufferedWriter bw = new BufferedWriter(pw);
			//1.检测是否拥有获取海豚村数据的权限
			if(!("55haitao".equals(user) && "123".equals(password))){
				response.setContentType("text/html;charset=UTF-8");//通知浏览器用那种UTF-8编码进行解析
				pw.println("用户名或密码错误");
				logger.error("用户名或密码错误,user:{},password:{}",user, password);
				return;
			}
			response.setHeader("Content-Disposition", "attachment;filename=haituncun.txt");
			response.setContentType("multipart/form-data");
			Long count = this.haituncunDao.countItems();
			page = (int)(count/pageSize) + 1;
			List<ItemDO> itemDOs = null;
			int sum = 0;
			for(int i=0; i<page; i++){
				itemDOs = this.haituncunDao.queryCompartiotorItemsByTaskId(i*pageSize, pageSize);
				for(ItemDO item : itemDOs){
					CrawlerJSONResult result = JsonUtils.json2bean(item.getValue(), CrawlerJSONResult.class);
					HaiTunCunRetBody retBody = result.getHtcRetBody();
					bw.write(JsonUtils.bean2json(retBody));
					bw.newLine();
					sum++;
				}
				bw.flush();
			}
			IOUtils.closeQuietly(bw);
			logger.info("=============================成功写入商品数量："+sum+"=========================");
		} catch (IOException e) {
			logger.error("IO异常导致海豚村获取数据失败！");
		}
	}
}
