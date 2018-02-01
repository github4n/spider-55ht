package com.haitao55.spider.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.cache.OnlineWebSiteCache;
import com.haitao55.spider.common.utils.SpiderDateTimeUtil;
import com.haitao55.spider.view.DomainCounter;

/**
 * eamil 工具类
 * @author denghuan
 *
 */
public class MailUtils {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CLEANING_FULL);
	
	//private static final String MAIL_BOBY = "Hi,爬虫组全量数据统计,请查收附件查看各个商家数量统计,谢谢!!!";
	private static final String MAIL_SUBJECT = "爬虫商品全量导出统计[重要]";
	private static final String MAIL_SUBJECT_WAAN = "!!!";
	private static final String RECEIVER_ADDRESSES_IDS_SEPARATOR = ",";
	private static final String TASK_START_TIME = " 00:10:00";
	private static final String ITEM_TIAO = "条";

	public static void doSendMail(String emailAddress,String emailPassword,String emailReceiver,
			String excelFilePath,String outputRootPath,String outputRootFileName) {
		
		String currTime = SpiderDateTimeUtil.format(new Date(), SpiderDateTimeUtil.FORMAT_SHORT_DATE);
		logger.info("start sendEmail emailReceiver : {}", emailReceiver);

		List<String> emailReceiverAddresses = convertReceiverAddresses(emailReceiver);

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", "smtp.exmail.qq.com");
		props.put("mail.user", emailAddress);
		props.put("mail.password", emailPassword);

		// 构建授权信息，用于进行SMTP进行身份验证
		Authenticator authenticator = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// 用户名、密码
				String userName = props.getProperty("mail.user");
				String password = props.getProperty("mail.password");
				return new PasswordAuthentication(userName, password);
			}
		};

		// 使用环境属性和授权信息，创建邮件会话
		Session mailSession = Session.getInstance(props, authenticator);

		// 创建邮件消息
		MimeMessage message = new MimeMessage(mailSession);

		try {
			// 设置发件人
			InternetAddress from = new InternetAddress(props.getProperty("mail.user"));
			message.setFrom(from);
			// 设置收件人
			for (String receiverAddr : emailReceiverAddresses) {
				InternetAddress to = new InternetAddress(receiverAddr);
				message.addRecipient(RecipientType.TO, to);
			}

			// 设置邮件标题
			// message.setSubject(MAIL_SUBJECT);

			// 向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
			Multipart multipart = new MimeMultipart();

			BodyPart messageBodyPart = new MimeBodyPart();
			// messageBodyPart.setText(mailBoby(outputRootPath,outputRootFileName));
			messageBodyPart.setContent(mailBoby(outputRootPath, outputRootFileName, excelFilePath, message),
					"text/html; charset=utf-8");

			multipart.addBodyPart(messageBodyPart);

			messageBodyPart = new MimeBodyPart();

			// 添加附件
			String filePath = excelFilePath + Constants.EXCEL_NAME + currTime + Constants.EXPORT_EXCEL_FILE_SUFFIX;
			DataSource source = new FileDataSource(filePath);

			messageBodyPart.setDataHandler(new DataHandler(source));

			String fileName = Constants.EXCEL_NAME + currTime + Constants.EXPORT_EXCEL_FILE_SUFFIX;

			try {
				messageBodyPart.setFileName(MimeUtility.encodeWord(fileName));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			multipart.addBodyPart(messageBodyPart);

			message.setContent(multipart);

			// 发送邮件
			Transport.send(message);
			logger.info("sendEmail success ================>>>>>>");
		} catch (AddressException e) {
			logger.error("Error send emial,Address-error::", e);
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
			logger.error("Error send emial,Messaging-error::", e);
		}
	}
	
	private static List<String> convertReceiverAddresses(String receiverAddresses) {
		List<String> result = new ArrayList<String>();

		String[] adds = StringUtils.splitByWholeSeparator(receiverAddresses, RECEIVER_ADDRESSES_IDS_SEPARATOR);

		if (ArrayUtils.isEmpty(adds)) {
			return result;
		}

		for (String ra : adds) {
			result.add(ra);
		}

		logger.info("converted-receiverAddresses-ids::List<String>::{}", result.toString());

		return result;
	}
	
	/**
	 * 商品统计
	 * @param outputRootPath
	 * @param outputRootFileName
	 * @param outputTotalSizePath
	 * @param message
	 * @return
	 */
	private static String mailBoby(String outputRootPath,String outputRootFileName,
			String outputTotalSizePath,MimeMessage message){
		StringBuilder sb = new StringBuilder();
		WriteFileUtil writeFile = new WriteFileUtil();
		String bfb = StringUtils.EMPTY;
		BufferedWriter bw = null;
		try {
			HashMap<String, String> webSiteMap = OnlineWebSiteCache.getInstance();
			List<DomainCounter> onLineLIst = CleaingFullUtil
					.getDomainCountList(Constants.CLEANING_FULL_ITEM_ONLINE_FIELD_PREFIX);
			List<DomainCounter> all_online_List = CleaingFullUtil
					.getDomainCountList(Constants.CLEANING_FULL_ITEM_ALL_ONLINE_FIELD_PREFIX);
			List<DomainCounter> all_online_sku_List = CleaingFullUtil
					.getDomainCountList(Constants.ITEM_ALL_ONLINE_SKU_SIZE);
			Collections.sort(onLineLIst, Collections.reverseOrder());

			String currTime = SpiderDateTimeUtil.format(new Date(), SpiderDateTimeUtil.FORMAT_SHORT_DATE);
			String endTime = SpiderDateTimeUtil.format(new Date(), SpiderDateTimeUtil.FORMAT_LONG_DATE);

			String filePath = outputRootPath + File.separator + outputRootFileName + currTime
					+ Constants.CLEANING_FULL_FILE_SUFFIX;

			Long toDayTotal = TotalFileSizeSequential.getTotalSizeOfFilesInDir(new File(filePath));// 获取文件大小

			String totalSize = TotalFileSizeSequential.getTotalFileSize(filePath);// 获取文件大小

			bw = writeFile.createOutputWriter(outputTotalSizePath + Constants.OUT_PUT_TOTAL_SIZE_FILE_NAME);

			String lastLineValueJson = writeFile
					.readFileLastLineValue(outputTotalSizePath + Constants.OUT_PUT_TOTAL_SIZE_FILE_NAME);

			writeFileLastValue(outputTotalSizePath, currTime, totalSize, writeFile, toDayTotal, bw);// 写文件

			if (StringUtils.isNotBlank(lastLineValueJson)) {
				JSONObject jsonObject = JSONObject.parseObject(lastLineValueJson);
				Long yestDayTotal = jsonObject.getLong("totalNumber");
				bfb = numberFormat(toDayTotal, yestDayTotal);// 比列
				String averageVal = pattern(bfb);
				if (Float.parseFloat(averageVal) <= 95) {
					message.setSubject(MAIL_SUBJECT + MAIL_SUBJECT_WAAN);
				} else {
					message.setSubject(MAIL_SUBJECT);
				}
			} else {
				message.setSubject(MAIL_SUBJECT);
			}

			sb.append("<br>");
			sb.append("开始时间 : ").append(currTime + TASK_START_TIME);
			sb.append("<br>");
			sb.append("结束时间 : ").append(endTime);
			sb.append("<br>");
			if (StringUtils.isNotBlank(totalSize)) {
				sb.append("文件大小 : ").append(totalSize);
			}
			sb.append("<br>");
			if (CollectionUtils.isNotEmpty(all_online_List)) {
				DomainCounter totalDomain = all_online_List.get(0);
				sb.append("商品SPU总量 : ").append(counterFormat(totalDomain.getCount()));
				sb.append(ITEM_TIAO);
			}
			sb.append("<br>");
			if (CollectionUtils.isNotEmpty(all_online_sku_List)) {
				DomainCounter totalDomain = all_online_sku_List.get(0);
				sb.append("商品SKU总量 : ").append(counterFormat(totalDomain.getCount()));
				sb.append(ITEM_TIAO);
			}
			sb.append("<br>");
			if (CollectionUtils.isNotEmpty(onLineLIst)) {
				sb.append("收录商家数量 : ").append(onLineLIst.size());
				sb.append("家");
			}
			sb.append("<br>");

			sb.append("上架商家数量 : ").append(webSiteMap.size());
			sb.append("家");

			sb.append("<br>");
			if (StringUtils.isNotBlank(lastLineValueJson)) {
				JSONObject jsonObjectValue = JSONObject.parseObject(lastLineValueJson);
				String lastValue = jsonObjectValue.getString("totalSize");
				if (StringUtils.isNotBlank(lastValue)) {
					sb.append("上次文件大小 : ").append(lastValue);
					if (StringUtils.isNotBlank(bfb)) {
						sb.append("&nbsp&nbsp&nbsp&nbsp&nbsp与上次文件大小对比比列 : ").append(bfb).append("%");
					}
				}
			}
			sb.append("<br>");
			sb.append("<br>");
			sb.append("<table width='1000' border='1' cellspacing='0' cellpadding='0'>");
			sb.append("<tr>");
			sb.append("<td width='80' style='text-align:center'><font size='3'>商家域名</font></td>");
			sb.append("<td width='80' style='text-align:center'><font size='3'>商品SPU数量</font></td>");
			sb.append("<td width='100' style='text-align:center'><font size='3'>商品SKU数量</font></td>");
			sb.append("<td width='100' style='text-align:center'><font size='3'>上架状态</font></td>");
			sb.append("</tr>");
			if (CollectionUtils.isNotEmpty(onLineLIst)) {
				for (DomainCounter domainCount : onLineLIst) {
					sb.append("<tr>");
					sb.append("<td style='text-align:center'><font size='3'>" + domainCount.getDomain()
							+ "</font></td> ");
					sb.append("<td style='text-align:center'><font size='3'>" + counterFormat(domainCount.getCount())
							+ ITEM_TIAO + "</font></td>");

					AtomicInteger domainSkuCount = DomainCounterCache.getInstance()
							.get(Constants.ITEM_ALL_ONLINE_SKU_SIZE_FIELD_PREFIX + domainCount.getDomain());
					if (domainSkuCount != null) {
						sb.append("<td style='text-align:center'><font size='3'>"
								+ counterFormat(domainSkuCount.intValue()) + ITEM_TIAO + "</font></td>");
					} else {
						sb.append("<td style='text-align:center'><font size='3'>0" + ITEM_TIAO + "</font></td>");
					}

					String webSite = webSiteMap.get(domainCount.getDomain());
					if (StringUtils.isNotBlank(webSite)) {
						sb.append("<td style='text-align:center'><font size='3'>已上架</font></td>");
					} else {
						sb.append("<td style='text-align:center'><font size='3' color='red'>已下架</font></td>");
					}

					sb.append("</tr>");
				}
			}
			sb.append("</table>");
			sb.append("<br>");
			sb.append("<br>");

		} catch (Exception e) {
			logger.error("Error mailBoby write..", e);
			e.printStackTrace();
		} finally {
			writeFile.closeOutputWriter(bw);
		}
		return sb.toString();
	}
	
	private static void writeFileLastValue(String outputTotalSizePath,String startTime,String totalSize,
			WriteFileUtil writeFile,Long totalNumber,BufferedWriter writer){
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("startTime", startTime);
			jsonObject.put("totalSize", totalSize);
			jsonObject.put("totalNumber", totalNumber);
			writeFile.writeLasted(jsonObject.toJSONString(), writer);
		} catch (Exception e) {
			logger.error("Error writeFileLastValue..", e);
			e.printStackTrace();
		}
	}
	
	private static String counterFormat(long counter){
		DecimalFormat df = new DecimalFormat("#,###"); 
		if(counter != 0){
			return df.format(counter);
		}
		return StringUtils.EMPTY;
	}
	
	private static String pattern(String pageCount){
		Pattern pattern = Pattern.compile("(\\d+(.\\d+)?)");
		Matcher matcher = pattern.matcher(pageCount);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	private static String numberFormat(Long yesterdayTotalNumber,Long toDayTotalNumber){
		NumberFormat numberFormat = NumberFormat.getInstance();

		numberFormat.setMaximumFractionDigits(2); // 设置精确到小数点后2位

		return numberFormat.format((float) yesterdayTotalNumber / (float) toDayTotalNumber * 100);
	}
	
}
