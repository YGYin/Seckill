package com.github.ygyin.service.impl;

import com.github.ygyin.entity.GoodsStock;
import com.github.ygyin.entity.User;
import com.github.ygyin.mapper.UserMapper;
import com.github.ygyin.service.GoodsOrderService;
import com.github.ygyin.service.GoodsStockService;
import com.github.ygyin.service.UserService;
import com.github.ygyin.utils.RedisSaltKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger MY_LOG = LoggerFactory.getLogger(GoodsOrderService.class);
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GoodsStockService stockService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String RANDOM_SALT = "addSalt";

    @Override
    public String getHash(Integer userId, Integer goodsId) throws Exception {
        // Verify whether the time is in seckill by yourself
        MY_LOG.info("Please make sure that it is in seckill time");

        // Verify user's validity
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null)
            throw new Exception("User not found");
        MY_LOG.info("User info: [{}]", user);

        // Verify goods' validity
        GoodsStock stock = stockService.getGoodsStockById(goodsId);
        if (stock == null)
            throw new Exception("Goods not found");
        MY_LOG.info("Goods info: [{}]", stock);

        // Generate the hash
        String info = RANDOM_SALT + userId + goodsId;
        String md5Hash = DigestUtils.md5DigestAsHex(info.getBytes());

        // Store the info and md5 Hash into the redis
        String hashKey = RedisSaltKey.HASH_KEY.getKey() + "_" + userId + "_" + goodsId;
        // Write your md5 hash value into redis, cache for 1800s
        // If the user doesn't purchase in 1800s, it needs to re-get the hash value
        redisTemplate.opsForValue().set(hashKey, md5Hash, 1800, TimeUnit.SECONDS);
        MY_LOG.info("Redis writing: [{}] [{}]", hashKey, md5Hash);
        return md5Hash;
    }
}
