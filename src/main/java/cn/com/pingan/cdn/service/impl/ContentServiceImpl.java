/**   
 * @Project: anubis-content
 * @File: ContentServiceImpl.java 
 * @Package cn.com.pingan.cdn.service.impl 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午9:44:14 
 */
package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.client.AnubisNotifyService;
import cn.com.pingan.cdn.common.*;
import cn.com.pingan.cdn.config.ContentLimitJsonConfig;
import cn.com.pingan.cdn.config.RedisLuaScriptService;
import cn.com.pingan.cdn.current.JxGaga;
import cn.com.pingan.cdn.exception.ContentException;
import cn.com.pingan.cdn.exception.ErrEnum;
import cn.com.pingan.cdn.exception.ErrorCode;
import cn.com.pingan.cdn.exception.RestfulException;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.model.mysql.*;
import cn.com.pingan.cdn.model.pgsql.Domain;
import cn.com.pingan.cdn.model.pgsql.OldContentHistory;
import cn.com.pingan.cdn.rabbitmq.config.RabbitListenerConfig;
import cn.com.pingan.cdn.rabbitmq.message.FanoutMsg;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.rabbitmq.producer.Producer;
import cn.com.pingan.cdn.repository.mysql.ExportRecordRepository;
import cn.com.pingan.cdn.repository.pgsql.OldContentHistoryRepository;
import cn.com.pingan.cdn.request.openapi.ContentDefaultNumDTO;
import cn.com.pingan.cdn.response.TaskDetailsResponse;
import cn.com.pingan.cdn.service.*;
import cn.com.pingan.cdn.utils.Utils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/** 
 * @ClassName: ContentServiceImpl 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午9:44:14 
 *  
 */
@Service
@Slf4j
public class ContentServiceImpl implements ContentService {


    @Autowired
    DateBaseService dateBaseService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private LineService lineService;

    @Autowired
    private AnubisNotifyService anubisNotifyService;

    @Autowired
    RedisLuaScriptService luaScriptService;

    @Autowired
    private OldContentHistoryRepository oldContentHistoryRepository;
    
    @Autowired
    Producer producer;

    public static Map<String,List<String>> urlVendorMap = new ConcurrentHashMap<String,List<String>>();
    
    private String contentPrefix = "contentNumber_";
    
    @Value("${day.max.refresh:1000}")
    private int maxRefresh;
    @Value("${day.max.preheat:1000}")
    private int maxPreheat;
    @Value("${day.max.dirRefresh:1000}")
    private int maxDirRefresh;

    static final long timeout = 365;

    @Value("${task.retry.num:3}")
    private int limitRetry;

    @Value("${task.retry.Rate:5000L}")
    private long limitRetryRate;

    @Value("${task.history.robinNum:10}")
    private int robinNum;

    @Value("${task.history.robinRate:180000}")
    private long robinRate;

    @Value("${task.history.export.limit:1000}")
    private int exportLimit;

    @Value("${task.notify.fail:false}")
    private Boolean notifyFail;

    @Value("${task.request.merge:false}")
    private Boolean requestMerge;

    @Value("${task.vendor.request.merge:false}")
    private Boolean vendorRequestMerge;

    @Autowired
    ContentLimitJsonConfig contentLimitJsonConfig;

    @Autowired
    private ExportRecordRepository exportRecordRepository;

    //test
    @Autowired
    NotifyAlarmService notifyAlarmService;
    @Autowired
    RabbitListenerConfig rabbitListenerConfig;

    @Autowired
    private RedisService redisService;
    

    @Override
    public ApiReceipt saveContent(GateWayHeaderDTO dto, List<String> data, RefreshType type){
        log.info("saveContent data:{}", data);
        if (null == data || data.size() == 0) {
            return ApiReceipt.error("0x004008", "内容为空,请正确填写!");
        }

        try{
            //校验data 的数据格式 去重
            List <String> tmpData = new ArrayList<>();
            HashSet h = new HashSet();

            for (String url :data) {
                String subUrl;
                boolean addFlag = false;
                if(url.toLowerCase().startsWith("http://") ||url.toLowerCase().startsWith("https://")){

                    if (RefreshType.url == type && url.toLowerCase().endsWith("/")) {
                        return ApiReceipt.error(ErrorCode.ENDURL);
                    }
                    if (RefreshType.preheat == type && url.toLowerCase().endsWith("/")) {
                        return ApiReceipt.error(ErrorCode.ENDPRELOAD);
                    }
                    if (RefreshType.dir == type && !url.toLowerCase().endsWith("/")) {
                        return ApiReceipt.error(ErrorCode.ENDDIR);
                    }

                    if(RefreshType.url == type){
                        subUrl = url.toLowerCase().substring(url.toLowerCase().indexOf("://") + 3);
                        if( -1 == subUrl.indexOf('/')){
                            addFlag = true;
                        }
                    }


                }else {
                    return ApiReceipt.error(ErrorCode.STARTURL);
                }

                if(h.add(url.toLowerCase())){
                    if(addFlag) {
                        tmpData.add(url + "/");
                    }else{
                        tmpData.add(url);
                    }
                }
            }

            data.clear();
            data.addAll(tmpData);

            boolean adminFlag = "true".equals(dto.getIsAdmin());

            //检查域名状态
            Map<String, String> urlDomainMap = checkDomainStatus(data, adminFlag, dto);
            log.info("检查域名状态完成");

            //计数
            checkUserLimit(adminFlag, type, dto.getSpcode(),dto.getUsername(), data.size());
            log.info("检查用户计数完成");

            //原始请求入库
            Date now = new Date();
            String taskId = UUID.randomUUID().toString().replaceAll("-", "");
            ContentHistory contentHistory = new ContentHistory();

            contentHistory.setType(type);
            contentHistory.setRequestId(taskId);
            contentHistory.setUserId(dto.getUid());
            contentHistory.setContent(JSON.toJSONString(data));
            contentHistory.setCreateTime(now);
            contentHistory.setUpdateTime(now);
            contentHistory.setStatus(HisStatus.WAIT);
            contentHistory.setContentNumber(data.size());
            contentHistory.setIsAdmin(dto.getIsAdmin());
            contentHistory.setFlowStatus(FlowEmun.init);

            dateBaseService.getContentHistoryRepository().save(contentHistory);
            log.info("用户请求入库完成 request id:{}", contentHistory.getRequestId());

            List<SplitHistory> toSaveSplit = new ArrayList<>();
            log.info("urlDomainMap:[{}]", urlDomainMap);
            for(String dm: urlDomainMap.keySet()){
                String recordId = UUID.randomUUID().toString().replaceAll("-", "");
                SplitHistory splitHistory = new SplitHistory();
                splitHistory.setTaskId(recordId);
                splitHistory.setRequestId(taskId);
                splitHistory.setContent(dm);
                splitHistory.setDomainName(urlDomainMap.get(dm));
                splitHistory.setCreateTime(now);
                splitHistory.setStatus(HisStatus.WAIT);
                splitHistory.setType(type);
                splitHistory.setUserId(dto.getUid());
                splitHistory.setIsAdmin(dto.getIsAdmin());
                toSaveSplit.add(splitHistory);
            }

            dateBaseService.getSplitHistoryRepository().saveAll(toSaveSplit);
            log.info("拆分任务数据入库完成[{}]", toSaveSplit);


            if(requestMerge){
                RequestRecord rc = new RequestRecord();
                rc.setRecordId(UUID.randomUUID().toString().replaceAll("-", ""));
                rc.setRequestId(taskId);
                dateBaseService.getRequestRecordRepository().save(rc);
                log.info("请求待合并:[{}]", taskId);
            }else {
                TaskMsg historyTaskMsg = new TaskMsg();
                //historyTaskMsg.setId(contentHistory.getId());
                historyTaskMsg.setTaskId(taskId);
                historyTaskMsg.setHisVersion(contentHistory.getVersion());
                historyTaskMsg.setSize(data.size());
                historyTaskMsg.setType(type);
                historyTaskMsg.setVersion(0);
                historyTaskMsg.setOperation(TaskOperationEnum.content_item);
                producer.sendTaskMsg(historyTaskMsg);
                log.info("发送到MQ完成:[{}]", historyTaskMsg);
            }

            return ApiReceipt.ok().data(taskId);
        }catch (Exception e){
            log.error("saveContent异常:{}", e);
            if(e instanceof ContentException){
                String code = ((ContentException) e).getCode();
                return ApiReceipt.error(code, e.getMessage());
            }else{
                return ApiReceipt.error(ErrorCode.INTERERR);
            }
        }
        
    }

