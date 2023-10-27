package com.nsw.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FFMpegTest {

	private String ffmpegEXE;
	
	public FFMpegTest(String ffmpegEXE) {
		super();
		this.ffmpegEXE = ffmpegEXE;
	}
	
	public void convertor(String videoInputPath,String VideoOutputPath) throws IOException {
		//ffmpeg -i input.mp4 output.avi   转换视频格式的命令
		
		List<String> command = new ArrayList<>();
		command.add(ffmpegEXE);
		command.add("-i");
		command.add(videoInputPath);
		command.add(VideoOutputPath);
		for (String string : command) {
			System.out.print(string);
		}
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
		
		FFMpegTest ffmpeg = new FFMpegTest("G:\\video_tool\\ffmpeg\\bin\\ffmpeg.exe");
		try {
			ffmpeg.convertor("G:\\video_tool\\video01.mp4", "G:\\\\video_tool\\\\niaho.avi");
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}

}
