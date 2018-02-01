package com.haitao55.spider.common.dos;

import java.io.Serializable;
import com.google.gson.annotations.Expose;

/**
 * 
  * @ClassName: LinksDO
  * @Description: link的实体类
  * @author songsong.xu
  * @date 2017年4月6日 上午10:40:43
  *
 */
public class LinksDO implements Serializable {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	@Expose(serialize = false)
	private String id;
	@Expose(serialize = false)
	private String orignal_doc_id;
    
	private String  orignal_url;
	
	private String target_url;
	
	private String result_url;
	
	private double expect_percent;
	
	private double result_percent;
	@Expose(serialize = false)
	private long create_time;
	@Expose(serialize = false)
	private long update_time;
	
	private String status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrignal_doc_id() {
		return orignal_doc_id;
	}

	public void setOrignal_doc_id(String orignal_doc_id) {
		this.orignal_doc_id = orignal_doc_id;
	}

	public String getOrignal_url() {
		return orignal_url;
	}

	public void setOrignal_url(String orignal_url) {
		this.orignal_url = orignal_url;
	}

	public String getTarget_url() {
		return target_url;
	}

	public void setTarget_url(String target_url) {
		this.target_url = target_url;
	}

	public String getResult_url() {
		return result_url;
	}

	public void setResult_url(String result_url) {
		this.result_url = result_url;
	}
	public double getExpect_percent() {
		return expect_percent;
	}

	public void setExpect_percent(double expect_percent) {
		this.expect_percent = expect_percent;
	}

	public double getResult_percent() {
		return result_percent;
	}

	public void setResult_percent(double result_percent) {
		this.result_percent = result_percent;
	}

	public long getCreate_time() {
		return create_time;
	}

	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}

	public long getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(long update_time) {
		this.update_time = update_time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

  @Override
  public String toString() {
    return "LinksDO [id="
        + id
        + ", orignal_doc_id="
        + orignal_doc_id
        + ", orignal_url="
        + orignal_url
        + ", target_url="
        + target_url
        + ", result_url="
        + result_url
        + ", expect_percent="
        + expect_percent
        + ", result_percent="
        + result_percent
        + ", create_time="
        + create_time
        + ", update_time="
        + update_time
        + ", status="
        + status
        + "]";
  }
	
	
	
}