package com.github.ygyin.service;

public interface GoodsOrderService {

    /**
     * Create a wrong order
     *
     * @param goodsId
     * @return orderId
     */
    public int createWrongOrder(int goodsId);

    /**
     * Create a proper order (with optimistic lock)
     *
     * @param goodsId
     * @return The number of goods remain
     * @throws Exception
     */
    int createOccOrder(int goodsId) throws Exception;

    int createPccOrder(int goodsId);

    int createHashOrder(Integer userId, Integer goodsId, String hash) throws Exception;

    /**
     * Check whether the user already has an order in the cache
     *
     * @param userId
     * @param goodsId
     * @return
     * @throws Exception
     */
    Boolean checkOrderInCache(Integer userId, Integer goodsId) throws Exception;

    /**
     * Create a proper order: Double-check the remaining stock
     * Order with OCC
     * Update the order info to cache
     *
     * @param userId
     * @param goodsId
     * @throws Exception
     */
    void createOrderByMQ(Integer userId, Integer goodsId) throws Exception;
}
