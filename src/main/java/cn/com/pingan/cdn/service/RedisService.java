package cn.com.pingan.cdn.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Classname RedisService
 * @Description TODO
 * @Date 2020/10/30 17:15
 * @Created by Luj
 */
public interface RedisService {

    public boolean isExisting(String uuid);
    public void put(String uuid,String domainStr);
    public void remove(String uuid);
    public String get(String uuid);
    public <T> T get(String key, Class<T> valueType) throws IOException;

    public void set(String key, String value, Long timeout);

    public void set(String key, String value, Long timeout, TimeUnit unit);

    Long getExpire(String key);

    //加锁
    boolean lock(String lockKey, String timeStamp);

    //释放锁
    void release(String lockKey, String timeStamp);

    //加锁
    boolean lockToday(String lockKey);

    //释放锁
    void releaseToday(String lockKey);
}
