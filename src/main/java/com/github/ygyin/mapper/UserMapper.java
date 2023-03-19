package com.github.ygyin.mapper;

import com.github.ygyin.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(Integer goodsId);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer goodsId);

    int updateByPrimaryKey(User record);

    int updateByPrimaryKeySelective(User record);
}
