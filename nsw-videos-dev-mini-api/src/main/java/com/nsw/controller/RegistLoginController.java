package com.nsw.controller;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nsw.pojo.Users;
import com.nsw.pojo.vo.UsersVO;
import com.nsw.service.UserService;
import com.nsw.utils.MD5Utils;
import com.nsw.utils.NswJSONResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value="用户注册登录的接口", tags= {"注册和登录的controller"})
public class RegistLoginController extends BasicController {

	@Autowired
	private UserService userService;
	
	@ApiOperation(value="用户注册", notes="用户注册的接口")
	@PostMapping("/regist")
	public NswJSONResult regist(@RequestBody Users user)throws Exception {
		
		//1判断用户名和密码不为空
		if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
			return NswJSONResult.errorMsg("用户名和密码不能为空");
		}
		
		//2判断用户名是否存在
		boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());
		
		//3保存用户，注册信息
		if (!usernameIsExist) {
			user.setNickname(user.getUsername());
			user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
			user.setFansCounts(0);
			user.setReceiveLikeCounts(0);
			user.setFollowCounts(0);
			userService.saveUser(user);
		} else {
			return NswJSONResult.errorMsg("用户名已经存在，请换一个再试");
		}
		user.setPassword("");
//		String uniqueToken = UUID.randomUUID().toString();
//		redis.set(USER_REDIS_SESSION+":"+user.getId(), uniqueToken,1000*60*30);
//		
//		UsersVO usersVO = new UsersVO();
//		BeanUtils.copyProperties(user, usersVO);
//		usersVO.setUserToken(uniqueToken);
		UsersVO usersVO = setUserRedisSessionToken(user);
		return NswJSONResult.ok(usersVO);
	}
	/**
	 * 设置userSession的方法
	 * @param userModel
	 * @return
	 */
	public UsersVO setUserRedisSessionToken(Users userModel) {
		String uniqueToken = UUID.randomUUID().toString();
		redis.set(USER_REDIS_SESSION+":"+userModel.getId(), uniqueToken,1000*60*30);
		
		UsersVO userVO = new UsersVO();
		BeanUtils.copyProperties(userModel, userVO);
		userVO.setUserToken(uniqueToken);
		return userVO;
	}
	
	@ApiOperation(value="用户登录", notes="用户登录的接口")
	@PostMapping("/login")
	public NswJSONResult login(@RequestBody Users user) throws Exception {
		String username = user.getUsername();
		String password = user.getPassword();
		
//		Thread.sleep(3000);
		
		// 1. 判断用户名和密码必须不为空
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			return NswJSONResult.ok("用户名或密码不能为空...");
		}
		
		// 2. 判断用户是否存在
		Users userResult = userService.queryUserForLogin(username, 
				MD5Utils.getMD5Str(user.getPassword()));
		
		// 3. 返回
		if (userResult != null) {
			userResult.setPassword("");
			UsersVO userVO = setUserRedisSessionToken(userResult);
			return NswJSONResult.ok(userVO);
		} else {
			return NswJSONResult.errorMsg("用户名或密码不正确, 请重试...");
		}
	}
	
	@ApiOperation(value="用户注销", notes="用户注销的接口")
	@ApiImplicitParam(name="userId", value="用户id", required=true, 
						dataType="String", paramType="query")
	@PostMapping("/logout")
	public NswJSONResult logout(String userId) throws Exception {
		//System.out.println("userId="+userId);
		redis.del(USER_REDIS_SESSION + ":" + userId);
		return NswJSONResult.ok();
	}
	
}
