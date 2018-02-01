package com.haitao55.spider.common.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;

/**
 * 
 * 功能：http 结果封装,包含所有http response信息（状态码，返回的header，返回的content等）
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午3:39:26
 * @version 1.0
 */
public class HttpResult {

	private static final String DEFAULT_CHARSET = "ISO-8859-1";

	// response status if any
	private int status;

	// response content if any
	private byte[] content;

	// response headers if any
	private List<NameValuePair> headers;

	// response charset:
	private String charset;

	/**
	 * response status code
	 * 
	 * @return status code
	 */
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * response content<br>
	 * may be html,xml,json,pic and so on<br>
	 * 
	 * @return
	 */
	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	/**
	 * response headers<br>
	 * 
	 * @return
	 */
	public List<NameValuePair> getHeaders() {
		return headers;
	}

	public void setHeaders(List<NameValuePair> headers) {
		this.headers = headers;
	}

	/**
	 * content charset<br>
	 * 
	 * <pre>
	 * firstly get from header( Content-Type:text/html;charset=UTF-8 )<br>
	 * if null,then get from content( <meta charset="gbk" /> or <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/> )<br>
	 * if still null,possibly not text or something like json,then return default charset ISO-8859-1
	 * </pre>
	 * 
	 * @return
	 */
	public String getCharset() throws IOException {
		parseCharset();
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * try to decode content as text<br>
	 * if content is not text(such as image or something else),possibly return
	 * gibberish
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getContentAsString() throws IOException {
		if (ArrayUtils.isEmpty(content)) {
			return StringUtils.EMPTY;
		}
		parseCharset();
		return IOUtils.toString(content, charset);
	}

	private void parseCharset() throws IOException {
		if (StringUtils.isBlank(charset) && content != null) {
			InputStream in = null;
			try {
				in = new ByteArrayInputStream(content);
				charset = EncodingSniffer.sniffEncoding(headers, in);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}

		if (StringUtils.isBlank(charset)) {
			charset = DEFAULT_CHARSET;
		}
	}

}