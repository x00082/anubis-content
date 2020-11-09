package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.common.TaskStatus;
import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import cn.com.pingan.cdn.repository.mysql.VendorTaskRepository;
import cn.com.pingan.cdn.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Autowired
    VendorTaskRepository vendorTaskRepository;

    @Autowired
    private TaskService taskService;

    @Scheduled(fixedRateString = "${task.check.fixedRate:60000}", initialDelay = 10000)
    public void queryStatus(){
        log.info("start set expire task...");
        if(isclear){
            Date now = new Date();
            Date expireDate = new Date(now.getTime() - expire);
            log.info("清理[{}]之前的厂商任务", expireDate);
            List<String> ts = new ArrayList<>();
            ts.add(TaskStatus.SUCCESS.name());
            ts.add(TaskStatus.FAIL.name());
            List<VendorContentTask> task = vendorTaskRepository.findByStatusNotINAndUpdateTimeLessThan(ts, expireDate);
            log.info("数量:[{}]",task.size());
            for(VendorContentTask vt: task) {
                vt.setStatus(TaskStatus.FAIL);
                vt.setMessage("任务超时");
            }
            vendorTaskRepository.saveAll(task);

            /*
            for(VendorContentTask vt: task) {
                TaskMsg vendorTaskMsg = new TaskMsg();
                vendorTaskMsg.setTaskId(vt.getTaskId());
                vendorTaskMsg.setVersion(vt.getVersion());
                vendorTaskMsg.setOperation(TaskOperationEnum.getVendorOperation(vt.getVendor()));
                taskService.pushTaskMsg(vendorTaskMsg);
            }
            */
        }

        log.info("end set expire task...");
    }

}