    @Override
    public ApiReceipt redoContentTask(String requestId, boolean flag){
        log.info("redoContentTask start [{}]", requestId);
        try {

            ContentHistory contentHistory = this.findHisttoryByRequestId(requestId);
            if (contentHistory == null) {
                log.error("用户任务[{}]不存在", requestId);
                return ApiReceipt.error("0x004008", String.format("[%s]原始任务记录不存在，禁止操作", requestId));
            }
            if (contentHistory.getStatus().equals(HisStatus.FAIL)) {//任务已失败或未开始
                contentHistory.setStatus(HisStatus.WAIT);
                contentHistory.setFlowStatus(FlowEmun.redo);
                contentHistory.setVersion(contentHistory.getVersion() + 1);
                contentHistory.setAllTaskNum(-1);
                contentHistory.setSuccessTaskNum(-100);
                dateBaseService.getContentHistoryRepository().save(contentHistory);
                log.info("[{}]请求任务已失败，设置为wait", requestId);

            } else if (contentHistory.getStatus().equals(HisStatus.WAIT)) {
                if(flag){
                    log.info("[{}]请求任务为wait,强制重试", requestId);
                    contentHistory.setFlowStatus(FlowEmun.redo);
                    contentHistory.setVersion(contentHistory.getVersion() + 1);
                    contentHistory.setAllTaskNum(-1);
                    contentHistory.setSuccessTaskNum(-100);
                    dateBaseService.getContentHistoryRepository().save(contentHistory);
                }else{
                    log.info("[{}]请求任务为wait,不需要重试", requestId);
                    if(contentHistory.getSuccessTaskNum().equals(contentHistory.getAllTaskNum())){
                        contentHistory.setStatus(HisStatus.SUCCESS);
                        dateBaseService.getContentHistoryRepository().save(contentHistory);
                    }
                    return ApiReceipt.ok();
                }
            } else {
                log.info("[{}]请求任务已成功，不再重试", requestId);
                return ApiReceipt.ok();

            }
            TaskMsg historyTaskMsg = new TaskMsg();
            historyTaskMsg.setForce(flag);
            historyTaskMsg.setTaskId(requestId);
            //historyTaskMsg.setId(contentHistory.getId());
            historyTaskMsg.setVersion(1);
            historyTaskMsg.setHisVersion(contentHistory.getVersion());
            historyTaskMsg.setType(contentHistory.getType());
            historyTaskMsg.setSize(contentHistory.getContentNumber());
            historyTaskMsg.setOperation(TaskOperationEnum.content_item);
            producer.sendTaskMsg(historyTaskMsg);
            log.info("redoContentTask[{}]结束", requestId);

            return ApiReceipt.ok();
        }catch (Exception e){
            if(e instanceof ContentException){
                String code = ((ContentException) e).getCode();
                return ApiReceipt.error(code, e.getMessage());
            }else{
                return ApiReceipt.error(ErrorCode.INTERERR);
            }
        }

    }

    @Override
    public ApiReceipt batchRedoContentTask(List<String> requestIds, boolean flag) throws ContentException {
        log.info("batchRedoContentTask[{}]开始", requestIds);
        try {

            List<ContentHistory> contentHistorys = dateBaseService.getContentHistoryRepository().findByRequestIdIn(requestIds);
            if(contentHistorys.size() == 0){
                log.error("用户任务不存在[{}]", requestIds);
                return ApiReceipt.error("0x004008", String.format("[%s]原始任务记录不存在，禁止操作", requestIds.toString()));
            }


            List<TaskMsg> msgList = new ArrayList<>();
            if(contentHistorys.size() != requestIds.size()) {//是否存在
                List<String> noData = new ArrayList<>();
                Map<String, ContentHistory> existMap = contentHistorys.stream().collect(Collectors.toMap(i -> i.getRequestId(), i -> i));
                for (String s : requestIds) {
                    if (!existMap.containsKey(s)) {
                        noData.add(s);
                    }
                }
                log.error("用户任务不存在[{}]", requestIds);
                return ApiReceipt.error("0x004008", String.format("[%s]原始任务记录不存在，禁止操作", noData.toString()));
            }


            List<ContentHistory> toSave = new ArrayList<>();
            for(ContentHistory ch: contentHistorys){
                if (ch.getStatus().equals(HisStatus.FAIL)) {//任务已失败或未开始
                    ch.setStatus(HisStatus.WAIT);
                    ch.setFlowStatus(FlowEmun.redo);
                    ch.setVersion(ch.getVersion() + 1);
                    ch.setAllTaskNum(-1);
                    ch.setSuccessTaskNum(-100);
                    log.info("[{}]请求任务已失败，设置为wait", ch.getRequestId());
                    toSave.add(ch);
                } else if (ch.getStatus().equals(HisStatus.WAIT)) {
                    if(flag){
                        log.info("[{}]请求任务为wait,强制重试", ch.getRequestId());
                        ch.setFlowStatus(FlowEmun.redo);
                        ch.setVersion(ch.getVersion() + 1);
                        ch.setAllTaskNum(-1);
                        ch.setSuccessTaskNum(-100);
                        toSave.add(ch);
                    }else{
                        log.info("[{}]请求任务为wait,不需要重试", ch.getRequestId());
                        if(ch.getSuccessTaskNum().equals(ch.getAllTaskNum())){
                            ch.setStatus(HisStatus.SUCCESS);
                            toSave.add(ch);
                        }
                        continue;
                    }
                } else {
                    log.info("[{}]请求任务已成功，不再重试", ch.getRequestId());
                    continue;

                }
                TaskMsg historyTaskMsg = new TaskMsg();
                historyTaskMsg.setForce(flag);
                //historyTaskMsg.setId(ch.getId());
                historyTaskMsg.setTaskId(ch.getRequestId());
                historyTaskMsg.setVersion(1);
                historyTaskMsg.setHisVersion(ch.getVersion());
                historyTaskMsg.setType(ch.getType());
                historyTaskMsg.setSize(ch.getContentNumber());
                historyTaskMsg.setOperation(TaskOperationEnum.content_item);
                msgList.add(historyTaskMsg);
            }
            if(toSave.size()>0) {
                dateBaseService.getContentHistoryRepository().saveAll(toSave);
                sendListMQ(msgList);
            }

            return ApiReceipt.ok();
        }catch (Exception e){
            if(e instanceof ContentException){
                String code = ((ContentException) e).getCode();
                return ApiReceipt.error(code, e.getMessage());
            }else{
                return ApiReceipt.error(ErrorCode.INTERERR);
            }
        }
    }

    @Override
    public ApiReceipt getContentTaskDetails(String requestId){
        log.info("enter getContentTaskDetails[{}]", requestId);
        try {
            ContentHistory contentHistory = this.findHisttoryByRequestId(requestId);
            if (contentHistory == null) {
                log.error("用户任务不存在");
                return ApiReceipt.error("0x004008", "用户任务不存在");
            }
            TaskDetailsResponse tdr = new TaskDetailsResponse();
            tdr.setRequestId(requestId);
            tdr.setContentStatus(contentHistory.getFlowStatus());
            if(contentHistory.getFlowStatus().equals(FlowEmun.split_vendor_done)){
                Map<String, TaskDetailsResponse.UrlStatus> taskDetailsMap = new HashMap<>();
                List<VendorContentTask> vendorContentTaskList = dateBaseService.getVendorTaskRepository().findByRequestId(requestId);
                for(VendorContentTask vct: vendorContentTaskList){
                    String vendorName = vct.getVendor();
                    String status = "处理中";
                    String msg = "";
                    List<String> urls = JSONArray.parseArray(vct.getContent(), String.class);
                    if(vct.getStatus().equals(TaskStatus.SUCCESS)){
                        status = "成功";
                    }else if(vct.getStatus().equals(TaskStatus.FAIL)){
                        status = "失败";
                        msg = vct.getMessage();
                    }else if(vct.getStatus().equals(TaskStatus.ROUND_ROBIN)){
                        status = "轮询中";
                    }else if(vct.getStatus().equals(TaskStatus.PROCESSING)){
                        status = "处理中";
                    }else{
                        status = "等待下发";
                    }

                    TaskDetailsResponse.UrlStatus taskDetails = new TaskDetailsResponse.UrlStatus();
                    taskDetails.setStatus(status);
                    taskDetails.setUrls(urls);
                    taskDetails.setMessage(msg);


                    taskDetailsMap.put(vendorName, taskDetails);
                }
                tdr.setTaskDetails(taskDetailsMap);
            }
            return ApiReceipt.ok(tdr);
        }catch (Exception e){
            log.error("getContentTaskDetails异常[{}]",e );
            return ApiReceipt.error(ErrorCode.INTERERR);
        }
    }

