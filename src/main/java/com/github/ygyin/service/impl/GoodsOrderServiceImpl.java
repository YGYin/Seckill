package com.github.ygyin.service.impl;

import com.github.ygyin.entity.GoodsOrder;
import com.github.ygyin.entity.GoodsStock;
import com.github.ygyin.mapper.GoodsOrderMapper;
import com.github.ygyin.service.GoodsOrderService;
import com.github.ygyin.service.GoodsStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsOrderServiceImpl implements GoodsOrderService {

    @Autowired
    private GoodsStockService stockService;
    @Autowired
    private GoodsOrderMapper orderMapper;

    @Override
    public int createWrongOrder(int goodsId) {
        // check the stock if remained
        GoodsStock stock = reviewStock(goodsId);
        // reduce the stock
        goodsSale(stock);
        // create the order
        return createOrder(stock);
    }

    /**
     * Only Create the order? (without user info)
     *
     * @param stock
     * @return
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

    /**
     * Goods are sale, refresh the stock
     *
     * @param stock
     */
    private void goodsSale(GoodsStock stock) {
        stock.setSales(stock.getSales() + 1);
        stockService.updateGoodsStockById(stock);
    }
}
