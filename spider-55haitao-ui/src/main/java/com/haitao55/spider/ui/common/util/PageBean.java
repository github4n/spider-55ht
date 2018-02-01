package com.haitao55.spider.ui.common.util;

/**
 * 
 * 功能：管理分页的工具类
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 下午4:47:47
 * @version 1.0
 */
public class PageBean {

	/**
	 */
	private Integer pageid = 0;
	/**
	 * 每页多少条
	 */
	private Integer pageNum = 10;
	/**
	 * 总条数
	 */
	private Integer count;
	/**
	 * 总页数
	 */
	private Integer pages;
	/**
	 * 上一页
	 */
	private Integer upPage;
	/**
	 * 下一页
	 */
	private Integer nextPage;
	/**
	 * 是否有下一页
	 */
	private String nextPageYOrN;
	/**
	 * 是否有上一页
	 */
	private String upPageYOrN;
	/**
	 * 尾页
	 */
	private Integer lastPage;

	/**
	 * 当前页的起始数
	 */
	private Integer pageidCount;

	private PageBean pageBean;

	public PageBean getPageBean() {
		return pageBean;
	}

	public void setPageBean(PageBean pageBean) {
		this.pageBean = pageBean;
	}

	/**
	 * 分页方法
	 * 
	 * @param count
	 *            总条数
	 * @param pageId
	 *            从第几页开始查
	 * @return pageBean对象
	 */
	public PageBean spiletPage(Integer count, Integer pageId) {
		PageBean pageBean = new PageBean();
		// 共多少条
		pageBean.setCount(count);
		// 共多少页
		if (count % pageNum == 0) {
			pageBean.setPages(count / pageNum);
		} else {
			pageBean.setPages((count / pageNum) + 1);
		}

		// 第几页

		if (pageId + pageBean.getPageNum() <= count) {
			pageBean.setPageid((pageId / pageNum) + 1);
		} else {
			pageBean.setPageid(pageBean.getPages());
			pageId = (pageBean.getPages() - 1) * pageNum;

			if (pageId < 0) {
				pageId = 0;
			}
		}

		// 下一页
		pageBean.setNextPage(pageId + pageNum);
		if ((pageId + pageNum) > count || (pageId + pageNum) == count) {
			// 没下一页
			pageBean.setNextPageYOrN("N");
		} else {
			// 还有下一页
			pageBean.setNextPageYOrN("Y");

		}
		// 上一页
		if (pageId == 0) {
			// 没有上一页
			pageBean.setUpPageYOrN("N");
		} else {
			pageBean.setUpPageYOrN("Y");
		}
		pageBean.setUpPage(pageId - pageNum);

		// 尾页
		pageBean.setLastPage((pageBean.getPages() * pageNum) - pageNum);

		pageBean.setPageidCount(pageId);
		return pageBean;
	}

	public Integer getPageidCount() {
		return pageidCount;
	}

	public void setPageidCount(Integer pageidCount) {
		this.pageidCount = pageidCount;
	}

	public Integer getPageid() {
		return pageid;
	}

	public void setPageid(Integer pageid) {
		this.pageid = pageid;
	}

	public Integer getPageNum() {
		return pageNum;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getPages() {
		return pages;
	}

	public void setPages(Integer pages) {
		this.pages = pages;
	}

	public Integer getUpPage() {
		return upPage;
	}

	public void setUpPage(Integer upPage) {
		this.upPage = upPage;
	}

	public Integer getNextPage() {
		return nextPage;
	}

	public void setNextPage(Integer nextPage) {
		this.nextPage = nextPage;
	}

	public String getNextPageYOrN() {
		return nextPageYOrN;
	}

	public void setNextPageYOrN(String nextPageYOrN) {
		this.nextPageYOrN = nextPageYOrN;
	}

	public String getUpPageYOrN() {
		return upPageYOrN;
	}

	public void setUpPageYOrN(String upPageYOrN) {
		this.upPageYOrN = upPageYOrN;
	}

	public Integer getLastPage() {
		return lastPage;
	}

	public void setLastPage(Integer lastPage) {
		this.lastPage = lastPage;
	}

}
