package com.nsw.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PublisherVideo {
    
    public UsersVO publisher;
    public boolean userLikeVideo;
    
    
	public UsersVO getPublisher() {
		return publisher;
	}
	public void setPublisher(UsersVO publisher) {
		this.publisher = publisher;
	}
	public boolean isUserLikeVideo() {
		return userLikeVideo;
	}
	public void setUserLikeVideo(boolean userLikeVideo) {
		this.userLikeVideo = userLikeVideo;
	}
    
    
    
}