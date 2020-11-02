package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Classname RedisServiceImpl
 * @Description TODO
 * @Date 2020/10/30 17:15
 * @Created by Luj
 */

@Service
public class RedisServiceImpl implements RedisService {

    public @Autowired
    StringRedisTemplate template;

    @Override
    public boolean isExisting(String uuid) {
        return this.template.hasKey(uuid);
    }

    @Override
    public void put(String uuid, String domainStr) {
        this.template.opsForValue().set(uuid, domainStr);
    }

    @Override
    public void remove(String uuid) {
        this.template.delete(uuid);
    }

    @Override
    public String get(String uuid) {
        return this.template.opsForValue().get(uuid);
    }

    @Override
    public <T> T get(String key, Class<T> valueType) throws IOException {

        String stringValue = this.template.opsForValue().get(key);
        if (!StringUtils.isEmpty(stringValue)) {
            T t = new ObjectMapper().readValue(stringValue, valueType);
            return t;
        }
        return null;

    }

    @Override
    public void set(String key, String value, Long timeout){
        template.opsForValue().set(key,value,timeout, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, String value, Long timeout, TimeUnit unit) {
        template.opsForValue().set(key,value,timeout, unit);
    }

    @Override
    public Long getExpire(String key){
        return  template.getExpire(key, TimeUnit.SECONDS);
    }

    //加锁
    @Override
    public boolean lock(String lockKey, String timeStamp){

        if(template.opsForValue().setIfAbsent(lockKey, timeStamp)){
            // 对应setnx命令，可以成功设置,也就是key不存在，获得锁成功
            return true;
        }

        //设置失败，获得锁失败
        // 判断锁超时 - 防止原来的操作异常，没有运行解锁操作 ，防止死锁
        String currentLock = template.opsForValue().get(lockKey);
        // 如果锁过期 currentLock不为空且小于当前时间
        if(!StringUtils.isEmpty(currentLock) && Long.parseLong(currentLock) < System.currentTimeMillis()){
            //如果lockKey对应的锁已经存在，获取上一次设置的时间戳之后并 lockKey对应的锁的时间戳
            String preLock = template.opsForValue().getAndSet(lockKey, timeStamp);

            //假设两个线程同时进来这里，因为key被占用了，而且锁过期了。
            //获取的值currentLock=A(get取的旧的值肯定是一样的),两个线程的timeStamp都是B,key都是K.锁时间已经过期了。
            //而这里面的getAndSet一次只会一个执行，也就是一个执行之后，上一个的timeStamp已经变成了B。
            //只有一个线程获取的上一个值会是A，另一个线程拿到的值是B。
            if(!StringUtils.isEmpty(preLock) && preLock.equals(currentLock)){
                return true;
            }
        }

        return false;
    }
    //释放锁
    @Override
    public void release(String lockKey, String timeStamp){
        try {
            String currentValue = template.opsForValue().get(lockKey);
            if(!StringUtils.isEmpty(currentValue) && currentValue.equals(timeStamp) ){
                // 删除锁状态
                template.opsForValue().getOperations().delete(lockKey);
            }
        } catch (Exception e) {
            System.out.println("解锁异常");
        }
    }


    //加锁
    @Override
    public boolean lockToday(String lockKey){

        //取当日凌晨时间字符串
        String time =getDayTimeStr(new Date());

        if(template.opsForValue().setIfAbsent(lockKey, time)){
            // 对应setnx命令，可以成功设置,也就是key不存在，获得锁成功
            return true;
        }

        //设置失败，获得锁失败
        // 判断锁超时 - 防止原来的操作异常，没有运行解锁操作 ，防止死锁
        String currentLock = template.opsForValue().get(lockKey);
        // 如果锁过期 currentLock不为空且小于当前时间
        //同一天只执行一次 非当日
        if(!StringUtils.isEmpty(currentLock) && !currentLock.equals(time)){
            //如果lockKey对应的锁已经存在，获取上一次设置的时间戳之后并 ockKey对应的锁的时间戳
            String preLock = template.opsForValue().getAndSet(lockKey, time);
            if(!StringUtils.isEmpty(preLock) && preLock.equals(currentLock)){
                return true;
            }
        }

        return false;
    }
    //释放锁
    @Override
    public void releaseToday(String lockKey){
        try {
            //取当日凌晨时间戳
            String timeStamp =getDayTimeStr(new Date());

            String currentValue = template.opsForValue().get(lockKey);
            if(!StringUtils.isEmpty(currentValue) && currentValue.equals(timeStamp) ){
                // 删除锁状态
                template.opsForValue().getOperations().delete(lockKey);
            }
        } catch (Exception e) {
            System.out.println("解锁异常");
        }
    }

    public static void main(String argus[]){

//		long today=getDayTimeStamp(new Date());
//		System.out.println(today);
        System.out.println(getDayTimeStr(new Date()));
//		long tomorrow=getDayTimeStamp(new Date());
//		System.out.println(today<tomorrow);

    }

    //取当前凌晨毫秒
    public  static String  getDayTimeStr(Date date) {

        String today="";
        if(null==date){
            return today;
        }
        SimpleDateFormat df=new SimpleDateFormat("yyyyMMdd");
        today=df.format(date);

        return today;

    }

    //取当前凌晨毫秒
    public  static long getDayTimeStamp(Date date) {

        long daytime= 0;
        if(null==date){
            return daytime;
        }
        SimpleDateFormat df=new SimpleDateFormat("yyyyMMdd");
        String today=df.format(date);
        System.out.println("当天日期:"+today);

        try {
            daytime = df.parse(today).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("当天日期时间戳:"+daytime);
        return daytime;

    }

    //获取当前系统下一天日期
    public static Date getNextday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        date = calendar.getTime();
        return date;
    }
}
