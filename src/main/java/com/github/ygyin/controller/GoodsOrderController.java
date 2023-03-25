package com.github.ygyin.controller;

import com.github.ygyin.service.GoodsOrderService;
import com.github.ygyin.service.UserService;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;


@Controller
public class GoodsOrderController {
    @Autowired
    private GoodsOrderService orderService;
    @Autowired
    private UserService userService;
    // 10 request will be released per second
    RateLimiter limiter = RateLimiter.create(10);
    private static final Logger MY_LOG = LoggerFactory.getLogger(GoodsOrderController.class);


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
        // The number of remain stock
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
}

