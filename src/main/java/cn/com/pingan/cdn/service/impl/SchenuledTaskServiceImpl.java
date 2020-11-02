package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.repository.mysql.ContentHistoryRepository;
import cn.com.pingan.cdn.repository.mysql.ContentItemRepository;
import cn.com.pingan.cdn.repository.mysql.VendorTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * @Classname SchenuledTaskServiceImpl
 * @Description TODO
 * @Date 2020/11/2 09:37
 * @Created by Luj
 */
@Slf4j
@Component
public class SchenuledTaskServiceImpl {

    @Value("${task.data.contentExpire:30}")
    private Integer contentExpire;

    @Value("${task.data.taskExpire:7}")
    private Integer taskExpire;

    @Autowired
    ContentHistoryRepository contentHistoryRepository;

    @Autowired
    ContentItemRepository contentItemRepository;

    @Autowired
    VendorTaskRepository vendorTaskRepository;


    @Scheduled(cron = "${task.expire.content.cron:0 0 1 * * ? }")
    public void clearData(){//这样删除可能数据不一致，后续修改

        Date expire = preNDay(contentExpire);
        log.info("清理{}之前的用户任务及其子任务",expire);
        contentHistoryRepository.clear(expire);
        contentItemRepository.clear(expire);

        expire = preNDay(taskExpire);
        log.info("清理{}之前的厂商任务",expire);
        vendorTaskRepository.clear(expire);
        log.info("清理数据结束");

    }


    private Date preNDay(int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - i);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();

    }
}
