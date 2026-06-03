package com.matt.mes.common.token;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存实现 TokenStorage
 */
@Component
public class InMemoryTokenStorage implements TokenStorage {

    private final Map<Long, String> tokenMap = new ConcurrentHashMap<>();

    @Override
    public void storeToken(Long userId, String token) {
        tokenMap.put(userId, token);
    }

    @Override
    public String getToken(Long userId) {
        return tokenMap.get(userId);
    }

    @Override
    public boolean validateToken(Long userId, String token) {
        if (userId == null || token == null) {
            return false;
        }
        String storedToken = tokenMap.get(userId);
        return token.equals(storedToken);
    }

    @Override
    public void invalidateToken(Long userId) {
        tokenMap.remove(userId);
    }
}
