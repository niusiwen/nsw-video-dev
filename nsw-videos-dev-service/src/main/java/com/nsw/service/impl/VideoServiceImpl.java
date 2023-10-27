package com.nsw.service.impl;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nsw.mapper.BgmMapper;
import com.nsw.mapper.CommentsMapper;
import com.nsw.mapper.CommentsMapperCustom;
import com.nsw.mapper.SearchRecordsMapper;
import com.nsw.mapper.UsersLikeVideosMapper;
import com.nsw.mapper.UsersMapper;
import com.nsw.mapper.VideosMapper;
import com.nsw.mapper.VideosMapperCustom;
import com.nsw.pojo.Bgm;
import com.nsw.pojo.Comments;
import com.nsw.pojo.SearchRecords;
import com.nsw.pojo.Users;
import com.nsw.pojo.UsersLikeVideos;
import com.nsw.pojo.Videos;
import com.nsw.pojo.vo.CommentsVO;
import com.nsw.pojo.vo.VideosVO;
import com.nsw.service.BgmService;
import com.nsw.service.VideoService;
import com.nsw.utils.PagedResult;
import com.nsw.utils.TimeAgoUtils;

import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

@Service
public class VideoServiceImpl implements VideoService {

	@Autowired
	private VideosMapper videosMapper;
	@Autowired
	private UsersMapper usersMapper;
	@Autowired
	private VideosMapperCustom videosMapperCustom;
	@Autowired
	private SearchRecordsMapper searchRecordsMapper;
	@Autowired
	private UsersLikeVideosMapper usersLikeVideosMapper;
	@Autowired
	private CommentsMapper commentsMapper;
	@Autowired
	private CommentsMapperCustom commentsMapperCustom;
	@Autowired
	private Sid sid;
	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public String saveVideo(Videos video) {
		String id = sid.nextShort();
		video.setId(id);
		//insertSelective 保存实体，如果属性为空使用数据库中的默认值
		videosMapper.insertSelective(video);	
		
		return id;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void updateVideo(String videoId, String coverPath) {
		Videos video = new Videos();
		video.setId(videoId);
		video.setCoverPath(coverPath);
		videosMapper.updateByPrimaryKeySelective(video);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public PagedResult getAllVideos(Videos video, Integer isSaveRecord, 
			Integer page, Integer pageSize) {

		//保存热搜词
		String desc = video.getVideoDesc();
		String userId  = video.getUserId(); //
		if(isSaveRecord != null && isSaveRecord == 1) {
			SearchRecords record = new SearchRecords();
			String recordId = sid.nextShort();
			record.setId(recordId);
			record.setContent(desc);
			searchRecordsMapper.insert(record);
		}
		
		PageHelper.startPage(page, pageSize);
		List<VideosVO> list = videosMapperCustom.queryAllVideos(desc, userId);//加上查询条件：userId
		
		PageInfo<VideosVO> pageList = new PageInfo<>(list);
		
		PagedResult pagedResult = new PagedResult();
		pagedResult.setPage(page);
		pagedResult.setTotal(pageList.getPages());
		pagedResult.setRows(list);
		pagedResult.setRecords(pageList.getTotal());
		
		return pagedResult;
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<String> getHotwords() {	
		return searchRecordsMapper.getHotwords();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void userLikeVideo(String userId, String videoId, String videoCreaterId) {
		//1.保存用户和视频的喜欢点赞关联关系表
		String likeId = sid.nextShort();		
		UsersLikeVideos ulv = new UsersLikeVideos();
		ulv.setId(likeId);
		ulv.setUserId(userId);
		ulv.setVideoId(videoId);		
		usersLikeVideosMapper.insert(ulv);
		
		//2.视频喜欢数量累加
		videosMapperCustom.addVideoLikeCount(videoId);
		
		//3.视频上传用户受喜欢数量的累加
		usersMapper.addReceiveLikeCount(videoCreaterId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void userUnLikeVideo(String userId, String videoId, String videoCreaterId) {
		// 1.删除用户和视频的喜欢点赞关联关系表
		Example example = new Example(UsersLikeVideos.class);
		Criteria criteria = example.createCriteria();
		criteria.andEqualTo("userId",userId);
		criteria.andEqualTo("videoId",videoId);
		usersLikeVideosMapper.deleteByExample(example);
		// 2.视频喜欢数量累减
		videosMapperCustom.reduceVideoLikeCount(videoId);

		// 3.视频上传用户受喜欢数量的累减
		usersMapper.reduceReceiveLikeCount(videoCreaterId);
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public PagedResult queryMyFollowVideos(String userId, Integer page, int pageSize) {
		PageHelper.startPage(page, pageSize);
		List<VideosVO> list = videosMapperCustom.queryMyFollowVideos(userId);
				
		PageInfo<VideosVO> pageList = new PageInfo<>(list);
		
		PagedResult pagedResult = new PagedResult();
		pagedResult.setTotal(pageList.getPages());
		pagedResult.setRows(list);
		pagedResult.setPage(page);
		pagedResult.setRecords(pageList.getTotal());
		
		return pagedResult;
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public PagedResult queryMyLikeVideos(String userId, Integer page, Integer pageSize) {
		PageHelper.startPage(page, pageSize);
		List<VideosVO> list = videosMapperCustom.queryMyLikeVideos(userId);
				
		PageInfo<VideosVO> pageList = new PageInfo<>(list);
		
		PagedResult pagedResult = new PagedResult();
		pagedResult.setTotal(pageList.getPages());
		pagedResult.setRows(list);
		pagedResult.setPage(page);
		pagedResult.setRecords(pageList.getTotal());
		
		return pagedResult;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void saveComment(Comments comment) {
		String id = sid.nextShort();
		comment.setId(id);
		comment.setCreateTime(new Date());
		
		commentsMapper.insert(comment);
		
	}

	@Override
	public PagedResult getAllComments(String videoId, Integer page, Integer pageSize) {
		//com.github.pagehelper分页框架的PageHelper设置page和pageSize
		PageHelper.startPage(page, pageSize);
		//查询需要分页的所有内容
		List<CommentsVO> list = commentsMapperCustom.queryComments(videoId);
		
		list.forEach(c ->{
			String timeAgo = TimeAgoUtils.format(c.getCreateTime());
			c.setTimeAgoStr(timeAgo);
		});
		//com.github.pagehelper分页框架 将内容放入PageInfo对象中
		PageInfo<CommentsVO> pageList = new PageInfo<>(list);
		//分页的工具类
		PagedResult gril = new PagedResult();
		gril.setTotal(pageList.getPages());//设置总页数
		gril.setRows(list);//内容
		gril.setPage(page);//当前页数
		gril.setRecords(pageList.getTotal());//总记录数
		
		return gril;
	}
	


	
}
