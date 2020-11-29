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

/**
 * @Classname CheckTaskServiceImpl
 * @Description TODO
 * @Date 2020/10/28 15:28
 * @Created by Luj
 */

//@Async
@Service
@Slf4j
public class CheckTaskServiceImpl {

    @Value("${task.check.clear:false}")
    private Boolean isclear;

    @Value("${task.check.expire:3600000}")
    private Long expire;

    @Value("${task.check.fixedRate:60000}")
    private String rate;

    @Value("${task.check.limit:1000}")
    private Integer expireLimit;

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


    private String mergeRequestKey = "mergeRequestTask";
    @Value("${task.request.merge.fixedRate:5000}")
    private String mergeRequestRate;
    @Value("${task.request.merge.read.limit:10000}")
    private Integer mergeRequestReadLimit;
    @Value("${task.request.merge.package.limit:50}")
    private Integer mergeRequestPackageLimit;

    private String mergeKey = "mergeTask";
    @Value("${task.merge.fixedRate:5000}")
    private String mergeRate;
    @Value("${task.merge.read.limit:10000}")
    private Integer mergeReadLimit;

    private String robinVendorKey = "robinVendorTask";
    @Value("${task.vendor.robin.fixedRate:5000}")
    private String robinVendorRate;
    @Value("${task.vendor.robin.read.limit:1000}")
    private Integer robinVendorReadLimit;
    @Value("${task.vendor.robin.package.limit:50}")
    private Integer robinVendorPackageLimit;

    private String robinHistoryKey = "robinHistoryTask";
    @Value("${task.history.robin.fixedRate:5000}")
    private String robinHistoryRate;
    @Value("${task.history.robin.read.limit:1000}")
    private Integer robinHistoryReadLimit;
    @Value("${task.history.robin.package.limit:50}")
    private Integer robinHistoryPackageLimit;

