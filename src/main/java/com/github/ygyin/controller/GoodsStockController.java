package com.github.ygyin.controller;

import com.github.ygyin.service.GoodsStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class GoodsStockController {
    private static final Logger MY_LOG = LoggerFactory.getLogger(GoodsStockController.class);
    @Autowired
    private GoodsStockService stockService;

    /**
     * Query the remaining stock by database
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/getRemainStockByDB/{goodsId}")
    public String getRemainStockByDB(@PathVariable int goodsId) {
        int stock;
        try {
            stock = stockService.getStockRemainByDB(goodsId);
        } catch (Exception e) {
            MY_LOG.error("Failed to query the remaining stock: [{}]", e.getMessage());
            return "Failed to query the remaining stock";
        }
        MY_LOG.info("Goods ID: [{}] Remain Stock: [{}]", goodsId, stock);
        return String.format("Goods ID: %d Remain Stock: %d", goodsId, stock);
    }

    // Todo: To rewrite the method by getStockCount()

    /**
     * Query the remaining stock by Redis cache.
     * Cache hit: Return the remaining stock.
     * Cache not hit: Query the DB then write the remaining stock into Redis
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/getRemainStockByCache/{goodsId}")
    public String getRemainStockByCache(@PathVariable int goodsId) {
        Integer stock;
        try {
            stock = stockService.getStockRemainByCache(goodsId);
            if (stock == null) {
                stock = stockService.getStockRemainByDB(goodsId);
                MY_LOG.info("Cache is not hit. Query the DB then write the remaining stock into cache");
                stockService.setStockRemainToCache(goodsId, stock);
            }
        } catch (Exception e) {
            MY_LOG.error("Failed to query the remaining stock: [{}]", e.getMessage());
            return "Failed to query the remaining stock";
        }
        MY_LOG.info("Goods ID: [{}] Remaining Stock: [{}]", goodsId, stock);
        return String.format("Goods ID: %d Remaining Stock: %d", goodsId, stock);
    }
}
