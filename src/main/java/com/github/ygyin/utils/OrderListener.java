package com.github.ygyin.utils;

import com.alibaba.fastjson.JSONObject;
import com.github.ygyin.service.GoodsOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.ygyin.config.RabbitMqConfig.ORDER_QUEUE;

@Component
@RabbitListener(queues = ORDER_QUEUE)
public class OrderListener {
    private static final Logger MY_LOG = LoggerFactory.getLogger(OrderListener.class);
    @Autowired
    private GoodsOrderService orderService;

    @RabbitHandler
    public void queueHandler(String msg) {
        MY_LOG.info("Order queue receives the msg and begin to create order: " + msg);
        JSONObject jsonObj = JSONObject.parseObject(msg);

        try {
            Integer userId = jsonObj.getInteger("userId");
            Integer goodsId = jsonObj.getInteger("goodsId");
            orderService.createOrderByMQ(userId, goodsId);
        } catch (Exception e) {
            MY_LOG.error("Order MQ has exception: ", e);
        }
    }
}
