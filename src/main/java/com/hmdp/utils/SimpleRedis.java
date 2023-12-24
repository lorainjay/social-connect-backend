package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SimpleRedis implements ILock {
    private String name;
    StringRedisTemplate stringRedisTemplate;


    public SimpleRedis(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX="lock :";
    private static final String ID_PREFIX = UUID.randomUUID().toString()+"-";
    @Override
    public boolean tryLock(long longTimeSec) {

        //获取线程标识

        String id = ID_PREFIX + Thread.currentThread().getId();
        //获取锁
        Boolean aBoolean = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, id + "", longTimeSec, TimeUnit.SECONDS);
        // 避免自动拆箱
        return Boolean.TRUE.equals(aBoolean) ;
    }

    @Override
    public void unLock() {
        // 获取线程标识
        String thread_id = ID_PREFIX + Thread.currentThread().getId();
        //获取锁中的标示
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX+name);
        // 判断标示是否一致
        if (thread_id.equals(id))
        stringRedisTemplate.delete(KEY_PREFIX + name);
    }
}
