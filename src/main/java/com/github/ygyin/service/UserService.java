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

    /**
     * Add the number of user access goods into Redis
     *
     * @param userId
     * @return The number of user access
     * @throws Exception
     */
    Long addUserAccess(Integer userId) throws Exception;

    /**
     * Check the number of user access whether it is larger than we allow
     * or whether it has access history
     *
     * @param userId
     * @return true: banned
     */
    boolean getUserStatus(Integer userId);
}
