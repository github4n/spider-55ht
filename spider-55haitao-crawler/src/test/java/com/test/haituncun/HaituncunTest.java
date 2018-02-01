package com.test.haituncun;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.HaiTunCunRetBody;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年3月8日 下午6:28:36  
 */
public class HaituncunTest {
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("/home/zhoushuo/zs/haituncun.csv"));
		String json = br.readLine();
		HaiTunCunRetBody retbody = JsonUtils.json2bean(json, HaiTunCunRetBody.class);
		System.out.println(retbody.getCategory());
		int count = 1;
		while(json != null){
			json = br.readLine();
			retbody = JsonUtils.json2bean(json, HaiTunCunRetBody.class);
			if(retbody == null){
				System.out.println(json);
				System.out.println("第"+count+"行");
			}
			count++;
		}
		System.out.println(count);
	}
}
