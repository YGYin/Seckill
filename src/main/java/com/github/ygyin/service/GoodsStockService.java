package com.github.ygyin.service;

import com.github.ygyin.entity.GoodsStock;

public interface GoodsStockService {
    /**
     * Use goods ID to get the stock info of the goods
     *
     * @param goodsId
     * @return Goods Stock
     */
    GoodsStock getGoodsStockById(int goodsId);

    /**
     * Update the info of goods stock in database (without optimistic lock)
     *
     * @param stock
     * @return Affected row
     */
    int updateGoodsStockById(GoodsStock stock);
}
