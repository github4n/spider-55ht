package com.haitao55.spider.common.dao;

import java.util.List;
import java.util.Set;

import com.haitao55.spider.common.dos.LinksDO;

/**
 * 
  * @ClassName: LinkHaiTaoDAO
  * @Description: linkhaitao urls
  * @author songsong.xu
  * @date 2017年4月6日 上午10:18:01
  *
 */
public interface LinkHaiTaoDAO {

	/**
	 * 批量更新/新增商品信息
	 * 
	 * @param itemList
	 *    商品信息列表
	 */
	public void insertLinks(Long taskId, List<LinksDO> linksDOList);
	
	
	public void upsertLinks(Long taskId,LinksDO itemDO);
	
	
	public void upsertLinksList(Long taskId, List<LinksDO> linksDOList);

	
	public List<LinksDO> queryAllLinks(Long taskId,String status,int limit);
	
	
	public Long countAllLinks(Long taskId);
	
	public Set<String> getAllColls();
	
}