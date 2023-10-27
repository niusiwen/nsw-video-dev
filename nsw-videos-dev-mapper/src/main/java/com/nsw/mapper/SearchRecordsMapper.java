package com.nsw.mapper;

import java.util.List;

import com.nsw.pojo.SearchRecords;
import com.nsw.utils.MyMapper;

public interface SearchRecordsMapper extends MyMapper<SearchRecords> {
	
	public List<String> getHotwords();
}