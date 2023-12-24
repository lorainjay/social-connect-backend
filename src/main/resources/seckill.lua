-- 1.1 订单id
local voucherId = ARGV[1]
-- 1.2 用户id
local useId = ARGV[2]

-- 数据key
-- 2.1 库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 2.2 订单key
local orderKey = 'seckill:order:' .. voucherId

-- 3.脚本业务
-- 3.1 判断库存是否充足 get stockKey
if (tonumber(redis.call('get',stockKey)) <= 0) then
    -- 不足返回1
    return 1

end
-- 3.2 库存充足,sismember看userId是否存在于orderId
if (redis.call('sismember', orderKey, userId) == 1) then
    -- 3.3 如果存在,说明下单了,返回2
    return 2
end
-- 3.4 扣减库存 incrby stockKey -1
redis.call('incriby', stockKey, -1)
-- 3.5下单(保存用户)
redis.call('sadd', orderKey, userId)