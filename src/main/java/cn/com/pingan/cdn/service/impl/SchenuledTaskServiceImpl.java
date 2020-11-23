package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.config.RedisLuaScriptService;
import cn.com.pingan.cdn.exception.ContentException;
import cn.com.pingan.cdn.model.mysql.ContentHistory;
import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import cn.com.pingan.cdn.service.DateBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    DateBaseService dateBaseService;

    @Autowired
    RedisLuaScriptService luaScriptService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String key = "clearTask";


    @Scheduled(cron = "${task.expire.content.cron:0 0 1 * * ? }")
    public void clearData(){//这样删除可能数据不一致，后续修改

        Date expire = preNDay(contentExpire);
        log.info("清理{}之前的用户任务及其子任务", formatter.format(expire));
        try {
            List<String> keys = new ArrayList<>();
            keys.add(key);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(String.valueOf(1000 * 60 * 60 * 24));
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
            if(re != 0 ){
                log.error("没有执行权限");
                return;
            }

            List<ContentHistory> historys = dateBaseService.getContentHistoryRepository().findByCreateTimeBefore(expire);
            for(ContentHistory ch: historys){
                clearsubTask(ch.getRequestId());
            }
            expire = preNDay(taskExpire);
            log.info("清理{}之前的厂商任务", formatter.format(expire));
            dateBaseService.getVendorTaskRepository().clear(expire);
            log.info("清理数据结束");
        }catch (Exception e){
            log.error("清理任务异常[{}]", e);
        }
    }


    private Date preNDay(int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - i);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();

    }

    private void clearsubTask(String taskId) throws ContentException {
        log.info("清理请求任务[{}]的子任务开始", taskId);
        clearVendorTask(taskId);
        log.info("清理请求任务[{}]的子任务结束");
    }

    private void clearVendorTask(String taskId) throws ContentException {
        log.info("清理item任务[{}]的厂商任务开始", taskId);
        List<VendorContentTask> vlist = dateBaseService.getVendorTaskRepository().findByRequestId(taskId);
        for(VendorContentTask vl: vlist  ){
            dateBaseService.getVendorTaskRepository().deleteById(vl.getId());
        }
        log.info("清理item任务[{}]的厂商任务结束");
    }

}
