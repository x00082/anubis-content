package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.client.*;
import cn.com.pingan.cdn.common.*;
import cn.com.pingan.cdn.config.RedisLuaScriptService;
import cn.com.pingan.cdn.current.JxGaga;
import cn.com.pingan.cdn.exception.RestfulException;
import cn.com.pingan.cdn.model.mysql.ContentHistory;
import cn.com.pingan.cdn.model.mysql.RobinRecord;
import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import cn.com.pingan.cdn.model.mysql.VendorInfo;
import cn.com.pingan.cdn.rabbitmq.config.RabbitListenerConfig;
import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.rabbitmq.producer.Producer;
import cn.com.pingan.cdn.service.DateBaseService;
import cn.com.pingan.cdn.service.TaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @Classname TaskServiceImpl
 * @Description TODO
 * @Date 2020/10/20 15:27
 * @Created by Luj
 */

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Value("${task.round.delay:60000}")
    private Long roundMs;

    @Value("${task.round.limit:10}")
    private Integer roundLimit;

    @Value("${task.timeout.delay:3000}")
    private Long timeOutMs;

    @Value("${task.timeout.limit:3}")
    private Integer timeOutLimit;

    private String roundRobinLimitPrefix = "robin_";

    private String subQpsAndSizePrefix = "qpsAndSize_";

    @Value("${task.new.request.qps.default:50}")
    private Integer requestQps;

    @Value("${task.new.request.size.default:500}")
    private Integer requestSize;

    @Value("${task.robin.qps.default:50}")
    private Integer robinQps;

    @Value("${task.vendor.status.default:up}")
    private String defaultVendorStatus;

    @Value("${task.database.qps:50}")
    private Integer dataBaseQps;

    @Value("${mq.sleep.ms:500}")
    private Long sleepMs;

    @Value("${task.notify.fail:false}")
    private Boolean notifyFail;

    @Value("${task.response.async:false}")
    private Boolean responseAsync;

    public static Map<String, List<String>> mergeHashMap= new ConcurrentHashMap<String,List<String>>();

    public static Map<String,VendorInfo> vendorInfoMap= new ConcurrentHashMap<String,VendorInfo>();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private VenusClientService venusClientService;

    @Autowired
    private TencentClientService tencentClientService;

    @Autowired
    private QiniuClientService qiniuClientService;

    @Autowired
    private KsyunClientService ksyunClientService;

    @Autowired
    private NetClientService netClientService;

    @Autowired
    private ChinaCacheClientService chinaCacheClientService;

    @Autowired
    private JdcloudClientService jdcloudClientService;

    @Autowired
    private AliyunClientService aliyunClientService;

    @Autowired
    private BaishanClientService baishanClientService;

    @Autowired
    private AnubisNotifyService anubisNotifyService;

    @Autowired
    DateBaseService dateBaseService;

    @Autowired
    private Producer producer;

    @Autowired
    RedisLuaScriptService luaScriptService;

    @Autowired
    RabbitListenerConfig rabbitListenerConfig;

    @Override
    public void pushTaskMsg(TaskMsg msg) throws RestfulException{
        log.info("push task:{} with version:{} to queue:{}", msg.getTaskId(), msg.getVersion(), msg.getOperation());
        if(msg.getDelay() <=0){
            // 增加task的版本号
            if(0 == addTaskVersion(msg)){
                // 将新版本号的消息推动到队列中
                this.producer.sendTaskMsg(msg);
            }
        }else{
            this.producer.sendDelayMsg(msg);
        }
    }


    @Override
    public int addTaskVersion(TaskMsg msg) throws RestfulException {
        try {
            int updateCount = dateBaseService.getVendorTaskRepository().updateVersion(msg.getTaskId(), msg.getVersion(), msg.getVersion() + 1);
            if (updateCount == 0) {
                throw new NoSuchElementException();
                //throw RestfulException.ErrNosuchTask;
            }
            msg.setVersion(msg.getVersion() + 1);
        } catch (Exception e) {
            this.handleTaskRepositityException(e);
            return -1;
        }
        return 0;
    }

    @Override
    public int handlerRequestTask(TaskMsg msg) throws RestfulException {

        if( -1 != msg.getOperation().toString().indexOf("_robin")){
            handlerRoundRobin(msg);
            return 0;
        }

        if(msg.getType().equals(RefreshType.url)) {
            handlerNewRequestUrl(msg);
        }else if(msg.getType().equals(RefreshType.dir)){
            handlerNewRequestDir(msg);
        }else{
            handlerNewRequestPreload(msg);
        }
        return 0;
    }

    @Override
    public int handlerRobinTask(TaskMsg msg) throws RestfulException {
        handlerRoundRobin(msg);
        return 0;
    }

    @Override
    public int handlerCommonTask(TaskMsg msg) throws RestfulException {
        boolean flag;
        try {
            flag = handlerCommon(msg);
            if(!flag){
                return -1;
            }
        }catch (Exception e){
            log.error("handlerCommonTask Exception:{}",e);
        }
        producer.sendAllMsg(msg);
        log.info("handlerCommonTask end:{}", msg);
        return 0;
    }

    /*
    @Override
    public int handlerTask(TaskMsg msg) throws RestfulException {
        log.info("enter handlerTask:{}",msg);
        VendorContentTask task = vendorTaskRepository.findByTaskId(msg.getTaskId());
        if(task ==null || task.getVersion() != msg.getVersion()){
            log.error("厂商任务:[{}]不存在或任务版本不一致,丢弃该消息", msg.getTaskId());
            return -1;
        }

        TaskStatus st = task.getStatus();//WAIT-newReq PROCESSING-queryStatus ROUND_ROBIN-queryStatus
        Boolean sendMq = true;
        switch (st){
            case WAIT:
                sendMq = handlerNewRequest(task, msg);
                break;
            case PROCESSING:
            case ROUND_ROBIN:
                sendMq = handlerRoundRobin(task, msg);
                break;
            case SUCCESS:
                sendMq = handlerSuccess(task, msg);
                break;
            case FAIL:
                sendMq = handlerFail(task, msg);
                break;
            default:
                break;
        }
        if(sendMq){
            pushTaskMsg(msg);
        }

        return 0;
    }*/

    @Override
    public int handlerMergeTask(TaskMsg msg , MergeType type) throws RestfulException {
        log.info("enter handlerMergeTask:{}",msg);
        try {
            handlerMerge(msg, type);
        }catch (Exception e){
            log.error("merge err");
            producer.sendAllMsg(msg);
        }
        return 0;
    }

    @Override
    public int fflushVendorInfoMap(String vendor) {
        log.info("enter fflushVendorInfoMap:{}",vendor);
        if(vendorInfoMap.containsKey(vendor)){
            vendorInfoMap.remove(vendor);
        }
        return 0;
    }

    @Override
    public Boolean handlerNewRequestUrl(TaskMsg msg) throws RestfulException {
        log.info("enter handlerNewRequestUrl:{}",msg);
        String taskId = msg.getTaskId();
        try {
            if(!handlerNewRequest(msg)){
                return false;
            }
        }catch (Exception e){
            log.error("handlerNewRequestUrl Exception:{}",e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                Map<String, Integer> failMap = new HashMap<>();
                Map<String, Integer> versionMap = new HashMap<>();
                log.error("[{}]handlerNewRequestUrl超过最大重试限制[{}]", taskId, timeOutLimit);
                List<VendorContentTask> vendorContentTaskList = dateBaseService.getVendorTaskRepository().findByMergeId(taskId);
                for(VendorContentTask v :vendorContentTaskList){
                    if(!failMap.containsKey(v.getRequestId())){
                        failMap.put(v.getRequestId(), 1);
                    }
                    if(!versionMap.containsKey(v.getRequestId())){
                        versionMap.put(v.getRequestId(), v.getVersion());
                    }
                    v.setMessage("重试失败");
                    v.setStatus(TaskStatus.FAIL);
                    v.setUpdateTime(new Date());
                }
                dateBaseService.getVendorTaskRepository().saveAll(vendorContentTaskList);

                if(failMap.keySet().size()>0) {
                    log.info("失败任务[{}]", failMap);
                    List<RobinCallBack> failList = new ArrayList<>();
                    for (String id : failMap.keySet()) {
                        log.debug("fail -> request id[{}], version[{}]", id, versionMap.get(id));
                        //dateBaseService.getContentHistoryRepository().updateStatusAndMessageByRequestIdAndVersion(id, HisStatus.FAIL.name(), "任务执行失败", versionMap.get(id));
                        RobinCallBack rcb = new RobinCallBack();
                        rcb.setRequestId(id);
                        rcb.setNum(1);
                        rcb.setVersion(versionMap.get(id));
                        failList.add(rcb);
                    }
                    TaskMsg failMsg = new TaskMsg();
                    failMsg.setOperation(TaskOperationEnum.content_vendor_fail);
                    failMsg.setRobinCallBackList(failList);
                    producer.sendTaskMsg(failMsg);
                    log.debug("send fail msg done");
                }
                return false;
            }else{
                msg.setDelay(timeOutMs);
            }
        }
        producer.sendAllMsg(msg);
        log.info("handlerNewRequestUrl end:{}", msg);
        return true;
    }

    @Override
    public Boolean handlerNewRequestDir(TaskMsg msg) throws RestfulException {
        log.info("enter handlerNewRequestDir:{}",msg);
        String taskId = msg.getTaskId();
        try {
            if(!handlerNewRequest(msg)){
                return false;
            }
        }catch (Exception e){
            log.error("handlerNewRequestDir Exception:{}",e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                Map<String, Integer> failMap = new HashMap<>();
                Map<String, Integer> versionMap = new HashMap<>();
                log.error("[{}]handlerNewRequestDir超过最大重试限制[{}]", taskId, timeOutLimit);
                List<VendorContentTask> vendorContentTaskList = dateBaseService.getVendorTaskRepository().findByMergeId(taskId);
                for(VendorContentTask v :vendorContentTaskList){
                    if(!failMap.containsKey(v.getRequestId())){
                        failMap.put(v.getRequestId(), 1);
                    }
                    if(!versionMap.containsKey(v.getRequestId())){
                        versionMap.put(v.getRequestId(), v.getVersion());
                    }
                    v.setMessage("重试失败");
                    v.setStatus(TaskStatus.FAIL);
                    v.setUpdateTime(new Date());
                }
                dateBaseService.getVendorTaskRepository().saveAll(vendorContentTaskList);

                if(failMap.keySet().size()>0) {
                    log.info("失败任务[{}]", failMap);
                    List<RobinCallBack> failList = new ArrayList<>();
                    for (String id : failMap.keySet()) {
                        log.debug("fail -> request id[{}], version[{}]", id, versionMap.get(id));
                        //dateBaseService.getContentHistoryRepository().updateStatusAndMessageByRequestIdAndVersion(id, HisStatus.FAIL.name(), "任务执行失败", versionMap.get(id));
                        RobinCallBack rcb = new RobinCallBack();
                        rcb.setRequestId(id);
                        rcb.setNum(1);
                        rcb.setVersion(versionMap.get(id));
                        failList.add(rcb);
                    }
                    TaskMsg failMsg = new TaskMsg();
                    failMsg.setOperation(TaskOperationEnum.content_vendor_fail);
                    failMsg.setRobinCallBackList(failList);
                    producer.sendTaskMsg(failMsg);
                    log.debug("send fail msg done");
                }
                return false;
            }else{
                msg.setDelay(timeOutMs);
            }
        }
        producer.sendAllMsg(msg);
        log.info("handlerNewRequestDir end:{}", msg);
        return true;
    }

    @Override
    public Boolean handlerNewRequestPreload(TaskMsg msg) throws RestfulException {
        log.info("enter handlerNewRequestPreload:{}",msg);
        String taskId = msg.getTaskId();
        try {
            if(!handlerNewRequest(msg)){
                return false;
            }
        }catch (Exception e){
            log.error("handlerNewRequestPreload Exception:{}",e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                Map<String, Integer> failMap = new HashMap<>();
                Map<String, Integer> versionMap = new HashMap<>();
                log.error("[{}]handlerNewRequestPreload超过最大重试限制[{}]", taskId, timeOutLimit);
                List<VendorContentTask> vendorContentTaskList = dateBaseService.getVendorTaskRepository().findByMergeId(taskId);
                for(VendorContentTask v :vendorContentTaskList){
                    if(!failMap.containsKey(v.getRequestId())){
                        failMap.put(v.getRequestId(), 1);
                    }
                    if(!versionMap.containsKey(v.getRequestId())){
                        versionMap.put(v.getRequestId(), v.getVersion());
                    }
                    v.setMessage("重试失败");
                    v.setStatus(TaskStatus.FAIL);
                    v.setUpdateTime(new Date());
                }
                dateBaseService.getVendorTaskRepository().saveAll(vendorContentTaskList);

                if(failMap.keySet().size()>0) {
                    log.info("失败任务[{}]", failMap);
                    List<RobinCallBack> failList = new ArrayList<>();
                    for (String id : failMap.keySet()) {
                        log.debug("fail -> request id[{}], version[{}]", id, versionMap.get(id));
                        //dateBaseService.getContentHistoryRepository().updateStatusAndMessageByRequestIdAndVersion(id, HisStatus.FAIL.name(), "任务执行失败", versionMap.get(id));
                        RobinCallBack rcb = new RobinCallBack();
                        rcb.setRequestId(id);
                        rcb.setNum(1);
                        rcb.setVersion(versionMap.get(id));
                        failList.add(rcb);
                    }
                    TaskMsg failMsg = new TaskMsg();
                    failMsg.setOperation(TaskOperationEnum.content_vendor_fail);
                    failMsg.setRobinCallBackList(failList);
                    producer.sendTaskMsg(failMsg);
                    log.debug("send fail msg done");
                }

                return false;
            }else{
                msg.setDelay(timeOutMs);
            }
        }
        producer.sendAllMsg(msg);
        log.info("handlerNewRequestPreload end:{}", msg);
        return true;
    }


    @Override
    public Boolean handlerRoundRobin(TaskMsg msg) throws RestfulException {
        log.info("enter handlerRoundRobin:{}", msg);
        //String taskId = msg.getTaskId();
        try {
            VendorInfo vendorInfo = null;
            if(vendorInfoMap.containsKey(msg.getVendor())){
                vendorInfo = vendorInfoMap.get(msg.getVendor());
            }else{
                vendorInfo = dateBaseService.getVendorInfoRepository().findByVendor(msg.getVendor());
                if(vendorInfo != null){
                    vendorInfoMap.put(msg.getVendor(), vendorInfo);
                }
            }
            String vendorStatus;
            if (vendorInfo == null) {
                log.warn("[{}] vendorInfo db is null", msg.getVendor());
                vendorStatus = defaultVendorStatus;
            } else {
                vendorStatus = vendorInfo.getStatus().name();
            }

            if ("down".equals(vendorStatus)) {
                msg.setDelay(1 * 60 * 1000L);
                //rabbitListenerConfig.stop(msg.getOperation().name());//关闭监听
                producer.sendAllMsg(msg);//放回队列
                Thread.currentThread().sleep(sleepMs);
                return false;
            }

            if (msg.getIsLimit()) {//
                //请求QPS限制
                int limit = vendorInfo!=null?vendorInfo.getRobinQps():robinQps;
                String redisKey = msg.getOperation().toString();
                // 0-成功，-1执行异常，-100超限
                int result = handlerTaskLimit(redisKey, limit);
                if (-100 == result) {
                    //stopAndSendDelayFanoutMsg(msg);
                    log.warn("redis:{} Limit:{}", redisKey, limit);
                    msg.setDelay(1000L);
                    producer.sendAllMsg(msg);
                    Thread.currentThread().sleep(sleepMs);
                    return false;
                } else if (-1 == result) {
                    log.warn("redis:{} Limit:{} 执行异常", redisKey, limit);
                    throw new Exception("redis err");
                }
            }

            JSONObject response;

            RefreshPreloadTaskStatusDTO dto = msg.getRobinTaskDto();

            VendorClientService vendorClient = getVendorClientVO(VendorEnum.getByCode(msg.getVendor()));
            response = vendorClient.queryRefreshPreloadTask(dto);
            if(response == null || !response.containsKey("data")){
                log.error("response is null or no data");
                throw new Exception(": response no data");
            }
            log.info("response:{}", response);

            Map<String, TaskStatus> responseMap = new HashMap<>();//终态succ or fail
            List<String> robinTask = new ArrayList<>();//仍需要轮询
            List<RefreshPreloadItem> waitItemList = new ArrayList<>();

            JSONArray jsonArray = response.getJSONArray("data");
            if (jsonArray != null && jsonArray.size() > 0) {
                TaskStatus ts = TaskStatus.ROUND_ROBIN;
                String message = "";
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json != null && json.getString("jobId") != null ) {
                        if (Constants.STATUS_SUCCESS.equals(json.getString("status"))) {
                            responseMap.put(json.getString("jobId"), TaskStatus.SUCCESS);
                            msg.setRetryNum(0);
                            log.debug("刷新预热任务完成，任务编号[{}]", json.getString("jobId"));
                        } else if (Constants.STATUS_FAIL.equals(json.getString("status"))) {
                            responseMap.put(json.getString("jobId"), TaskStatus.FAIL);
                            msg.setRetryNum(0);
                            log.debug("刷新预热任务失败，任务编号[{}]", json.getString("jobId"));
                        } else if (Constants.STATUS_WAIT.equals(json.getString("status"))) {
                            //msg.setRoundRobinNum(msg.getRoundRobinNum() + 1);
                            if(msg.getRoundRobinNum() > roundLimit){
                                responseMap.put(json.getString("jobId"), TaskStatus.FAIL);
                            }else{
                                msg.setDelay(roundMs);
                                robinTask.add(json.getString("jobId"));
                            }
                            log.debug("刷新预热任务未完成，任务编号[{}], 等待{}ms后查询", json.getString("jobId"), roundMs);
                            msg.setRetryNum(0);
                        } else {
                            log.debug("返回值状态异常，期望的状态是SUCCESS/FAIL/WAIT,收到的状态是[{}]", json.getString("status"));
                            msg.setRetryNum(msg.getRetryNum() + 1);
                            if(msg.getRetryNum() > timeOutLimit){
                            }else{
                                msg.setDelay(timeOutMs);
                            };
                        }
                    }
                }

                Map<String, Integer> successMap = new HashMap<>();
                Map<String, Integer> failMap = new HashMap<>();
                Map<String, Integer> versionMap = new HashMap<>();
                if(responseMap.keySet().size()>0) {
                    List<VendorContentTask> vendorContentTaskList = dateBaseService.getVendorTaskRepository().findByJobIdIn(new ArrayList<>(responseMap.keySet()));
                    if (vendorContentTaskList.size() > 0) {
                        for (VendorContentTask v : vendorContentTaskList) {
                            if(!versionMap.containsKey(v.getRequestId())){
                                versionMap.put(v.getRequestId(), v.getVersion());//存入version
                            }

                            if (responseMap.get(v.getJobId()).equals(TaskStatus.SUCCESS)) {
                                successMap.put(v.getRequestId(), successMap.containsKey(v.getRequestId()) ? successMap.get(v.getRequestId()) + 1 : 1);
                                v.setMessage("任务执行成功");
                                v.setStatus(TaskStatus.SUCCESS);
                            } else {
                                failMap.put(v.getRequestId(), 1);
                                v.setMessage("任务执行失败");
                                v.setStatus(TaskStatus.FAIL);

                            }
                            v.setUpdateTime(new Date());
                        }
                        dateBaseService.getVendorTaskRepository().batchUpdate(vendorContentTaskList);
                    }else{
                        log.error("任务记录不存在，丢弃该消息");
                        return false;
                    }
                }

                //首次轮询时设置状态
                if(msg.getRoundRobinNum()>0 && robinTask.size()>0){
                    List<VendorContentTask> waitContentTaskList = dateBaseService.getVendorTaskRepository().findByJobIdIn(robinTask);
                    if(waitContentTaskList.size()>0){
                        for(VendorContentTask waitV: waitContentTaskList){
                            waitV.setStatus(TaskStatus.ROUND_ROBIN);
                        }
                        dateBaseService.getVendorTaskRepository().batchUpdate(waitContentTaskList);
                    }
                }

                if(successMap.keySet().size() >0){
                    log.info("成功任务[{}]", successMap);
                    List<RobinCallBack> succList = new ArrayList<>();
                    for(String id: successMap.keySet()){
                        log.debug("success -> request id[{}], succ[{}], version[{}]", id, successMap.get(id), versionMap.get(id));
                        RobinCallBack rcb = new RobinCallBack();
                        rcb.setRequestId(id);
                        rcb.setNum(successMap.get(id));
                        rcb.setVersion(versionMap.get(id));
                        succList.add(rcb);
                    }
                    TaskMsg succMsg = new TaskMsg();
                    succMsg.setOperation(TaskOperationEnum.content_vendor_success);
                    succMsg.setRobinCallBackList(succList);
                    producer.sendTaskMsg(succMsg);
                    log.debug("send success msg done");
                }

                if(failMap.keySet().size()>0) {
                    log.info("失败任务[{}]", failMap);
                    List<RobinCallBack> failList = new ArrayList<>();
                    for (String id : failMap.keySet()) {
                        log.debug("fail -> request id[{}], version[{}]", id, versionMap.get(id));
                        //dateBaseService.getContentHistoryRepository().updateStatusAndMessageByRequestIdAndVersion(id, HisStatus.FAIL.name(), "任务执行失败", versionMap.get(id));
                        RobinCallBack rcb = new RobinCallBack();
                        rcb.setRequestId(id);
                        rcb.setNum(1);
                        rcb.setVersion(versionMap.get(id));
                        failList.add(rcb);
                    }
                    TaskMsg failMsg = new TaskMsg();
                    failMsg.setOperation(TaskOperationEnum.content_vendor_fail);
                    failMsg.setRobinCallBackList(failList);
                    producer.sendTaskMsg(failMsg);
                    log.debug("send fail msg done");
                }

                for(RefreshPreloadItem it: dto.getTaskList()){
                    log.debug("jobId[{}], ", it.getJobId());
                    if(responseMap !=null && responseMap.containsKey(it.getJobId()) && ( responseMap.get(it.getJobId()).equals(TaskStatus.SUCCESS) || responseMap.get(it.getJobId()).equals(TaskStatus.FAIL))){
                        continue;
                    }
                    waitItemList.add(it);
                }
                if(waitItemList.size()>0){
                    log.info("再次轮询[{}]", waitItemList.size());
                    msg.getRobinTaskDto().getTaskList().clear();
                    msg.getRobinTaskDto().getTaskList().addAll(waitItemList);
                    msg.setRoundRobinNum(msg.getRoundRobinNum() + 1);
                    producer.sendAllMsg(msg);
                    return true;
                }else{
                    log.info("不再轮询");
                    return false;
                }


            }else{
                log.error("返回无效数据{}", response);
                throw new Exception(" response err");
            }
        }catch (Exception e){
            log.error("HandlerRoundRobin Exception:{}",e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                List<String> ids = new ArrayList<>();
                for(RefreshPreloadItem it:msg.getRobinTaskDto().getTaskList()){
                    ids.add(it.getJobId());
                }
                Map<String, Integer> failMap = new HashMap<>();
                Map<String, Integer> versionMap = new HashMap<>();
                List<VendorContentTask> vendorContentTaskList = dateBaseService.getVendorTaskRepository().findByJobIdIn(ids);
                for(VendorContentTask v :vendorContentTaskList){
                    if(!failMap.containsKey(v.getRequestId())){
                        failMap.put(v.getRequestId(), 1);
                    }
                    if(!versionMap.containsKey(v.getRequestId())){
                        failMap.put(v.getRequestId(), v.getVersion());
                    }
                    v.setMessage("轮询超出重试次数");
                    v.setStatus(TaskStatus.FAIL);
                    v.setUpdateTime(new Date());
                }
                dateBaseService.getVendorTaskRepository().saveAll(vendorContentTaskList);

                if(failMap.keySet().size()>0) {
                    log.info("失败任务[{}]", failMap);
                    List<RobinCallBack> failList = new ArrayList<>();
                    for (String id : failMap.keySet()) {
                        log.debug("fail -> request id[{}], version[{}]", id, versionMap.get(id));
                        //dateBaseService.getContentHistoryRepository().updateStatusAndMessageByRequestIdAndVersion(id, HisStatus.FAIL.name(), "任务执行失败", versionMap.get(id));
                        RobinCallBack rcb = new RobinCallBack();
                        rcb.setRequestId(id);
                        rcb.setNum(1);
                        rcb.setVersion(versionMap.get(id));
                        failList.add(rcb);
                    }
                    TaskMsg failMsg = new TaskMsg();
                    failMsg.setOperation(TaskOperationEnum.content_vendor_fail);
                    failMsg.setRobinCallBackList(failList);
                    producer.sendTaskMsg(failMsg);
                    log.debug("send fail msg done");
                }
                return false;
            }else{
                msg.setDelay(timeOutMs);
            }
        }
        producer.sendAllMsg(msg);
        return true;
    }

    @Override
    public Boolean handlerSuccess(TaskMsg msg) throws RestfulException{
        log.info("handlerSuccess:{}",msg);
        try {
            if (msg != null && msg.getRobinCallBackList() != null && msg.getRobinCallBackList().size() > 0) {
                msg.setRoundRobinNum(msg.getRoundRobinNum() +1);
                JxGaga gg = JxGaga.of(Executors.newCachedThreadPool(), msg.getRobinCallBackList().size());
                List<String> rs = new ArrayList<>();
                log.debug("更新用户历史状态->[Success]");
                for (RobinCallBack rcb : msg.getRobinCallBackList()) {
                    gg.work(() -> {
                        dateBaseService.getContentHistoryRepository().updateSuccessNumByRequestIdAndVersion(rcb.getRequestId(), rcb.getNum(), rcb.getVersion());
                        return "update done";
                    }, j -> rs.add(j), q -> {
                        q.getMessage();
                    });
                }
                gg.merge((i) -> {
                    log.info("update success [{}]",i.size());
                    /*
                    i.forEach(j -> {
                        log.info(j);
                    });
                    */
                    //}, rs, 5, TimeUnit.SECONDS).exit();
                }, rs).exit();
            }
            log.info("end handlerSuccess:{}",msg);
            return true;
        }catch (Exception e){
            log.error("更新用户历史状态异常{}", e);
            if(msg.getRetryNum() > 3 || msg.getRoundRobinNum() >10){
                return false;
            }else{
                msg.setRetryNum(msg.getRetryNum() + 1);
                msg.setDelay(3000L);
                producer.sendAllMsg(msg);
                return true;
            }
        }
    }

    @Override
    public Boolean handlerFail(TaskMsg msg) throws RestfulException{
        log.info("handlerFail:{}",msg);
        //dateBaseService.getContentHistoryRepository().updateStatusAndMessageByRequestIdAndVersion(id, HisStatus.FAIL.name(), "任务执行失败",versionMap.get(id));
        try {
            if (msg != null && msg.getRobinCallBackList() != null && msg.getRobinCallBackList().size() > 0) {
                msg.setRoundRobinNum(msg.getRoundRobinNum() +1);
                JxGaga gg = JxGaga.of(Executors.newCachedThreadPool(), msg.getRobinCallBackList().size());
                List<String> rs = new ArrayList<>();
                log.debug("更新用户历史状态->[Fail]");
                for (RobinCallBack rcb : msg.getRobinCallBackList()) {
                    gg.work(() -> {
                        dateBaseService.getContentHistoryRepository().updateStatusAndMessageByRequestIdAndVersion(rcb.getRequestId(), HisStatus.FAIL.name(),"任务执行失败", rcb.getVersion());
                        return rcb.getRequestId();
                    }, j -> rs.add(j), q -> {
                        q.getMessage();
                    });
                }
                gg.merge((i) -> {
                    log.info("update fail [{}]",i.size());
                    /*
                    i.forEach(j -> {
                        log.info(j);
                    });
                    */
                    //}, rs, 5, TimeUnit.SECONDS).exit();
                }, rs).exit();

                if(msg.getRobinCallBackList().size()>0 && notifyFail){
                    List<String> requestIds = new ArrayList<>();
                    for(RobinCallBack rcb : msg.getRobinCallBackList()){
                        requestIds.add(rcb.getRequestId());
                    }

                    List<ContentHistory> contentHistorys = dateBaseService.getContentHistoryRepository().findByRequestIdIn(requestIds);
                    gg = JxGaga.of(Executors.newCachedThreadPool(), contentHistorys.size());
                    List<Integer> results = new ArrayList<>();
                    for(ContentHistory his : contentHistorys){
                        gg.work(() -> {
                        anubisNotifyService.emailNotifyApiException(new AnubisNotifyExceptionRequest("anubis-content", his.getType().name(), his.getUserId(), his.getRequestId(),"刷新预热任务失败"));
                            return 0;
                        }, j -> results.add(j), q -> {
                            q.getMessage();
                        });
                    }
                    gg.merge((i) -> {
                        log.info("通知失败消息[{}]",i.size());
                    }, results).exit();
                }

            }
            log.info("end handlerFail:{}",msg);
            return true;
        }catch (Exception e){
            log.error("更新用户历史状态异常{}", e);
            if(msg.getRetryNum() > 3 || msg.getRoundRobinNum() >10){
                return false;
            }else{
                msg.setRetryNum(msg.getRetryNum() + 1);
                msg.setDelay(3000L);
                producer.sendAllMsg(msg);
                return true;
            }
        }
    }

    @Override
    public VendorInfo findVendorInfo(String vendor) throws RestfulException{
        return dateBaseService.getVendorInfoRepository().findByVendor(vendor);
    }

    private Boolean handlerNewRequest(TaskMsg msg) throws Exception {

        String taskId = msg.getTaskId();
        RefreshType type = msg.getType();
        VendorInfo vendorInfo = null;
        if(vendorInfoMap.containsKey(msg.getVendor())){
            vendorInfo = vendorInfoMap.get(msg.getVendor());
        }else{
            vendorInfo = dateBaseService.getVendorInfoRepository().findByVendor(msg.getVendor());
            if(vendorInfo != null){
                vendorInfoMap.put(msg.getVendor(), vendorInfo);
            }
        }
        String vendorStatus;
        if(vendorInfo == null){
            log.warn("[{}] vendorInfo db is null", msg.getVendor());
            vendorStatus = defaultVendorStatus;
        }else{
            vendorStatus = vendorInfo.getStatus().name();
        }

        if ("down".equals(vendorStatus)) {
            msg.setDelay(1 * 60 * 1000L);
            //rabbitListenerConfig.stop(msg.getOperation().name());//关闭监听
            producer.sendAllMsg(msg);//放回队列
            Thread.currentThread().sleep(sleepMs);
            return false;
        }

        if (msg.getIsLimit()) {
            //请求QPS限制
            int qps = vendorInfo!=null?vendorInfo.getTotalQps():requestQps;
            int size = vendorInfo!=null?vendorInfo.getTotalSize():requestSize;
            String redisKey = subQpsAndSizePrefix + msg.getVendor();
            // 0-成功，-1执行异常，-100超限
            int result = handlerQpsAndSizeLimit(redisKey,1, msg.getSize(), qps, size);
            if (-100 == result) {//设置delay为了防止多次接受，导致cpu增高
                log.warn("redis:{} LimitQps:{}", redisKey, qps);
                Thread.currentThread().sleep(sleepMs);
                return true;
            }else if(-200 == result) {
                log.warn("redis:{} LimitSize:{}", redisKey, size);
                Thread.currentThread().sleep(sleepMs);
                return true;
            } else if (-1 == result) {
                log.warn("redis:{} Limit: 执行异常", redisKey);
                throw new Exception(taskId + ": redis err");
            }
        }

        List<String> urls = new ArrayList<>();
        List<VendorContentTask> vendorContentTaskList = dateBaseService.getVendorTaskRepository().findByMergeId(taskId);
        if(vendorContentTaskList.size() == 0){
            return false;
        }
        for(VendorContentTask vct:vendorContentTaskList){
            if(vct.getContent() !=null && vct.getContentNumber()>0){
                urls.addAll(JSONArray.parseArray(vct.getContent(), String.class));
            }
        }
        if(urls.size() == 0 ){
            return false;
        }


        JSONObject response = null;
        RefreshPreloadData data = new RefreshPreloadData();
        data.setUrls(urls);

        VendorClientService vendorClient = getVendorClientVO(VendorEnum.getByCode(msg.getVendor()));
        switch (type.name()){
            case "url":
                response = vendorClient.refreshUrl(data);
                break;
            case "dir":
                response = vendorClient.refreshDir(data);
                break;
            case "preheat":
                response = vendorClient.preloadUrl(data);
                break;
            default:
                throw new Exception();
        }

        if(response == null){
            log.error("response is null");
            throw new Exception(taskId + ": response is null");
        }

        log.info("response:{}", response);
        if(response.containsKey("status") && response.containsKey("message")){
            String jobId = null;
            String message = response.getString("message");
            if(response.getString("status").equals(Constants.STATUS_WAIT)) {
                if(response.getString("message").equals(Constants.QPS_LIMIT)){
                    log.error("vendor:{}  QPS LIMIT", response.getString("vendor"));
                    msg.setDelay(timeOutMs);

                }else{
                    jobId = response.getJSONObject("data") == null ? null : response.getJSONObject("data").getString("taskId");
                    if(StringUtils.isNoneBlank(jobId)) {
                        if(responseAsync){
                            msg.setJobId(jobId);
                            msg.setTaskStatus(TaskStatus.PROCESSING);
                            msg.setRetryNum(0);//状态改变，清空计数
                            msg.setCallBack(CallBackEnum.request);
                            msg.setOperation(TaskOperationEnum.getVendorOperationCommon(msg.getVendor()));
                        }else{
                            for (VendorContentTask vct : vendorContentTaskList) {
                                vct.setJobId(jobId);
                                vct.setStatus(TaskStatus.PROCESSING);
                                vct.setMessage("任务请求厂商成功");
                                vct.setUpdateTime(new Date());
                            }
                            log.debug("handlerCommon request update");
                            dateBaseService.getVendorTaskRepository().saveAll(vendorContentTaskList);
                            log.debug("handlerCommon request update done");

                            RobinRecord robinRecord = new RobinRecord();
                            robinRecord.setRecordId(UUID.randomUUID().toString().replaceAll("-", ""));
                            robinRecord.setRobinId(jobId);
                            robinRecord.setType(type);
                            robinRecord.setVendor(msg.getVendor());
                            dateBaseService.getRobinRecordRepository().save(robinRecord);
                            log.info("记录待轮询[{}]", robinRecord);
                            return false;
                        }

                    }else{
                        log.error("[{}]无效的jobId", msg.getVendor());
                        throw new Exception(taskId + "无效的jobId");
                    }
                }
            }else if(response.getString("status").equals(Constants.STATUS_FAIL)){
                log.error("[{}]HandlerNewRequest失败", taskId);
                msg.setRetryNum(msg.getRetryNum() + 1);
                if(msg.getRetryNum() > timeOutLimit){
                    Map<String, Integer> failMap = new HashMap<>();
                    Map<String, Integer> versionMap = new HashMap<>();
                    log.error("[{}]handlerNewRequestUrl超过最大重试限制[{}]", taskId, timeOutLimit);
                    for(VendorContentTask v :vendorContentTaskList){
                        if(!failMap.containsKey(v.getRequestId())){
                            failMap.put(v.getRequestId(), 1);
                        }
                        if(!versionMap.containsKey(v.getRequestId())){
                            versionMap.put(v.getRequestId(), v.getVersion());
                        }
                        v.setMessage("厂商执行失败");
                        v.setStatus(TaskStatus.FAIL);
                        v.setUpdateTime(new Date());
                    }
                    dateBaseService.getVendorTaskRepository().saveAll(vendorContentTaskList);

                    if(failMap.keySet().size()>0) {
                        log.info("失败任务[{}]", failMap);
                        List<RobinCallBack> failList = new ArrayList<>();
                        for (String id : failMap.keySet()) {
                            log.debug("fail -> request id[{}], version[{}]", id, versionMap.get(id));
                            RobinCallBack rcb = new RobinCallBack();
                            rcb.setRequestId(id);
                            rcb.setNum(1);
                            rcb.setVersion(versionMap.get(id));
                            failList.add(rcb);
                        }
                        TaskMsg failMsg = new TaskMsg();
                        failMsg.setOperation(TaskOperationEnum.content_vendor_fail);
                        failMsg.setRobinCallBackList(failList);
                        producer.sendTaskMsg(failMsg);
                        log.debug("send fail msg done");
                    }
                    return false;
                }else{
                    msg.setDelay(timeOutMs);
                }

            }
        }else {
            log.error("response err:[{}]", response);
            throw new Exception(taskId + ": response err");
        }
        return true;
    }

    private Boolean handlerCommon(TaskMsg msg) throws Exception{
        try {
            if (msg.getCallBack().equals(CallBackEnum.request)) {//存入请求返回，并合并轮询请求，再次收敛请求数
                log.info("handlerCommon request");

                String mergeId = msg.getTaskId();
                List<VendorContentTask> vendorContentTaskList = dateBaseService.getVendorTaskRepository().findByMergeId(mergeId);
                if(vendorContentTaskList.size() == 0){
                    return false;
                }
                for (VendorContentTask vct : vendorContentTaskList) {
                    vct.setJobId(msg.getJobId());
                    //vct.setMergeId(msg.getJobId());
                    vct.setStatus(msg.getTaskStatus());
                    vct.setMessage("任务请求厂商成功");
                    vct.setUpdateTime(new Date());
                }
                log.debug("handlerCommon request update");
                dateBaseService.getVendorTaskRepository().saveAll(vendorContentTaskList);
                log.debug("handlerCommon request update done");

                RobinRecord robinRecord = new RobinRecord();
                robinRecord.setRecordId(UUID.randomUUID().toString().replaceAll("-", ""));
                robinRecord.setRobinId(msg.getJobId());
                robinRecord.setType(msg.getType());
                robinRecord.setVendor(msg.getVendor());
                dateBaseService.getRobinRecordRepository().save(robinRecord);
                log.info("记录待轮询[{}]", robinRecord);
                return false;
                //msg.setOperation(TaskOperationEnum.getVendorOperationRobin(msg.getVendor()));
                //msg.setTaskId(msg.getJobId());
            } else{

            }
            return false;
        }catch (Exception e){
            log.info("handlerCommon 异常[{}]", e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                return false;
            }else{
                msg.setDelay(timeOutMs);
                return true;
            }
        }

    }

    private synchronized int handlerMerge(TaskMsg msg, MergeType type) throws Exception{
        log.info("enter handlerMergeTask:{}, type[{}]",msg, type);
        return 0;
    }


    private int handlerTaskLimit(String key, int limit){
        //请求QPS限制
        List<String> keys = new ArrayList<>();
        keys.add(key);
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(limit));
        // 0-成功，-1执行异常，-100超限
        return luaScriptService.executeQpsScript(keys, args);
    }

    private int handlerQpsAndSizeLimit(String key, int curentLimit, int curentSize, int limit, int size){
        List<String> keys = new ArrayList<>();
        keys.add(key);
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(curentLimit));
        args.add(String.valueOf(curentSize));
        args.add(String.valueOf(limit));
        args.add(String.valueOf(size));
        // 0-成功，-1执行异常，-100超限
        return luaScriptService.executeQpsAndTotalScript(keys, args);
    }


    private void handleTaskRepositityException(Exception e) throws RestfulException {
        if (e instanceof EmptyResultDataAccessException || e instanceof NoSuchElementException) {
            log.error("no such date");
            //throw RestfulException.ErrNosuchTask;
        } else {
            log.error("db err:{}", e.getMessage());
            //throw new RestfulException(ErrCodeEnum.ErrDBInternal.getCode(), e.getMessage());
        }
    }

    private VendorClientService getVendorClientVO( VendorEnum vendor) {
        switch (vendor) {
            case venus:
                return venusClientService;
            case tencent:
                return tencentClientService;
            case qiniu:
                return qiniuClientService;
            case ksyun:
                return ksyunClientService;
            case net:
                return netClientService;
            case jdcloud:
                return jdcloudClientService;
            case chinacache:
                return chinaCacheClientService;
            case aliyun:
                return aliyunClientService;
            case baishan:
                return baishanClientService;
            default:
                log.info("该厂商不存在，请检查[{}]", vendor.getCode());
                return null;
        }
    }


    private int stopAndSendDelayFanoutMsg(TaskMsg taskMsg){
        log.info("关闭消费[{}]", taskMsg.getOperation().name());
        rabbitListenerConfig.stop(taskMsg.getOperation().name());
        String key = taskMsg.getOperation().name() + FanoutType.consumer_start;
        int re = handlerTaskLimit(key, 1);
        if (re == 0) {
            TaskMsg DelayFanoutMsg = new TaskMsg();
            DelayFanoutMsg.setIsFanout(true);
            DelayFanoutMsg.setOperation(taskMsg.getOperation());
            DelayFanoutMsg.setFanoutOpt(FanoutType.consumer_start);
            DelayFanoutMsg.setDelay(1000L);
            producer.sendFanoutDelayMsg(DelayFanoutMsg);
        }
        return re;
    }
}
