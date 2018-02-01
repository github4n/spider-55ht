package com.haitao55.spider.chart.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.chart.entity.FullData;
import com.haitao55.spider.chart.entity.RealTimeCount;
import com.haitao55.spider.chart.entity.RealTimeSection;
import com.haitao55.spider.chart.service.FullDataService;
import com.haitao55.spider.chart.service.RealTimeCountService;
import com.haitao55.spider.chart.service.RealTimeSectionService;

/**
 * @Description:
 * @author: zhoushuo
 * @date: 2017年5月22日 下午2:19:40
 */
@Controller
public class StatisticsController {

	private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
	
	private static final SimpleCache<RealTimeCount> cache = new SimpleCache<>();

	@Autowired
	private RealTimeCountService realTimeCountService;

	@Autowired
	private RealTimeSectionService realTimeSectionService;

	@Autowired
	private FullDataService fullDataService;

	@RequestMapping("realtimeTimesPerMinites.action")
	@ResponseBody
	public String realtimeTimesPerMinites(String content) throws IOException {
		logger.info("=======content======:{}", content);
		JSONObject json = null;
		try {
			json = JSON.parseObject(content);
		} catch (Exception e) {
			System.err.println("json 解析异常");
			return "json is error, please check.";
		}
		if (json == null)
			return "json is null";
		List<RealTimeSection> list = new ArrayList<>();
		this.handleRealtimeTimes(list, json);
		if (realTimeSectionService.saveList(list) > 0)
			return "success";
		return "fail";
	}

