package cn.com.pingan.cdn.service.impl;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * @Classname CheckTaskServiceImpl
 * @Description TODO
 * @Date 2020/10/28 15:28
 * @Created by Luj
 */
public class CheckTaskServiceImpl {

    @Scheduled(fixedRate = 5*60000, initialDelay = 10000)
    public void queryStatus(){

    }
}
