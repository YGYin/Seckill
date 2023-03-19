package com.github.ygyin.service.impl;

import com.github.ygyin.entity.GoodsStock;
import com.github.ygyin.mapper.GoodsStockMapper;
import com.github.ygyin.service.GoodsStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsStockServiceImpl implements GoodsStockService {
    @Autowired
    private GoodsStockMapper stockMapper;

    @Override
    public GoodsStock getGoodsStockById(int goodsId) {
        return stockMapper.selectByPrimaryKey(goodsId);
    }

    @Override
    public int updateGoodsStockById(GoodsStock stock) {
        return stockMapper.updateByPrimaryKeySelective(stock);
    }


}