	@RequestMapping("realtimeTotalPerMinites.action")
	@ResponseBody
	public String realtimeTotalPerMinites(String content) throws IOException, ParseException {
		logger.info("=======content======:{}", content);
		JSONObject json = null;
		try {
			json = JSON.parseObject(content);
		} catch (Exception e) {
			System.err.println("json 解析异常");
			return "json is error, please check.";
		}
		if (json == null)
			return "json is null";
		String currentTime = json.getString("currentMinute");
		int count = json.getInteger("currentMinuteCount");
		RealTimeCount model = new RealTimeCount();
		model.setCurrentCount(count);
		model.setCurrentTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(currentTime));
		RealTimeCount lastOne = cache.Get("lastOne");
		if(currentTime.contains("00:00")){
			model.setTotal(count);
		} else if(lastOne == null){
			logger.info("无法从缓存中取到上次的记录，如果程序不是第一次加载缓存，请检查！");
			lastOne = realTimeCountService.getNewestRecord().get(0);//如果从缓存里未取到，则从数据库里拿
			logger.info("成功从数据库取到数据，time:{},count:{},total:{}", lastOne.getCurrentTime(), lastOne.getCurrentCount(), lastOne.getTotal());
			model.setTotal(count + lastOne.getTotal());
		} else {
			model.setTotal(count+lastOne.getTotal());
		}
		if (realTimeCountService.save(model) > 0){
			cache.Put("lastOne", model);
			logger.info("成功将对象放入缓存,time:{},count:{},total:{}", model.getCurrentTime(), model.getCurrentCount(), model.getTotal());
			return "success";
		}
		else
			return "fail";
	}

	@RequestMapping("getTotalData.action")
	@ResponseBody
	public String getTotalData(String content) throws IOException {
		logger.info("=======content======:{}", content);
		JSONArray json = null;
		try {
			json = JSONObject.parseArray(content);
		} catch (Exception e) {
			System.err.println("json 解析异常");
			return "json is error, please check.";
		}
		if (json == null)
			return "json is null";
		if (fullDataService.saveList(this.handleWithFullData(json)) > 0)
			return "success";
		else
			return "fail";
	}

	//全量统计
	@RequestMapping("allcount.action")
	public @ResponseBody Map<String, Object> allcount() {
		List<FullData> datas = fullDataService.getByCondition(new FullData() {
			{
				setDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			}
		});
		if (CollectionUtils.isNotEmpty(datas)) {
			datas.sort(new Comparator<FullData>() {

				@Override
				public int compare(FullData o1, FullData o2) {
					return o2.getCount() - o1.getCount();
				}
			});
		} else {
			return null;
		}
		List<Integer> numbers = new ArrayList<>();
		List<String> hosts = new ArrayList<>();
		int total = 0;
		for (FullData data : datas) {
			int current = data.getCount();
			numbers.add(current);
			hosts.add(data.getDomain());
			total += current;
		}
		Map<String, Object> map = new HashMap<>();
		map.put("total", total);
		map.put("x", numbers.toArray());
		map.put("y", hosts.toArray());
		return map;
	}

	//时间段内核价统计
	@RequestMapping("realtimeCount.action")
	public @ResponseBody Map<String, Object> realtimeCount(String startTime, String endTime) {
		List<RealTimeSection> sections = null;
		if(StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)){
			sections = realTimeSectionService.getByDuring(startTime, endTime);
		} else {
			//默认显示当前分钟，如果当前分钟尚未入库，则显示上一分钟的数据
			String currentMinite = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
			String lastMinite = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(new Date().getTime() - 60000));
			sections = realTimeSectionService.getByCondition(new RealTimeSection() {
				{
					setCurrentTime(currentMinite);
				}
			});
			if (CollectionUtils.isEmpty(sections)) {
				sections = realTimeSectionService.getByCondition(new RealTimeSection() {
					{
						setCurrentTime(lastMinite);
					}
				});
			}
			if(CollectionUtils.isEmpty(sections)){
				logger.error("未获取到当前时间的统计数据，请检查！");
				return null;
			}
		}
		sections.sort(new Comparator<RealTimeSection>() {
			@Override
			public int compare(RealTimeSection o1, RealTimeSection o2) {
				return Integer.valueOf(o1.getTimeSection()) - Integer.valueOf(o2.getTimeSection());
			}
		});
		int total = 0;
		List<Integer> list = new ArrayList<>();
		for (RealTimeSection rs : sections) {
			list.add(rs.getCount());
			total += rs.getCount();
		}
		Map<String, Object> map = new HashMap<>();
		map.put("total", total);
		map.put("x", list.toArray());
		return map;
	}

	//每分钟实时核价走势
	@RequestMapping("realtimePerCount.action")
	public @ResponseBody String realtimePerCount(String startTime, String endTime) throws ParseException {
		List<RealTimeCount> list = null;
		if(StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			list = realTimeCountService.getByTime(sdf.parse(startTime), sdf.parse(endTime));
		} else 
			list = realTimeCountService.getCurrentDayDate();
		StringBuilder sb = new StringBuilder("[");
		if (CollectionUtils.isNotEmpty(list)) {
			int len = list.size();
			for (int i = 0; i < len; i++) {
				sb.append("[").append(list.get(i).getCurrentTime().getTime()).append(",")
						.append(list.get(i).getCurrentCount()).append("]");
				if (i != len - 1)
					sb.append(",");
				else
					sb.append("]");
			}
		}
		return sb.toString();
	}

	//实时核价总量走势
	@RequestMapping("realtimeTotal.action")
	public @ResponseBody String realtimeTotal(String startTime, String endTime) throws ParseException {
		List<RealTimeCount> list = null;
		if(StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			list = realTimeCountService.getByTime(sdf.parse(startTime), sdf.parse(endTime));
		} else 
			list = realTimeCountService.getCurrentDayDate();
		StringBuilder sb = new StringBuilder("[");
		if (CollectionUtils.isNotEmpty(list)) {
			int len = list.size();
			for (int i = 0; i < len; i++) {
				sb.append("[").append(list.get(i).getCurrentTime().getTime()).append(",")
						.append(list.get(i).getTotal()).append("]");
				if (i != len - 1)
					sb.append(",");
				else
					sb.append("]");
			}
		}
		return sb.toString();
	}
	
	@RequestMapping("viewAll.action")
	public @ResponseBody Map<String, Object> viewAll(){
		Map<String, Object> map = new HashMap<>();
		map.put("fullData", this.getTotalData());
		map.put("realtime", this.getRealTimeData());
		map.put("realtimePerCount", this.getRealTimePerCountData());
		map.put("realtimeTotalCount", this.getRealTimeTotalCountData());
		return map;
	}

	private void handleRealtimeTimes(List<RealTimeSection> list, JSONObject json) {
		String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
		for (int i = 1; i <= 11; i++) {
			RealTimeSection section = new RealTimeSection();
			section.setCurrentTime(currentTime);
			if (i == 11) {
				section.setTimeSection("999");
				section.setCount(json.getInteger("999"));
			} else {
				section.setTimeSection(i + "");
				section.setCount(json.getInteger(String.valueOf(i)));
			}
			list.add(section);
		}
	}

	private List<FullData> handleWithFullData(JSONArray json) {
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		List<FullData> list = new ArrayList<>();
		int len = json.size();
		for (int i = 0; i < len; i++) {
			JSONObject obj = json.getJSONObject(i);
			String key = obj.keySet().iterator().next();
			FullData data = new FullData();
			data.setDate(date);
			data.setDomain(key);
			data.setCount(obj.getIntValue(key));
			list.add(data);
		}
		return list;
	}
	
	private Map<String, Object> getTotalData(){
		List<FullData> datas = fullDataService.getByCondition(new FullData() {
			{
				setDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			}
		});
		if (CollectionUtils.isNotEmpty(datas)) {
			datas.sort(new Comparator<FullData>() {

				@Override
				public int compare(FullData o1, FullData o2) {
					return o2.getCount() - o1.getCount();
				}
			});
		} else {
			return null;
		}
		List<Integer> numbers = new ArrayList<>();
		List<String> hosts = new ArrayList<>();
		int total = 0;
		for (FullData data : datas) {
			int current = data.getCount();
			numbers.add(current);
			hosts.add(data.getDomain());
			total += current;
		}
		Map<String, Object> map = new HashMap<>();
		map.put("total", total);
		map.put("x", numbers.toArray());
		map.put("y", hosts.toArray());
		return map;
	}
	
	private Map<String, Object> getRealTimeData(){
		String currentMinite = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
		String lastMinite = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(new Date().getTime() - 60000));
		List<RealTimeSection> sections = realTimeSectionService.getByCondition(new RealTimeSection() {
			{
				setCurrentTime(currentMinite);
			}
		});
		if (CollectionUtils.isEmpty(sections)) {
			sections = realTimeSectionService.getByCondition(new RealTimeSection() {
				{
					setCurrentTime(lastMinite);
				}
			});
		}
		if(CollectionUtils.isEmpty(sections)){
			logger.error("未获取到当前时间的统计数据，请检查！");
			return null;
		}
		sections.sort(new Comparator<RealTimeSection>() {
			@Override
			public int compare(RealTimeSection o1, RealTimeSection o2) {
				return Integer.valueOf(o1.getTimeSection()) - Integer.valueOf(o2.getTimeSection());
			}
		});
		int total = 0;
		List<Integer> list = new ArrayList<>();
		for (RealTimeSection rs : sections) {
			list.add(rs.getCount());
			total += rs.getCount();
		}
		Map<String, Object> map = new HashMap<>();
		map.put("total", total);
		map.put("x", list.toArray());
		return map;
	}
	
	private String getRealTimePerCountData(){
		StringBuilder sb = new StringBuilder("[");
		List<RealTimeCount> list = realTimeCountService.getCurrentDayDate();
		if (CollectionUtils.isNotEmpty(list)) {
			int len = list.size();
			for (int i = 0; i < len; i++) {
				sb.append("[").append(list.get(i).getCurrentTime().getTime()).append(",")
						.append(list.get(i).getCurrentCount()).append("]");
				if (i != len - 1)
					sb.append(",");
				else
					sb.append("]");
			}
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
	
	private String getRealTimeTotalCountData(){
		StringBuilder sb = new StringBuilder("[");
		List<RealTimeCount> list = realTimeCountService.getCurrentDayDate();
		if (CollectionUtils.isNotEmpty(list)) {
			int len = list.size();
			for (int i = 0; i < len; i++) {
				sb.append("[").append(list.get(i).getCurrentTime().getTime()).append(",")
						.append(list.get(i).getTotal()).append("]");
				if (i != len - 1)
					sb.append(",");
				else
					sb.append("]");
			}
		}
		System.out.println(sb.toString());
		return sb.toString();
	}

	public static void main(String[] args) throws InterruptedException, ParseException {
		String content = "{\"currentMinute\":\"2017-05-31 11:09\", \"currentMinuteCount\":\"63\"}";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
		JSONObject obj = JSON.parseObject(content);
		RealTimeCount model = new RealTimeCount();
		model.setCurrentCount(obj.getInteger("currentMinuteCount"));
		model.setCurrentTime(sdf.parse(obj.getString("currentMinute")));
		System.out.println(model.getCurrentCount());
		System.out.println(model.getCurrentTime());

		String currentMinite = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
		String lastMinite = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(new Date().getTime() - 60000));
		System.out.println(currentMinite);
		System.out.println(lastMinite);
	}
}
