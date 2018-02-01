package com.haitao55.spider.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import main.java.com.UpYun;

public class SpiderStringUtil {

    public static boolean include(String str, String search){
        int index = StringUtils.indexOf(str, search);
        return index >= 0;
    }
    
    public static boolean equal(String str, String search){
        return StringUtils.equals(str, search);
    }
    
    public static boolean startWith(String str, String search){
        return StringUtils.startsWith(str, search);
    }
    
    public static boolean endWith(String str, String search){
        return StringUtils.endsWith(str, search);
    }
    
    public static boolean notEqual(String str, String search){
        return !SpiderStringUtil.equal(str, search);
    }
    
    public static boolean notInclude(String str, String search){
        return !SpiderStringUtil.include(str, search);
    }
    
    public static boolean notStartWith(String str, String search){
        return !SpiderStringUtil.startWith(str, search);
    }
    
    public static boolean notEndWith(String str, String search){
        return !SpiderStringUtil.endWith(str, search);
    }
    
    public static String urlEncode(String s){
        try {
            s = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }
    
    public static String urlDecode(String s){
        try {
            s = URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }
    
    public static String escapeRegex(String s){
        s = StringUtils.replace(s, ".", "\\.");
        s = StringUtils.replace(s, "?", "\\?");
        s = StringUtils.replace(s, "{", "\\{");
        s = StringUtils.replace(s, "}", "\\}");
        return s;
    }
    
    public static String checkBlank(String s, String defaultStr){
        if(StringUtils.isBlank(s)){
            return defaultStr;
        } else {
            return s;
        }
    }
    
    public static String checkBlank(String s){
        return checkBlank(s, "-");
    }
    
    /**
     * 对字符串做MD5编码运算
     * 
     * @param str
     * @return
     */
    public static String md5Encode(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }

        try {
            byte[] byteArray = str.getBytes("UTF-8");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md5.digest(byteArray);

            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }

            return hexValue.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public static String getAmazonDomain(String url){
		Pattern pattern = Pattern.compile("(https://|http://)www.(.*?)/.*");
		Matcher matcher = pattern.matcher(url);
		if(matcher.find()){
			return matcher.group(2);
		}
		return null;
	}
    
    public static String getAmazonItemId(String url){
    	Pattern p = Pattern.compile("www.*/dp/(.*?)/");
		Matcher m = p.matcher(url);
		if(m.find()){
			return  m.group(1);
		}
		return null;
	}
    
    
    public static String upYunFileName(String url){
        
        String md5 = StringUtils.substring(UpYun.md5(url), 0, 16);
        if(StringUtils.isNotBlank(md5)){
            long value = NumberUtils.hexToLong(md5.getBytes());
            return new String(NumberUtils.longToHex(value));
        }
        
        //replaced by xusongsong see above
        
    	/*String md5Encode = SpiderStringUtil.md5Encode(url).substring(0, 16);
		long parseLong = Long.parseUnsignedLong(md5Encode,16);
//		long a=0x7fffffffffffffffl;
		long b=Long.parseLong("7fffffffffffffff",16);
		long l = parseLong & b;
		String hexString = Long.toHexString(l);*/
		return null;
    }
    
    
    public static String replaceHtmlToSpace(String content){
    	if(StringUtils.isNotBlank(content)){
    		String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式 
            Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE); 
            Matcher m_html=p_html.matcher(content); 
            return m_html.replaceAll(""); 
    	}
    	return null;
    }
    
    public static void main(String[] args) {
        String s = "0x7FFFFFFFFFFFFFFF";
        System.out.println(Long.toHexString(0111)); 
        System.out.println(upYunFileName("https://slimages.macysassets.com/is/image/MCY//products/7/optimized/8196197_fpx.tif?op_sharpen=1&wid=400&hei=489&fit=fit,1&$filterlrg$") ); 
    	System.out.println(UpYun.md5("https://slimages.macysassets.com/is/image/MCY//products/7/optimized/8196197_fpx.tif?op_sharpen=1&wid=400&hei=489&fit=fit,1&$filterlrg$").substring(0, 16)); 
        System.out.println(md5Encode("https://slimages.macysassets.com/is/image/MCY//products/7/optimized/8196197_fpx.tif?op_sharpen=1&wid=400&hei=489&fit=fit,1&$filterlrg$").substring(0, 16)); 

    }
}