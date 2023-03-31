package com.github.ygyin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.ygyin.service.GoodsOrderService;
import com.github.ygyin.service.GoodsStockService;
import com.github.ygyin.service.UserService;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

import static com.github.ygyin.config.RabbitMqConfig.DELETE_QUEUE;
import static com.github.ygyin.config.RabbitMqConfig.ORDER_QUEUE;


@Controller
public class GoodsOrderController {
    @Autowired
    private GoodsOrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private AmqpTemplate mqTemplate;

    @Autowired
    private GoodsStockService stockService;
    // 10 request will be released per second
    RateLimiter limiter = RateLimiter.create(10);
    private static final Logger MY_LOG = LoggerFactory.getLogger(GoodsOrderController.class);

    private static final String SECKILL_SUCCEED = "Seckill Request Succeed";
    private static final String SECKILL_FAIL = "Seckill Request failed, server is in busy...";
    private static final String OUT_OF_STOCK = "Seckill Request failed, run out of remaining stock";


/*
    /**
     * 下单接口：该接口可能会导致超卖
     *
     * @param goodsId
     * @return String of id
     * /
    @RequestMapping("/createWrongOrder/{goodsId}")
    @ResponseBody
    public String createWrongOrder(@PathVariable int goodsId) {
        int id = 0;
        try {
            id = orderService.createWrongOrder(goodsId);
            MY_LOG.info("Create order id: [{}]", id);
        } catch (Exception e) {
            MY_LOG.error("Exception: ", e);
        }
        return String.valueOf(id);
    }
*/

    @RequestMapping("/createOccOrder/{goodsId}")
    @ResponseBody
    public String createOccOrder(@PathVariable int goodsId) {
        // 阻塞式获取令牌：请求进来后，若令牌桶内没有足够令牌，阻塞并等待令牌发放
        // MY_LOG.info("Waiting for a while: " + limiter.acquire());
        // 非阻塞式：请求进来后，若令牌桶内没有足够令牌，会尝试等待设置好的时间后看尝试能不能拿到令牌
        //          若不能拿到，直接返回抢购失败
        if (!limiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            MY_LOG.warn("Unfortunately, you've been cut off");
            return "Purchase failed, run out of stock";
        }
        int stock;
        try {
            stock = orderService.createOccOrder(goodsId);
            // why stock = balance?
            MY_LOG.info("Purchase successfully, remain stock: [{}]", stock);
        } catch (Exception e) {
            MY_LOG.error("Purchase failed: [{}]", e.getMessage());
            return "Purchase failed, run out of stock";
        }
        return String.format("Purchase successfully, remain stock: %d", stock);
    }

    /**
     * Use "for update" with transaction to update the stock
     *
     * @param goodsId
     * @return
     */
    @RequestMapping("/createPccOrder/{goodsId}")
    @ResponseBody
    public String createPccOrder(@PathVariable int goodsId) {
        int stock = 0;
        try {
            stock = orderService.createPccOrder(goodsId);
            MY_LOG.info("Purchase successfully, remain stock: [{}]", stock);
        } catch (Exception e) {
            MY_LOG.error("Purchase failed: [{}]", e.getMessage());
            return "Purchase failed, run out of stock";
        }
        return String.format("Purchase successfully, remain stock: %d", stock);
    }

    /**
     * Get validation hash value
     *
     * @param userId
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/getHash", method = {RequestMethod.GET})
    @ResponseBody
    public String getHash(@RequestParam(value = "userId") Integer userId,
                          @RequestParam(value = "goodsId") Integer goodsId) {
        String hash;
        try {
            hash = userService.getHash(userId, goodsId);
        } catch (Exception e) {
            MY_LOG.error("Get validation hash failed, reason: [{}]", e.getMessage());
            return "Get validation hash failed";
        }
        return String.format("Get validation hash successfully: %s", hash);
    }

    /**
     * Use the validation hash value to create an order
     *
     * @param userId
     * @param goodsId
     * @param hash
     * @return
     */
    @RequestMapping(value = "/createOrderWithHash", method = {RequestMethod.GET})
    @ResponseBody
    public String createOrderWithHash(@RequestParam(value = "userId") Integer userId,
                                      @RequestParam(value = "goodsId") Integer goodsId,
                                      @RequestParam(value = "hash") String hash) {
        // The number of remaining stock
        int stock;

        try {
            stock = orderService.createHashOrder(userId, goodsId, hash);
            MY_LOG.info("Purchase successfully, remain stock: [{}]", stock);
        } catch (Exception e) {
            MY_LOG.error("Purchase failed: [{}]", e.getMessage());
            return e.getMessage();
        }
        return String.format("Purchase successfully, remain stock: %d", stock);
    }

