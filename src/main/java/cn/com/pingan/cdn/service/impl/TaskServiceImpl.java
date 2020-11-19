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

    public static Map<String,Object> mergeHashMap= new ConcurrentHashMap<String,Object>();

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
    public int handlerMergeTask(TaskMsg msg) throws RestfulException {
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
    public Boolean handlerNewRequest(VendorContentTask task, TaskMsg msg) throws RestfulException {
        log.info("enter handlerNewRequest:{}",task);
        Boolean flag = true;//原本控制是否需要循环，目前都为true
        try{
            VendorInfo vendorInfo = vendorInfoRepository.findByVendor(task.getVendor());
            String vendorStatus;
            if(vendorInfo == null){
                log.warn("[{}] vendorInfo db is null", task.getVendor());
                vendorStatus = defaultVendorStatus;
            }else{
                vendorStatus = vendorInfo.getStatus().name();
            }
            if ("down".equals(vendorStatus)) {
                msg.setDelay(1 * 60 * 1000L);
                rabbitListenerConfig.stop(msg.getOperation().name());//关闭监听
                this.pushTaskMsg(msg);//放回队列
                return flag;
            }
            if (msg.getIsLimit()) {//
                //请求QPS限制
                int qps = vendorInfo!=null?vendorInfo.getTotalQps():requestQps;
                String redisKey = msg.getOperation().toString();
                // 0-成功，-1执行异常，-100超限
                int result = handlerTaskLimit(msg, redisKey,qps);
                if (-100 == result) {
                    log.warn("redis:{} Limit:{}", redisKey, qps);
                    //msg.setDelay(1000L);
                    return flag;
                } else if (-1 == result) {
                    log.warn("redis:{} Limit:{} 执行异常", redisKey, qps);
                    msg.setDelay(timeOutMs);
                    return flag;//3秒后重试
                }
            }

            JSONObject response = null;
            RefreshPreloadData data = new RefreshPreloadData();
            List<String> urls = new ArrayList<>();
            urls.add(task.getContent());
            data.setUrls(urls);

            VendorClientService vendorClient = getVendorClientVO(VendorEnum.getByCode(task.getVendor()));

            switch (task.getType().name()){
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
                throw new Exception(task.getTaskId() + ": response is null");
            }

            log.info("response:{}", response);

            if(response.containsKey("status") && response.containsKey("message")){
                task.setMessage(response.getString("message"));
                if(response.getString("status").equals(Constants.STATUS_WAIT)) {
                    task.setStatus(TaskStatus.WAIT);
                    if(response.getString("message").equals(Constants.QPS_LIMIT)){
                        log.error("vendor:{}  QPS LIMIT", response.getString("vendor"));
                        msg.setDelay(timeOutMs);

                    }else{
                        task.setJobId(response.getJSONObject("data") == null ? null : response.getJSONObject("data").getString("taskId"));
                        task.setStatus(TaskStatus.PROCESSING);
                        msg.setDelay(30000L);//30秒后查询
                        msg.setRetryNum(0);//状态改变，清空计数
                        msg.setOperation(TaskOperationEnum.of(msg.getOperation().name()+"_robin"));
                    }
                }else if(response.getString("status").equals(Constants.STATUS_FAIL)){
                    msg.setRetryNum(msg.getRetryNum() + 1);
                    if(msg.getRetryNum() > timeOutLimit){
                        task.setStatus(TaskStatus.FAIL);
                    }else{
                        msg.setDelay(timeOutMs);
                    }
                }
                vendorTaskRepository.save(task);

            }else {
                log.error("response err:[{}]", response);
                throw new Exception(task.getTaskId() + ": response err");
            }

        }catch (Exception e){
            log.error("HandlerNewRequest Exception:{}",e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                log.error("[{}]HandlerNewRequest超过最大重试限制[{}]", task.getTaskId(), timeOutLimit);
                task.setStatus(TaskStatus.FAIL);
                vendorTaskRepository.save(task);
            }else{
                msg.setDelay(timeOutMs);
            }
        }
        return flag;
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
                int limit = robinQps;
                String redisKey = msg.getOperation().toString();
                // 0-成功，-1执行异常，-100超限
                int result = handlerTaskLimit(msg, redisKey, limit);
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
    public Boolean handlerRoundRobin(VendorContentTask task, TaskMsg msg) throws RestfulException {
        log.info("enter handlerRoundRobin:{}",task);
        Boolean flag = true;

        VendorInfo vendorInfo = vendorInfoRepository.findByVendor(task.getVendor());
        String vendorStatus;
        if(vendorInfo == null){
            log.warn("[{}] vendorInfo db is null", task.getVendor());
            vendorStatus = defaultVendorStatus;
        }else{
            vendorStatus = vendorInfo.getStatus().name();
        }
        if ("down".equals(vendorStatus)) {
            msg.setDelay(1 * 60 * 1000L);
            rabbitListenerConfig.stop(msg.getOperation().name());//关闭监听
            producer.sendAllMsg(msg);//放回队列
            return flag;
        }
        if (msg.getIsLimit()) {//
            //请求QPS限制
            int limit = robinQps;
            String redisKey = msg.getOperation().toString();
            // 0-成功，-1执行异常，-100超限
            int result = handlerTaskLimit(msg, redisKey, limit);
            if (-100 == result) {
                log.warn("redis:{} Limit:{}", redisKey, limit);
                //msg.setDelay(1000L);
                return flag;
            } else if (-1 == result) {
                log.warn("redis:{} Limit:{} 执行异常", redisKey, limit);
                msg.setDelay(timeOutMs);
                return flag;//3秒后重试
            }
        }

        JSONObject response;

        RefreshPreloadTaskStatusDTO dto = new RefreshPreloadTaskStatusDTO();
        String type;
        if(task.getType().equals(RefreshType.url) || task.getType().equals(RefreshType.dir)){
            type = "refresh";
        }else if(task.getType().equals(RefreshType.preheat)){
            type = "preload";
        }else{
            log.error("无效Task");
            return false;
        }

        RefreshPreloadItem item = new RefreshPreloadItem();
        item.setJobId(task.getJobId());
        item.setJobType(type);

        List<RefreshPreloadItem> itemList = new ArrayList<>();
        itemList.add(item);
        dto.setTaskList(itemList);

        VendorClientService vendorClient = getVendorClientVO(VendorEnum.getByCode(task.getVendor()));
        try {
            response = vendorClient.queryRefreshPreloadTask(dto);
            if(response == null || !response.containsKey("data")){
                log.error("response is null or no data");
                throw new Exception(task.getTaskId() + ": response no data");
            }
            log.info("response:{}", response);

            JSONArray jsonArray = response.getJSONArray("data");
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json != null && json.getString("jobId") != null && json.getString("jobId").equals(task.getJobId())) {
                        if (Constants.STATUS_SUCCESS.equals(json.getString("status"))) {
                            task.setStatus(TaskStatus.SUCCESS);
                            task.setMessage(StringUtils.isNoneBlank(json.getString("message"))?json.getString("message"):"厂商执行成功");
                            task.setUpdateTime(new Date());

                            flag = true;
                            vendorTaskRepository.save(task);
                            msg.setRetryNum(0);
                            log.info("刷新预热任务完成，任务编号[{}]", json.getString("jobId"));
                        } else if (Constants.STATUS_FAIL.equals(json.getString("status"))) {
                            task.setStatus(TaskStatus.FAIL);
                            task.setMessage(StringUtils.isNoneBlank(json.getString("message"))?json.getString("message"):"厂商执行失败");
                            task.setUpdateTime(new Date());
                            flag = true;
                            vendorTaskRepository.save(task);
                            msg.setRetryNum(0);
                            log.info("刷新预热任务失败，任务编号[{}]", json.getString("jobId"));
                        } else if (Constants.STATUS_WAIT.equals(json.getString("status"))) {
                            if(!task.getStatus().equals(TaskStatus.ROUND_ROBIN)) {
                                task.setStatus(TaskStatus.ROUND_ROBIN);
                                task.setUpdateTime(new Date());
                                vendorTaskRepository.save(task);
                            }
                            msg.setRoundRobinNum(msg.getRoundRobinNum() + 1);
                            if(msg.getRoundRobinNum() > roundLimit){
                                task.setStatus(TaskStatus.FAIL);
                            }else{
                                msg.setDelay(roundMs);
                            }
                            log.info("刷新预热任务未完成，任务编号[{}], 等待{}ms后查询", json.getString("jobId"), roundMs);
                            msg.setRetryNum(0);
                            flag = true;
                        } else {
                            log.info("返回值状态异常，期望的状态是SUCCESS/FAIL/WAIT,收到的状态是[{}]", json.getString("status"));
                            msg.setRetryNum(msg.getRetryNum() + 1);
                            if(msg.getRetryNum() > timeOutLimit){
                                task.setStatus(TaskStatus.FAIL);
                            }else{
                                msg.setDelay(timeOutMs);
                            }
                            flag = true;
                        }
                        break;
                    }
                }
            }else{
                log.info("返回无效数据{}", response);
                throw new Exception(task.getTaskId());
            }

        }catch (Exception e){
            log.error("HandlerRoundRobin Exception:{}",e);
            msg.setRetryNum(msg.getRetryNum() + 1);
            if(msg.getRetryNum() > timeOutLimit){
                task.setStatus(TaskStatus.FAIL);
                log.error("[{}]HandlerRoundRobin超过最大重试限制[{}]", task.getTaskId(), timeOutLimit);
                vendorTaskRepository.save(task);
            }else{
                msg.setDelay(timeOutMs);
            }
            flag = true;
        }
        return flag;
    }

    @Override
    public Boolean handlerSuccess(VendorContentTask task, TaskMsg msg) throws RestfulException{
        log.info("handlerSuccess:{}",task);

        /*
        boolean allSuccess = true;
        ContentItem item = contentItemRepository.findByItemId(task.getItemId());
        if(item.getStatus().equals(HisStatus.WAIT)){
            List<VendorContentTask> tasks = vendorTaskRepository.findByItemId(task.getItemId());
            if(JSONArray.parseArray(item.getVendor()).size() != tasks.size()){
                item.setStatus(HisStatus.FAIL);
                item.setUpdateTime(new Date());
                item.setMessage("任务数入库不正确");
                contentItemRepository.save(item);
                contentHistoryRepository.updateStatusAndMessage(item.getRequestId(), HisStatus.FAIL.name(), "任务数入库不正确");
                log.error("厂商任务数[{}]与厂商数[{}]不一致，设置用户胡历史任务[{}]为失败", tasks.size(), JSONArray.parseArray(item.getVendor()).size(), item.getRequestId());
            }else{
                for(VendorContentTask t: tasks){
                    if(t.getStatus().equals(TaskStatus.FAIL)){
                        item.setStatus(HisStatus.FAIL);
                        contentItemRepository.save(item);
                        log.info("设置拆分任务状态：{}", HisStatus.FAIL);
                        allSuccess = false;
                        break;
                    }else if(t.getStatus().equals(TaskStatus.WAIT)){
                        allSuccess = false;
                        break;
                    }else{
                        continue;
                    }
                }
                if(allSuccess ){
                    item.setStatus(HisStatus.SUCCESS);
                    item.setMessage("所有厂商任务成功");
                    item.setUpdateTime(new Date());
                    contentItemRepository.save(item);
                    log.info("设置拆分任务状态：{}", HisStatus.SUCCESS);

                    ContentHistory history = contentHistoryRepository.findByRequestId(item.getRequestId());
                    if(history.getStatus().equals(HisStatus.WAIT)) {
                        List<ContentItem> items = contentItemRepository.findByRequestId(item.getRequestId());
                        if (items.size() != history.getContentNumber()) {
                            contentHistoryRepository.updateStatusAndMessage(item.getRequestId(), HisStatus.FAIL.name(), "任务数入库不正确");
                            log.error("拆分任务数[{}]与用户任务数[{}]不一致, 用户请求历史任务[{}]设置为失败", items.size(), history.getContentNumber(), item.getRequestId());
                        }else {
                            for(ContentItem it: items){
                                if(it.getStatus().equals(HisStatus.FAIL)){
                                    contentHistoryRepository.updateStatus(history.getRequestId(), HisStatus.FAIL.name());
                                    allSuccess = false;
                                    break;
                                }else if(it.getStatus().equals(HisStatus.WAIT)){
                                    allSuccess = false;
                                    break;
                                }else{
                                    continue;
                                }
                            }

                            if(allSuccess){
                                contentHistoryRepository.updateStatusAndMessage(history.getRequestId(), HisStatus.SUCCESS.name(), "任务执行成功");
                                log.info("设置用户请求历史任务[{}]的状态[{}]", history.getRequestId(), HisStatus.SUCCESS);
                            }
                        }
                    }
                }
            }
        }
        */
        return false;
    }

    @Override
    public Boolean handlerFail(VendorContentTask task, TaskMsg msg) throws RestfulException{
        log.info("handlerFail:{}",task);
/*
        ContentItem item = contentItemRepository.findByItemId(task.getItemId());
        item.setStatus(HisStatus.FAIL);
        item.setMessage(StringUtils.isNoneBlank(task.getMessage())?task.getMessage():"任务执行失败");
        item.setUpdateTime(new Date());
        contentItemRepository.save(item);
        log.info("设置拆分任务状态：{}", HisStatus.FAIL);
        contentHistoryRepository.updateStatusAndMessageByRequestId(item.getRequestId(), HisStatus.FAIL.name(),"任务执行失败");
        */
        log.info("设置用户请求历史任务状态：{}", HisStatus.FAIL);

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
                //msg.setDelay(1000L);
                return true;
            }else if(-200 == result) {
                log.warn("redis:{} LimitSize:{}", redisKey, size);
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
            TaskStatus ts = TaskStatus.WAIT;
            String jobId = null;
            String message = response.getString("message");
            if(response.getString("status").equals(Constants.STATUS_WAIT)) {
                ts = TaskStatus.WAIT;
                if(response.getString("message").equals(Constants.QPS_LIMIT)){
                    log.error("vendor:{}  QPS LIMIT", response.getString("vendor"));
                    msg.setDelay(timeOutMs);

                }else{
                    jobId = response.getJSONObject("data") == null ? null : response.getJSONObject("data").getString("taskId");
                    if(StringUtils.isNoneBlank(jobId)) {
                        ts = TaskStatus.ROUND_ROBIN;
                        msg.setDelay(30000L);//30秒后查询
                        msg.setRetryNum(0);//状态改变，清空计数
                        msg.setOperation(TaskOperationEnum.getVendorOperationRobin(msg.getVendor()));
                        msg.setTaskId(jobId);
                    }else{
                        log.error("[{}]无效的jobId", msg.getVendor());
                    }
                }
            }else if(response.getString("status").equals(Constants.STATUS_FAIL)){
                msg.setRetryNum(msg.getRetryNum() + 1);
                if(msg.getRetryNum() > timeOutLimit){
                    ts = TaskStatus.FAIL;
                }else{
                    msg.setDelay(timeOutMs);
                }
            }
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

        }else {
            log.error("response err:[{}]", response);
            throw new Exception(taskId + ": response err");
        }
        return true;
    }


    private int handlerTaskLimit(TaskMsg msg, String key, int limit){
        //VendorInfo vendorInfo = this.findVendorInfo(TaskOperationEnum.getVendorString(msg.getOperation()));
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
