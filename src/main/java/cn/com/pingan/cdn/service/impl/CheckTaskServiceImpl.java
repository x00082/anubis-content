package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.common.FanoutType;
import cn.com.pingan.cdn.common.TaskOperationEnum;
import cn.com.pingan.cdn.common.TaskStatus;
import cn.com.pingan.cdn.config.RedisLuaScriptService;
import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import cn.com.pingan.cdn.rabbitmq.message.FanoutMsg;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.rabbitmq.producer.Producer;
import cn.com.pingan.cdn.repository.mysql.VendorTaskRepository;
import cn.com.pingan.cdn.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Classname CheckTaskServiceImpl
 * @Description TODO
 * @Date 2020/10/28 15:28
 * @Created by Luj
 */
@Service
@Slf4j
public class CheckTaskServiceImpl {

    @Value("${task.check.clear:false}")
    private Boolean isclear;

    @Value("${task.check.expire:3600000}")
    private Long expire;

    @Value("${task.check.fixedRate:60000}")
    private String rate;

    @Autowired
    VendorTaskRepository vendorTaskRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    Producer producer;

    @Autowired
    RedisLuaScriptService luaScriptService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String key = "checkStatus";

    @Scheduled(fixedRateString = "${task.check.fixedRate:60000}", initialDelay = 10000)
    public void queryStatus(){
        log.info("start set timeout task...");
        try {
            if (isclear) {
                List<String> keys = new ArrayList<>();
                keys.add(key);
                List<String> args = new ArrayList<>();
                args.add(String.valueOf(System.currentTimeMillis()));
                args.add(rate);
                // 0-成功，-1执行异常，-100超限
                int re = luaScriptService.executeExpireScript(keys, args);
                if(re != 0 ){
                    log.error("没有执行权限");
                    return;
                }

                Set<String> requestIdSet = new HashSet<String>();
                Date now = new Date();
                Date expireDate = new Date(now.getTime() - expire);
                log.info("设置[{}]之前的等待超时厂商任务", formatter.format(expireDate));
                List<String> ts = new ArrayList<>();
                ts.add(TaskStatus.SUCCESS.name());
                ts.add(TaskStatus.FAIL.name());
                List<VendorContentTask> task = vendorTaskRepository.findByStatusNotINAndUpdateTimeLessThan(ts, expireDate);
                log.info("数量:[{}]", task.size());
                for (VendorContentTask vt : task) {
                    requestIdSet.add(vt.getRequestId());
                    vt.setStatus(TaskStatus.FAIL);
                    vt.setMessage("任务超时");
                }
                vendorTaskRepository.saveAll(task);
                log.info("发送轮询消息数量[{}]", requestIdSet.size());
                for (String s : requestIdSet) {
                    TaskMsg robinTaskMsg = new TaskMsg();
                    robinTaskMsg.setTaskId(s);
                    robinTaskMsg.setOperation(TaskOperationEnum.content_vendor_robin);
                    robinTaskMsg.setVersion(0);
                    robinTaskMsg.setRetryNum(0);
                    //robinTaskMsg.setDelay(robinRate);//TODO
                    producer.sendDelayMsg(robinTaskMsg);
                }
                log.info("发送轮询消息结束");
            }
        }catch (Exception e){
            log.info("处理超时任务检测异常[{}]", e);
        }
        log.info("end set expire task...");
    }

    @Scheduled(fixedRateString = "${task.fflush.domain.fixedRate:300000}", initialDelay = 10000)
    public void fflush(){
        log.info("start fflush.domain...");
        try {
            FanoutMsg msg = new FanoutMsg();
            msg.setOperation(FanoutType.fflush_domain_vendor);
            producer.sendFanoutMsg(msg);
        }catch (Exception e){
            log.info("处理超时任务检测异常[{}]", e);
        }
        log.info("end fflush.domain...");
    }

}
