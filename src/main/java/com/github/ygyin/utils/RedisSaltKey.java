package com.github.ygyin.utils;

public enum RedisSaltKey {
    HASH_KEY("hash_salt");

    private String key;

    private RedisSaltKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