    @RequestMapping(value = "/createOrderWithHashAndAccessLimit", method = {RequestMethod.GET})
    @ResponseBody
    public String createOrderWithHashAndAccessLimit(@RequestParam(value = "userId") Integer userId,
                                                    @RequestParam(value = "goodsId") Integer goodsId,
                                                    @RequestParam(value = "hash") String hash) {
        // The number of remaining stock
        int stock;
        try {
            Long accessNum = userService.addUserAccess(userId);
            MY_LOG.info("Current access times of the user: [{}]", accessNum);
            boolean isBanned = userService.getUserStatus(userId);
            if (isBanned)
                return "Purchase failed. Exceeding frequency limit";

            stock = orderService.createHashOrder(userId, goodsId, hash);
            MY_LOG.info("Purchase successfully, remain stock: [{}]", stock);
        } catch (Exception e) {
            MY_LOG.error("Purchase failed: [{}]", e.getMessage());
            return e.getMessage();
        }
        return String.format("Purchase successfully, remain stock: %d", stock);
    }


    /**
     * Update the DB first, then delete the cache,
     * then retry to delete the cache
     *
     * @param goodsId
     * @return
     */
    @RequestMapping("/createOrderWithCache/{goodsId}")
    @ResponseBody
    public String createOrderWithCache(@PathVariable int goodsId) {
        // The number of remaining stock
        int stock;

        try {
            // TODO: It may change the method
            stock = orderService.createPccOrder(goodsId);
            MY_LOG.info("Finished the transaction of creating an order");
            // Delete the remaining stock cache
            stockService.deleteStockCache(goodsId);
            // TODO: Delete the cache again after a specified delay
            // ...
            // If the 2nd delete doesn't success, use MQ to delete the cache
            sendMsgToDeleteCache(String.valueOf(goodsId));
        } catch (Exception e) {
            MY_LOG.error("Purchase failed [{}]", e.getMessage());
            return "Purchase failed, run out of remaining stock";
        }
        MY_LOG.info("Purchase successfully, remain stock: [{}]", stock);
        return String.format("Purchase successfully, remain stock: %d", stock);
    }

    /**
     * 1. Check if the user has already placed an order in the cache
     * 2. If not snapped up, check the cache for remaining stock
     * 3. If cache miss, check the DB for remaining stock
     * 4. Has remaining stock, send userId and goodsId to MQ to deal with it
     *
     * @param userId
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "createOrderWithMQ", method = {RequestMethod.GET})
    @ResponseBody
    public String createOrderWithMQ(@RequestParam(value = "userId") Integer userId,
                                    @RequestParam(value = "goodsId") Integer goodsId) {
        try {
            // Check if the user has already placed an order in the cache
//            Boolean orderExist = orderService.checkOrderInCache(userId, goodsId);
//            if (orderExist != null && orderExist) {
//                MY_LOG.info("The user has already snapped it up");
//                return "The user has already snapped it up";
//            }

            // Not snapped up, check whether the remaining stock is in the cache
            // If not, it will check the DB for remaining stock
            MY_LOG.info("The user has not snapped up any items. To check if there is any stock in the cache");
            Integer stockRemain = stockService.getStockRemain(goodsId);
            if (stockRemain == 0)
                return OUT_OF_STOCK;

            // It has remaining stock
            // 将 userId & goodId 封装为消息体传给 MQ 处理
            // 此时有库存为查缓存中的结论，可能有脏数据，MQ 会再次查表验证库存
            MY_LOG.info("Goods [{}] has remaining stock: [{}]", goodsId, stockRemain);
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("userId", userId);
            jsonObj.put("goodsId", goodsId);
            sendMsgToOrderQueue(jsonObj.toJSONString());
            return SECKILL_SUCCEED;
        } catch (Exception e) {
            MY_LOG.error("API createOrderWithMQ: Asynchronously processing order exceptions", e);
            return SECKILL_FAIL;
        }
    }

    /**
     * Send msg to MQ ORDER_QUEUE
     *
     * @param msg
     */
    private void sendMsgToOrderQueue(String msg) {
        MY_LOG.info("通知消息队列开始下单: [{}]", msg);
        mqTemplate.convertAndSend(ORDER_QUEUE, msg);
    }

    /**
     * Send msg to MQ DELETE_QUEUE
     *
     * @param msg
     */
    private void sendMsgToDeleteCache(String msg) {
        MY_LOG.info("通知消息队列开始再次尝试删除缓存: [{}]", msg);
        mqTemplate.convertAndSend(DELETE_QUEUE, msg);
    }
}

