package com.nsw.service;

import java.util.List;

import com.nsw.pojo.Bgm;
import com.nsw.pojo.Comments;
import com.nsw.pojo.Videos;
import com.nsw.utils.PagedResult;

public interface VideoService {

	
	/**
	 * 保存视频
	 * @param id
	 * @return
	 */
	public String saveVideo(Videos video);
	
	/**
	 * 修改视频的封面
	 * @param videoId
	 * @param coverPath
	 */
	public void updateVideo(String videoId, String coverPath);
	/**
	 * 分页查询视频列表
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public PagedResult getAllVideos(Videos video, Integer isSaveRecord, 
			Integer page, Integer pageSize);
	/**
	 * 获取热搜词列表
	 * @return
	 */
	public List<String> getHotwords();
	
	/**
	 * 用户喜欢视频/点赞视频
	 * @param userId
	 * @param videoId
	 * @param videoCreaterId
	 */
	public void userLikeVideo(String userId, String videoId, String videoCreaterId);
	
	/**
	 * 用户不喜欢视频/取消点赞视频
	 * @param userId
	 * @param videoId
	 * @param videoCreaterId
	 */
	public void userUnLikeVideo(String userId, String videoId, String videoCreaterId);
	/**
	 * 分页查询我关注的人发布的视频
	 * @param userId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public PagedResult queryMyFollowVideos(String userId, Integer page, int pageSize);
	/**
	 * 分页查询我点赞的视频
	 * @param userId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public PagedResult queryMyLikeVideos(String userId, Integer page, Integer pageSize);

	/**
	 * 添加评论
	 * @param comment
	 */
	public void saveComment(Comments comment);
	/**
	 * 评论列表
	 * @param videoId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public PagedResult getAllComments(String videoId, Integer page, Integer pageSize);
	
	
}
