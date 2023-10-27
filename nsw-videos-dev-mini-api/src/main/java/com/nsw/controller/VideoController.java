package com.nsw.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nsw.enums.VideoStatusEnum;
import com.nsw.pojo.Bgm;
import com.nsw.pojo.Comments;
import com.nsw.pojo.Users;
import com.nsw.pojo.Videos;
import com.nsw.service.BgmService;
import com.nsw.service.VideoService;
import com.nsw.utils.FetchVideoCover;
import com.nsw.utils.MergeVideoMp3;
import com.nsw.utils.NswJSONResult;
import com.nsw.utils.PagedResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(value="视频相关业务的接口", tags= {"视频相关业务的controller"})
@RequestMapping("/video")
public class VideoController extends BasicController {

	@Autowired
	private BgmService bgmService;
	@Autowired
	private VideoService videoService;
	
	@ApiOperation(value="上传视频", notes="上传视频的接口")
	@ApiImplicitParams({
		@ApiImplicitParam(name="userId", value="用户id", required=true, 
				dataType="String", paramType="form"),//paramType="query"：在url后直接加参数 ，form：使用json对象传参
		@ApiImplicitParam(name="bgmId", value="背景音乐id", required=false, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoSeconds", value="背景音乐的长度", required=true, 
				dataType="double", paramType="form"),
		@ApiImplicitParam(name="videoWidth", value="视频宽度", required=true, 
				dataType="int", paramType="form"),
		@ApiImplicitParam(name="videoHeight", value="视频高度", required=true, 
				dataType="int", paramType="form"),
		@ApiImplicitParam(name="desc", value="视频描述", required=false, 
				dataType="String", paramType="form")
	})	
	@PostMapping(value="/upload",headers="content-type=multipart/form-data")
	public NswJSONResult upload(String userId,
			String bgmId,double videoSeconds,int videoWidth,int videoHeight,
			String desc,
			@ApiParam(value="短视频", required=true)
			MultipartFile file) throws Exception {
		
		if(StringUtils.isBlank(userId)) {
			return NswJSONResult.errorMsg("用户ID不能为空...");
		}
		
//		//文件保存的命名空间
//		String fileSpace = "G:/nsw_video_dev";
		//保存到数据库中的相对路径
		String uploadPathDB = "/" + userId + "/video";
		String coverPathDB = "/" + userId + "/video";
		
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;
		//文件上传的最终保存路径
		String finalVideoPath = "";
		try {
			if(file != null ) {
						
				String fileName = file.getOriginalFilename();
				//获取文件名的前缀  
				//String fileNamePrefix = fileName.split("\\.")[0];
				// fix bug: 解决小程序端OK，PC端(微信开发工具)不OK的bug，原因：PC端和小程序端对临时视频的命名不同
				//微信开发工具的命名：wxe509c46e3e830812.o6zAJs8LjTgT6MNw5Af1FjCv0HU4.8ySB42fyXx877658499968bbac8300d8480fe5456a7b.mp4
				String arrayFilenameItem[] =  fileName.split("\\.");
				String fileNamePrefix = "";
				for (int i = 0 ; i < arrayFilenameItem.length-1 ; i ++) {
					fileNamePrefix += arrayFilenameItem[i];
				}
				
				if(StringUtils.isNotBlank(fileName)) {
					//文件上传的最终保存路径
					finalVideoPath = FILE_SPACE + uploadPathDB + "/" + fileName;
					//设置数据库保存的路径
					uploadPathDB += ("/" + fileName);
					coverPathDB = coverPathDB + "/" + fileNamePrefix + ".jpg";
					
					File outFile = new File(finalVideoPath);
					if(outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
						//创建父文件夹
						outFile.getParentFile().mkdirs();
					}
					
					fileOutputStream = new FileOutputStream(outFile);
					inputStream = file.getInputStream();
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
		//判断bgmId是否为空，不为空就查询bgm的信息，并合并视频，产生新的视频
		if(!StringUtils.isBlank(bgmId)) {
			Bgm bgm = bgmService.queryBgmById(bgmId);
			String mp3InputPath = FILE_SPACE + bgm.getPath();
					
			MergeVideoMp3 tool = new MergeVideoMp3(FFMPEG_EXE);
			String videoInputPath = finalVideoPath;//原始视频上传的位置
			//合并后视频的名字
			String videoOutputName = UUID.randomUUID().toString() + ".mp4";
			uploadPathDB = "/" + userId + "/video" + "/" + videoOutputName; //重新定义新视频数据库位置
			finalVideoPath = FILE_SPACE + uploadPathDB;  //新视频最终保存的位置
			tool.convertor(videoInputPath, mp3InputPath, videoSeconds, finalVideoPath);
		}
		System.out.println("uploadPathDB="+uploadPathDB);
		System.out.println("finalVideoPath="+finalVideoPath);
		
		//对视频进行截图
		FetchVideoCover videoInfo = new FetchVideoCover(FFMPEG_EXE);		
		videoInfo.getCover(finalVideoPath, FILE_SPACE + coverPathDB);
		
		//上传成功保存到数据库
		Videos video = new Videos();
		video.setAudioId(bgmId);
		video.setUserId(userId);
		video.setVideoSeconds((float)videoSeconds);
		video.setVideoHeight(videoHeight);
		video.setVideoWidth(videoWidth);
		video.setVideoDesc(desc);
		video.setVideoPath(uploadPathDB);
		video.setCoverPath(coverPathDB);
		video.setStatus(VideoStatusEnum.SUCCESS.value);
		video.setCreateTime(new Date());
		String videoId = videoService.saveVideo(video);
		
		return NswJSONResult.ok(videoId);
	}
	
	@ApiOperation(value="上传封面", notes="上传封面的接口")
	@ApiImplicitParams({
		@ApiImplicitParam(name="userId", value="用户id", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoId", value="视频主键id", required=true, 
				dataType="String", paramType="form"),//paramType="query"：在url后直接加参数
	})	
	@PostMapping(value="/uploadCover",headers="content-type=multipart/form-data")
	public NswJSONResult uploadCover(String userId,String videoId,
			@ApiParam(value="封面", required=true)
			MultipartFile file) throws Exception {
		
		if(StringUtils.isBlank(videoId) || StringUtils.isBlank(userId)) {
			return NswJSONResult.errorMsg("视频主键ID不能为空...");
		}
		
//		//文件保存的命名空间
//		String fileSpace = "G:/nsw_video_dev";
		//保存到数据库中的相对路径
		String uploadPathDB = "/" + userId + "/video";
		
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;
		//文件上传的最终保存路径
		String finalCoverPath = "";
		try {
			if(file != null ) {
						
				String fileName = file.getOriginalFilename();
				if(StringUtils.isNotBlank(fileName)) {
					//文件上传的最终保存路径
					finalCoverPath = FILE_SPACE + uploadPathDB + "/" + fileName;
					//设置数据库保存的路径
					uploadPathDB += ("/" + fileName);
					
					File outFile = new File(finalCoverPath);
					if(outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
						//创建父文件夹
						outFile.getParentFile().mkdirs();
					}
					
					fileOutputStream = new FileOutputStream(outFile);
					inputStream = file.getInputStream();
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
		//
		videoService.updateVideo(videoId, uploadPathDB);
		
		
		return NswJSONResult.ok();
	}
	
//	@ApiOperation(value="上传封面", notes="上传封面的接口")
//	@ApiImplicitParams({
//		@ApiImplicitParam(name="page", value="页数", required=true, 
//				dataType="Integer", paramType="form"),
//	})	
	/**
	 * 分页和搜索查询视频列表
	 * @param video
	 * @param isSaveRecord 1 需要保存  0不需要保存  或者为空的时候
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/showAll")
	public NswJSONResult showAll(@RequestBody Videos video, Integer isSaveRecord,
			Integer page) throws Exception {
		
		if(page == null) {
			page = 1;
		}
		PagedResult result = videoService.getAllVideos(video, isSaveRecord, page, PAGE_SIZE);			
		return NswJSONResult.ok(result);
	}
	
	/**
	 * @Description: 我关注的人发的视频
	 */
	@PostMapping("/showMyFollow")
	public NswJSONResult showMyFollow(String userId, Integer page) throws Exception {
		
		if (StringUtils.isBlank(userId)) {
			return NswJSONResult.ok();
		}
		
		if (page == null) {
			page = 1;
		}

		int pageSize = 6;
		
		PagedResult videosList = videoService.queryMyFollowVideos(userId, page, pageSize);
		
		return NswJSONResult.ok(videosList);
	}
	
	/**
	 * @Description: 我收藏(点赞)过的视频列表
	 */
	@PostMapping("/showMyLike")
	public NswJSONResult showMyLike(String userId, Integer page, Integer pageSize) throws Exception {
		
		if (StringUtils.isBlank(userId)) {
			return NswJSONResult.ok();
		}
		
		if (page == null) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = 6;
		}
		
		PagedResult videosList = videoService.queryMyLikeVideos(userId, page, pageSize);
		
		return NswJSONResult.ok(videosList);
	}
	
	/**
	 * 查询热搜词
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/hot")
	public NswJSONResult hot() throws Exception {		
		return NswJSONResult.ok(videoService.getHotwords());
	}
	/**
	 * 点赞视频
	 * @param userId
	 * @param videoId
	 * @param videoCreaterId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/userLike")
	public NswJSONResult userLike(String userId,String videoId,String videoCreaterId) throws Exception {
		
		videoService.userLikeVideo(userId, videoId, videoCreaterId);		
		return NswJSONResult.ok();
	}
	/**
	 * 取消点赞
	 * @param userId
	 * @param videoId
	 * @param videoCreaterId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/userUnLike")
	public NswJSONResult userUnLike(String userId,String videoId,String videoCreaterId) throws Exception {	
		
		videoService.userUnLikeVideo(userId, videoId, videoCreaterId);		
		return NswJSONResult.ok();
	}
	
	@PostMapping("/saveComment")
	public NswJSONResult saveComment(@RequestBody Comments comment, 
			String fatherCommentId, String toUserId) throws Exception {
		
		comment.setFatherCommentId(fatherCommentId);
		comment.setToUserId(toUserId);
		
		videoService.saveComment(comment);
		return NswJSONResult.ok();
	}
	
	@PostMapping("/getVideoComments")
	public NswJSONResult getVideoComments(String videoId, Integer page, Integer pageSize) throws Exception {
		
		if (StringUtils.isBlank(videoId)) {
			return NswJSONResult.ok();
		}
		
		// 分页查询视频列表，时间顺序倒序排序
		if (page == null) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = 10;
		}
		
		PagedResult list = videoService.getAllComments(videoId, page, pageSize);
		
		return NswJSONResult.ok(list);
	}
	
}
