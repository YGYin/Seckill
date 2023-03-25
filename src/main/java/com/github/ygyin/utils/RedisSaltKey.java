package com.github.ygyin.utils;

public enum RedisSaltKey {
    HASH_KEY("hash_salt"),

    USER_LIMIT_KEY("user_access_limit_salt");

    private String key;

    private RedisSaltKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
