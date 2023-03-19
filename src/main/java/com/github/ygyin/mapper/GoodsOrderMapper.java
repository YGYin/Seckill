package com.github.ygyin.mapper;

import com.github.ygyin.entity.GoodsOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsOrderMapper {

    int deleteByPrimaryKey(Integer goodsId);

    int insert(GoodsOrder record);

    int insertSelective(GoodsOrder record);

    GoodsOrder selectByPrimaryKey(Integer goodsId);

    int updateByPrimaryKey(GoodsOrder record);

    int updateByPrimaryKeySelective(GoodsOrder record);

}
