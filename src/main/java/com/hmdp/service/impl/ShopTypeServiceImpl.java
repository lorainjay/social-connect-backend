package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.events.StreamEndEvent;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_LIST_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {


    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryTypeList() {
        // 1. 先查询缓存，看看缓存里面有没有
        String list = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE_LIST_KEY);
        // 2. 命中就直接返回
        if (StrUtil.isNotBlank(list)){
            List<ShopType> shopTypeList = JSONUtil.toList(list, ShopType.class);
            return Result.ok(shopTypeList);
        }
        // 3. 没有命中就查询数据库
        List<ShopType> list2 = query().orderByAsc("sort").list();

        // 4. 判断数据库是否为空

        // 4.1 如果为空
        if (list2.isEmpty()){
            return Result.fail("商铺类型不存在");
        }
        // 4.2 存在，返回数据，保存到redis中
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE_LIST_KEY, JSONUtil.toJsonStr(list2));
        return Result.ok(list2);
    }
}
