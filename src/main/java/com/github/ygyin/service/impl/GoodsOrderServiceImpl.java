package com.github.ygyin.service.impl;

import com.github.ygyin.entity.GoodsOrder;
import com.github.ygyin.entity.GoodsStock;
import com.github.ygyin.mapper.GoodsOrderMapper;
import com.github.ygyin.service.GoodsOrderService;
import com.github.ygyin.service.GoodsStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoodsOrderServiceImpl implements GoodsOrderService {

    @Autowired
    private GoodsStockService stockService;
    @Autowired
    private GoodsOrderMapper orderMapper;

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
        goodsSaleWithOcc(stock);
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


    /**
     * Only Create the order? (without user info)
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

    private void goodsSaleWithOcc(GoodsStock stock) {
        MY_LOG.info("Query DB and try to update the stock");
        int sales = stockService.updateStockByOcc(stock);
        if (sales == 0)
            throw new RuntimeException("并发更新库存失败，ver 不匹配");
//        return sales!=0;
    }
}
