package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.client.*;
import cn.com.pingan.cdn.common.*;
import cn.com.pingan.cdn.config.RedisLuaScriptService;
import cn.com.pingan.cdn.exception.RestfulException;
import cn.com.pingan.cdn.model.mysql.ContentHistory;
import cn.com.pingan.cdn.model.mysql.ContentItem;
import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import cn.com.pingan.cdn.model.mysql.VendorInfo;
import cn.com.pingan.cdn.rabbitmq.config.RabbitListenerConfig;
import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.rabbitmq.producer.Producer;
import cn.com.pingan.cdn.repository.mysql.ContentHistoryRepository;
import cn.com.pingan.cdn.repository.mysql.VendorInfoRepository;
import cn.com.pingan.cdn.repository.mysql.VendorTaskRepository;
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

    @Value("${task.robin.qps.default:10}")
    private Integer robinQps;

    @Value("${task.vendor.status.default:up}")
    private String defaultVendorStatus;

    @Value("${spring.datasource.mysql.max-total:50}")
    private Integer dataBaseQps;

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
    private VendorTaskRepository vendorTaskRepository;

    @Autowired
    private  VendorInfoRepository vendorInfoRepository;

    @Autowired
    ContentHistoryRepository contentHistoryRepository;

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
            int updateCount = this.vendorTaskRepository.updateVersion(msg.getTaskId(), msg.getVersion(), msg.getVersion() + 1);
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
        boolean isMerge = msg.getIsMerge();
        boolean flag = true;
        try {
            flag = handlerNewRequest(msg);
            if(!flag){
                return false;
            }
        }catch (Exception e){
            log.error("handlerNewRequestUrl Exception:{}",e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                log.error("[{}]handlerNewRequestUrl超过最大重试限制[{}]", taskId, timeOutLimit);
                if(isMerge){
                    List<VendorContentTask> vendorContentTaskList = vendorTaskRepository.findByMergeId(taskId);
                    for(VendorContentTask v :vendorContentTaskList){
                        v.setMessage("重试失败");
                        v.setStatus(TaskStatus.FAIL);
                        v.setUpdateTime(new Date());
                    }
                    vendorTaskRepository.saveAll(vendorContentTaskList);
                }else{
                    //TODO
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
        boolean isMerge = msg.getIsMerge();
        boolean flag = true;
        try {
            flag = handlerNewRequest(msg);
            if(!flag){
                return false;
            }
        }catch (Exception e){
            log.error("handlerNewRequestDir Exception:{}",e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                log.error("[{}]handlerNewRequestDir超过最大重试限制[{}]", taskId, timeOutLimit);
                if(isMerge){
                    List<VendorContentTask> vendorContentTaskList = vendorTaskRepository.findByMergeId(taskId);
                    for(VendorContentTask v :vendorContentTaskList){
                        v.setMessage("重试失败");
                        v.setStatus(TaskStatus.FAIL);
                        v.setUpdateTime(new Date());
                    }
                    vendorTaskRepository.saveAll(vendorContentTaskList);
                }else{
                    //TODO
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
        boolean isMerge = msg.getIsMerge();
        boolean flag = true;
        try {
            flag = handlerNewRequest(msg);
            if(!flag){
                return false;
            }
        }catch (Exception e){
            log.error("handlerNewRequestPreload Exception:{}",e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                log.error("[{}]handlerNewRequestPreload超过最大重试限制[{}]", taskId, timeOutLimit);
                if(isMerge){
                    List<VendorContentTask> vendorContentTaskList = vendorTaskRepository.findByMergeId(taskId);
                    for(VendorContentTask v :vendorContentTaskList){
                        v.setMessage("重试失败");
                        v.setStatus(TaskStatus.FAIL);
                        v.setUpdateTime(new Date());
                    }
                    vendorTaskRepository.saveAll(vendorContentTaskList);
                }else{
                    //TODO
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
        String taskId = msg.getTaskId();
        try {

            VendorInfo vendorInfo = null;
            if(vendorInfoMap.containsKey(msg.getVendor())){
                vendorInfo = vendorInfoMap.get(msg.getVendor());
            }else{
                vendorInfo = vendorInfoRepository.findByVendor(msg.getVendor());
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
                rabbitListenerConfig.stop(msg.getOperation().name());//关闭监听
                producer.sendAllMsg(msg);//放回队列
                return false;
            }
            if (msg.getIsLimit()) {//
                //请求QPS限制
                int limit = vendorInfo!=null?vendorInfo.getRobinQps():robinQps;
                String redisKey = msg.getOperation().toString();
                // 0-成功，-1执行异常，-100超限
                int result = handlerTaskLimit(redisKey, limit);
                if (-100 == result) {
                    log.warn("redis:{} Limit:{}", redisKey, limit);
                    //msg.setDelay(1000L);
                    producer.sendAllMsg(msg);
                    return false;
                } else if (-1 == result) {
                    log.warn("redis:{} Limit:{} 执行异常", redisKey, limit);
                    throw new Exception(taskId + ": redis err");
                }
            }

            JSONObject response;

            RefreshPreloadTaskStatusDTO dto = new RefreshPreloadTaskStatusDTO();
            String type;
            if(msg.getType().equals(RefreshType.url) || msg.getType().equals(RefreshType.dir)){
                type = "refresh";
            }else if(msg.getType().equals(RefreshType.preheat)){
                type = "preload";
            }else{
                log.error("无效Task");
                return false;
            }

            RefreshPreloadItem item = new RefreshPreloadItem();
            item.setJobId(taskId);
            item.setJobType(type);

            List<RefreshPreloadItem> itemList = new ArrayList<>();
            itemList.add(item);
            dto.setTaskList(itemList);

            VendorClientService vendorClient = getVendorClientVO(VendorEnum.getByCode(msg.getVendor()));
            response = vendorClient.queryRefreshPreloadTask(dto);
            if(response == null || !response.containsKey("data")){
                log.error("response is null or no data");
                throw new Exception(taskId + ": response no data");
            }
            log.info("response:{}", response);

            JSONArray jsonArray = response.getJSONArray("data");
            if (jsonArray != null && jsonArray.size() > 0) {
                TaskStatus ts = TaskStatus.ROUND_ROBIN;
                String message = "";
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json != null && json.getString("jobId") != null && json.getString("jobId").equals(taskId)) {
                        if (Constants.STATUS_SUCCESS.equals(json.getString("status"))) {
                            ts = TaskStatus.SUCCESS;
                            //message = StringUtils.isNoneBlank(json.getString("message"))?json.getString("message"):"厂商执行成功";
                            message = "厂商执行成功";
                            msg.setRetryNum(0);
                            log.info("刷新预热任务完成，任务编号[{}]", json.getString("jobId"));
                        } else if (Constants.STATUS_FAIL.equals(json.getString("status"))) {
                            ts = TaskStatus.FAIL;
                            //message = StringUtils.isNoneBlank(json.getString("message"))?json.getString("message"):"厂商执行失败";
                            message = "厂商执行失败";
                            msg.setRetryNum(0);
                            log.info("刷新预热任务失败，任务编号[{}]", json.getString("jobId"));
                        } else if (Constants.STATUS_WAIT.equals(json.getString("status"))) {
                            msg.setRoundRobinNum(msg.getRoundRobinNum() + 1);
                            if(msg.getRoundRobinNum() > roundLimit){
                                ts = TaskStatus.FAIL;
                                message = "轮询超出重试次数";
                            }else{
                                msg.setDelay(roundMs);
                            }
                            log.info("刷新预热任务未完成，任务编号[{}], 等待{}ms后查询", json.getString("jobId"), roundMs);
                            msg.setRetryNum(0);
                        } else {
                            log.info("返回值状态异常，期望的状态是SUCCESS/FAIL/WAIT,收到的状态是[{}]", json.getString("status"));
                            msg.setRetryNum(msg.getRetryNum() + 1);
                            if(msg.getRetryNum() > timeOutLimit){
                                ts = TaskStatus.FAIL;
                                message = "超出重试次数";
                            }else{
                                msg.setDelay(timeOutMs);
                            };
                        }
                        break;
                    }
                }
                if(ts.equals(TaskStatus.SUCCESS) || ts.equals(TaskStatus.FAIL)){
                    Set<String> ids = new HashSet<String>();
                    if(msg.getIsMerge()){
                        List<VendorContentTask> vendorContentTaskList = vendorTaskRepository.findByMergeId(taskId);
                        if(vendorContentTaskList.size()>0) {
                            for (VendorContentTask v : vendorContentTaskList) {
                                ids.add(v.getRequestId());
                                v.setMessage(message);
                                v.setStatus(ts);
                                v.setUpdateTime(new Date());
                            }
                            vendorTaskRepository.saveAll(vendorContentTaskList);
                            if(ts.equals(TaskStatus.SUCCESS)){
                                for(String id :ids){
                                    contentHistoryRepository.updateSuccessNumByRequestIdAndVersion(id, msg.getHisVersion());
                                }
                            }else{
                                if(ids.size() == 1){
                                    contentHistoryRepository.updateStatusAndMessageByRequestIdAndVersion(new ArrayList<String>(ids).get(0), msg.getHisVersion(), HisStatus.FAIL.name(), message);
                                }
                                List<ContentHistory> chs = contentHistoryRepository.findByRequestIdIn(new ArrayList<String>(ids));
                                if(chs.size()>0){
                                    boolean f = false;
                                    for(ContentHistory ch: chs){
                                        if(ch.getVersion()!= msg.getHisVersion()){
                                            continue;
                                        }
                                        ch.setStatus(HisStatus.FAIL);
                                        ch.setMessage(message);
                                        ch.setUpdateTime(new Date());
                                        f = true;
                                    }
                                    if(f) contentHistoryRepository.saveAll(chs);
                                }
                            }
                        }
                    }else{
                        throw new Exception(taskId + ": not merge");
                    }
                    return true;
                }
            }else{
                log.info("返回无效数据{}", response);
                throw new Exception(taskId + ": response err");
            }
        }catch (Exception e){
            log.error("HandlerRoundRobin Exception:{}",e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                if(msg.getIsMerge()){
                    List<VendorContentTask> vendorContentTaskList = vendorTaskRepository.findByMergeId(taskId);
                    for(VendorContentTask v :vendorContentTaskList){
                        v.setMessage("轮询超出重试次数");
                        v.setStatus(TaskStatus.FAIL);
                        v.setUpdateTime(new Date());
                    }
                    vendorTaskRepository.saveAll(vendorContentTaskList);
                }else{
                    //TODO
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
        return false;
    }

    @Override
    public Boolean handlerFail(TaskMsg msg) throws RestfulException{
        log.info("handlerFail:{}",msg);
        return false;
    }

    @Override
    public VendorInfo findVendorInfo(String vendor) throws RestfulException{
        /*
        try{
            VendorInfo info = vendorInfoRepository.findByVendor(vendor);
            if(info != null){
                return info;
            }else{
                //TODO 抛异常，通知
                log.error("findVendorInfo:{} null", vendor);
                throw new NoSuchElementException();
            }
        }catch (Exception e){
            log.error("findVendorInfo:{} err:{}", vendor, e);
            handleTaskRepositityException(e);
        }
        */
        return vendorInfoRepository.findByVendor(vendor);
    }

    private Boolean handlerNewRequest(TaskMsg msg) throws Exception {

        String taskId = msg.getTaskId();
        boolean isMerge = msg.getIsMerge();
        RefreshType type = msg.getType();
        VendorInfo vendorInfo = null;
        if(vendorInfoMap.containsKey(msg.getVendor())){
            vendorInfo = vendorInfoMap.get(msg.getVendor());
        }else{
            vendorInfo = vendorInfoRepository.findByVendor(msg.getVendor());
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
            rabbitListenerConfig.stop(msg.getOperation().name());//关闭监听
            producer.sendAllMsg(msg);//放回队列
            return false;
        }
        if (msg.getIsLimit()) {//
            //请求QPS限制
            int qps = vendorInfo!=null?vendorInfo.getTotalQps():requestQps;
            int size = vendorInfo!=null?vendorInfo.getTotalSize():requestSize;
            String redisKey = subQpsAndSizePrefix + msg.getVendor();
            // 0-成功，-1执行异常，-100超限
            int result = handlerQpsAndSizeLimit(redisKey,1, msg.getSize(), qps, size);
            if (-100 == result) {
                log.warn("redis:{} LimitQps:{}", redisKey, qps);
                /*
                if(msg.getSize() <= 20){
                    handlerMergeTask(msg, MergeType.add);
                }
                */
                //msg.setDelay(1000L);
                return true;
            }else if(-200 == result) {
                log.warn("redis:{} LimitSize:{}", redisKey, size);
                /*
                if(msg.getSize() <= 20){
                    handlerMergeTask(msg, MergeType.add);
                }
                */
                //msg.setDelay(1000L);
                return true;
            } else if (-1 == result) {
                log.warn("redis:{} Limit: 执行异常", redisKey);
                throw new Exception(taskId + ": redis err");
            }
        }
        List<VendorContentTask> vendorContentTaskList = null;
        if(isMerge){
            vendorContentTaskList = vendorTaskRepository.findByMergeId(taskId);
        }else{
            throw new Exception(taskId + ": not merge");
        }
        List<String> urls = vendorContentTaskList.stream().map(i->i.getContent()).collect(Collectors.toList());

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
                        msg.setJobId(jobId);
                        msg.setTaskStatus(TaskStatus.PROCESSING);
                        msg.setRetryNum(0);//状态改变，清空计数
                        msg.setCallBack(CallBackEnum.request);
                        msg.setOperation(TaskOperationEnum.getVendorOperationCommon(msg.getVendor()));

                    }else{
                        log.error("[{}]无效的jobId", msg.getVendor());
                    }
                }
            }else if(response.getString("status").equals(Constants.STATUS_FAIL)){
                msg.setRetryNum(msg.getRetryNum() + 1);
                if(msg.getRetryNum() > timeOutLimit){
                    for(VendorContentTask v :vendorContentTaskList){
                        v.setMessage(message);
                        v.setStatus(TaskStatus.FAIL);
                        if(StringUtils.isNoneBlank(jobId)){
                            v.setJobId(jobId);
                            v.setMergeId(jobId);
                        }
                        v.setUpdateTime(new Date());
                    }
                    vendorTaskRepository.saveAll(vendorContentTaskList);
                    log.error("[{}]HandlerNewRequest失败", taskId);
                    return false;
                }else{
                    msg.setDelay(timeOutMs);
                }
            }
            /*
            for(VendorContentTask v :vendorContentTaskList){
                v.setMessage(message);
                v.setStatus(ts);
                if(StringUtils.isNoneBlank(jobId)){
                    v.setJobId(jobId);
                    v.setMergeId(jobId);
                }
                v.setUpdateTime(new Date());
            }
            vendorTaskRepository.saveAll(vendorContentTaskList);
            if(ts.equals(TaskStatus.FAIL)){
                log.error("[{}]HandlerNewRequest失败", taskId);
                return false;
            }else if(ts.equals(TaskStatus.ROUND_ROBIN)){
                log.info("[{}]HandlerNewRequest成功", taskId);
            }
            */

        }else {
            log.error("response err:[{}]", response);
            throw new Exception(taskId + ": response err");
        }
        return true;
    }

    private Boolean handlerCommon(TaskMsg msg) throws Exception{
        try {
            if (msg.getCallBack().equals(CallBackEnum.request)) {
                log.info("handlerCommon request");
                String redisKey = msg.getOperation().name()+ "_" + CallBackEnum.request.name();
                // 0-成功，-1执行异常，-100超限
                int result = handlerTaskLimit(redisKey, dataBaseQps);
                if (-100 == result) {
                    log.warn("redis:{} LimitQps:{}", redisKey, dataBaseQps);
                    return true;
                } else if (-1 == result) {
                    log.warn("redis:{} Limit: 执行异常", redisKey);
                    throw new Exception( redisKey + ": redis err");
                }

                String mergeId = msg.getTaskId();
                List<VendorContentTask> vendorContentTaskList = vendorTaskRepository.findByMergeId(mergeId);
                for (VendorContentTask vct : vendorContentTaskList) {
                    vct.setJobId(msg.getJobId());
                    vct.setMergeId(msg.getJobId());
                    vct.setStatus(msg.getTaskStatus());
                    vct.setMessage("任务请求厂商成功");
                    vct.setUpdateTime(new Date());
                }
                log.info("handlerCommon request update");
                vendorTaskRepository.saveAll(vendorContentTaskList);
                log.info("handlerCommon request update done");

                msg.setOperation(TaskOperationEnum.getVendorOperationRobin(msg.getVendor()));
                msg.setTaskId(msg.getJobId());
            } else{

            }
            return true;
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

        List<String> ids;
        if(type.equals(MergeType.add)) {
            if (mergeHashMap.containsKey(msg.getOperation().name())) {
                ids = mergeHashMap.get(msg.getOperation().name());
                ids.add(msg.getTaskId());
            } else {
                ids = Collections.synchronizedList(new LinkedList<String>());
                ids.add(msg.getTaskId());
                mergeHashMap.put(msg.getOperation().name(), ids);
            }
            log.info("handlerMerge新增完成");
        }else{
            List<VendorContentTask> toSaveContentTaskList = new ArrayList<>();
            List<TaskMsg> toSendMq = new ArrayList<>();
            for(String k: mergeHashMap.keySet()){
                ids = mergeHashMap.get(k);
                VendorInfo vendorInfo = null;
                String vendorName = TaskOperationEnum.getVendorString(TaskOperationEnum.of(k));
                if(vendorInfoMap.containsKey(vendorName)){
                    vendorInfo = vendorInfoMap.get(vendorName);
                }else{
                    vendorInfo = vendorInfoRepository.findByVendor(vendorName);
                    if(vendorInfo != null){
                        vendorInfoMap.put(vendorName, vendorInfo);
                    }
                }
                int mergeSize = 10;
                RefreshType refershType = RefreshType.url;
                if(vendorInfo != null){
                    if( -1 != k.indexOf("_url")){
                        mergeSize = vendorInfo.getMergeUrlCount();
                        refershType = RefreshType.url;
                    }else if(-1 != k.indexOf("_dir")){
                        mergeSize = vendorInfo.getMergeDirCount();
                        refershType = RefreshType.dir;
                    }else if(-1 != k.indexOf("_preheat")) {
                        mergeSize = vendorInfo.getMergePreheatCount();
                        refershType = RefreshType.preheat;
                    }else{
                        log.warn("err merge Operation:[{}]", k);
                    }
                }

                log.info("handlerMerge查询");
                List<VendorContentTask> vendorContentTaskList = vendorTaskRepository.findByMergeIdIn(ids);
                log.info("handlerMerge查询数量[{}]", vendorContentTaskList.size());
                int curCount =0;
                String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                for(VendorContentTask vct: vendorContentTaskList){
                    if((curCount % mergeSize) == 0){
                        mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                        TaskMsg vendorTaskMsg = new TaskMsg();
                        vendorTaskMsg.setTaskId(mergeId);
                        vendorTaskMsg.setOperation(TaskOperationEnum.of(k));
                        vendorTaskMsg.setVendor(vendorName);
                        vendorTaskMsg.setVersion(0);
                        vendorTaskMsg.setHisVersion(0);
                        vendorTaskMsg.setIsMerge(true);
                        vendorTaskMsg.setType(refershType);
                        vendorTaskMsg.setRetryNum(0);
                        vendorTaskMsg.setSize((vendorContentTaskList.size() - curCount) >= mergeSize?mergeSize:(vendorContentTaskList.size() - curCount));
                        toSendMq.add(vendorTaskMsg);
                    }
                    vct.setMergeId(mergeId);
                    toSaveContentTaskList.add(vct);
                    curCount++;
                }
            }
            if(toSaveContentTaskList.size() > 0 ) {
                log.info("handlerMerge入库数量[{}]", toSaveContentTaskList.size());
                vendorTaskRepository.saveAll(toSaveContentTaskList);
                log.info("handlerMerge入库完成");
                for (TaskMsg mergeMsg : toSendMq) {
                    producer.sendTaskMsg(mergeMsg);
                }
                log.info("handlerMerge消息完成");
            }
            mergeHashMap.clear();
            log.info("handlerMerge清理完成");

        }
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
}
