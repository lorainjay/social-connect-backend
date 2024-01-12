package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import com.hmdp.utils.ILock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock {

    //    业务名称，因为这个 SimpleRedisLock 实现类是通用的，所以key不能写死
    private String name;
    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX = "lock:";

    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    //    在项目一启动类加载时就加载static代码块，只加载一次，性能最好。
// DefaultRedisScript是实现类，泛型为脚本的返回值类型
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
//        因为要写不止一行，所以放到代码块
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
//        去类路径下找
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
//        设置返回值类型
        UNLOCK_SCRIPT.setResultType(Long.class);
    }



    @Override
    public void unlock() {
//        释放锁
//        stringRedisTemplate.delete(KEY_PREFIX + name);

    /*// 获取线程标示
    String threadId = ID_PREFIX + Thread.currentThread().getId();
    // 获取锁中的标示
    String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
    // 判断标示是否一致
    if(threadId.equals(id)) {
        // 释放锁
        stringRedisTemplate.delete(KEY_PREFIX + name);
    }*/
        // 调用lua脚本
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                // 生成单元素的集合：singletonList方法
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());
    }
    @Override
    public boolean tryLock(long timeoutSec) {
/*        // 获取线程标示
        long threadId = Thread.currentThread().getId();
        // 获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId+"", timeoutSec, TimeUnit.SECONDS);
        //自动拆箱，防止空指针
        return Boolean.TRUE.equals(success);*/

        // 获取线程标示
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);

    }

//    @Override
//    public void unlock() {
////        释放锁
////        stringRedisTemplate.delete(KEY_PREFIX + name);
//
//        // 获取线程标示
//        String threadId = ID_PREFIX + Thread.currentThread().getId();
//        // 获取锁中的标示
//        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
//        // 判断标示是否一致
//        if(threadId.equals(id)) {
//            // 释放锁
//            stringRedisTemplate.delete(KEY_PREFIX + name);
//        }
//
//    }
}