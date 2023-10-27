package com.nsw.service;

import java.util.List;

import com.nsw.pojo.Bgm;

public interface BgmService {

	/**
	 * @Description: 查询背景音乐列表
	 */
	public List<Bgm> queryBgmList();
	/**
	 * 根据id查询bgm信息
	 * @param id
	 * @return
	 */
	public Bgm queryBgmById(String BgmId);
}
