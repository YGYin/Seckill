package com.github.ygyin.utils;

public enum RedisSaltKey {
    HASH_KEY("hash_salt"),
    USER_LIMIT_KEY("user_access_limit_salt"),
    STOCK_REMAIN_KEY("stock_remain_salt"),
    ORDER_EXISTED_KEY("order_already_exists_salt");

    private String key;

    private RedisSaltKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
