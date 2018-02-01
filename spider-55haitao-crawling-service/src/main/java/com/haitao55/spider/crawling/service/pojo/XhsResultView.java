package com.haitao55.spider.crawling.service.pojo;

import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.xiaohongshu.XiaoHongShu;

/**
 * 包装返回信息
 * @author denghuan
 *
 */
public class XhsResultView {

	private String code;

    private String msg;
    
    private XiaoHongShu xiaoHongShu;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public XiaoHongShu getXiaoHongShu() {
		return xiaoHongShu;
	}

	public void setXiaoHongShu(XiaoHongShu xiaoHongShu) {
		this.xiaoHongShu = xiaoHongShu;
	}
    
	public String parseTo() {
		return JsonUtils.bean2json(this);
	}
}
