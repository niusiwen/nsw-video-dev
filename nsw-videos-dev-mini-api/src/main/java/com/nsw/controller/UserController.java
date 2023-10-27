package com.nsw.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nsw.pojo.Users;
import com.nsw.pojo.UsersReport;
import com.nsw.pojo.vo.PublisherVideo;
import com.nsw.pojo.vo.UsersVO;
import com.nsw.service.UserService;
import com.nsw.utils.MD5Utils;
import com.nsw.utils.NswJSONResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value="用户相关业务的接口", tags= {"用户相关业务的controller"})
@RequestMapping("/user")
public class UserController extends BasicController {

	@Autowired
	private UserService userService;

	
	@ApiOperation(value="用户上传头像", notes="用户上传头像的接口")
	@ApiImplicitParam(name="userId", value="用户id", required=true, 
						dataType="String", paramType="query")
	@PostMapping("/uploadFace")
	public NswJSONResult uploadFace(String userId,
			@RequestParam("file") MultipartFile[] files) throws Exception {
		
		if(StringUtils.isBlank(userId)) {
			return NswJSONResult.errorMsg("用户ID不能为空...");
		}
		
		//文件保存的命名空间
		String fileSpace = "G:/nsw_video_dev";
		//保存到数据库中的相对路径
		String uploadPathDB = "/" + userId + "/face";
		
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;
		try {
			if(files != null && files.length > 0) {
						
				String fileName = files[0].getOriginalFilename();
				if(StringUtils.isNotBlank(fileName)) {
					//文件上传的最终保存路径
					String finalFacePath = fileSpace + uploadPathDB + "/" + fileName;
					//设置数据库保存的路径
					uploadPathDB += ("/" + fileName);
					
					File outFile = new File(finalFacePath);
					if(outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
						//创建父文件夹
						outFile.getParentFile().mkdirs();
					}
					
					fileOutputStream = new FileOutputStream(outFile);
					inputStream = files[0].getInputStream();
					IOUtils.copy(inputStream, fileOutputStream);
				}
			}else {
				return NswJSONResult.errorMsg("上传出错...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return NswJSONResult.errorMsg("上传出错...");
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}
		//上传成功保存到数据库
		Users user = new Users();
		user.setId(userId);
		user.setFaceImage(uploadPathDB);
		userService.updateUserInfo(user);
		
		return NswJSONResult.ok(uploadPathDB);
	}
	
	@ApiOperation(value="查询用户信息", notes="查询用户信息的接口")
	@ApiImplicitParam(name="userId", value="用户id", required=true, 
						dataType="String", paramType="query")
	@PostMapping("/query")
	public NswJSONResult query(String userId, String fanId) throws Exception {
	
		if(StringUtils.isBlank(userId)) {
			return NswJSONResult.errorMsg("用户ID不能为空...");
		}
		Users userInfo = userService.queryUserInfo(userId);
		UsersVO userVO = new UsersVO();
		
		//查询是否关注了用户
		userVO.setFollow(userService.queryIfFollow(userId, fanId));
		
		BeanUtils.copyProperties(userInfo, userVO);
		
		return NswJSONResult.ok(userVO);
	}
	
	/**
	 * 查询发布视频用户的信息
	 * @param loginUserId 登陆用户id
	 * @param videoId   视频id
	 * @param publishUserId 发布用户id
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/queryPublisher")
	public NswJSONResult queryPublisher(String loginUserId, String videoId, 
			String publishUserId) throws Exception {
	
		if(StringUtils.isBlank(publishUserId)) {
			return NswJSONResult.errorMsg("");
		}
		//1.查询视频发布者的信息
		Users userInfo = userService.queryUserInfo(publishUserId);
		UsersVO publisher = new UsersVO();
		BeanUtils.copyProperties(userInfo, publisher);
		
		//2.查询当前登陆者和视频的点赞关系
		boolean userLikeVideo = userService.isUserLikeVideo(loginUserId, videoId);
		
		PublisherVideo bean = new PublisherVideo();
		bean.setPublisher(publisher);
		bean.setUserLikeVideo(userLikeVideo);
		
		return NswJSONResult.ok(bean);
	}
	
	/**
	 * 关注用户
	 * @param userId
	 * @param fanId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/beyourfans")
	public NswJSONResult beyourfans(String userId, String fanId) throws Exception {
	
		if(StringUtils.isBlank(userId) || StringUtils.isBlank(fanId)) {
			return NswJSONResult.errorMsg("");
		}
		//
		userService.saveUserFanRelation(userId, fanId);;
	
		return NswJSONResult.ok("关注成功...");
	}
	
	/**
	 * 取消关注
	 * @param userId
	 * @param fanId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/dontbeyourfans")
	public NswJSONResult dontbeyourfans(String userId, String fanId) throws Exception {
	
		if(StringUtils.isBlank(userId) || StringUtils.isBlank(fanId)) {
			return NswJSONResult.errorMsg("");
		}
		//
		userService.deleteUserFanRelation(userId, fanId);;
	
		return NswJSONResult.ok("取消关注成功...");
	}
	
	@PostMapping("/reportUser")
	public NswJSONResult reportUser(@RequestBody UsersReport usersReport) throws Exception {
		
		// 保存举报信息
		userService.reportUser(usersReport);
		
		return NswJSONResult.errorMsg("举报成功...有你平台变得更美好...");
	}
	
}
