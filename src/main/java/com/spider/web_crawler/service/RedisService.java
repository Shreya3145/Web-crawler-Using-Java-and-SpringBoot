package com.spider.web_crawler.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String VISITED_KEY = "crawler:visited";

    public RedisService(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    public boolean isVisited(String url){
        return redisTemplate.opsForSet().isMember(VISITED_KEY, url);
    }
    public void markVisited(String url){
        redisTemplate.opsForSet().add(VISITED_KEY, url);
    }
    public void clearVisited(){
        redisTemplate.delete(VISITED_KEY);
    }
}
