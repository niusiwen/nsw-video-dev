package com.nsw.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MergeVideoMp3 {

	private String ffmpegEXE;
	
	public MergeVideoMp3(String ffmpegEXE) {
		super();
		this.ffmpegEXE = ffmpegEXE;
	}
	
	public void convertor(String videoInputPath,String mp3InputPath,
			double seconds,String VideoOutputPath) throws IOException {
		//ffmpeg.exe -i input.mp4 output.avi   转换视频格式的命令
		//ffmpeg.exe -i video01.mp4 -i bgm.mp3 -t 10 -y newvideo.mp4  合并视频 音频的命令
		
		List<String> command = new ArrayList<>();		
		command.add(ffmpegEXE);
		
		command.add("-i");
		command.add(videoInputPath);
		
		command.add("-i");	
		command.add(mp3InputPath);
		
		command.add("-t");	
		command.add(String.valueOf(seconds));
		
		command.add("-y");	
		command.add(VideoOutputPath);
		
//		for (String string : command) {
//			System.out.print(string);
//		}
		//java中调用可执行文件的类
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process process = processBuilder.start();//开始执行
		//读取执行文件时产生的流碎片，来释放资源
		InputStream errorStream = process.getErrorStream();
		InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
		BufferedReader br = new BufferedReader(inputStreamReader);
		
		String line = "";
		while((line = br.readLine()) !=null) {
			//errorStream里边的数据读出来就可以，不需要做其他处理
		}
		//关闭流
		if(br != null) {
			br.close();
		}
		if(inputStreamReader != null) {
			inputStreamReader.close();
		}
		if(errorStream != null) {
			errorStream.close();
		}
	}
	
	public static void main(String[] args) {
		
		MergeVideoMp3 ffmpeg = new MergeVideoMp3("G:\\video_tool\\ffmpeg\\bin\\ffmpeg.exe");
		try {
			ffmpeg.convertor("G:\\video_tool\\video01.mp4","G:\\video_tool\\bgm.mp3",10,"G:\\\\video_tool\\\\myvideo.mp4");
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}

}
