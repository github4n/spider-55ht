package com.test.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

public class JavaMailTesting {

	public static void main(String... args) {

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", "smtp.exmail.qq.com");
		props.put("mail.user", "liushizhen@55haitao.com");
		props.put("mail.password", "xxx");

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
			InternetAddress to = new InternetAddress("liusz_ok@126.com");
			message.setRecipient(RecipientType.TO, to);
			message.addRecipient(RecipientType.TO, new InternetAddress("liushizhen@55haitao.com"));

			// 设置邮件标题
			message.setSubject("使用JavaMail测试程序发送邮件");

			// 设置邮件的内容体
			StringBuilder sb = new StringBuilder();
			sb.append("abcde11111a").append("<br />");
			sb.append("abcde22222b").append("<br />");
			sb.append("abcde33333c").append("<br />");
			sb.append("abcde44444d").append("<br />");
			message.setContent(sb.toString(), "text/html;charset=UTF-8");

			// 发送邮件
			Transport.send(message);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}