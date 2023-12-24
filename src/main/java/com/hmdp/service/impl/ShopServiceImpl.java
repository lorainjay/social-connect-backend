package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
       // Shop shop = queryWithThrough(id);

//        Shop shop = queryWithMutex(id);
        // 调用封装的Redis工具
        CacheClient cacheClient = new CacheClient(stringRedisTemplate);

        Shop shop = cacheClient.queryWithThrough(CACHE_SHOP_KEY,id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
//1
//        Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY,id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.SECONDS);
        if(shop == null)
            return  Result.fail("店铺不存在");
        return Result.ok(shop);
    }
//    public Shop queryWithMutex(Long id){
//        String key  = CACHE_SHOP_KEY +id;
//        // 从redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 判断是否存在
//
//        //如果存在, 返回商铺信息
//        if(StrUtil.isNotBlank(shopJson)){
//            Shop shop = JSONUtil.toBean(shopJson,Shop.class);
//            return shop;
//        }
//        // 判断命中的是否是空值
//        if (shopJson == ""){
//            Result.fail("店铺不存在");
//        }
//        //1.如果不存在, 尝试获取互斥锁
//        String lock_key = "lock:shop"+key;
//        boolean flag = tryLock(lock_key);
//        // 1.1 判断是否获取锁
//        //1.2 没有获得互斥锁=>  休眠
//        Shop shop = null;
//        try {
//            if (!flag){
//                Thread.sleep(50);
//                return queryWithMutex(id);
//            }
//            //1.3 获得互斥锁=> 根据id查询数据库
//            shop = getById(id);
//            // 判断数据库信息是否存在
//
//            //不存在则返回错误信息
//            if (shop == null){
//                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL,TimeUnit.MINUTES);
//                return null;
//            }
//            //1.4 将商铺数据写入redis
//            stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            //1.5 释放互斥锁
//            delLock(key);
//        }
//
//        return shop;
//    }
//    public Shop queryWithThrough(Long id){
//        String key  = CACHE_SHOP_KEY +id;
//        // 从redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 判断是否存在
//
//        //如果存在, 返回商铺信息
//        if(StrUtil.isNotBlank(shopJson)){
//            Shop shop = JSONUtil.toBean(shopJson,Shop.class);
//            return shop;
//        }
//        // 判断命中的是否是空值
//        if (shopJson == ""){
//            Result.fail("店铺不存在");
//        }
//        //如果不存在, 查询数据库
//        Shop shop = getById(id);
//        // 判断数据库信息是否存在
//
//        //不存在则返回错误信息
//        if (shop == null){
//            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL,TimeUnit.MINUTES);
//            return null;
//        }
//        // 存在, 则将商品信息写入redis
//        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        return shop;
//    }
//    public Shop queryWithLogicalExpire(Long id){
//        String key  = CACHE_SHOP_KEY +id;
//        // 从redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 1.判断是否存在
//
//        //1.1 如果不存在
//        if (StrUtil.isBlank(shopJson)){
//            return null;
//        }
//        //1.2如果命中, 先把Json反序列化为对象
//        RedisData redisData =  JSONUtil.toBean(shopJson, RedisData.class);
//        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
//        LocalDateTime expireTime = redisData.getExpireTime();
//        //2. 判断缓存是否过期
//        // 2.1 如果没有过期, 返回商品信息
//       if (expireTime.isAfter(LocalDateTime.now())){
//           return shop;
//       }
//       // 3. 如果过期, 则需要缓存重建
//        String lockKey = LOCK_SHOP_KEY + id;
//        boolean isLock = tryLock(lockKey);
//        // 3.1 如果获取到了互斥锁
//        try {
//            if (isLock){
//                //开启独立线程, 实现缓存重建
//                CACHE_REBUILD_EXECUTOR.submit(()->{
//                    //重建缓存
//                    this.saveShop2RedisData(id,20L);
//                });
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            // 释放锁
//            delLock(lockKey);
//        }
//        // 如果过期, 则直接返回商品信息
//        return shop;
//    }
//    public boolean  tryLock(String key){
//        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10L, TimeUnit.SECONDS);
//        return BooleanUtil.isTrue(flag);
//    }
//    private  void delLock(String key){
//        stringRedisTemplate.delete(key);
//    }
//    private void saveShop2RedisData(Long id, Long expireTime){
//        // 查询店铺数据
//        Shop shop = getById(id);
//        //封装逻辑过期时间
//        RedisData redisData = new RedisData();
//        redisData.setData(shop);
//        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireTime));
//        // 将数据写入redis, 并设置过期时间
//       stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id, JSONUtil.toJsonStr(redisData));
//
//    }


    @Override
    @Transactional
    public Result update(Shop shop) {
        if (shop.getId()==null){
            return Result.fail("店铺不存在");
        }
        // 更新数据库
        updateById(shop);
        //删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY+shop.getId());

        return Result.ok();
    }
}
