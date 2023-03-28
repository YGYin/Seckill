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
     * Query balance: Through the database
     *
     * @param goodsId
     * @return The number of remaining balance
     */
    int getStockRemainByDB(int goodsId);

    /**
     * Query balance: Through the cache
     *
     * @param goodsId
     * @return The number of remaining balance
     */
    Integer getStockRemainByCache(int goodsId);


    /**
     * Update the info of goods stock in database(without optimistic lock)
     *
     * @param stock
     * @return Affected row
     */
    int updateGoodsStockById(GoodsStock stock);

    /**
     * Update the stock info in database(within optimistic lock)
     *
     * @param stock
     * @return
     */
    int updateStockByOcc(GoodsStock stock);

    /**
     * Use goods ID to get the stock info of the goods(For Update)
     *
     * @param goodsId
     * @return
     */
    GoodsStock getGoodsStockByIdForUpdate(int goodsId);

    /**
     * Writing the remaining stock into Redis cache
     *
     * @param goodsId
     * @param stockRemain
     */
    void setStockRemainToCache(int goodsId, int stockRemain);

    /**
     * Delete the cache of remaining stock of goods
     *
     * @param goodsId
     */
    void deleteStockCache(int goodsId);
}
