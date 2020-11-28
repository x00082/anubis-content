package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.config.RedisLuaScriptService;
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

    @Value("${task.data.taskExpire:10}")
    private Integer taskExpire;

    @Value("${task.data.clear.ms:86400000}")
    private Long clearRate;

    @Value("${task.data.clear.limit:1000}")
    private Integer clearLimit;

    @Autowired
    DateBaseService dateBaseService;

    @Autowired
    RedisLuaScriptService luaScriptService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String key = "clearTask";


    @Scheduled(cron = "${task.expire.content.cron:0 0 1 * * ? }")
    public void clearData(){

        Date expire = preNDay(contentExpire);
        log.info("清理{}之前的用户任务及其子任务", formatter.format(expire));
        try {
            List<String> keys = new ArrayList<>();
            keys.add(key);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(String.valueOf(clearRate));
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
            if(re != 0 ){
                log.error("没有执行权限");
                return;
            }
            int count =0;
            re =0;
            do{
                re = dateBaseService.getContentHistoryRepository().clear(expire, clearLimit);
                log.info("单次修改数[{}]", re);

            }while (re == clearLimit);
            log.info("删除历史数量[{}]", count);


            expire = preNDay(taskExpire);
            log.info("清理{}之前的厂商任务", formatter.format(expire));
            count =0;
            re =0;
            do{
                re = dateBaseService.getVendorTaskRepository().clearWithhHistory(expire, clearLimit);
                log.info("单次修删除[{}]", re);

            }while (re == clearLimit);
            log.info("删除厂商任务数量[{}]", count);

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

    /*
    private void clearsubTask(String taskId) throws ContentException {
        log.info("清理请求任务[{}]的子任务开始", taskId);
        clearVendorTask(taskId);
        log.info("清理请求任务[{}]的子任务结束");
    }

    private void clearVendorTask(String taskId) throws ContentException {
        log.info("清理item任务[{}]的厂商任务开始", taskId);
        List<VendorContentTask> vlist = dateBaseService.getVendorTaskRepository().findByRequestId(taskId);

        dateBaseService.getVendorTaskRepository().deleteInBatch(vlist);

        log.info("清理item任务[{}]的厂商任务结束");
    }
    */

}
