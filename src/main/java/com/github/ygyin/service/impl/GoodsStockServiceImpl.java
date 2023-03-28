package com.github.ygyin.service.impl;

import com.github.ygyin.entity.GoodsStock;
import com.github.ygyin.mapper.GoodsStockMapper;
import com.github.ygyin.service.GoodsStockService;
import com.github.ygyin.utils.RedisSaltKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class GoodsStockServiceImpl implements GoodsStockService {
    @Autowired
    private GoodsStockMapper stockMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Logger MY_LOG = LoggerFactory.getLogger(GoodsStockServiceImpl.class);

    @Override
    public GoodsStock getGoodsStockById(int goodsId) {
        return stockMapper.selectByPrimaryKey(goodsId);
    }

    @Override
    public int getStockRemainByDB(int goodsId) {
        GoodsStock stock = stockMapper.selectByPrimaryKey(goodsId);
        return stock.getBalance() - stock.getSales();
    }

    @Override
    public Integer getStockRemainByCache(int goodsId) {
        String hashKey = RedisSaltKey.STOCK_REMAIN_KEY.getKey() + "_" + goodsId;
        String stockRemain = redisTemplate.opsForValue().get(hashKey);
        return stockRemain != null ? Integer.parseInt(stockRemain) : null;
    }


    @Override
    public int updateGoodsStockById(GoodsStock stock) {
        return stockMapper.updateByPrimaryKeySelective(stock);
    }

    @Override
    public int updateStockByOcc(GoodsStock stock) {
        return stockMapper.updateByOcc(stock);
    }

    @Override
    public GoodsStock getGoodsStockByIdForUpdate(int goodsId) {
        return stockMapper.selectByPrimaryKeyForUpdate(goodsId);
    }

    @Override
    public void setStockRemainToCache(int goodsId, int stockRemain) {
        String hashKey = RedisSaltKey.STOCK_REMAIN_KEY.getKey() + "_" + goodsId;
        redisTemplate.opsForValue().set(hashKey, String.valueOf(stockRemain), 1800, TimeUnit.SECONDS);
        MY_LOG.info("Writing the remaining stock into Redis: [{}] [{}]", hashKey, stockRemain);
    }

    @Override
    public void deleteStockCache(int goodsId) {
        String hashKey = RedisSaltKey.STOCK_REMAIN_KEY.getKey() + "_" + goodsId;
        boolean delRes = Boolean.TRUE.equals(redisTemplate.delete(hashKey));
        MY_LOG.info("DELETE [{}]: The remaining stock cache of goods [{}]", delRes, goodsId);
    }


}
