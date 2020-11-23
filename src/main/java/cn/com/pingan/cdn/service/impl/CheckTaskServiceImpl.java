package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.common.*;
import cn.com.pingan.cdn.config.RedisLuaScriptService;
import cn.com.pingan.cdn.current.JxGaga;
import cn.com.pingan.cdn.model.mysql.*;
import cn.com.pingan.cdn.rabbitmq.message.FanoutMsg;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.rabbitmq.producer.Producer;
import cn.com.pingan.cdn.service.DateBaseService;
import cn.com.pingan.cdn.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    DateBaseService dateBaseService;

    @Autowired
    private TaskService taskService;

    @Autowired
    Producer producer;

    @Autowired
    RedisLuaScriptService luaScriptService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String key = "checkStatus";

    private String fflushKey = "fflushDomain";

    @Value("${task.fflush.domain.fixedRate:300000}")
    private String fflushRate;

    private String mergeKey = "mergeTask";
    @Value("${task.merge.fixedRate:5000}")
    private String mergeRate;

    private String robinKey = "robinTask";
    @Value("${task.robin.fixedRate:5000}")
    private String robinRate;

    private String robinHistoryKey = "robinHistoryTask";
    @Value("${task.hittory.robin.fixedRate:5000}")
    private String robinHistoryRate;

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
                Map<String, Long> idMap = new HashMap<>();
                Date now = new Date();
                Date expireDate = new Date(now.getTime() - expire);
                log.info("设置[{}]之前的等待超时厂商任务", formatter.format(expireDate));
                List<String> ts = new ArrayList<>();
                ts.add(TaskStatus.SUCCESS.name());
                ts.add(TaskStatus.FAIL.name());
                List<VendorContentTask> task = dateBaseService.getVendorTaskRepository().findByStatusNotINAndUpdateTimeLessThan(ts, expireDate);
                log.info("数量:[{}]", task.size());
                for (VendorContentTask vt : task) {
                    requestIdSet.add(vt.getRequestId());
                    idMap.put(vt.getRequestId(), vt.getId());
                    vt.setStatus(TaskStatus.FAIL);
                    vt.setMessage("任务超时");
                }
                dateBaseService.getVendorTaskRepository().saveAll(task);
                log.info("发送轮询消息数量[{}]", requestIdSet.size());
                for (String s : requestIdSet) {
                    TaskMsg robinTaskMsg = new TaskMsg();
                    robinTaskMsg.setTaskId(s);
                    robinTaskMsg.setId(idMap.get(s));
                    robinTaskMsg.setOperation(TaskOperationEnum.content_vendor_robin);
                    robinTaskMsg.setVersion(0);
                    robinTaskMsg.setHisVersion(-1);
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
            List<String> keys = new ArrayList<>();
            keys.add(fflushKey);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(fflushRate);
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
            if(re != 0 ){
                log.error("没有执行权限");
                return;
            }

            FanoutMsg msg = new FanoutMsg();
            msg.setOperation(FanoutType.fflush_domain_vendor);
            producer.sendFanoutMsg(msg);
        }catch (Exception e){
            log.info("处理超时任务检测异常[{}]", e);
        }
        log.info("end fflush.domain...");
    }

    @Scheduled(fixedRateString = "${task.merge.fixedRate:5000}", initialDelay = 10000)
    public void mergeTask(){
        log.info("start mergeTask...");
        try {
            List<String> keys = new ArrayList<>();
            keys.add(mergeKey);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(mergeRate);
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
            if(re != 0 ){
                log.error("没有执行权限");
                return;
            }
            List<String> records = new ArrayList<>();
            List<MergeRecord> mergeRecords = dateBaseService.getMergeRecordRepository().findByLimit(10000);
            for(MergeRecord r: mergeRecords){
                records.add(r.getMergeId());
            }
            if(records.size()>0){
                log.info("有需要合并记录[{}]", records.size());
                Map<String, TaskItem> vMap = new HashMap<>();
                List<VendorContentTask> allVCT = dateBaseService.getVendorTaskRepository().findByMergeIdIn(records);
                for(VendorContentTask v: allVCT){
                    if(!vMap.containsKey(TaskOperationEnum.getVendorOperation(v.getVendor(), v.getType()).name())){
                        TaskItem item = new TaskItem();
                        item.setOpt(TaskOperationEnum.getVendorOperation(v.getVendor(), v.getType()));
                        item.setVendor(v.getVendor());
                        item.setType(v.getType());
                        List<VendorContentTask> l = new ArrayList<>();
                        item.setTaskList(l);
                        vMap.put(TaskOperationEnum.getVendorOperation(v.getVendor(), v.getType()).name(), item);
                    }
                    vMap.get(TaskOperationEnum.getVendorOperation(v.getVendor(), v.getType()).name()).getTaskList().add(v);
                }

                Map<String, List<VendorContentTask>> toSaveVendorContentTaskMap = new HashMap<>();
                List<TaskMsg> toSendMQList = new ArrayList<>();
                for(String s : vMap.keySet()){
                    TaskItem it = vMap.get(s);
                    VendorInfo info = TaskServiceImpl.vendorInfoMap.get(it.getVendor());
                    int limit;
                    if(it.getType().equals(RefreshType.url)){
                        limit = info!=null?info.getMergeUrlCount():StaticValue.SINGLE_URL_REFRESH_LIMIT;
                    }else if(it.getType().equals(RefreshType.dir)){
                        limit = info!=null?info.getMergeDirCount():StaticValue.SINGLE_DIR_REFRESH_LIMIT;
                    }else if(it.getType().equals(RefreshType.preheat)){
                        limit = info!=null?info.getMergePreheatCount():StaticValue.SINGLE_URL_PRELOAD_LIMIT;
                    }else{
                        limit = 10;
                    }
                    int num = 0;
                    String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                    for(VendorContentTask v : it.getTaskList()){
                        if((num % limit) == 0){
                            mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                            TaskMsg vendorTaskMsg = new TaskMsg();
                            vendorTaskMsg.setTaskId(mergeId);
                            vendorTaskMsg.setOperation(TaskOperationEnum.of(s));
                            vendorTaskMsg.setVendor(it.getVendor());
                            vendorTaskMsg.setVersion(0);
                            //vendorTaskMsg.setHisVersion(contentHistory.getVersion());
                            vendorTaskMsg.setIsMerge(true);
                            vendorTaskMsg.setType(it.getType());
                            vendorTaskMsg.setRetryNum(0);
                            vendorTaskMsg.setSize(it.getTaskList().size()- num >= limit?limit:it.getTaskList().size() - num);
                            toSendMQList.add(vendorTaskMsg);
                            List<VendorContentTask> vl = new ArrayList<>();
                            toSaveVendorContentTaskMap.put(mergeId, vl);
                        }
                        v.setMergeId(mergeId);
                        toSaveVendorContentTaskMap.get(mergeId).add(v);
                        num++;
                    }
                }
                if(toSaveVendorContentTaskMap.keySet().size()>0){
                    JxGaga gg = JxGaga.of(Executors.newCachedThreadPool(), toSaveVendorContentTaskMap.keySet().size());
                    List<String> rs = new ArrayList<>();
                    log.info("存入合并数据");
                    for(String s: toSaveVendorContentTaskMap.keySet()){
                        gg.work(() -> {
                            dateBaseService.getVendorTaskRepository().saveAll(toSaveVendorContentTaskMap.get(s));
                            return "succ";
                        }, j -> rs.add(j),  q -> {q.getMessage();});
                    }
                    gg.merge((i) -> {
                        i.forEach(j -> {log.info(j);});
                    //}, rs, 5, TimeUnit.SECONDS).exit();
                    }, rs).exit();
                    log.info("存入合并数据完成");

/*
                    log.info("存入合并数据");
                    dateBaseService.getVendorTaskRepository().saveAll(toSaveVendorContentTask);
                    log.info("存入合并数据完成");

*/
                    sendListMQ(toSendMQList);
                    log.info("发送Mq完成");
                }else{
                    log.info("没有合并数据写入");
                }

                dateBaseService.getMergeRecordRepository().deleteInBatch(mergeRecords);

                log.info("清理已合并任务");
            }else{
                log.info("没有需要合并记录");
            }
        }catch (Exception e){
            log.info("处理合并异常[{}]", e);
        }
        log.info("end mergeTask...");
    }



    @Scheduled(fixedRateString = "${task.robin.fixedRate:5000}", initialDelay = 10000)
    public void robinTask(){
        log.info("start robinTask...");
        try {
            List<String> keys = new ArrayList<>();
            keys.add(robinKey);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(robinRate);
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
            if(re != 0 ){
                log.error("没有执行权限");
                return;
            }
            List<String> records = new ArrayList<>();
            List<RobinRecord> robinRecord = dateBaseService.getRobinRecordRepository().findByLimit(100);


            Map<String, List<RefreshPreloadTaskStatusDTO>> vendorRobinDtoMap = new HashMap<>();
            if(robinRecord.size()>0) {
                log.info("需要轮询记录数量[{}]", robinRecord.size());
                for (RobinRecord rr:robinRecord){
                    if(!vendorRobinDtoMap.containsKey(rr.getVendor())){
                        List<RefreshPreloadTaskStatusDTO> rptsList = new ArrayList<>();
                        vendorRobinDtoMap.put(rr.getVendor(), rptsList);
                    }
                    boolean toNew =true;
                    for(RefreshPreloadTaskStatusDTO dto: vendorRobinDtoMap.get(rr.getVendor())){
                        if(dto.getTaskList().size() <= 10){
                            RefreshPreloadItem item = new RefreshPreloadItem();
                            item.setJobId(rr.getRobinId());
                            item.setJobType(rr.getType().equals(RefreshType.preheat)?"preload":"refresh");
                            dto.getTaskList().add(item);
                            toNew = false;
                            break;
                        }
                    }
                    if(toNew){
                        RefreshPreloadTaskStatusDTO rpts = new RefreshPreloadTaskStatusDTO();

                        RefreshPreloadItem item = new RefreshPreloadItem();
                        item.setJobId(rr.getRobinId());
                        item.setJobType(rr.getType().equals(RefreshType.preheat)?"preload":"refresh");

                        List<RefreshPreloadItem> itemList = new ArrayList<>();
                        itemList.add(item);
                        rpts.setTaskList(itemList);
                        vendorRobinDtoMap.get(rr.getVendor()).add(rpts);
                    }
                }
                log.info("组装轮序请求列表[{}]", vendorRobinDtoMap);
                List<TaskMsg> toSendMq = new ArrayList<>();
                for(String s :vendorRobinDtoMap.keySet()){
                    for(RefreshPreloadTaskStatusDTO dto: vendorRobinDtoMap.get(s) ){
                        TaskMsg msg = new TaskMsg();
                        msg.setOperation(TaskOperationEnum.getVendorOperationRobin(s));
                        msg.setRobinTaskDto(dto);
                        msg.setIsLimit(true);
                        msg.setVendor(s);
                        toSendMq.add(msg);
                    }
                }
                if(toSendMq.size()>0){
                    sendListMQ(toSendMq);
                    log.info("发送mq数量[{}]", toSendMq.size());

                }

                dateBaseService.getRobinRecordRepository().deleteInBatch(robinRecord);
                log.info("清理已轮询任务");

            }else{
                log.info("没有需要轮询记录");
            }
        }catch (Exception e){
            log.info("处理轮询异常[{}]", e);
        }
        log.info("end robinTask...");
    }


    @Scheduled(fixedRateString = "${task.history.robin.fixedRate:5000}", initialDelay = 10000)
    public void historyRobinTask(){
        log.info("start historyRobinTask...");
        try {
            List<String> keys = new ArrayList<>();
            keys.add(robinHistoryKey);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(robinHistoryRate);
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
            if(re != 0 ){
                log.error("没有执行权限");
                return;
            }
            List<String> records = new ArrayList<>();
            List<HistoryRecord> historyRecords = dateBaseService.getHistoryRecordRepository().findByLimit(1000);


            Map<String, TaskMsg> reqTaskMap = new HashMap<>();
            if(historyRecords.size()>0) {
                log.info("需要轮询记录数量[{}]", historyRecords.size());
                int i=0;
                String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                for (HistoryRecord hr:historyRecords) {
                    if (i % 50 == 0) {
                        taskId = UUID.randomUUID().toString().replaceAll("-", "");
                        TaskMsg msg = new TaskMsg();
                        msg.setTaskId(taskId);
                        msg.setOperation(TaskOperationEnum.content_vendor_robin);
                        List<RobinCallBack> rcbList = new ArrayList<>();
                        msg.setRobinCallBackList(rcbList);
                        msg.setIsMerge(true);
                        reqTaskMap.put(taskId, msg);
                    }
                    RobinCallBack rcb = new RobinCallBack();
                    rcb.setRequestId(hr.getRequestId());
                    rcb.setVersion(hr.getVersion());
                    rcb.setNum(0);
                    reqTaskMap.get(taskId).getRobinCallBackList().add(rcb);
                }

                if(reqTaskMap.values().size()>0){
                    sendListMQ(new ArrayList<>(reqTaskMap.values()));
                    log.info("发送合并后数量[{}]", reqTaskMap.values().size());
                }
                dateBaseService.getHistoryRecordRepository().deleteInBatch(historyRecords);
                log.info("清理已轮询任务");

            }else{
                log.info("没有需要轮询的历史记录");
            }
        }catch (Exception e){
            log.info("处理用户历史轮询异常[{}]", e);
        }
        log.info("end historyRobinTask...");
    }



    /*
    @Scheduled(fixedRateString = "${task.merge.fixedRate:5000}", initialDelay = 10000)
    public void mergeTask(){
        log.info("start mergeTask...");
        try {
            taskService.handlerMergeTask(null, MergeType.delete);
        }catch (Exception e){
            log.info("处理merge任务异常[{}]", e);
        }
        log.info("end mergeTask...");
    }
    */



    private void sendListMQ(List<TaskMsg> toSendMsg) throws IOException {
        int size = toSendMsg.size();
        List<String> rl = new ArrayList<>();
        JxGaga gg = JxGaga.of(Executors.newCachedThreadPool(), size);
        for(TaskMsg tm: toSendMsg){
            gg.work(() -> {
                return producer.sendAllMsg(tm);
            }, j -> rl.add(j),  q -> {q.getMessage();});
        }
        gg.merge((i) -> {
            i.forEach(j -> {log.info(j);});
        }, rl, 2, TimeUnit.SECONDS).exit();
    }


}
