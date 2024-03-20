# ConnectHub
## 简介
ConnectHub一个以用户点评和商户信息为主要内容的本地生活服务平台，基于SpringBoot实现了登录注册、点赞等功能能。项目注重利用Redis的特性解决不同业务业务场暴中的问题，核心工作包括设计并实现缓存更新策略，解决缓存击穿、缓存穿透等问题，解决优惠券超卖的线程安全问题，并基于Redis实现分布式锁。
## 架构图
![image](https://github.com/axdmdYJ/social-connect-backend/assets/121956515/68de3479-9cf7-4965-aeb4-3b528509dfba)
## 项目目录
```
├── README.md
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── hmdp
│   │   │           ├── HmDianPingApplication.java
│   │   │           ├── config
│   │   │           │   ├── MvcConfig.java
│   │   │           │   ├── MybatisConfig.java
│   │   │           │   ├── RedissonConfig.java
│   │   │           │   └── WebExceptionAdvice.java
│   │   │           ├── controller
│   │   │           │   ├── BlogCommentsController.java
│   │   │           │   ├── BlogController.java
│   │   │           │   ├── FollowController.java
│   │   │           │   ├── ShopController.java
│   │   │           │   ├── ShopTypeController.java
│   │   │           │   ├── UploadController.java
│   │   │           │   ├── UserController.java
│   │   │           │   ├── VoucherController.java
│   │   │           │   └── VoucherOrderController.java
│   │   │           ├── dto
│   │   │           │   ├── LoginFormDTO.java
│   │   │           │   ├── Result.java
│   │   │           │   ├── ScrollResult.java
│   │   │           │   └── UserDTO.java
│   │   │           ├── entity
│   │   │           │   ├── Blog.java
│   │   │           │   ├── BlogComments.java
│   │   │           │   ├── Follow.java
│   │   │           │   ├── SeckillVoucher.java
│   │   │           │   ├── Shop.java
│   │   │           │   ├── ShopType.java
│   │   │           │   ├── User.java
│   │   │           │   ├── UserInfo.java
│   │   │           │   ├── Voucher.java
│   │   │           │   └── VoucherOrder.java
│   │   │           ├── mapper
│   │   │           │   ├── BlogCommentsMapper.java
│   │   │           │   ├── BlogMapper.java
│   │   │           │   ├── FollowMapper.java
│   │   │           │   ├── SeckillVoucherMapper.java
│   │   │           │   ├── ShopMapper.java
│   │   │           │   ├── ShopTypeMapper.java
│   │   │           │   ├── UserInfoMapper.java
│   │   │           │   ├── UserMapper.java
│   │   │           │   ├── VoucherMapper.java
│   │   │           │   └── VoucherOrderMapper.java
│   │   │           ├── mdc
│   │   │           │   ├── MdcAspect.java
│   │   │           │   ├── MdcDot.java
│   │   │           │   ├── MdcUtil.java
│   │   │           │   └── SelfTraceIdGenerator.java
│   │   │           ├── mq
│   │   │           │   ├── AsyncSaveVoucherListener.java
│   │   │           │   └── RabbitmqConfig.java
│   │   │           ├── service
│   │   │           │   ├── IBlogCommentsService.java
│   │   │           │   ├── IBlogService.java
│   │   │           │   ├── IFollowService.java
│   │   │           │   ├── ISeckillVoucherService.java
│   │   │           │   ├── IShopService.java
│   │   │           │   ├── IShopTypeService.java
│   │   │           │   ├── IUserInfoService.java
│   │   │           │   ├── IUserService.java
│   │   │           │   ├── IVoucherOrderService.java
│   │   │           │   ├── IVoucherService.java
│   │   │           │   └── impl
│   │   │           │       ├── BlogCommentsServiceImpl.java
│   │   │           │       ├── BlogServiceImpl.java
│   │   │           │       ├── FollowServiceImpl.java
│   │   │           │       ├── SeckillVoucherServiceImpl.java
│   │   │           │       ├── ShopServiceImpl.java
│   │   │           │       ├── ShopTypeServiceImpl.java
│   │   │           │       ├── UserInfoServiceImpl.java
│   │   │           │       ├── UserServiceImpl.java
│   │   │           │       ├── VoucherOrderServiceImpl.java
│   │   │           │       └── VoucherServiceImpl.java
│   │   │           └── utils
│   │   │               ├── CacheClient.java
│   │   │               ├── ILock.java
│   │   │               ├── IpUtil.java
│   │   │               ├── LoginInterceptor.java
│   │   │               ├── PasswordEncoder.java
│   │   │               ├── RedisConstants.java
│   │   │               ├── RedisData.java
│   │   │               ├── RedisWorker.java
│   │   │               ├── RefreshIntercepter.java
│   │   │               ├── RegexPatterns.java
│   │   │               ├── RegexUtils.java
│   │   │               ├── SimpleRedisLock.java
│   │   │               ├── SystemConstants.java
│   │   │               └── UserHolder.java
│   │   └── resources
│   │       ├── application.yaml
│   │       ├── db
│   │       │   └── hmdp.sql
│   │       ├── mapper
│   │       │   └── VoucherMapper.xml
│   │       ├── seckill.lua
│   │       └── unlock.lua
│   └── test
│       └── java
│           └── com
│               └── hmdp
│                   └── HmDianPingApplicationTests.java
└── target
    ├── classes
    │   ├── application.yaml
    │   ├── com
    │   │   └── hmdp
    │   │       ├── HmDianPingApplication.class
    │   │       ├── config
    │   │       │   ├── MvcConfig.class
    │   │       │   ├── MybatisConfig.class
    │   │       │   ├── RedissonConfig.class
    │   │       │   └── WebExceptionAdvice.class
    │   │       ├── controller
    │   │       │   ├── BlogCommentsController.class
    │   │       │   ├── BlogController.class
    │   │       │   ├── FollowController.class
    │   │       │   ├── ShopController.class
    │   │       │   ├── ShopTypeController.class
    │   │       │   ├── UploadController.class
    │   │       │   ├── UserController.class
    │   │       │   ├── VoucherController.class
    │   │       │   └── VoucherOrderController.class
    │   │       ├── dto
    │   │       │   ├── LoginFormDTO.class
    │   │       │   ├── Result.class
    │   │       │   ├── ScrollResult.class
    │   │       │   └── UserDTO.class
    │   │       ├── entity
    │   │       │   ├── Blog.class
    │   │       │   ├── BlogComments.class
    │   │       │   ├── Follow.class
    │   │       │   ├── SeckillVoucher.class
    │   │       │   ├── Shop.class
    │   │       │   ├── ShopType.class
    │   │       │   ├── User.class
    │   │       │   ├── UserInfo.class
    │   │       │   ├── Voucher.class
    │   │       │   └── VoucherOrder.class
    │   │       ├── mapper
    │   │       │   ├── BlogCommentsMapper.class
    │   │       │   ├── BlogMapper.class
    │   │       │   ├── FollowMapper.class
    │   │       │   ├── SeckillVoucherMapper.class
    │   │       │   ├── ShopMapper.class
    │   │       │   ├── ShopTypeMapper.class
    │   │       │   ├── UserInfoMapper.class
    │   │       │   ├── UserMapper.class
    │   │       │   ├── VoucherMapper.class
    │   │       │   └── VoucherOrderMapper.class
    │   │       ├── mdc
    │   │       │   ├── MdcAspect.class
    │   │       │   ├── MdcDot.class
    │   │       │   ├── MdcUtil.class
    │   │       │   └── SelfTraceIdGenerator.class
    │   │       ├── mq
    │   │       │   ├── AsyncSaveVoucherListener.class
    │   │       │   ├── RabbitmqConfig$1.class
    │   │       │   ├── RabbitmqConfig$2.class
    │   │       │   └── RabbitmqConfig.class
    │   │       ├── service
    │   │       │   ├── IBlogCommentsService.class
    │   │       │   ├── IBlogService.class
    │   │       │   ├── IFollowService.class
    │   │       │   ├── ISeckillVoucherService.class
    │   │       │   ├── IShopService.class
    │   │       │   ├── IShopTypeService.class
    │   │       │   ├── IUserInfoService.class
    │   │       │   ├── IUserService.class
    │   │       │   ├── IVoucherOrderService.class
    │   │       │   ├── IVoucherService.class
    │   │       │   └── impl
    │   │       │       ├── BlogCommentsServiceImpl.class
    │   │       │       ├── BlogServiceImpl.class
    │   │       │       ├── FollowServiceImpl.class
    │   │       │       ├── SeckillVoucherServiceImpl.class
    │   │       │       ├── ShopServiceImpl.class
    │   │       │       ├── ShopTypeServiceImpl.class
    │   │       │       ├── UserInfoServiceImpl.class
    │   │       │       ├── UserServiceImpl.class
    │   │       │       ├── VoucherOrderServiceImpl.class
    │   │       │       └── VoucherServiceImpl.class
    │   │       └── utils
    │   │           ├── CacheClient.class
    │   │           ├── ILock.class
    │   │           ├── IpUtil.class
    │   │           ├── LoginInterceptor.class
    │   │           ├── PasswordEncoder.class
    │   │           ├── RedisConstants.class
    │   │           ├── RedisData.class
    │   │           ├── RedisWorker.class
    │   │           ├── RefreshIntercepter.class
    │   │           ├── RegexPatterns.class
    │   │           ├── RegexUtils.class
    │   │           ├── SimpleRedisLock.class
    │   │           ├── SystemConstants.class
    │   │           └── UserHolder.class
    │   ├── db
    │   │   └── hmdp.sql
    │   ├── mapper
    │   │   └── VoucherMapper.xml
    │   ├── seckill.lua
    │   └── unlock.lua
    ├── generated-sources
    │   └── annotations
    ├── generated-test-sources
    │   └── test-annotations
    └── test-classes
        └── com
            └── hmdp
                └── HmDianPingApplicationTests.class
```
## 主要功能
+ 找店铺
+ 写点评
+ 看热评
+ 点赞关注
## 部分展示
![image](https://github.com/axdmdYJ/social-connect-backend/assets/121956515/3f431248-13f7-4eb8-b2ce-4895a26043f3)
![image](https://github.com/axdmdYJ/social-connect-backend/assets/121956515/7039137f-91f5-432e-96d7-5e11a9d82708)



