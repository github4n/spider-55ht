package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;

import org.apache.commons.net.telnet.TelnetClient;

public class ConnectNetWork {
	
	private static final String KD_NAME = "kd";
	private static final String KD_USERNAME = "21007sads@dsf";
	private static final String KD_PASS = "5454";
	
	/** 
     * 执行CMD命令,并返回String字符串 
     */  
    public static String executeCmd(String strCmd) throws Exception {  
        Process p = Runtime.getRuntime().exec("cmd /c " + strCmd);  
        StringBuilder sbCmd = new StringBuilder();  
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));  
        String line;  
        while ((line = br.readLine()) != null) {  
            sbCmd.append(line + "\n");  
        }  
        return sbCmd.toString();  
    }  
  
    /** 
     * 连接ADSL 
     */  
    public static boolean connAdsl() throws Exception {  
        System.out.println("正在建立连接.");  
        String adslCmd = "rasdial " + KD_NAME + " " + KD_USERNAME + " "+ KD_PASS;  
        String tempCmd = executeCmd(adslCmd);  
        // 判断是否连接成功  
        if (tempCmd.indexOf("已连接") > 0) {  
            System.out.println("已成功建立连接.");  
            return true;  
        } else {  
            System.err.println(tempCmd);  
            System.err.println("建立连接失败");  
            return false;  
        }  
    }  
  
    /** 
     * 断开ADSL 
     */  
    public static boolean cutAdsl() throws Exception {  
        String cutAdsl = "rasdial " + KD_NAME + " /disconnect";  
        String result = executeCmd(cutAdsl);  
         
        if (result.indexOf("没有连接")!=-1){  
            System.err.println(KD_NAME + "连接不存在!");  
            return false;  
        } else {  
            System.out.println("连接已断开");  
            return true;  
        }  
    }  
    public static boolean restartAdsl() throws Exception{
    	boolean unconn = cutAdsl();
    	Thread.sleep(500);
    	boolean conn = connAdsl();
    	Thread.sleep(500);
    	/*//ConnectException
    	TelnetClient telnet = new TelnetClient();
    	//System.out.println(telnet.isConnected());
    	int count = 0;
    	while(!telnet.isConnected()){
    		try{
        		telnet.connect("104.197.229.122", 7912);
        	}catch(ConnectException e){
        		Thread.sleep(1000);
        		count++;
        		if(count > 2){
        			unconn = cutAdsl();
        	    	Thread.sleep(500);
        	    	conn = connAdsl();
        	    	Thread.sleep(2000);
        		}
        		continue;
        	}
    	}*/
    	boolean flag = unconn && conn;
    	if(flag){
    		System.out.println("restart adsl success");
    	} else {
    		System.out.println("restart adsl fail");
    	}
		return flag;
    }
    
     
    public static void main(String[] args) throws Exception {  
       /* connAdsl();  
        Thread.sleep(1000);  
        cutAdsl();  
        Thread.sleep(1000);  
        //再连，分配一个新的IP  
        connAdsl();  */
    }  

}
