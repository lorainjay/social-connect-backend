package com.hmdp.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedisData {
    private Object Data;
    private LocalDateTime ExpireTime;
}
