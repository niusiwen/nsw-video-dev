package com.nsw.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.nsw.pojo.Videos;
import com.nsw.pojo.vo.VideosVO;
import com.nsw.utils.MyMapper;

public interface VideosMapperCustom extends MyMapper<Videos> {
	
	/**
	 * 根据条件查询所有视频信息列表
	 * @param videoDesc
	 * @param userId
	 * @return
	 */
	public List<VideosVO> queryAllVideos(@Param("videoDesc") String videoDesc, @Param("userId") String userId);
	
	/**
	 * 对喜欢的视频数量进行累加
	 * @param videoId
	 */
	public void addVideoLikeCount(String videoId);
	
	/**
	 * 对喜欢的视频数量进行累减
	 * @param videoId
	 */
	public void reduceVideoLikeCount(String videoId);
	/**
	 * 查询我关注的人的视频列表
	 * @param userId
	 * @return
	 */
	public List<VideosVO> queryMyFollowVideos(String userId);
	/**
	 * 查询我点赞的视频列表
	 * @param userId
	 * @return
	 */
	public List<VideosVO> queryMyLikeVideos(@Param("userId") String userId);
}