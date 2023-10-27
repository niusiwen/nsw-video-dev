package com.nsw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nsw.utils.RedisOperator;

@RestController
public class BasicController {

	@Autowired
	public RedisOperator redis;
	
	public static final String USER_REDIS_SESSION = "USER-REDIS-SESSION";
	
	//文件保存的命名空间
	public static final String FILE_SPACE = "G:/nsw_video_dev";
	
	//ffmpeg所在的目录
	public static final String FFMPEG_EXE = "G:\\video_tool\\ffmpeg\\bin\\ffmpeg.exe";
	
	//每页数量
	public static final Integer PAGE_SIZE = 5;
}
