package com.github.ygyin.service.impl;

import com.github.ygyin.entity.GoodsOrder;
import com.github.ygyin.entity.GoodsStock;
import com.github.ygyin.entity.User;
import com.github.ygyin.mapper.GoodsOrderMapper;
import com.github.ygyin.mapper.UserMapper;
import com.github.ygyin.service.GoodsOrderService;
import com.github.ygyin.service.GoodsStockService;
import com.github.ygyin.utils.RedisSaltKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoodsOrderServiceImpl implements GoodsOrderService {

    @Autowired
    private GoodsStockService stockService;
    @Autowired
    private GoodsOrderMapper orderMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserMapper userMapper;

    private static final Logger MY_LOG = LoggerFactory.getLogger(GoodsOrderService.class);

    @Override
    public int createWrongOrder(int goodsId) {
        // check the stock if remained
        GoodsStock stock = reviewStock(goodsId);
        // reduce the stock
        goodsSale(stock);
        // create the order
        return createOrder(stock);
    }

    @Override
    public int createOccOrder(int goodsId) throws Exception {
        // check the stock if remained
        GoodsStock stock = reviewStock(goodsId);
        // Use optimistic lock to update the stock
        if (!goodsSaleWithOcc(stock))
            throw new RuntimeException("Version is outdated, sale updated with OCC failed");
        // Create order
        int order = createOrder(stock);
        return stock.getBalance() - (stock.getSales() + 1);
    }

    // 如果遇到回滚，则返回 Exception
    // 事务传播 REQUIRED 代表支持当前事务，如果当前没有事务，就新建一个事务
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int createPccOrder(int goodsId) {
        // check the stock if remained(with Pessimistic lock for update)
        // 行锁
        GoodsStock stock = reviewStockForUpdate(goodsId);
        // update the stock
        goodsSale(stock);
        // create the order
        int id = createOrder(stock);
        return stock.getBalance() - stock.getSales();
    }

    @Override
    public int createHashOrder(Integer userId, Integer goodsId, String hash) throws Exception {
        // Verify whether the time is in seckill by yourself
        MY_LOG.info("Please make sure that it is in seckill time");

        String hashKey = RedisSaltKey.HASH_KEY.getKey() + "_" + userId + "_" + goodsId;
        String hashInRedis = redisTemplate.opsForValue().get(hashKey);
        if (!hash.equals(hashInRedis))
            throw new Exception("Hash value is not as the same as it in Redis");
        MY_LOG.info("Hash value verified successfully");

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

        // Update the stock by optimistic lock, sales ++
        if (!goodsSaleWithOcc(stock))
            throw new RuntimeException("Version is outdated, sale updated with OCC failed");
        MY_LOG.info("Update the stock with OCC successfully");

        // Create the order
        // todo 此处 stock 为乐观锁更新库存前获取的对象，导致最后返回库存剩余数目时需要加 1
        createOrderWithInfoInDB(userId, stock);
        MY_LOG.info("Create order with user info successfully");

        return stock.getBalance() - (stock.getSales() + 1);
    }

    /**
     * Create the order with user info and write to the DB
     *
     * @param userId
     * @param stock
     * @return
     */
    private int createOrderWithInfoInDB(Integer userId, GoodsStock stock) {
        GoodsOrder order = new GoodsOrder();
        order.setUserId(userId);
        order.setGoodsId(stock.getGoodsId());
        order.setGoodsName(stock.getGoodsName());
        return orderMapper.insertSelective(order);
    }

    /**
     * Only Create the order (without user info)
     *
     * @param stock
     * @return The stock of goods
     */
    private int createOrder(GoodsStock stock) {
        GoodsOrder order = new GoodsOrder();
        order.setGoodsId(stock.getGoodsId());
        order.setGoodsName(stock.getGoodsName());
        // todo: return what parameter?
        return orderMapper.insertSelective(order);
    }

    /**
     * Review the stock of goods,
     *
     * @param goodsId
     * @return GoodsStock if success
     */
    private GoodsStock reviewStock(int goodsId) {
        GoodsStock stock = stockService.getGoodsStockById(goodsId);
        if (stock.getSales().equals(stock.getBalance()))
            throw new RuntimeException("Out of stock.");
        return stock;
    }

    private GoodsStock reviewStockForUpdate(int goodsId) {
        GoodsStock stock = stockService.getGoodsStockByIdForUpdate(goodsId);
        if (stock.getSales().equals(stock.getBalance()))
            throw new RuntimeException("Out of stock.");
        return stock;
    }

    /**
     * Goods are sale, refresh the stock
     *
     * @param stock
     */
    private void goodsSale(GoodsStock stock) {
        stock.setSales(stock.getSales() + 1);
        stockService.updateGoodsStockById(stock);
    }

    private boolean goodsSaleWithOcc(GoodsStock stock) {
        MY_LOG.info("Query DB and try to update the stock");
        int sales = stockService.updateStockByOcc(stock);
        if (sales == 0)
            throw new RuntimeException("并发更新库存失败，ver 不匹配");
        return sales != 0;
    }
}
