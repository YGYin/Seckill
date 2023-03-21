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
}
