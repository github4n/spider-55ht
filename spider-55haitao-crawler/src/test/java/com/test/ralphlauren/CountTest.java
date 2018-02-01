package com.test.ralphlauren;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月18日 上午10:27:19  
 */
public class CountTest {
	public static void main(String[] args) throws Exception{
		Set<String> specialHomeSet = new HashSet<>();
		Set<String> customMadeSet = new HashSet<>();
		BufferedReader br = new BufferedReader(new FileReader("/home/zhoushuo/specialHome.txt"));
		BufferedReader brmade = new BufferedReader(new FileReader("/home/zhoushuo/customMade.txt"));
		String line = br.readLine();
		while(line!=null){
			specialHomeSet.add(StringUtils.substringAfterLast(line, "url").trim());
			line = br.readLine();
		}
		br.close();
		String lineMade = brmade.readLine();
		while(lineMade!=null){
			customMadeSet.add(StringUtils.substringAfterLast(lineMade, "url").trim());
			lineMade = brmade.readLine();
		}
		brmade.close();
		System.out.println(specialHomeSet.size());
		System.out.println(customMadeSet.size());
		int count = 0;
		for(String ss : customMadeSet){
			for(String s : specialHomeSet){
				if(ss.equals(s)){
					System.err.println(s);
					count++;
				}
			}
		}
		System.out.println(count);
	}
}
