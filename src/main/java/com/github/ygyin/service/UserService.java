package com.github.ygyin.service;

public interface UserService {
    /**
     * Get the hash for user verification
     *
     * @param userId
     * @param goodsId
     * @return MD5 verify hash
     * @throws Exception
     */
    String getHash(Integer userId, Integer goodsId) throws Exception;
}