    @Override
    public void saveVendorTask(TaskMsg taskMsg) throws ContentException {
        String requestId = taskMsg.getTaskId();
        boolean force = taskMsg.getForce();
        boolean isMerge = taskMsg.getIsMerge();
        log.info("saveVendorTask start [{}]", requestId);
        try {

            if(!isMerge) {
                ContentHistory contentHistory = dateBaseService.getContentHistoryRepository().findByRequestId(requestId);
                if (contentHistory == null) {
                    log.error("用户任务不存在,丢弃消息");
                    return;
                }

                if (taskMsg.getRetryNum() > limitRetry) {
                    contentHistory.setStatus(HisStatus.FAIL);
                    contentHistory.setUpdateTime(new Date());
                    contentHistory.setMessage("拆分任务及其子任务失败超限");
                    contentHistory.setFlowStatus(FlowEmun.split_vendor_err);
                    dateBaseService.getContentHistoryRepository().save(contentHistory);
                    log.error("超出重试次数,设置任务失败并丢弃消息");
                    return;
                }

                List<String> urls = JSONArray.parseArray(contentHistory.getContent(), String.class);
                Map<String, List<String>> lostUrlsMap = new HashMap<>();
                Map<String, List<String>> vendorUrlMap;
                vendorUrlMap = getDomainVendorsMapNew(urls);
                if (vendorUrlMap == null) {
                    log.error("获取厂商失败");
                    taskMsg.setDelay(limitRetryRate);
                    taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
                    producer.sendTaskMsg(taskMsg);
                    return;
                }
                log.debug("获取厂商成功");

                Map<String, VendorContentTask> toSaveVendorTaskMap = new HashMap<>();
                Map<String, String> vendorTaskId = new HashMap<>();

                contentHistory.setVersion(contentHistory.getVersion() + 1);
                if (taskMsg.getVersion() == 0) {//区别新请求与重试
                    log.debug("saveVendorTask当前为首次请求，存入全部数据");
                    lostUrlsMap.putAll(vendorUrlMap);
                    for (String v : lostUrlsMap.keySet()) {
                        if (!vendorTaskId.containsKey(v) || StringUtils.isBlank(vendorTaskId.get(v))) {
                            String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                            vendorTaskId.put(v, mergeId);
                        }
                        if (!toSaveVendorTaskMap.containsKey(v) || toSaveVendorTaskMap.get(v) == null) {
                            String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                            VendorContentTask vendorTask = new VendorContentTask();
                            vendorTask.setTaskId(taskId);
                            vendorTask.setRequestId(requestId);
                            vendorTask.setVendor(v);
                            vendorTask.setMergeId(vendorTaskId.get(v));
                            vendorTask.setVersion(contentHistory.getVersion());
                            vendorTask.setType(contentHistory.getType());
                            vendorTask.setContent(JSON.toJSONString(vendorUrlMap.get(v)));
                            vendorTask.setContentNumber(vendorUrlMap.get(v).size());
                            vendorTask.setHistoryCreateTime(contentHistory.getCreateTime());
                            vendorTask.setCreateTime(new Date());
                            vendorTask.setStatus(TaskStatus.WAIT);
                            toSaveVendorTaskMap.put(v, vendorTask);
                        }
                    }
                    taskMsg.setVersion(1);
                } else {
                    log.debug("saveVendorTask当前非首次请求，对比数据");
                    int totalTaskSize = 0;
                    int existTaskSize = 0;

                    List<VendorContentTask> existVendorTaskAll = dateBaseService.getVendorTaskRepository().findByRequestId(requestId);
                    for (String s : vendorUrlMap.keySet()) {
                        totalTaskSize += vendorUrlMap.get(s).size();
                    }
                    log.debug("需要生成厂商任务数量:[{}]", totalTaskSize);
                    for (VendorContentTask vc : existVendorTaskAll) {
                        List<String> us = JSONArray.parseArray(vc.getContent(), String.class);
                        existTaskSize += us.size();
                    }
                    if (existTaskSize == 0) {
                        log.debug("缺失全部子任务");
                        lostUrlsMap.putAll(vendorUrlMap);
                        for (String v : lostUrlsMap.keySet()) {
                            if (!vendorTaskId.containsKey(v) || StringUtils.isBlank(vendorTaskId.get(v))) {
                                String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                                vendorTaskId.put(v, mergeId);
                            }
                            if (!toSaveVendorTaskMap.containsKey(v) || toSaveVendorTaskMap.get(v) == null) {
                                String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                                VendorContentTask vendorTask = new VendorContentTask();
                                vendorTask.setTaskId(taskId);
                                vendorTask.setRequestId(requestId);
                                vendorTask.setVendor(v);
                                vendorTask.setMergeId(vendorTaskId.get(v));
                                vendorTask.setVersion(contentHistory.getVersion());
                                vendorTask.setType(contentHistory.getType());
                                vendorTask.setContent(JSON.toJSONString(vendorUrlMap.get(v)));
                                vendorTask.setContentNumber(vendorUrlMap.get(v).size());
                                vendorTask.setHistoryCreateTime(contentHistory.getCreateTime());
                                vendorTask.setCreateTime(new Date());
                                vendorTask.setStatus(TaskStatus.WAIT);
                                toSaveVendorTaskMap.put(v, vendorTask);
                            }
                        }

                    } else if (existTaskSize == totalTaskSize) {
                        log.debug("子任务不缺失，只重试失败的任务");
                        boolean allSuccess=true;
                        for (VendorContentTask vct : existVendorTaskAll) {
                            if (!vct.getStatus().equals(TaskStatus.SUCCESS)) {
                                allSuccess = false;
                                if (!vendorTaskId.containsKey(vct.getVendor()) || StringUtils.isBlank(vendorTaskId.get(vct.getVendor()))) {
                                    String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                                    vendorTaskId.put(vct.getVendor(), mergeId);
                                }
                                if (!toSaveVendorTaskMap.containsKey(vct.getVendor()) || toSaveVendorTaskMap.get(vct.getVendor()) == null) {
                                    vct.setStatus(TaskStatus.WAIT);
                                    vct.setVersion(contentHistory.getVersion());
                                    vct.setUpdateTime(new Date());
                                    vct.setMergeId(vendorTaskId.get(vct.getVendor()));
                                    toSaveVendorTaskMap.put(vct.getVendor(), vct);
                                }
                            }
                        }
                        if(allSuccess){
                            contentHistory.setUpdateTime(new Date());
                            contentHistory.setFlowStatus(FlowEmun.split_vendor_done);
                            contentHistory.setAllTaskNum(existVendorTaskAll.size());
                            contentHistory.setSuccessTaskNum(existVendorTaskAll.size());
                            contentHistory.setStatus(HisStatus.SUCCESS);
                            dateBaseService.getContentHistoryRepository().save(contentHistory);
                            log.info("saveVendorTask设置请求任务[{}]状态为:[split_vendor_done],[SUCCESS]", requestId);
                            return;
                        }
                    } else {
                        log.debug("子任务数量异常，清理后重新入库");
                        clearVendorTask(requestId);
                        lostUrlsMap.putAll(vendorUrlMap);
                        for (String v : lostUrlsMap.keySet()) {
                            if (!vendorTaskId.containsKey(v) || StringUtils.isBlank(vendorTaskId.get(v))) {
                                String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                                vendorTaskId.put(v, mergeId);
                            }
                            if (!toSaveVendorTaskMap.containsKey(v) || toSaveVendorTaskMap.get(v) == null) {
                                String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                                VendorContentTask vendorTask = new VendorContentTask();
                                vendorTask.setTaskId(taskId);
                                vendorTask.setRequestId(requestId);
                                vendorTask.setVendor(v);
                                vendorTask.setMergeId(vendorTaskId.get(v));
                                vendorTask.setVersion(contentHistory.getVersion());
                                vendorTask.setType(contentHistory.getType());
                                vendorTask.setContent(JSON.toJSONString(vendorUrlMap.get(v)));
                                vendorTask.setContentNumber(vendorUrlMap.get(v).size());
                                vendorTask.setHistoryCreateTime(contentHistory.getCreateTime());
                                vendorTask.setCreateTime(new Date());
                                vendorTask.setStatus(TaskStatus.WAIT);
                                toSaveVendorTaskMap.put(v, vendorTask);
                            }
                        }
                    }
                }

                int allNum = toSaveVendorTaskMap.values().size();

                if (allNum > 0) {
                    dateBaseService.getVendorTaskRepository().saveAll(new ArrayList<>(toSaveVendorTaskMap.values()));
                    log.info("saveVendorTask存入厂商任务完成:[{}]", allNum);
                }

                contentHistory.setUpdateTime(new Date());
                contentHistory.setFlowStatus(FlowEmun.split_vendor_done);
                contentHistory.setAllTaskNum(allNum);
                contentHistory.setSuccessTaskNum(0);

                dateBaseService.getContentHistoryRepository().save(contentHistory);
                log.info("saveVendorTask设置请求任务[{}]状态为:[split_vendor_done]", requestId);

                List<MergeRecord> records = new ArrayList<>();
                for (String v : vendorTaskId.keySet()) {
                    if ( !vendorRequestMerge || (toSaveVendorTaskMap.get(v).getContentNumber() >= 20 && toSaveVendorTaskMap.get(v).getContentNumber() <= 50)) {
                        TaskMsg vendorTaskMsg = new TaskMsg();
                        vendorTaskMsg.setTaskId(vendorTaskId.get(v));
                        vendorTaskMsg.setOperation(TaskOperationEnum.getVendorOperation(v, contentHistory.getType()));
                        vendorTaskMsg.setVendor(v);
                        vendorTaskMsg.setVersion(0);
                        vendorTaskMsg.setHisVersion(contentHistory.getVersion());
                        //vendorTaskMsg.setIsMerge(true);
                        vendorTaskMsg.setType(contentHistory.getType());
                        vendorTaskMsg.setRetryNum(0);
                        vendorTaskMsg.setSize(toSaveVendorTaskMap.get(v).getContentNumber());
                        producer.sendTaskMsg(vendorTaskMsg);
                        log.info("saveContentVendor发送Mq[{}]", v);
                    } else {//TODO
                        MergeRecord record = new MergeRecord();
                        record.setRecordId(UUID.randomUUID().toString().replaceAll("-", ""));
                        record.setMergeId(vendorTaskId.get(v));
                        records.add(record);
                        log.info("saveContentVendor记录合并record:[{}]", vendorTaskId.get(v));
                    }
                }
                if (records.size() > 0) {
                    dateBaseService.getMergeRecordRepository().saveAll(records);
                    log.debug("记录合并[{}]", records.size());
                }

                HistoryRecord historyRecord = new HistoryRecord();
                historyRecord.setRecordId(UUID.randomUUID().toString().replaceAll("-", ""));
                historyRecord.setRequestId(requestId);
                historyRecord.setVersion(contentHistory.getVersion());
                dateBaseService.getHistoryRecordRepository().save(historyRecord);

                log.info("saveVendorTask结束[{}]", requestId);
            }else{

                List<ContentHistory> contentHistories =  dateBaseService.getContentHistoryRepository().findByRequestIdIn(taskMsg.getRequestRecordList());
                Map<String, List<VendorContentTask>> rsMap =  new ConcurrentHashMap<>();
                Map<String, ContentHistory> reMap = new HashMap<>();
                Map<String, ContentHistory> allMap = new HashMap<>();
                if(contentHistories.size()>0) {
                    log.debug("saveVendorTask获取信息");
                    JxGaga gg = JxGaga.of(Executors.newCachedThreadPool(), contentHistories.size());
                    for (ContentHistory c : contentHistories) {
                        gg.work(() -> {
                            return splitVendorTask(c, taskMsg.getVersion());
                        }, j -> rsMap.put(j.getRequestId(), j.getVendorContentTaskList()), q -> {
                            log.info(q.getMessage());
                        });
                    }
                    gg.merge((i) -> {
                        log.info("生成成功的用户任务数[{}]， 总数[{}]", i.size(), contentHistories.size());
                    }, rsMap.keySet()).exit();
                    log.debug("saveVendorTask获取信息完成[{}]", contentHistories.size());

                    List<VendorContentTask> toSaveVendorContentTask = new ArrayList<>();
                    List<ContentHistory> toSaveContentHistory = new ArrayList<>();
                    List<HistoryRecord> historyRecords = new ArrayList<>();
                    for (ContentHistory ch : contentHistories) {
                        allMap.put(ch.getRequestId(), ch);
                        if (rsMap.containsKey(ch.getRequestId())) {
                            ch.setAllTaskNum(rsMap.get(ch.getRequestId()).size());
                            ch.setUpdateTime(new Date());
                            ch.setFlowStatus(FlowEmun.split_vendor_done);
                            ch.setSuccessTaskNum(0);
                            toSaveContentHistory.add(ch);
                            toSaveVendorContentTask.addAll(rsMap.get(ch.getRequestId()));

                            HistoryRecord historyRecord = new HistoryRecord();
                            historyRecord.setRecordId(UUID.randomUUID().toString().replaceAll("-", ""));
                            historyRecord.setRequestId(ch.getRequestId());
                            historyRecord.setVersion(ch.getVersion());
                            historyRecords.add(historyRecord);
                        } else {
                            reMap.put(ch.getRequestId(), ch);
                        }
                    }

                    if(toSaveVendorContentTask.size()>0){
                        dateBaseService.getVendorTaskRepository().saveAll(toSaveVendorContentTask);
                        log.info("saveVendorTask存入厂商任务完成:[{}]", toSaveVendorContentTask.size());
                    }

                    if (toSaveContentHistory.size() > 0) {
                        dateBaseService.getContentHistoryRepository().saveAll(toSaveContentHistory);
                        log.info("saveVendorTask设置请求任务[{}]状态为:[split_vendor_done]", toSaveContentHistory.size());
                    }

                    List<TaskMsg> toSendTaskList = new ArrayList<>();
                    List<MergeRecord> records = new ArrayList<>();
                    if (toSaveVendorContentTask.size() > 0) {
                        for (VendorContentTask vct : toSaveVendorContentTask) {
                            if (!vendorRequestMerge || (vct.getContentNumber() >= 20 && vct.getContentNumber() <= 50)) {
                                TaskMsg vendorTaskMsg = new TaskMsg();
                                vendorTaskMsg.setTaskId(vct.getMergeId());
                                vendorTaskMsg.setOperation(TaskOperationEnum.getVendorOperation(vct.getVendor(), vct.getType()));
                                vendorTaskMsg.setVendor(vct.getVendor());
                                vendorTaskMsg.setVersion(0);
                                vendorTaskMsg.setHisVersion(allMap.get(vct.getRequestId()).getVersion());
                                //vendorTaskMsg.setIsMerge(true);
                                vendorTaskMsg.setType(vct.getType());
                                vendorTaskMsg.setRetryNum(0);
                                vendorTaskMsg.setSize(vct.getContentNumber());
                                toSendTaskList.add(vendorTaskMsg);
                            } else {
                                MergeRecord record = new MergeRecord();
                                record.setRecordId(UUID.randomUUID().toString().replaceAll("-", ""));
                                record.setMergeId(vct.getMergeId());
                                records.add(record);
                                log.info("record:[{}]", vct.getMergeId());
                            }
                        }
                    }
                    if (toSendTaskList.size() > 0) {
                        sendListMQ(toSendTaskList);
                        log.info("发送MQ[{}]", toSendTaskList.size());
                    }
                    if (records.size() > 0) {
                        dateBaseService.getMergeRecordRepository().saveAll(records);
                        log.info("记录合并[{}]", records.size());
                    }

                    if (historyRecords.size() > 0) {
                        dateBaseService.getHistoryRecordRepository().saveAll(historyRecords);
                        log.info("记录用户轮询[{}]", historyRecords.size());
                    }

                    if (reMap.keySet().size() > 0) {
                        log.error("saveVendorTask失败数[{}]", reMap.keySet().size());
                        if (taskMsg.getRetryNum() > limitRetry) {
                            for (ContentHistory ch : reMap.values()) {
                                ch.setStatus(HisStatus.FAIL);
                                ch.setUpdateTime(new Date());
                                ch.setMessage("拆分任务及其子任务失败超限");
                                ch.setFlowStatus(FlowEmun.split_vendor_err);
                            }
                            dateBaseService.getContentHistoryRepository().saveAll(new ArrayList<>(reMap.values()));
                            log.error("超出重试次数,设置任务失败并丢弃消息");
                        } else {
                            taskMsg.setVersion(1);
                            taskMsg.setRequestRecordList(new ArrayList<>(reMap.keySet()));
                            taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
                            producer.sendDelayMsg(taskMsg);
                            log.error("重试任务[{}]", taskMsg.getRequestRecordList());
                        }
                    }
                }
                log.info("saveVendorTask合并模式结束[{}]", requestId);
            }

        }catch (Exception e){
            log.error("saveVendorTask异常{}", e);
            if (taskMsg.getRetryNum() > limitRetry) {
                return;
            }
            taskMsg.setDelay(limitRetryRate);
            taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
            producer.sendDelayMsg(taskMsg);
            return;
        }
    }


