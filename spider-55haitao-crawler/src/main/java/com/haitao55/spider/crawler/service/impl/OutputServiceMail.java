package com.haitao55.spider.crawler.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.service.impl.RedisService;
import com.haitao55.spider.common.utils.CrawlerJSONResultUtils;
import com.haitao55.spider.crawler.common.cache.ExchangeRateCache;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.service.AbstractOutputService;
import com.haitao55.spider.crawler.service.OutputService;
import com.haitao55.spider.crawler.utils.ExchangeRateUtils;
import com.haitao55.spider.crawler.utils.SpringUtils;

/**
 * 
 * 功能：检查价格和库存有无变化,如有变化则发邮件
 * 
 * @author Arthur.Liu
 * @time 2016年10月20日 下午8:14:36
 * @version 1.0
 */
public class OutputServiceMail extends AbstractOutputService implements OutputService {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private static final String FIELD_NAME_PRICE = "Price";
	private static final String FIELD_NAME_STOCK = "Stock";
	private static final String FIELD_NAME_PROPERTY = "Property";
//	private static final String REDIS_KEY_PREFIX = "check_price_stock_send_mail_";
	private static final String PRICE_STOCK_HASH = "price_stock";
	private static final String PROPERTY_HASH = "color_size_width";
	private static final String SIZE = "Size";
	private static final String WIDTH = "Width";

	private RedisService redisService;

