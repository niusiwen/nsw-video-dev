package com.nsw.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nsw.mapper.UsersFansMapper;
import com.nsw.mapper.UsersLikeVideosMapper;
import com.nsw.mapper.UsersMapper;
import com.nsw.mapper.UsersReportMapper;
import com.nsw.pojo.Users;
import com.nsw.pojo.UsersFans;
import com.nsw.pojo.UsersLikeVideos;
import com.nsw.pojo.UsersReport;
import com.nsw.service.UserService;
import com.nsw.utils.NswJSONResult;

import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UsersMapper userMapper;
	@Autowired
	private UsersFansMapper usersFansMapper;
	@Autowired
	private UsersLikeVideosMapper usersLikeVideosMapper;
	@Autowired
	private UsersReportMapper usersReportMapper;
	@Autowired
	private Sid sid;
	
	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public boolean queryUsernameIsExist(String username) {
		Users user = new Users();
		user.setUsername(username);
		
		Users result = userMapper.selectOne(user);
		
		return result == null ? false : true;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void saveUser(Users user) {
		String userId = sid.nextShort();
		user.setId(userId);
		userMapper.insert(user);
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public Users queryUserForLogin(String username, String password) {
		//使用tk.mybatis框架的Example来实现登陆验证
		Example userExample = new Example(Users.class);
		Criteria criteria = userExample.createCriteria();
		criteria.andEqualTo("username", username);
		criteria.andEqualTo("password", password);
		Users result = userMapper.selectOneByExample(userExample);
		
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void updateUserInfo(Users user) {
		Example userExample = new Example(Users.class);
		Criteria criteria = userExample.createCriteria();
		criteria.andEqualTo("id", user.getId());
		userMapper.updateByExampleSelective(user, userExample);
		
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public Users queryUserInfo(String userId) {
		Example userExample = new Example(Users.class);
		Criteria criteria = userExample.createCriteria();
		criteria.andEqualTo("id", userId);
		Users user = userMapper.selectOneByExample(userExample);
		return user;
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public boolean isUserLikeVideo(String userId, String videoId) {
		if(StringUtils.isBlank(userId) || StringUtils.isBlank(videoId)) {
			return false;
		}
		Example example = new Example(UsersLikeVideos.class);
		Criteria criteria = example.createCriteria();
		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("videoId", videoId);
		
		List<UsersLikeVideos> list = usersLikeVideosMapper.selectByExample(example);
		
		if(list != null && list.size() > 0) {
			return true;
		}
		return false;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void saveUserFanRelation(String userId, String fanId) {
		//保存用户粉丝关系数据
		String relId = sid.nextShort();
		UsersFans usersFans = new UsersFans();
		usersFans.setId(relId);
		usersFans.setUserId(userId);
		usersFans.setFanId(fanId);		
		usersFansMapper.insert(usersFans);
		//用户增加粉丝数
		userMapper.addFansCount(userId);
		//粉丝增加关注数
		userMapper.addFollowersCount(fanId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteUserFanRelation(String userId, String fanId) {
		//删除用户粉丝关系数据
		Example example = new Example(UsersFans.class);
		Criteria criteria = example.createCriteria();
		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("fanId", fanId);
		usersFansMapper.deleteByExample(example);
		//用户减少粉丝数
		userMapper.reduceFansCount(userId);
		//粉丝减少关注数
		userMapper.reduceFollowersCount(fanId);
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public boolean queryIfFollow(String userId, String fanId) {
		Example example = new Example(UsersFans.class);
		Criteria criteria = example.createCriteria();
		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("fanId", fanId);
		List<UsersFans> list = usersFansMapper.selectByExample(example);
		if(list != null && !list.isEmpty() && list.size() > 0) {
			return true;
		}
		return false;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void reportUser(UsersReport usersReport) {
		String relId = sid.nextShort();
		usersReport.setId(relId);
		usersReport.setCreateDate(new Date());
		
		usersReportMapper.insert(usersReport);
	}

}