    @Override
    public int fflushDomainVendor(FanoutMsg taskMsg) {
        log.info("enter fflushDomainVendor[{}]", taskMsg);
        urlVendorMap.clear();
        return 0;
    }

    @Override
    public void exportAndImport(TaskMsg taskMsg) {
        log.info("exportAndImport start[{}]", taskMsg);
        int pageIndex = 0;
        int pageSize = exportLimit;
        String taskId = taskMsg.getTaskId();
        try {
            ExportRecord exportRecord = exportRecordRepository.findByExportId(taskId);
            if (exportRecord == null) {
                return;
            }

            if(taskMsg.getRetryNum() > 3){
                log.error("重试次数超过限制，导入任务失败");
                exportRecord.setUpdateTime(new Date());
                exportRecord.setStatus(HisStatus.FAIL);
                exportRecordRepository.save(exportRecord);
                return;
            }

            Date startTime = taskMsg.getExportInfo().getStartTime();
            Date endTime = taskMsg.getExportInfo().getEndTime();

            Sort sort = new Sort(Sort.Direction.ASC, "id");
            long count = 0L;
            long allCount = 0L;
            long num = 0L;
            do {

                PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, sort);

                Page<OldContentHistory> pager = this.oldContentHistoryRepository.findAll(new Specification<OldContentHistory>() {
                    private static final long serialVersionUID = 1L;

                    public Predicate toPredicate(Root<OldContentHistory> root, CriteriaQuery<?> query,
                                                 CriteriaBuilder criteriaBuilder) {
                        List<Predicate> cond = new ArrayList<>();

                        //大于等于
                        cond.add(criteriaBuilder.greaterThanOrEqualTo(root.get("optTime").as(Date.class), startTime));
                        //小于等于
                        cond.add(criteriaBuilder.lessThanOrEqualTo(root.get("optTime").as(Date.class), endTime));

                        return query.where(cond.toArray(new Predicate[0])).getRestriction();
                    }

                }, pageRequest);
                if (allCount == 0) {
                    allCount = pager.getTotalElements();
                }

                List<ContentHistory> data = new ArrayList<>();
                for (OldContentHistory old : pager) {
                    ContentHistory tmp = new ContentHistory();
                    if(StringUtils.isBlank(old.getTaskId())){
                        log.info("task id is null, skip");
                        //old.setTaskId(UUID.randomUUID().toString().replaceAll("-", ""));
                        continue;
                    }
                    tmp.setRequestId(old.getTaskId());
                    tmp.setCreateTime(old.getOptTime());
                    tmp.setUpdateTime(new Date());
                    tmp.setStatus(old.getStatus());
                    tmp.setFlowStatus(FlowEmun.split_vendor_done);
                    tmp.setContent(old.getContent());
                    tmp.setContentNumber(old.getContentNumber());
                    tmp.setIsAdmin(old.getIsAdmin());
                    tmp.setType(old.getType());
                    tmp.setUserId(old.getUserId());
                    data.add(tmp);
                    num++;
                }
                if(data.size()>0) {
                    dateBaseService.getContentHistoryRepository().saveAll(data);
                }

                count += pager.getNumberOfElements();
                pageIndex++;

                log.info("导入进度---->已导出[{}], 已导入有效[{}], 当前次数[{}], 总数[{}]", count, num, pageIndex, allCount);

            } while (count < allCount);

            exportRecord.setCount(num);
            exportRecord.setUpdateTime(new Date());
            exportRecord.setStatus(HisStatus.SUCCESS);
            exportRecordRepository.save(exportRecord);
            log.info("exportAndImport done[{}]", taskMsg);
        }catch (Exception e){
            log.error("导入数据异常", e);
            taskMsg.setDelay(5000L);
            taskMsg.setRetryNum(taskMsg.getRetryNum()+1);
            producer.sendDelayMsg(taskMsg);
        }

    }


    @Override
    public void contentHistoryRobin(TaskMsg taskMsg) throws ContentException {
        String requestId = taskMsg.getTaskId();
        log.info("contentHistoryRobin开始[{}]", requestId);
        try {
            if(!taskMsg.getIsMerge()) {
                log.info("one contentHistoryRobin task[{}]", requestId);
                ContentHistory contentHistory = dateBaseService.getContentHistoryRepository().findByRequestId(requestId);
                if (contentHistory == null) {
                    log.error("用户任务不存在,丢弃消息");
                    return;
                }

                if (taskMsg.getHisVersion() == null || (taskMsg.getHisVersion() != -1 && taskMsg.getHisVersion() != contentHistory.getVersion())) {
                    log.error("版本不一致,丢弃消息");
                    return;
                }

                if (contentHistory.getStatus().equals(HisStatus.SUCCESS) || contentHistory.getStatus().equals(HisStatus.FAIL)) {
                    log.info("任务[{}]状态[{}]", requestId, contentHistory.getStatus());
                    return;
                }

                if (taskMsg.getRetryNum() > limitRetry || taskMsg.getRoundRobinNum() > robinNum || taskMsg.getHisVersion() == -1) {

                    List<VendorContentTask> vcts = dateBaseService.getVendorTaskRepository().findByRequestId(requestId);
                    if(vcts.size()>0){
                        boolean flag = true;
                        for(VendorContentTask vct :vcts){
                            if(!vct.getStatus().equals(TaskStatus.SUCCESS)){
                                flag = false;
                                break;
                            }
                        }
                        if(flag){
                            contentHistory.setMessage("任务执行成功");
                            contentHistory.setUpdateTime(new Date());
                            contentHistory.setStatus(HisStatus.SUCCESS);
                            dateBaseService.getContentHistoryRepository().save(contentHistory);

                            List<SplitHistory> splitHistorys = dateBaseService.getSplitHistoryRepository().findByRequestId(contentHistory.getRequestId());
                            if(splitHistorys.size()>0) {
                                for (SplitHistory sh : splitHistorys) {
                                    sh.setStatus(HisStatus.SUCCESS);
                                }
                                dateBaseService.getSplitHistoryRepository().saveAll(splitHistorys);
                                log.info("变更拆分任务数[{}]",splitHistorys.size());
                            }

                            return;
                        }
                    }

                    contentHistory.setStatus(HisStatus.FAIL);
                    contentHistory.setUpdateTime(new Date());
                    contentHistory.setMessage("任务超时失败");
                    dateBaseService.getContentHistoryRepository().save(contentHistory);
                    log.error("[{}]contentVendorRobin超出重试次数,设置任务失败并丢弃消息", requestId);

                    List<SplitHistory> splitHistorys = dateBaseService.getSplitHistoryRepository().findByRequestId(contentHistory.getRequestId());
                    if(splitHistorys.size()>0) {
                        for (SplitHistory sh : splitHistorys) {
                            sh.setStatus(HisStatus.FAIL);
                        }
                        dateBaseService.getSplitHistoryRepository().saveAll(splitHistorys);
                        log.info("变更拆分任务数[{}]",splitHistorys.size());
                    }

                    if(notifyFail) {
                        anubisNotifyService.emailNotifyApiException(new AnubisNotifyExceptionRequest("anubis-content", contentHistory.getType().name(), contentHistory.getUserId(), contentHistory.getRequestId(), "刷新预热任务失败"));
                    }
                    return;
                }

                if(contentHistory.getSuccessTaskNum() >= contentHistory.getAllTaskNum()){
                    List<VendorContentTask> vcts = dateBaseService.getVendorTaskRepository().findByRequestId(requestId);
                    if(vcts.size()>0){
                        boolean flag = true;
                        for(VendorContentTask vct :vcts){
                            if(!vct.getStatus().equals(TaskStatus.SUCCESS)){
                                flag = false;
                                break;
                            }
                        }
                        if(flag){
                            contentHistory.setMessage("任务执行成功");
                            contentHistory.setUpdateTime(new Date());
                            contentHistory.setStatus(HisStatus.SUCCESS);
                            dateBaseService.getContentHistoryRepository().save(contentHistory);
                            List<SplitHistory> splitHistorys = dateBaseService.getSplitHistoryRepository().findByRequestId(contentHistory.getRequestId());
                            if(splitHistorys.size()>0) {
                                for (SplitHistory sh : splitHistorys) {
                                    sh.setStatus(HisStatus.SUCCESS);
                                }
                                dateBaseService.getSplitHistoryRepository().saveAll(splitHistorys);
                                log.info("变更拆分任务数[{}]",splitHistorys.size());
                            }
                            return;
                        }
                    }
                }


                taskMsg.setDelay(robinRate);//TODO
                taskMsg.setRoundRobinNum(taskMsg.getRoundRobinNum() + 1);
                producer.sendDelayMsg(taskMsg);
            }else{
                log.info("merge contentHistoryRobin task");
                if(taskMsg.getRobinCallBackList() == null && taskMsg.getRobinCallBackList().size() == 0){
                    log.error("无效的消息，弃之", requestId);
                    return;
                }
                Map<String, RobinCallBack> idsVersionMap = new HashMap<>();
                for(RobinCallBack rcb: taskMsg.getRobinCallBackList()){
                    idsVersionMap.put(rcb.getRequestId(), rcb);
                }
                List<ContentHistory> contentHistorys = dateBaseService.getContentHistoryRepository().findByRequestIdIn(new ArrayList<>(idsVersionMap.keySet()));

                List<ContentHistory> toSaveList = new ArrayList<>();
                List<RobinCallBack> toSendList = new ArrayList<>();
                for(ContentHistory ch: contentHistorys){
                    if(ch.getStatus().equals(HisStatus.SUCCESS)|| ch.getStatus().equals(HisStatus.FAIL)){
                        continue;
                    }
                    if(ch.getVersion() != idsVersionMap.get(ch.getRequestId()).getVersion()){
                        continue;
                    }

                    log.debug("SuccessTaskNum:[{}], AllTaskNum:[{}]",ch.getSuccessTaskNum(), ch.getAllTaskNum());
                    if(ch.getSuccessTaskNum() >= ch.getAllTaskNum()){
                        List<VendorContentTask> vcts = dateBaseService.getVendorTaskRepository().findByRequestId(ch.getRequestId());
                        log.info("vcts:[{}]",vcts.size());
                        if(vcts.size()>0){
                            boolean flag = true;
                            for(VendorContentTask vct :vcts){
                                if(!vct.getStatus().equals(TaskStatus.SUCCESS)){
                                    flag = false;
                                    break;
                                }
                            }

                            if(flag){
                                ch.setMessage("任务执行成功");
                                ch.setUpdateTime(new Date());
                                ch.setStatus(HisStatus.SUCCESS);
                                toSaveList.add(ch);
                                continue;
                            }
                        }
                    }

                    if(taskMsg.getRetryNum() > limitRetry || taskMsg.getRoundRobinNum() > robinNum  ){//最后一次遍历所有厂商任务，确保的确失败了
                        List<VendorContentTask> vcts = dateBaseService.getVendorTaskRepository().findByRequestId(ch.getRequestId());
                        if(vcts.size()>0){
                            boolean flag = true;
                            for(VendorContentTask vct :vcts){
                                if(!vct.getStatus().equals(TaskStatus.SUCCESS)){
                                    flag = false;
                                    break;
                                }
                            }
                            if(flag){
                                ch.setMessage("任务执行成功");
                                ch.setUpdateTime(new Date());
                                ch.setStatus(HisStatus.SUCCESS);
                                toSaveList.add(ch);
                                continue;
                            }
                        }
                        ch.setMessage("任务执行失败");
                        ch.setUpdateTime(new Date());
                        ch.setStatus(HisStatus.FAIL);
                        toSaveList.add(ch);
                    }else {
                        toSendList.add(idsVersionMap.get(ch.getRequestId()));
                    }

                }

                if(toSaveList.size()>0){

                    dateBaseService.getContentHistoryRepository().saveAll(toSaveList);
                    log.info("更新用户任务数量[{}]", toSaveList.size());


                    Map<String, HisStatus> reIdMap = new HashMap<>();
                    for(ContentHistory ch: toSaveList){
                        reIdMap.put(ch.getRequestId(), ch.getStatus());
                    }
                    List<SplitHistory> splitHistories = dateBaseService.getSplitHistoryRepository().findByRequestIdIn(new ArrayList<>(reIdMap.keySet()));
                    for(SplitHistory sh:splitHistories){
                        if(reIdMap.containsKey(sh.getRequestId())){
                            sh.setStatus(reIdMap.get(sh.getRequestId()));
                        }
                    }
                    dateBaseService.getSplitHistoryRepository().saveAll(splitHistories);
                    log.info("变更拆分任务数[{}]",splitHistories.size());
                }

                if(toSendList.size()>0){
                    taskMsg.setDelay(robinRate);//TODO
                    taskMsg.setRoundRobinNum(taskMsg.getRoundRobinNum() + 1);
                    taskMsg.getRobinCallBackList().clear();
                    taskMsg.getRobinCallBackList().addAll(toSendList);
                    producer.sendDelayMsg(taskMsg);
                }
            }

        }catch (Exception e){
            log.error("任务轮询异常[{}]", e);
            taskMsg.setDelay(limitRetryRate);
            taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
            if (taskMsg.getRetryNum() > limitRetry || taskMsg.getRoundRobinNum() > robinNum || taskMsg.getHisVersion() == -1) {
                log.error("[{}]contentHistoryRobin超出重试次数,设置任务失败并丢弃消息",requestId);
                return;
            }
            producer.sendDelayMsg(taskMsg);
        }
        log.info("contentHistoryRobin结束[{}]", requestId);
    }




    @Override
    public void clearErrorTask(TaskMsg taskMsg) throws ContentException {
        String requestId = taskMsg.getTaskId();
        //ContentHistory contentHistory = contentHistoryRepository.findByRequestId(requestId);
        clearVendorTask(requestId);
    }

    private void clearVendorTask(String taskId) throws ContentException {
        log.info("清理item任务[{}]的厂商任务开始", taskId);
        List<VendorContentTask> vlist = dateBaseService.getVendorTaskRepository().findByRequestId(taskId);
        dateBaseService.getVendorTaskRepository().deleteInBatch(vlist);
        log.info("清理item任务[{}]的厂商任务结束");
    }

    @Override
    public ApiReceipt setUserContentNumber(ContentLimitDTO command) {
        StringBuilder redisKey = new StringBuilder("contentNumber_").append(command.getSpCode());
        //默认设置为本次修改时间
        command.setLastModify(new Date().getTime());

        String redisValue = JSON.toJSONString(command);

        redisService.set(redisKey.toString(), redisValue, timeout, TimeUnit.DAYS);

        return ApiReceipt.ok();
    }

    @Override
    public ApiReceipt getUserContentNumber(String spCode) throws IOException {
        StringBuilder redisKey = new StringBuilder("contentNumber_").append(spCode);
        ContentLimitDTO limitDTO = null;
        //重置用户使用上限标志
        boolean resetFlag = false;
        try {
            limitDTO = redisService.get(redisKey.toString(), ContentLimitDTO.class);
        } catch (IOException e) {
            log.error("查询用户[{}]刷新预热用量异常,[{}]", spCode, e == null ? "null exception" : e.getMessage());
            throw new IOException("查询redis用户刷新预热上限异常");
        }
        ContentLimit limit = new ContentLimit();

        if (limitDTO != null) {
            if (isToday(limitDTO.getLastModify())) {
                log.info("最后刷新预热是当天，不需要更新用量信息");
                limit.setUrlRefreshNumber(limitDTO.getUrlRefreshNumber());
                limit.setDirRefreshNumber(limitDTO.getDirRefreshNumber());
                limit.setUrlPreloadNumber(limitDTO.getUrlPreloadNumber());
            } else {
                log.info("最后刷新预热非当天，需要更新用量信息");
                limit.setDirRefreshNumber(new Item(0, limitDTO.getDirRefreshNumber().getLimit()));
                limit.setUrlPreloadNumber(new Item(0, limitDTO.getUrlPreloadNumber().getLimit()));
                limit.setUrlRefreshNumber(new Item(0, limitDTO.getUrlRefreshNumber().getLimit()));
                //最后的请求是刷新预热的，所以本地需要更新用量数据
                resetFlag = true;
            }
        } else {
            limitDTO = new ContentLimitDTO();
            limit.setDirRefreshNumber(new Item(0, maxDirRefresh));
            limit.setUrlPreloadNumber(new Item(0, maxPreheat));
            limit.setUrlRefreshNumber(new Item(0, maxRefresh));
            resetFlag = true;
        }


        if (resetFlag) {
            //最后的请求是刷新预热的，所以本地需要更新用量数据
            limitDTO.setDirRefreshNumber(limit.getDirRefreshNumber());
            limitDTO.setUrlPreloadNumber(limit.getUrlPreloadNumber());
            limitDTO.setUrlRefreshNumber(limit.getUrlRefreshNumber());
            limitDTO.setLastModify(System.currentTimeMillis());
            redisService.set(redisKey.toString(), JSON.toJSONString(limitDTO), timeout, TimeUnit.DAYS);
        }
        return ApiReceipt.ok(limit);
    }