	@Override
	public void write(OutputObject oo) {
		try {
			String url = oo.getUrl().getValue();
			// 基于sku核价
			String convertItem2Json = oo.convertItem2Message();
			CrawlerJSONResult buildFrom = CrawlerJSONResult.buildFrom(convertItem2Json);
			if (null != buildFrom) {
				RetBody retbody = buildFrom.getRetbody();
				if (null != retbody) {
					Sku sku = retbody.getSku();
					if (null != sku) {
						List<LSelectionList> l_selection_list = sku.getL_selection_list();
						if (CollectionUtils.isNotEmpty(l_selection_list)) {
							for (LSelectionList lSelectionList : l_selection_list) {
								// 作为 redis key
								String redisKey = CrawlerJSONResultUtils.buildSkuid(lSelectionList);
								String price = getCNYPrice(lSelectionList);
								String stock = lSelectionList.getStock_status() + "";
								Entity entityCurrent = new Entity(price, stock);
								String valueFromRedis = this.redisService.hget(PRICE_STOCK_HASH, redisKey);
								Entity entityFromRedis = Entity.unserialize(valueFromRedis);
								if (entityFromRedis.isEmpty()) {
									logger.warn("got none from redis,url: {}", url);
									String valueToRedis = entityCurrent.serialize();
									this.redisService.hSet(PRICE_STOCK_HASH, redisKey, valueToRedis);
									return;// 从redis中没有取到数据的时候(比如第一次执行抓取的情况),则在保存当前数据到redis之后,不进行邮件发送
								}

								// color size width 属性加入redis
								String propertyFromRedis = this.redisService.hget(PROPERTY_HASH, redisKey);
								if (StringUtils.isBlank(propertyFromRedis)) {
									logger.warn("got color_size_width none from redis,url: {}", url);
									String propertValue = getPropertyRedisValue(lSelectionList);
									this.redisService.hSet(PROPERTY_HASH, redisKey, propertValue);
									propertyFromRedis = this.redisService.hget(PROPERTY_HASH, redisKey);
								}
								boolean isSame = entityCurrent.equals(entityFromRedis);
								if (!isSame) {
									String valueToRedis = entityCurrent.serialize();
									this.redisService.hSet(PRICE_STOCK_HASH, redisKey, valueToRedis);
									// 构造单条邮件内容
									String docid = retbody.getDOCID();
									String singleMailContent = this.buildSingleMailContent(url, entityFromRedis,
											entityCurrent, propertyFromRedis, docid);
									logger.info("Put email content:{}", singleMailContent);
									MailContentCache.getInstance().put(redisKey, singleMailContent.toString());
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error OutputServiceMail, e:{}", e);
		}
	}

	/**
	 * 获取sale_price 的人民币价格
	 * 
	 * @param lSelectionList
	 * @return
	 */
	private String getCNYPrice(LSelectionList lSelectionList) {
		float price = 0f;
		float sale_price = lSelectionList.getSale_price();
		String price_unit = lSelectionList.getPrice_unit();
		if(MapUtils.isEmpty(ExchangeRateCache.getInstance())){
			ExchangeRateUtils.exchangeRate();
		}
		Float rate = ExchangeRateCache.getInstance().get(price_unit);
		price = sale_price*rate;
		//增加汇率上浮逻辑
		price = ExchangeRateUtils.rateUp(price);
		return price+"";
	}

	/**
	 * 
	 * @param lSelectionList
	 * @return
	 */
	private String getPropertyRedisValue(LSelectionList lSelectionList) {
		String color = lSelectionList.getStyle_id();
		String size = StringUtils.EMPTY;
		String width = StringUtils.EMPTY;
		List<Selection> selections = lSelectionList.getSelections();
		if (CollectionUtils.isNotEmpty(selections)) {
			for (Selection selection : selections) {
				String select_name = selection.getSelect_name();
				String value = selection.getSelect_value();
				if (StringUtils.equalsIgnoreCase(SIZE, select_name)) {
					size = value;
				} else if (StringUtils.equalsIgnoreCase(WIDTH, select_name)) {
					width = value;
				}
			}
		}
		StringBuffer buffer = new StringBuffer();
		if (StringUtils.isNotBlank(color)) {
			buffer.append("color:").append(color).append(",");
		}
		if (StringUtils.isNotBlank(size)) {
			buffer.append("size:").append(size).append(",");
		}
		if (StringUtils.isNotBlank(width)) {
			buffer.append("width:").append(width).append(",");
		}
		return buffer.toString();
	}

	private static final String M_T_S_LEFT_OUTTER = "[";
	private static final String M_T_S_RIGHT_OUTTER = "]";
	private static final String M_T_S_LEFT_INNER = "(";
	private static final String M_T_S_RIGHT_INNER = ")";
	private static final String M_T_S_COMMA = ",";
	private static final String M_T_S_COLON = ":";
	private static final String M_T_S_ARROW = "-->";

	/**
	 * 邮件内容格式:[http://www.rebeccaminkoff.com/unlined-feed-bag-u16?color=9607],[
	 * Price:( $294.00 --> $295.00)],[Stock:(null --> null)]
	 * 
	 * @param url
	 * @param entityFromRedis
	 * @param entityCurrent
	 * @param propertyFromRedis
	 * @param docid
	 * @return
	 */
	private String buildSingleMailContent(String url, Entity entityFromRedis, Entity entityCurrent,
			String propertyFromRedis, String docid) {
		StringBuilder singleMailContent = new StringBuilder();

		singleMailContent.append(M_T_S_LEFT_OUTTER);
		singleMailContent.append("docid:	" + docid);
		singleMailContent.append(M_T_S_RIGHT_OUTTER);
		singleMailContent.append(M_T_S_COMMA);

		singleMailContent.append(M_T_S_LEFT_OUTTER);
		singleMailContent.append("url:	" + url);
		singleMailContent.append(M_T_S_RIGHT_OUTTER);
		singleMailContent.append(M_T_S_COMMA);

		singleMailContent.append(M_T_S_LEFT_OUTTER);
		singleMailContent.append(FIELD_NAME_PRICE).append(M_T_S_COLON).append(M_T_S_LEFT_INNER);
		singleMailContent.append(entityFromRedis.getPrice()).append(M_T_S_ARROW).append(entityCurrent.getPrice());
		singleMailContent.append(M_T_S_RIGHT_INNER);
		singleMailContent.append(M_T_S_RIGHT_OUTTER);
		singleMailContent.append(M_T_S_COMMA);

		singleMailContent.append(M_T_S_LEFT_OUTTER);
		singleMailContent.append(FIELD_NAME_STOCK).append(M_T_S_COLON).append(M_T_S_LEFT_INNER);
		singleMailContent.append(entityFromRedis.getStock()).append(M_T_S_ARROW).append(entityCurrent.getStock());
		singleMailContent.append(M_T_S_RIGHT_INNER);
		singleMailContent.append(M_T_S_RIGHT_OUTTER);

		singleMailContent.append(M_T_S_LEFT_OUTTER);
		singleMailContent.append(FIELD_NAME_PROPERTY).append(M_T_S_COLON).append(M_T_S_LEFT_INNER);
		singleMailContent.append(propertyFromRedis);
		singleMailContent.append(M_T_S_RIGHT_INNER);
		singleMailContent.append(M_T_S_RIGHT_OUTTER);

		return singleMailContent.toString();
	}

	@Override
	public boolean existInRepertory(Image image) {
		// ignore
		return true;
	}

	@Override
	public void uploadImage(Image image, OutputObject oo) {
		// ignore
	}

	@Override
	public void createImageRepertoryUrl(Image image, OutputObject oo) {
		// ignore
	}

	public RedisService getRedisService() {
		return redisService;
	}

	public void setRedisService(RedisService redisService) {
		this.redisService = redisService;
	}

	private static class MailContentCache extends ConcurrentHashMap<String, String> {
		/**
		 * default serialVersionUID
		 */
		private static final long serialVersionUID = 1L;

		private static final String MAIL_LINE_BREAK = "<br />";

		private static final String EMAIL_SEND_INTERVAL_NAME = "fanli.support.email.send.interval";
		private static final String EMAIL_SENDER_ADDRESS_NAME = "fanli.support.email.sender.address";
		private static final String EMAIL_SENDER_PASSWORD_NAME = "fanli.support.email.sender.password";
		private static final String EMAIL_RECEIVER_ADDRESSES_NAME = "fanli.support.email.receiver.addresses";
		private static final String EMAIL_ADDRESS_SEPARATOR = ",";

		private static final Timer timer = new Timer("Mail-Content-Cache-Timer");

		private long emailSendInterval;
		private String emailSenderAddress;
		private String emailSenderPassword;
		private List<String> emailReceiverAddresses = new ArrayList<String>();

		private MailContentCache() {
			// 在构造方法中初始化两个与发送邮件相关的变量的值,通过从spring的context中获取的方式
			this.emailSendInterval = Long.parseLong(SpringUtils.getProperty(EMAIL_SEND_INTERVAL_NAME));
			this.emailSenderAddress = SpringUtils.getProperty(EMAIL_SENDER_ADDRESS_NAME);
			this.emailSenderPassword = SpringUtils.getProperty(EMAIL_SENDER_PASSWORD_NAME);
			String receiverAddresses = SpringUtils.getProperty(EMAIL_RECEIVER_ADDRESSES_NAME);
			String[] receiverAddrs = StringUtils.splitByWholeSeparator(receiverAddresses, EMAIL_ADDRESS_SEPARATOR);
			if (receiverAddrs != null && receiverAddrs.length >= 1) {
				for (String receiverAddr : receiverAddrs) {
					this.emailReceiverAddresses.add(receiverAddr);
				}
			}

			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					MailContentCache.this.sendMail();
				}
			}, emailSendInterval, emailSendInterval);// 1分钟检查一次缓存内容,如果有待发送的邮件内容,则执行发送
		}

		private void sendMail() {
			StringBuilder mailContent = new StringBuilder();

			for (Entry<String, String> entry : this.entrySet()) {
				String singleMailContent = entry.getValue();
				mailContent.append(singleMailContent).append(MAIL_LINE_BREAK);
			}
			this.clear();

			String mailBody = mailContent.toString();

			if (StringUtils.isBlank(mailBody)) {
				logger.info("there is none email in cache to send(fanli-support-function)");
				return;
			}

			logger.info("there is some email in cache to send(fanli-support-function)");
			this.doSendMail(mailBody);
		}

		private void doSendMail(String mailBody) {
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.host", "smtp.exmail.qq.com");
			props.put("mail.user", this.emailSenderAddress);
			props.put("mail.password", this.emailSenderPassword);

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
				for (String receiverAddr : this.emailReceiverAddresses) {
					InternetAddress to = new InternetAddress(receiverAddr);
					message.addRecipient(RecipientType.TO, to);
				}

				// 设置邮件标题
				message.setSubject("官网直购");

				// 设置邮件的内容体
				message.setContent(mailBody, "text/html;charset=UTF-8");

				// 发送邮件
				Transport.send(message);
			} catch (AddressException e) {
				logger.error("Error send emial,Address-error::", e);
			} catch (MessagingException e) {
				logger.error("Error send emial,Messaging-error::", e);
			}
		}

		private static class Holder {
			private static MailContentCache instance = new MailContentCache();
		}

		public static MailContentCache getInstance() {
			return Holder.instance;
		}
	}

	private static class Entity {
		private static final String FIELDS_SERIALIZE_SEPARATOR = "#";

		private String price;
		private String stock;

		private Entity() {
			// only be used in this class itself
		}

		public Entity(String price, String stock) {
			this.price = price;
			this.stock = stock;
		}

		public String getPrice() {
			return price;
		}

		public void setPrice(String price) {
			this.price = price;
		}

		public String getStock() {
			return stock;
		}

		public void setStock(String stock) {
			this.stock = stock;
		}

		public boolean isEmpty() {
			return StringUtils.isBlank(this.price) && StringUtils.isBlank(this.stock);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((price == null) ? 0 : price.hashCode());
			result = prime * result + ((stock == null) ? 0 : stock.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entity other = (Entity) obj;
			if (price == null) {
				if (other.price != null)
					return false;
			} else if (!price.equals(other.price))
				return false;
			if (stock == null) {
				if (other.stock != null)
					return false;
			} else if (!stock.equals(other.stock))
				return false;
			return true;
		}

		public String serialize() {
			StringBuilder result = new StringBuilder();

			String price2Serialized = StringUtils.isBlank(this.price) ? "" : this.price;
			String stock2Serialized = StringUtils.isBlank(this.stock) ? "" : this.stock;
			result.append(price2Serialized).append(FIELDS_SERIALIZE_SEPARATOR).append(stock2Serialized);

			return result.toString();
		}

		public static Entity unserialize(String value) {
			Entity result = new Entity();

			String[] array = StringUtils.splitByWholeSeparator(value, FIELDS_SERIALIZE_SEPARATOR);
			if (array != null) {
				if (array.length >= 1) {
					result.setPrice(StringUtils.isBlank(array[0]) ? null : array[0]);
				}
				if (array.length >= 2) {
					result.setStock(StringUtils.isBlank(array[1]) ? null : array[1]);
				}
			}

			return result;
		}
	}
}