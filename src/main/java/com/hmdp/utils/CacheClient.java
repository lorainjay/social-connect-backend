package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.events.Event;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.*;

@Slf4j
@Component
public class CacheClient {
     private StringRedisTemplate stringRedisTemplate;
     private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
     public CacheClient(StringRedisTemplate stringRedisTemplate){
          this.stringRedisTemplate = stringRedisTemplate;
     }
     public void set(String key, Object value, Long time, TimeUnit timeUnit){
          stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, timeUnit);
     }
     // 设置逻辑过期时间
     public void  setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){

          RedisData redisData = new RedisData();
          redisData.setData(value);
          redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
          // 写入redis
          stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value));
     }
     public <R,ID> R queryWithThrough(String prefix, ID id, Class<R> type, Function<ID,R> dbFailBack
     ,Long time, TimeUnit timeUnit){
          String key  = prefix +id;
          // 从redis查询商铺缓存
          String json = stringRedisTemplate.opsForValue().get(key);

          // 判断是否存在

          //如果存在, 返回商铺信息
          if(StrUtil.isNotBlank(json)){
          // 存在, 直接返回
               return JSONUtil.toBean(json, type);
          }
          // 判断命中的是否是空值
          if (json == ""){
               Result.fail("店铺不存在");
          }
          //如果不存在, 查询数据库
          R r = dbFailBack.apply(id);
          // 判断数据库信息是否存在

          //不存在则返回错误信息
          if (r == null){
               stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL,TimeUnit.MINUTES);
               return null;
          }
          // 存在, 则将商品信息写入redis
         this.set(key,r, time, timeUnit);
          return r;
     }
     // 逻辑过期解决缓存击穿
     public <R,ID>R queryWithLogicalExpire(String prefix, ID id, Class<R> type, Function<ID,R> dbFailBack
             ,Long time, TimeUnit timeUnit){
          String key  = prefix +id;
          // 从redis查询商铺缓存
          String json = stringRedisTemplate.opsForValue().get(key);
          // 1.判断是否存在
          //1.1 如果不存在
          if (StrUtil.isBlank(json)){
               return null;
          }
          //1.2如果命中, 先把Json反序列化为对象
          RedisData redisData =  JSONUtil.toBean(json, RedisData.class);
          R r = JSONUtil.toBean((JSONObject)redisData.getData(), type);
          LocalDateTime expireTime = redisData.getExpireTime();
          //2. 判断缓存是否过期
          // 2.1 如果没有过期, 返回商品信息
          if (expireTime.isAfter(LocalDateTime.now())){
               return r;
          }
          // 3. 如果过期, 则需要缓存重建
          String lockKey = LOCK_SHOP_KEY + id;
          boolean isLock = tryLock(lockKey);
          // 3.1 如果获取到了互斥锁
          try {
               if (isLock){
                    R r1 = dbFailBack.apply(id);
                    //开启独立线程, 实现缓存重建
                    CACHE_REBUILD_EXECUTOR.submit(()->{
                         //重建缓存
                         this.setWithLogicalExpire(key,r1,time,timeUnit);
                    });
               }
          } catch (Exception e) {
               throw new RuntimeException(e);
          } finally {
               // 释放锁
               delLock(lockKey);
          }
          // 如果过期, 则直接返回商品信息
          return r;
     }
     public boolean  tryLock(String key){
          Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10L, TimeUnit.SECONDS);
          return BooleanUtil.isTrue(flag);
     }
     private  void delLock(String key){
          stringRedisTemplate.delete(key);
     }

}