/*
    @Override
    public ApiReceipt getUserContentNumber(String spCode) throws IOException {
        StringBuilder redisKey = new StringBuilder("contentNumber_").append(spCode);
        ContentLimitDTO limitDTO = null;
        //重置用户使用上限标志
        boolean resetFlag = false;
            try {
            limitDTO = redisService.get(redisKey.toString(), ContentLimitDTO.class);
        } catch (IOException e) {
            log.error("查询用户[{}]刷新预热用量异常,[{}]", spCode, e == null ? "null exception" : e.getMessage());
            throw new IOException("查询redis用户刷新预热上限异常");
        }
        ContentLimit limit = new ContentLimit();

        if (limitDTO != null) {
            if (isToday(limitDTO.getLastModify())) {
                log.info("最后刷新预热是当天，不需要更新用量信息");
                limit.setUrlRefreshNumber(limitDTO.getUrlRefreshNumber());
                limit.setDirRefreshNumber(limitDTO.getDirRefreshNumber());
                limit.setUrlPreloadNumber(limitDTO.getUrlPreloadNumber());
            } else {
                log.info("最后刷新预热非当天，需要更新用量信息");
                limit.setDirRefreshNumber(new Item(0, limitDTO.getDirRefreshNumber().getLimit()));
                limit.setUrlPreloadNumber(new Item(0, limitDTO.getUrlPreloadNumber().getLimit()));
                limit.setUrlRefreshNumber(new Item(0, limitDTO.getUrlRefreshNumber().getLimit()));
                //最后的请求是刷新预热的，所以本地需要更新用量数据
                resetFlag = true;
            }
        } else {
            UserLimit userLimit = dateBaseService.getUserLimitRepository().findByUserId(spCode);
            limitDTO = new ContentLimitDTO();
            limit.setDirRefreshNumber(new Item(0, userLimit != null?userLimit.getDefaultUrlLimit():maxDirRefresh));
            limit.setUrlPreloadNumber(new Item(0, userLimit != null?userLimit.getDefaultDirLimit():maxPreheat));
            limit.setUrlRefreshNumber(new Item(0, userLimit != null?userLimit.getDefaultPreloadLimit():maxRefresh));
            resetFlag = true;
        }


            if (resetFlag) {
            //最后的请求是刷新预热的，所以本地需要更新用量数据
            limitDTO.setDirRefreshNumber(limit.getDirRefreshNumber());
            limitDTO.setUrlPreloadNumber(limit.getUrlPreloadNumber());
            limitDTO.setUrlRefreshNumber(limit.getUrlRefreshNumber());
            limitDTO.setLastModify(System.currentTimeMillis());
            redisService.set(redisKey.toString(), JSON.toJSONString(limitDTO), timeout, TimeUnit.DAYS);
        }
            return ApiReceipt.ok(limit);
    }

*/

    @Override
    public  ContentHistory findHisttoryByRequestId(String id)throws ContentException{

        return dateBaseService.getContentHistoryRepository().findByRequestId(id);
    }

    
    private Map<String, String> checkDomainStatus(List<String> data, boolean adminFlag, GateWayHeaderDTO dto) throws ContentException {
        //判断当前域名状态，查当前用户的所有域名
        List<Domain> domains = new ArrayList<>();
        //根据domain来过滤
        List<String> domainParam = new ArrayList<>();

        Map<String, String> urlDomainMap = new HashMap<>();

        for (String url : data) {
            String host = null;
            try {
                host = (new URL(url)).getHost();
            } catch (MalformedURLException e) {
                log.error("无效的URL [{}]",url);
                throw new ContentException("0x004008", String.format("无效的URL:[%s]，禁止操作", url));
            }
            domainParam.add(host);
            urlDomainMap.put(url, host);
        }

        if (adminFlag) {
            //管理员查全部域名
            domains = dateBaseService.getDomainRepository().findByDomainIn(domainParam);
        } else {
            domains = dateBaseService.getDomainRepository().findByUserCodeAndDomainIn(dto.getSpcode(), domainParam);
        }
        log.info("domains:{}",domains);
        if (null == domains || domains.size() == 0) {
            throw new ContentException("0x004008", "该用户无有效域名，禁止操作");
        }
        Map<String, Domain> domainMap = new HashMap<>();
        //判断域名状态
        for (Domain d : domains) {
            boolean createFlag =  "create_domain".equals(d.getTaskName()) && !"RUNNING".equals(d.getStatus());
            boolean statusFlag = "DELETE".equals(d.getStatus()) || "FORBIDDEN".equals(d.getStatus());
            boolean flag = createFlag || statusFlag;//符合其中一个条件则不允许操作
            if (!flag) {
                domainMap.put(d.getDomain(), d);
            }
        }
        if (domainMap.size() == 0) {
            throw new ContentException("0x004008", "域名无效，禁止操作");
        }

        for (String host : domainParam) {
            if (!domainMap.containsKey(host)) {
                throw new ContentException("0x004008", String.format("域名:[%s]不可用，禁止操作", host));
            }
        }

        return urlDomainMap;
    }
    
    private void checkUserLimit(boolean adminFlag,RefreshType type,String spCode, String userName, int size) throws ContentException {
        if(adminFlag) return;
        //UserLimit userLimit = dateBaseService.getUserLimitRepository().findByUserId(spCode);
        UserLimit userLimit = null;
        JSONObject contentLimit = contentLimitJsonConfig.getDefaultContentLimit();
        long lastModify = System.currentTimeMillis();
        if(contentLimit == null){
            contentLimit = new JSONObject();
            contentLimit.put("urlRefreshNumber",new Item(0, userLimit != null?userLimit.getDefaultUrlLimit():maxRefresh));
            contentLimit.put("dirRefreshNumber",new Item(0, userLimit != null?userLimit.getDefaultDirLimit():maxDirRefresh));
            contentLimit.put("urlPreloadNumber",new Item(0, userLimit != null?userLimit.getDefaultPreloadLimit():maxPreheat));

        }
        contentLimit.put("lastModify",lastModify);

        List<String> keys = new ArrayList<>();
        keys.add(contentPrefix + spCode);
        log.info(contentPrefix + spCode);
        List<String> args  = new ArrayList<>();
        args.add(String.valueOf(adminFlag));
        args.add(type.name());
        args.add(String.valueOf(size));
        args.add(String.valueOf(System.currentTimeMillis()));
        args.add(contentLimit.toJSONString());
        args.add(String.valueOf(todayTimeStamp()));
        // 0-成功，-1执行异常，-100超限
        int result = luaScriptService.executeCountScript(keys,args);
        if(-100 == result){
            anubisNotifyService.emailNotifyApiException(new AnubisNotifyExceptionRequest("anubis-content",type.name(),new StringBuilder("spCode:").append(spCode).append(", userName:").append(userName).toString(),new ApiReceipt().getRequestId(),"超过每日数量上限"));
            throw new ContentException("0x004012");
        }else if(-1 == result){
            anubisNotifyService.emailNotifyApiException(new AnubisNotifyExceptionRequest("anubis-content",type.name(),new StringBuilder("spCode:").append(spCode).append(", userName:").append(userName).toString(),new ApiReceipt().getRequestId(),"刷新预热lua计数脚本执行异常"));
            throw new ContentException("0x0002");
        }
    }

    
    private long todayTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

       return calendar.getTime().getTime();
    }

    private boolean isToday(Long lastModify) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (lastModify != null && lastModify > calendar.getTime().getTime()) {
            //
            return true;
        }
        return false;
    }


    public Map<String, List<String>> getDomainVendorsMapNew(List<String> contents) throws Exception{
        Map<String, List<String>> domainUrlsMap = new HashMap<String, List<String>>();
        URL domainUrl = null;
        Boolean flag = true;
        try {
            for (String url : contents) {
                domainUrl = new URL(url);
                String host = domainUrl.getHost();
                List<String> domainUrls = domainUrlsMap.get(host);
                if (domainUrls == null) {
                    domainUrls = new ArrayList<String>();
                }
                domainUrls.add(url);
                domainUrlsMap.put(host, domainUrls);
            }
        }catch (Exception e) {
            log.error("parse url failed, err:{}", e.getMessage());
            throw RestfulException.builder().code(ErrEnum.ErrInternal.getCode()).message(e.getMessage()).build();
        }

        List<String> allDomains = new ArrayList<String>(domainUrlsMap.keySet());
        Map<String, List<String>> existDomainsMap = new HashMap<>();
        List<String> lostDomains = new ArrayList<String>();

        for(String s:allDomains){
            if(!urlVendorMap.containsKey(s) || urlVendorMap.get(s) == null){
                lostDomains.add(s);
            }else{
                existDomainsMap.put(s, urlVendorMap.get(s));
            }
        }

        if(lostDomains.size()>0) {
            // 获取域名信息
            log.debug("domains:{}", Utils.objectToString(lostDomains));
            List<Domain> domainInfos = dateBaseService.getDomainRepository().findAllByDomainInAndStatusNot(lostDomains, "DELETE");
            if (domainInfos.size() != lostDomains.size()) {
                log.error("cannot find all domains info from db");
                flag = false;
            }
            // 获取域名所在的厂商
            Set<String> lineIds = new HashSet<String>();
            domainInfos.forEach(d -> {
                if (!StringUtils.isEmpty(d.getLineId())) {
                    lineIds.add(d.getLineId());
                } else {
                    //List<String> expectedVendor = getExpectedBaseLine(d.getExpectedVendor());
                    //lineIds.addAll(expectedVendor);
                    lineIds.addAll(d.getExpectedBaseLine());
                }
            });
            if(lineIds.size() == 0){
                log.error("无法获取线路");
                return null;
            }
            List<LineVendor> vendors = dateBaseService.getLineToVendorRepository().findByLineIdList(new ArrayList<String>(lineIds));
            Map<String, LineVendor> lineIdLineVendorMap = vendors.stream().collect(Collectors.toMap(i -> i.getLineId(), i -> i));
            Map<String, List<String>> lineIdVendorMap = new HashMap<String, List<String>>();
            for (LineVendor lv : vendors) {
                lineIdVendorMap.put(lv.getLineId(), JSONArray.parseArray(lv.getVendors(), String.class));
            }
            if (vendors.size() != lineIds.size()) {
                List<String> lostLineVendor = new ArrayList<>();
                for (String s : lineIds) {
                    if (!lineIdLineVendorMap.containsKey(s)) {
                        lostLineVendor.add(s);
                    }
                }
                if (lostLineVendor.size() > 0) {
                    List<LineResponse.LineDetail> lineInfos = this.lineService.getLineByIds(new ArrayList<String>(lostLineVendor), true);
                    if (lineInfos.size() != lostLineVendor.size()) {
                        log.error("获取Line数量不全");
                        flag = false;
                    }
                    for (LineResponse.LineDetail l : lineInfos) {
                        List<String> lineVendors = new ArrayList<String>();

                        if (l.getType().equals(LineResponse.LineType.base.name())) {
                            lineVendors.add(l.getVendor());
                        } else {
                            if (l.getBaseLines() != null) {
                                for (String baselineId : l.getBaseLines().keySet()) {
                                    lineVendors.add(l.getBaseLines().get(baselineId).getVendor());
                                }
                            }
                        }
                        if (lineVendors.size() > 0) {//不为空时才入库
                            lineIdVendorMap.put(l.getId(), lineVendors);
                            LineVendor LineVendorAdd = new LineVendor();
                            LineVendorAdd.setLineId(l.getId());
                            LineVendorAdd.setVendors(JSONObject.toJSONString(lineVendors));
                            dateBaseService.getLineToVendorRepository().save(LineVendorAdd);
                        } else {
                            log.error("厂商数量为空");
                            flag = false;
                        }
                    }
                }
            }

            if (!flag) {
                log.error("获取厂商不全");
                return null;
            }

            // domain: [vendor]
            Map<String, Set<String>> domainVendorsMap = new HashMap<String, Set<String>>();
            domainInfos.forEach(d -> {
                Set<String> domainVendors = domainVendorsMap.get(d);
                if (domainVendors == null) {
                    domainVendors = new HashSet<String>();
                }
                if (!StringUtils.isEmpty(d.getLineId())) {
                    domainVendors.addAll(lineIdVendorMap.get(d.getLineId()));
                } else {
                    for (String l : d.getExpectedBaseLine()) {
                        domainVendors.addAll(lineIdVendorMap.get(l));
                    }
                }
                domainVendorsMap.put(d.getDomain(), domainVendors);
                urlVendorMap.putIfAbsent(d.getDomain(), new ArrayList<>(domainVendors));
                existDomainsMap.put(d.getDomain(), new ArrayList<>(domainVendors));
            });
            log.info("domain-vendors:{}", Utils.objectToString(domainVendorsMap));
        }


        Map<String, List<String>> vendorUrlsMap = new HashMap<String, List<String>>();
        allDomains.forEach(d -> {
            // 域名所在的厂商
            List<String> dVendors = existDomainsMap.get(d);
            // 域名要刷新/预取的urls
            List<String> dUrls = domainUrlsMap.get(d);
            dVendors.forEach(v -> {
                List<String> vendorUrls = vendorUrlsMap.get(v);
                if (vendorUrls == null) {
                    vendorUrls = new ArrayList<String>();
                }
                vendorUrls.addAll(dUrls);
                vendorUrlsMap.put(v, vendorUrls);
            });
        });

        return vendorUrlsMap;
    }




    public List<String> getExpectedBaseLine(String expectedVendor) {
        try {
            return Utils.stringToArrayObject( expectedVendor, String.class);
        }catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }
    
    
    @Override
    public ApiReceipt test(GateWayHeaderDTO dto, List<String> data, RefreshType type) throws ContentException {
        /*
        List<VendorContentTask> toNewSaveList = new ArrayList<>();
        for(int i=0;i<100;i++){
            String taskId = UUID.randomUUID().toString().replaceAll("-", "");
            VendorContentTask vendorTask = new VendorContentTask();
            vendorTask.setTaskId(taskId);
            vendorTask.setRequestId(taskId);
            vendorTask.setVendor("qiniu");
            vendorTask.setMergeId(taskId);
            vendorTask.setVersion(-1);
            vendorTask.setHistoryCreateTime(new Date());
            vendorTask.setType(RefreshType.url);
            vendorTask.setContent("test");
            vendorTask.setCreateTime(new Date());
            vendorTask.setStatus(TaskStatus.FAIL);
            toNewSaveList.add(vendorTask);
        }

        dateBaseService.getVendorTaskRepository().batchSave(toNewSaveList);
        */


        Date expire = preNDay(30);
        //log.info("清理{}之前的用户任务及其子任务", formatter.format(expire));

        int count =0;
        int re =0;
        do{
            re = dateBaseService.getContentHistoryRepository().clear(expire, 1000);
            log.info("单次删除历史[{}]", re);

        }while (re == 1000);
        log.info("删除历史数量[{}]", count);


        expire = preNDay(7);
        //log.info("清理{}之前的厂商任务", formatter.format(expire));
        count =0;
        re =0;
        do{
            re = dateBaseService.getVendorTaskRepository().clearWithhHistory(expire, 1000);
            log.info("单次删除厂商任务[{}]", re);

        }while (re == 1000);
        log.info("删除厂商任务数量[{}]", count);
        return null;
    }
    private Date preNDay(int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - i);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();

    }



    @Override
    public ApiReceipt setUserDefaultContentNumber(ContentDefaultNumDTO command) {
        UserLimit userLimit = dateBaseService.getUserLimitRepository().findByUserId(command.getSpCode());
        if(userLimit == null){
            userLimit = new UserLimit();
        }
        userLimit.setDefaultUrlLimit(command.getUrlRefreshNumber());
        userLimit.setDefaultDirLimit(command.getDirRefreshNumber());
        userLimit.setDefaultPreloadLimit(command.getUrlPreloadNumber());
        userLimit.setUpdateTime(new Date());
        dateBaseService.getUserLimitRepository().save(userLimit);
        return ApiReceipt.ok();
    }

    @Override
    public ApiReceipt getUserDefaultContentNumber(String spCode) throws IOException {
        UserLimit userLimit = dateBaseService.getUserLimitRepository().findByUserId(spCode);
        if(userLimit == null){
            return ApiReceipt.ok();
        }else{
            return ApiReceipt.ok(userLimit.response());
        }

    }

    public void sendListMQ(List<TaskMsg> toSendMsg) throws IOException {
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
 	        }, rl).exit();
    }


    private RequestMergeItem splitVendorTask(ContentHistory contentHistory, int version) throws Exception{

        String requestId = contentHistory.getRequestId();
        List<String> urls = JSONArray.parseArray(contentHistory.getContent(), String.class);
        Map<String, List<String>> lostUrlsMap = new HashMap<>();
        Map<String, List<String>> vendorUrlMap;
        RefreshType type = contentHistory.getType();
        vendorUrlMap = getDomainVendorsMapNew(urls);
        if (vendorUrlMap == null) {
            log.error("获取厂商失败");
            throw new Exception("获取厂商失败");
        }
        log.debug("获取厂商成功");


        Map<String, String> vendorTaskId = new HashMap<>();
        Map<String, VendorContentTask> toSaveVendorTaskMap = new HashMap<>();

        contentHistory.setVersion(contentHistory.getVersion() + 1);
        if (version == 0) {//区别新请求与重试
            log.debug("saveVendorTask当前为首次请求，存入全部数据");
            lostUrlsMap.putAll(vendorUrlMap);
            for (String v : lostUrlsMap.keySet()) {
                if (!vendorTaskId.containsKey(v) || StringUtils.isBlank(vendorTaskId.get(v))) {
                    String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                    vendorTaskId.put(v, mergeId);
                }
                if (!toSaveVendorTaskMap.containsKey(v) || toSaveVendorTaskMap.get(v) == null) {
                    String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                    VendorContentTask vendorTask = new VendorContentTask();
                    vendorTask.setTaskId(taskId);
                    vendorTask.setRequestId(requestId);
                    vendorTask.setVendor(v);
                    vendorTask.setMergeId(vendorTaskId.get(v));
                    vendorTask.setVersion(contentHistory.getVersion());
                    vendorTask.setType(contentHistory.getType());
                    vendorTask.setContent(JSON.toJSONString(vendorUrlMap.get(v)));
                    vendorTask.setContentNumber(vendorUrlMap.get(v).size());
                    vendorTask.setHistoryCreateTime(contentHistory.getCreateTime());
                    vendorTask.setCreateTime(new Date());
                    vendorTask.setStatus(TaskStatus.WAIT);
                    toSaveVendorTaskMap.put(v, vendorTask);
                }
            }
        } else {
            log.debug("saveVendorTask当前非首次请求，对比数据");
            int totalTaskSize = 0;
            int existTaskSize = 0;

            List<VendorContentTask> existVendorTaskAll = dateBaseService.getVendorTaskRepository().findByRequestId(requestId);
            for (String s : vendorUrlMap.keySet()) {
                totalTaskSize += vendorUrlMap.get(s).size();
            }
            log.debug("需要生成厂商任务数量:[{}]", totalTaskSize);
            for (VendorContentTask vc : existVendorTaskAll) {
                List<String> us = JSONArray.parseArray(vc.getContent(), String.class);
                existTaskSize += us.size();
            }
            if (existTaskSize == 0) {
                log.debug("缺失全部子任务");
                lostUrlsMap.putAll(vendorUrlMap);
                for (String v : lostUrlsMap.keySet()) {
                    if (!vendorTaskId.containsKey(v) || StringUtils.isBlank(vendorTaskId.get(v))) {
                        String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                        vendorTaskId.put(v, mergeId);
                    }
                    if (!toSaveVendorTaskMap.containsKey(v) || toSaveVendorTaskMap.get(v) == null) {
                        String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                        VendorContentTask vendorTask = new VendorContentTask();
                        vendorTask.setTaskId(taskId);
                        vendorTask.setRequestId(requestId);
                        vendorTask.setVendor(v);
                        vendorTask.setMergeId(vendorTaskId.get(v));
                        vendorTask.setVersion(contentHistory.getVersion());
                        vendorTask.setType(contentHistory.getType());
                        vendorTask.setContent(JSON.toJSONString(vendorUrlMap.get(v)));
                        vendorTask.setContentNumber(vendorUrlMap.get(v).size());
                        vendorTask.setHistoryCreateTime(contentHistory.getCreateTime());
                        vendorTask.setCreateTime(new Date());
                        vendorTask.setStatus(TaskStatus.WAIT);
                        toSaveVendorTaskMap.put(v, vendorTask);
                    }
                }

            } else if (existTaskSize == totalTaskSize) {
                log.debug("子任务不缺失，只重试失败的任务");
                for (VendorContentTask vct : existVendorTaskAll) {
                    if (!vct.getStatus().equals(TaskStatus.SUCCESS)) {
                        if (!vendorTaskId.containsKey(vct.getVendor()) || StringUtils.isBlank(vendorTaskId.get(vct.getVendor()))) {
                            String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                            vendorTaskId.put(vct.getVendor(), mergeId);
                        }
                        if (!toSaveVendorTaskMap.containsKey(vct.getVendor()) || toSaveVendorTaskMap.get(vct.getVendor()) == null) {
                            vct.setStatus(TaskStatus.WAIT);
                            vct.setVersion(contentHistory.getVersion());
                            vct.setUpdateTime(new Date());
                            vct.setMergeId(vendorTaskId.get(vct.getVendor()));
                            toSaveVendorTaskMap.put(vct.getVendor(), vct);
                        }
                    }
                }
            } else {
                log.debug("子任务数量异常，清理后重新入库");
                clearVendorTask(requestId);
                lostUrlsMap.putAll(vendorUrlMap);
                for (String v : lostUrlsMap.keySet()) {
                    if (!vendorTaskId.containsKey(v) || StringUtils.isBlank(vendorTaskId.get(v))) {
                        String mergeId = UUID.randomUUID().toString().replaceAll("-", "");
                        vendorTaskId.put(v, mergeId);
                    }
                    if (!toSaveVendorTaskMap.containsKey(v) || toSaveVendorTaskMap.get(v) == null) {
                        String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                        VendorContentTask vendorTask = new VendorContentTask();
                        vendorTask.setTaskId(taskId);
                        vendorTask.setRequestId(requestId);
                        vendorTask.setVendor(v);
                        vendorTask.setMergeId(vendorTaskId.get(v));
                        vendorTask.setVersion(contentHistory.getVersion());
                        vendorTask.setType(contentHistory.getType());
                        vendorTask.setContent(JSON.toJSONString(vendorUrlMap.get(v)));
                        vendorTask.setContentNumber(vendorUrlMap.get(v).size());
                        vendorTask.setHistoryCreateTime(contentHistory.getCreateTime());
                        vendorTask.setCreateTime(new Date());
                        vendorTask.setStatus(TaskStatus.WAIT);
                        toSaveVendorTaskMap.put(v, vendorTask);
                    }
                }
            }
        }

        RequestMergeItem requestMergeItem = new RequestMergeItem();

        if(toSaveVendorTaskMap != null ) {
            requestMergeItem.setRequestId(requestId);
            requestMergeItem.setVendorContentTaskList(new ArrayList<>(toSaveVendorTaskMap.values()));
            return requestMergeItem;
        }else{
            throw new Exception("数据异常");
        }
    }


}
