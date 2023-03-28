package com.github.ygyin.utils;

import com.github.ygyin.service.GoodsStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.ygyin.config.RabbitMqConfig.DELETE_QUEUE;

@Component
@RabbitListener(queues = DELETE_QUEUE)
public class DeleteCacheListener {
    private static final Logger MY_LOG = LoggerFactory.getLogger(DeleteCacheListener.class);
    @Autowired
    private GoodsStockService stockService;

    @RabbitHandler
    public void queueHandler(String msg) {
        MY_LOG.info("DeleteCacheListener 收到信息: " + msg);
        MY_LOG.info("DeleteCacheListener 开始删除缓存: " + msg);
        stockService.deleteStockCache(Integer.parseInt(msg));
    }

}
