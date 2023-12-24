package com.hmdp.utils;

public interface ILock {

    public boolean tryLock(long longTimeSec);

    public void unLock();


}
