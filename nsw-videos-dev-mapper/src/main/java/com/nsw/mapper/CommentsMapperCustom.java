package com.nsw.mapper;

import java.util.List;

import com.nsw.pojo.Comments;
import com.nsw.pojo.vo.CommentsVO;
import com.nsw.utils.MyMapper;

public interface CommentsMapperCustom extends MyMapper<Comments> {

	public List<CommentsVO> queryComments(String videoId);
}