package com.github.ygyin.service;

public interface GoodsOrderService {

    /**
     * Create a wrong order
     *
     * @param goodsId
     * @return orderId
     */
    public int createWrongOrder(int goodsId);


}
