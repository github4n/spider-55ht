package com.haitao55.spider.common.gson.bean.competitor;

import java.io.Serializable;

/**
 * 
  * @ClassName: CtorTitle
  * @Description: 爬取结果json的标题节点
  * @author 赵新落
  * @date 2017年2月21日 下午2:07:23
  *
 */
public class CtorTitle implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8987451597093546216L;
	/**主标题*/
	private String main;
	/**副标题*/
	private String deputy;
	
	public CtorTitle() {
		super();
	}
	public CtorTitle(String main, String deputy) {
		super();
		this.main = main;
		this.deputy = deputy;
	}
	public String getMain() {
		return main;
	}
	public void setMain(String main) {
		this.main = main;
	}
	public String getDeputy() {
		return deputy;
	}
	public void setDeputy(String deputy) {
		this.deputy = deputy;
	}
	
	@Override
	public String toString() {
		return "CtorTitle [main=" + main + ", deputy=" + deputy + "]";
	}
	
}
