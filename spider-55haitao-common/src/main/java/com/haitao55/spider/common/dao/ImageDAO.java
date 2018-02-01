package com.haitao55.spider.common.dao;

import java.util.List;

import com.haitao55.spider.common.dos.ImageDO;

/**
 * 
  * @ClassName: ImageDAO
  * @Description: TODO
  * @author songsong.xu
  * @date 2016年11月16日 下午6:35:43
  *
 */
public interface ImageDAO {
	
	public void insertImage(Long taskId, ImageDO imageDO);

	public void insertImages(Long taskId, List<ImageDO> imageDOList);
	
	public void updateImages(Long taskId, List<ImageDO> imageList);
	
	public void upsertImages(Long taskId, List<ImageDO> imageList);
	
	public List<ImageDO> queryImages(Long taskId, int limit);
	
	public long count(Long taskId);
	
	public long countImages(Long taskId);
	
	public long countImagesBy(Long taskId,ImageDO imageDO);
	
	public boolean checkCollecionIsExists(Long taskId);
}