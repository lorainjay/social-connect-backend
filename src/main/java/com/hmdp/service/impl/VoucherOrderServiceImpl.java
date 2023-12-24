package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Voucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisWorker;
import com.hmdp.utils.SimpleRedis;
import com.hmdp.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    SeckillVoucherServiceImpl seckillVoucherService;
    @Resource
    RedisWorker redisWorker;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Override
    @Transactional
    public Result seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        // 1. 提交优惠券id
        //2. 查询优惠券信息
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        //3. 判断秒杀是否开始
        //3.1 如果没有开始, 返回异常结果
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())){
            Result.fail("秒杀尚未开始");
        }
        // 3.2 判断秒杀是否已经结束
        if (LocalDateTime.now().isAfter(voucher.getEndTime())){
            Result.fail("秒杀已经结束");
        }
        // 4. 秒杀开始, 判断库存是否充足
        if (voucher.getStock() < 1){
            //4.1 不足, 返回异常结果
            Result.fail("库存不足");
        }
        // 创建锁对象
        SimpleRedis lock = new SimpleRedis("order:" + userId, stringRedisTemplate);
        // 获取锁
        boolean isLock = lock.tryLock(1200);
        if (!isLock){
            //获取锁失败
            return Result.fail("不允许重复下单");
        }
        try {
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            lock.unLock();
        }
        return null;
    }
    @Transactional
        public Result createVoucherOrder(Long voucherId){

            // 实现一人一单
            Long userId = UserHolder.getUser().getId();
            int count = query().eq("user_id",userId).eq("voucher_id",voucherId).count();
            if (count > 0){
                Result.fail("用户已经购买过一次");
            }
            //4.2 充足, 扣减库存
            boolean isSuccess = seckillVoucherService.update().setSql("stock=stock-1")
                    .eq("voucher_id", voucherId).gt("stock",0) // 如果大于0就能购买优惠券
                    .update();
            if (!isSuccess){
                return  Result.fail("库存不足");
            }
            //4.3 创建订单
            VoucherOrder voucherOrder = new VoucherOrder();
            long orderId = redisWorker.nextId("order");
            voucherOrder.setId(orderId);

            voucherOrder.setUserId(userId);

            voucherOrder.setVoucherId(voucherId);
            save(voucherOrder);
            //4.4 返回订单id

            return Result.ok(orderId);
        }
}