    @Scheduled(fixedDelayString = "${task.check.fixedRate:60000}", initialDelay = 10000)
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
                    log.warn("没有执行权限");
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
                //re = dateBaseService.getContentHistoryRepository().updateStatusFailNotINAndUpdateTimeLessThanLimit(ts, expireDate, expireLimit);
                int count =0;
                re =0;
                //List<ContentHistory> contentHistories = dateBaseService.getContentHistoryRepository().findStatusNotINAndUpdateTimeLessThanAndLimit(ts, expireDate, expireLimit);
                do{
                    re = dateBaseService.getContentHistoryRepository().updateStatusFailNotINAndUpdateTimeLessThanLimit(ts, expireDate, expireLimit);
                    log.info("本次修改数[{}]", re);
                    count+=re;
                    /*
                    count += contentHistories.size();
                    for(ContentHistory c : contentHistories){
                        c.setMessage("任务超时");
                        c.setUpdateTime(new Date());
                        c.setStatus(HisStatus.FAIL);
                    }
                    dateBaseService.getContentHistoryRepository().saveAll(contentHistories);
                    contentHistories = dateBaseService.getContentHistoryRepository().findStatusNotINAndUpdateTimeLessThanAndLimit(ts, expireDate, expireLimit);
                */
                }while (re == expireLimit);
                log.info("设置超时任务数[{}]", count);

            }
        }catch (Exception e){
            log.info("set timeout task 异常[{}]", e);
        }
        log.info("end set expire task...");
    }

    @Scheduled(fixedDelayString = "${task.fflush.domain.fixedRate:300000}", initialDelay = 10000)
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
                log.warn("没有执行权限");
                return;
            }

            FanoutMsg msg = new FanoutMsg();
            msg.setOperation(FanoutType.fflush_domain_vendor);
            producer.sendFanoutMsg(msg);
        }catch (Exception e){
            log.info("fflush.domain异常[{}]", e);
        }
        log.info("end fflush.domain...");
    }

    @Scheduled(fixedDelayString = "${task.request.merge.fixedRate:5000}", initialDelay = 10000)
    public void mergeRequest(){
        log.info("start mergeRequest...");
        try {
            List<String> keys = new ArrayList<>();
            keys.add(mergeRequestKey);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(mergeRequestRate);
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
            if(re != 0 ){
                log.warn("mergeRequest没有执行权限");
                if(re == -200){
                    log.error("上一次执行错误");
                }
                return;
            }
            List<String> records = new ArrayList<>();
            List<RequestRecord> requestRecords = dateBaseService.getRequestRecordRepository().findByLimit(mergeRequestReadLimit);
            for(RequestRecord r: requestRecords){
                records.add(r.getRequestId());
            }
            if(records.size()>0){
                log.info("有需要合并的请求[{}]", records.size());

                int num = 0;
                Map<String, TaskMsg> toSendRequestMap = new HashMap<>();
                String taskId = UUID.randomUUID().toString().replaceAll("-", "");//没有实际意义，只是标记
                for(String rId :records){
                    if((num % mergeRequestPackageLimit) == 0){
                        taskId = UUID.randomUUID().toString().replaceAll("-", "");
                        TaskMsg mergeRequestTaskMsg = new TaskMsg();
                        mergeRequestTaskMsg.setTaskId(taskId);
                        mergeRequestTaskMsg.setIsMerge(true);
                        mergeRequestTaskMsg.setVersion(0);
                        mergeRequestTaskMsg.setOperation(TaskOperationEnum.content_item);

                        List<String> l = new ArrayList<>();
                        mergeRequestTaskMsg.setRequestRecordList(l);
                        toSendRequestMap.put(taskId, mergeRequestTaskMsg);
                    }
                    toSendRequestMap.get(taskId).getRequestRecordList().add(rId);
                    num++;
                }

                if(toSendRequestMap.values().size()>0){
                    sendListMQ(new ArrayList<>(toSendRequestMap.values()));
                    log.info("mergeRequest发送Mq完成");
                }else{
                    log.info("mergeRequest没有合并数据写入");
                }

                dateBaseService.getRequestRecordRepository().deleteInBatch(requestRecords);

                log.info("mergeRequest清理已合并任务");
            }else{
                log.info("mergeRequest没有需要合并记录");
            }
        }catch (Exception e){
            log.info("mergeRequest处理合并异常[{}]", e);
            List<String> keys = new ArrayList<>();
            keys.add(mergeRequestKey);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(String.valueOf(-1));
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
        }
        log.info("end mergeRequest...");
    }


    @Scheduled(fixedDelayString = "${task.merge.fixedRate:5000}", initialDelay = 10000)
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
                log.warn("mergeTask没有执行权限");
                if(re == -200){
                    log.error("上一次执行错误");
                }
                return;
            }
            List<String> records = new ArrayList<>();
            List<MergeRecord> mergeRecords = dateBaseService.getMergeRecordRepository().findByLimit(mergeReadLimit);
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

                //Map<String, List<VendorContentTask>> toSaveVendorContentTaskMap = new HashMap<>();
                List<VendorContentTask> toSaveVendorContentTaskList = new ArrayList<>();
                Map<String, TaskMsg> toSendMQMap = new HashMap<>();
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
                        limit = 20;
                    }
                    int num = 0;
                    String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                    for(VendorContentTask v : it.getTaskList()){
                        int count = v.getContentNumber();
                        if(count + num > limit){
                            num = 0;
                        }
                        if(num == 0){
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
                            //vendorTaskMsg.setSize(it.getTaskList().size()- num >= limit?limit:it.getTaskList().size() - num);
                            toSendMQMap.put(mergeId, vendorTaskMsg);
                        }
                        num += count;
                        v.setMergeId(mergeId);
                        toSaveVendorContentTaskList.add(v);
                        toSendMQMap.get(mergeId).setSize(num);
                    }
                }
                if(toSaveVendorContentTaskList.size()>0){

                    log.info("存入合并数据");
                    dateBaseService.getVendorTaskRepository().saveAll(toSaveVendorContentTaskList);
                    log.info("存入合并数据完成");
                    sendListMQ(new ArrayList<>(toSendMQMap.values()));
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
            log.info("mergeTask处理合并异常[{}]", e);
            List<String> keys = new ArrayList<>();
            keys.add(mergeKey);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(String.valueOf(-1));
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
        }
        log.info("end mergeTask...");
    }



    @Scheduled(fixedDelayString = "${task.vendor.robin.fixedRate:5000}", initialDelay = 10000)
    public void robinTask(){
        log.info("start robinTask...");
        try {
            List<String> keys = new ArrayList<>();
            keys.add(robinVendorKey);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(robinVendorRate);
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
            if(re != 0 ){
                log.warn("robinTask没有执行权限");
                if(re == -200){
                    log.error("上一次执行错误");
                }
                return;
            }
            List<String> records = new ArrayList<>();
            List<RobinRecord> robinRecord = dateBaseService.getRobinRecordRepository().findByLimit(robinVendorReadLimit);


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
                        if(dto.getTaskList().size() < robinVendorPackageLimit){
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
                log.info("组装轮询请求列表[{}]", vendorRobinDtoMap);
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
            log.info("robinTask处理轮询异常[{}]", e);
            List<String> keys = new ArrayList<>();
            keys.add(robinVendorKey);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(String.valueOf(-1));
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
        }
        log.info("end robinTask...");
    }


    @Scheduled(fixedDelayString = "${task.history.robin.fixedRate:5000}", initialDelay = 10000)
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
                log.warn("historyRobinTask没有执行权限");
                if(re == -200){
                    log.error("上一次执行错误");
                }
                return;
            }
            List<String> records = new ArrayList<>();
            List<HistoryRecord> historyRecords = dateBaseService.getHistoryRecordRepository().findByLimit(robinHistoryReadLimit);


            Map<String, TaskMsg> reqTaskMap = new HashMap<>();
            if(historyRecords.size()>0) {
                log.info("需要轮询记录数量[{}]", historyRecords.size());
                int i=0;
                String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                for (HistoryRecord hr:historyRecords) {
                    if (i % robinHistoryPackageLimit == 0) {
                        taskId = UUID.randomUUID().toString().replaceAll("-", "");
                        TaskMsg msg = new TaskMsg();
                        msg.setTaskId(taskId);
                        msg.setOperation(TaskOperationEnum.content_history_robin);
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
                    i++;
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
            log.info("historyRobinTask处理用户历史轮询异常[{}]", e);
            List<String> keys = new ArrayList<>();
            keys.add(robinHistoryKey);
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(System.currentTimeMillis()));
            args.add(String.valueOf(-1));
            // 0-成功，-1执行异常，-100超限
            int re = luaScriptService.executeExpireScript(keys, args);
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
            //i.forEach(j -> {log.debug(j);});
            log.info("已发送mq[{}]",i.size());
        }, rl).exit();
    }


}
