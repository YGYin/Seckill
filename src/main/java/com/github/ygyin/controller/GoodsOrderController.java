package com.github.ygyin.controller;

import com.github.ygyin.service.GoodsOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class GoodsOrderController {
    @Autowired
    private GoodsOrderService orderService;


    private static final Logger MY_LOG = LoggerFactory.getLogger(GoodsOrderController.class);


    /**
     * 下单接口：该接口可能会导致超卖
     *
     * @param goodsId
     * @return String of id
     */
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
}
