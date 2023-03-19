package com.github.ygyin.mapper;

import com.github.ygyin.entity.GoodsStock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsStockMapper {

    int deleteByPrimaryKey(Integer goodsId);

    int insert(GoodsStock record);

    int insertSelective(GoodsStock record);

    GoodsStock selectByPrimaryKey(Integer goodsId);

    GoodsStock selectByPrimaryKeyForUpdate(Integer goodsId);

    int updateByOptimistic(GoodsStock record);

    int updateByPrimaryKey(GoodsStock record);

    int updateByPrimaryKeySelective(GoodsStock record);

}
