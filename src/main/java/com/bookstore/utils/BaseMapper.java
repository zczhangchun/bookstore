package com.bookstore.utils;

import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface BaseMapper<T,E> extends InsertListMapper<T>,DeleteByIdListMapper<T,E>, Mapper<T>, SelectByIdListMapper<T,E> {
}
