package com.matt.mes.common.token;

public interface TokenStorage {

    /**
     * 存储用户Token
     *
     * @param userId 用户ID
     * @param token  Token值
     */
    void storeToken(Long userId, String token);

    /**
     * 获取用户当前Token
     *
     * @param userId 用户ID
     * @return Token值，不存在返回null
     */
    String getToken(Long userId);

    /**
     * 验证Token是否有效
     *
     * @param userId 用户ID
     * @param token  Token值
     * @return true=有效，false=无效
     */
    boolean validateToken(Long userId, String token);

    /**
     * 使Token失效
     *
     * @param userId 用户ID
     */
    void invalidateToken(Long userId);
}
