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
import cn.com.pingan.cdn.rabbitmq.config.RabbitListenerConfig;
import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.rabbitmq.producer.Producer;
import cn.com.pingan.cdn.repository.mysql.*;
import cn.com.pingan.cdn.repository.pgsql.DomainRepository;
import cn.com.pingan.cdn.request.openapi.ContentDefaultNumDTO;
import cn.com.pingan.cdn.service.*;
import cn.com.pingan.cdn.utils.Utils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
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
    private ContentHistoryRepository contentHistoryRepository;

    @Autowired
    private UserLimitRepository userLimitRepository;
    
    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private ContentItemRepository contentItemRepository;

    @Autowired
    private LineToVendorRepository lineToVendorRepository;

    @Autowired
    private VendorTaskRepository vendorTaskRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private LineService lineService;

    @Autowired
    private AnubisNotifyService anubisNotifyService;
    
    @Autowired
    Producer producer;
    
    
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

    
    @Autowired
    RedisLuaScriptService luaScriptService;

    @Autowired
    ContentLimitJsonConfig contentLimitJsonConfig;

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
                if(h.add(url.toLowerCase())){
                    tmpData.add(url);
                }
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
                }else {
                    return ApiReceipt.error(ErrorCode.STARTURL);
                }
            }

            data.clear();
            data.addAll(tmpData);

            boolean adminFlag = "true".equals(dto.getIsAdmin());

            //检查域名状态
            log.info("检查域名状态开始");
            checkDomainStatus(data, adminFlag, dto);
            log.info("检查域名状态结束");

            //计数
            log.info("检查用户计数开始");
            checkUserLimit(adminFlag, type, dto.getSpcode(), data.size());
            log.info("检查用户计数结束");

            //原始请求入库
            String taskId = UUID.randomUUID().toString().replaceAll("-", "");
            ContentHistory contentHistory = new ContentHistory();

            contentHistory.setType(type);
            contentHistory.setRequestId(taskId);
            contentHistory.setUserId(dto.getUid());
            contentHistory.setContent(JSON.toJSONString(data));
            contentHistory.setCreateTime(new Date());
            contentHistory.setStatus(HisStatus.WAIT);
            contentHistory.setContentNumber(data.size());
            contentHistory.setIsAdmin(dto.getIsAdmin());

            contentHistoryRepository.save(contentHistory);
            log.info("contentHistory id:{}", contentHistory.getId());


            TaskMsg historyTaskMsg = new TaskMsg();
            historyTaskMsg.setTaskId(taskId);
            historyTaskMsg.setVersion(0);
            historyTaskMsg.setOperation(TaskOperationEnum.content_item);
            producer.sendTaskMsg(historyTaskMsg);

            log.info("sendTaskMsg:{} to MQ", historyTaskMsg);

            return ApiReceipt.ok().data(taskId);
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
    public ApiReceipt redoContentTask(String requestId, boolean flag){
        log.info("redoContentTask[{}]开始", requestId);
        try {

            ContentHistory contentHistory = this.findHisttoryByRequestId(requestId);
            if (contentHistory == null) {
                log.error("用户任务[{}]不存在", requestId);
                return ApiReceipt.error("0x004008", String.format("[%s]原始任务记录不存在，禁止操作", requestId));
            }
            if (contentHistory.getStatus().equals(HisStatus.FAIL)) {//任务已失败或未开始
                contentHistory.setStatus(HisStatus.WAIT);
                contentHistoryRepository.save(contentHistory);
                log.info("[{}]请求任务已失败，设置为wait", requestId);

            } else if (contentHistory.getStatus().equals(HisStatus.WAIT)) {
                if(flag){
                    log.info("[{}]请求任务为wait,强制重试", requestId);
                }else{
                    log.info("[{}]请求任务为wait,不需要重试", requestId);
                    return ApiReceipt.ok();
                }
            } else {
                log.info("[{}]请求任务已成功，不再重试", requestId);
                return ApiReceipt.ok();

            }
            TaskMsg historyTaskMsg = new TaskMsg();
            historyTaskMsg.setForce(flag);
            historyTaskMsg.setTaskId(requestId);
            historyTaskMsg.setVersion(1);
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
    public void saveContentItem(TaskMsg taskMsg) throws ContentException {
        String requestId = taskMsg.getTaskId();
        boolean force = taskMsg.getForce();
        Boolean flag = false;
        boolean toNext = false;
        log.info("saveContentItem开始[{}]", requestId);
        try {
            ContentHistory contentHistory = this.findHisttoryByRequestId(requestId);
            if (contentHistory == null) {
                log.error("用户任务不存在,丢弃消息");
                return;
            }

            if (taskMsg.getRetryNum() > limitRetry) {
                contentHistory.setStatus(HisStatus.FAIL);
                contentHistory.setUpdateTime(new Date());
                contentHistory.setMessage("拆分任务及其子任务失败超限");
                contentHistoryRepository.save(contentHistory);
                log.error("超出重试次数,设置任务失败并丢弃消息");
                return;
            }

            List<String> urls = JSONArray.parseArray(contentHistory.getContent(), String.class);
            List<String> lostUrls = new ArrayList<>();
            RefreshType type = contentHistory.getType();

            List<ContentItem> existItemsAll = null;
            List<ContentItem> toSaveList = new ArrayList<>();
            List<TaskMsg> toSendMsg = new ArrayList<>();
            if (taskMsg.getVersion() == 0) {//区别新请求与重试
                log.info("saveContentItem当前为首次请求，存入全部数据");
                lostUrls.addAll(urls);
                taskMsg.setVersion(1);
                flag = true;
            } else {
                log.info("saveContentItem当前为重试请求，补全差异数据");
                existItemsAll = contentItemRepository.findByRequestId(requestId);
                if (existItemsAll.size() != contentHistory.getContentNumber()) {
                    if(existItemsAll.size() > contentHistory.getContentNumber()){
                        clearsubTask(requestId);
                        existItemsAll.clear();
                    }
                    if (existItemsAll.size() == 0) {
                        lostUrls.addAll(urls);
                    } else {
                        Map<String, ContentItem> existUrlMap = existItemsAll.stream().collect(Collectors.toMap(i -> i.getContent(), i -> i));
                        for (String s : urls) {
                            if (!existUrlMap.containsKey(s)) {
                                log.info("添加缺失url:{}", s);
                                lostUrls.add(s);
                            }
                        }
                    }
                }
                log.info("saveContentItem重置任务状态开始");
                for(ContentItem it: existItemsAll) {//已存在的失败任务重发
                    if(force){
                        if (it.getStatus().equals(HisStatus.FAIL)){
                            it.setUpdateTime(new Date());
                            it.setStatus(HisStatus.WAIT);
                            toSaveList.add(it);
                            toNext = true;
                        }else if(it.getStatus().equals(HisStatus.WAIT)){
                            toNext = true;
                        }
                    }else {
                        if (it.getStatus().equals(HisStatus.FAIL)) {
                            it.setUpdateTime(new Date());
                            it.setStatus(HisStatus.WAIT);
                            toSaveList.add(it);
                            toNext = true;
                        }
                    /*
                    //contentItemRepository.save(it);
                    //log.info("saveContentItem重新下发失败任务[{}]", it.getItemId());
                    TaskMsg itemTaskMsg = new TaskMsg();
                    itemTaskMsg.setTaskId(it.getItemId());
                    itemTaskMsg.setOperation(TaskOperationEnum.content_vendor);
                    itemTaskMsg.setVersion(1);
                    itemTaskMsg.setRetryNum(0);
                    //itemTaskMsg.setDelay(1000L);
                    toSendMsg.add(itemTaskMsg);
                    //producer.sendAllMsg(itemTaskMsg);
                    */
                    }
                }
                log.info("saveContentItem重置任务状态结束");
            }
            if (lostUrls.size() > 0) {//有缺失的数据
                Map<String, Set<String>> domainVendorsMap;
                log.info("开始查询厂商");
                domainVendorsMap = getDomainVendorsMapNew(lostUrls);
                if (domainVendorsMap == null) {
                    log.error("获取厂商失败");
                    taskMsg.setDelay(limitRetryRate);
                    taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
                    producer.sendTaskMsg(taskMsg);
                    return;
                }
                log.info("获取厂商成功");

                for (String u : lostUrls) {
                    String itemId = UUID.randomUUID().toString().replaceAll("-", "");
                    ContentItem item = new ContentItem();
                    item.setRequestId(requestId);
                    item.setItemId(itemId);
                    item.setType(type);
                    item.setContent(u);
                    item.setCreateTime(new Date());
                    item.setStatus(HisStatus.WAIT);

                    URL domainUrl = new URL(u);
                    String host = domainUrl.getHost();
                    item.setVendor(JSONObject.toJSONString(domainVendorsMap.get(host)));
                    toSaveList.add(item);
                    /*
                    //contentItemRepository.save(item);
                    //log.info("拆分任务[{}]，入库成功", item.getItemId());
                    TaskMsg itemTaskMsg = new TaskMsg();
                    itemTaskMsg.setTaskId(itemId);
                    itemTaskMsg.setOperation(TaskOperationEnum.content_vendor);
                    itemTaskMsg.setVersion(0);
                    itemTaskMsg.setRetryNum(0);
                    //itemTaskMsg.setDelay(1000L);
                    toSendMsg.add(itemTaskMsg);
                    //producer.sendAllMsg(itemTaskMsg);
                    */
                }
            }
            if(toSaveList.size()>0) {
                log.info("saveContentItem数量[{}]", toSaveList.size());
                contentItemRepository.saveAll(toSaveList);
                log.info("saveContentItem入库完成");
                toNext = true;
            }
            if(toNext){
                TaskMsg itemTaskMsg = new TaskMsg();
                itemTaskMsg.setForce(force);
                itemTaskMsg.setTaskId(requestId);
                itemTaskMsg.setOperation(TaskOperationEnum.content_vendor);
                itemTaskMsg.setVersion(flag?0:1);
                itemTaskMsg.setRetryNum(0);
                //itemTaskMsg.setDelay(1000L);
                producer.sendAllMsg(itemTaskMsg);
                log.info("saveContentItem发送MQ完成");
            }else{
                log.info("saveContentItem没有可执行任务");
            }
            log.info("saveContentItem任务入库并发送消息成功");
        }catch (Exception e){
            log.error("获取厂商失败");
            taskMsg.setDelay(limitRetryRate);
            taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
            producer.sendDelayMsg(taskMsg);
            return;
        }

        /*
        log.info("轮询ContentHistory[{}]", requestId);
        TaskMsg robinTaskMsg = new TaskMsg();
        robinTaskMsg.setTaskId(requestId);
        robinTaskMsg.setOperation(TaskOperationEnum.content_item_robin);
        robinTaskMsg.setVersion(0);
        robinTaskMsg.setRetryNum(0);
        robinTaskMsg.setDelay(3 * 60 *1000L);//TODO
        producer.sendDelayMsg(robinTaskMsg);
        log.info("saveContentItem结束[{}]", requestId);
        */
    }

    @Override
    public void saveContentVendor(TaskMsg taskMsg) throws ContentException{
        String requestId = taskMsg.getTaskId();
        boolean force = taskMsg.getForce();
        //String itemId = taskMsg.getTaskId();
        List<String> itemIds = new ArrayList<>();
        log.info("saveContentVendor开始[{}]", requestId);
        try {
            List<ContentItem> allContentItemList = contentItemRepository.findByRequestId(requestId);
            RefreshType type = null;
            if (allContentItemList.size() <= 0) {
                ContentHistory contentHistory = this.findHisttoryByRequestId(requestId);
                if (contentHistory == null) {
                    log.error("用户任务不存在,丢弃消息");
                    return;
                }
                log.error("[{}]丢失条目任务", requestId);
                return;
            }

            if (taskMsg.getRetryNum() > limitRetry) {
                for (ContentItem ci : allContentItemList) {
                    ci.setStatus(HisStatus.FAIL);
                    ci.setUpdateTime(new Date());
                    ci.setMessage("拆分任务超时失败");
                }
                contentItemRepository.saveAll(allContentItemList);
                log.error("saveContentVendor超出重试次数,设置任务失败并丢弃消息");
                return;
            }

            List<VendorContentTask> toSaveList = new ArrayList<>();
            List<TaskMsg> toSendMsg = new ArrayList<>();
            Map<String, MergeTaskItem> vendorMergeMap = new HashMap<>();
            int Version = taskMsg.getVersion();
            for (ContentItem ci : allContentItemList){
                type = ci.getType();
                String url = ci.getContent();
                String itemId = ci.getItemId();
                itemIds.add(itemId);
                List<String> lostVendor = new ArrayList<>();
                List<VendorContentTask> existVendorTaskAll;
                List<String> vendors = JSONArray.parseArray(ci.getVendor(), String.class);
                if (Version == 0) {
                    lostVendor.addAll(vendors);
                    taskMsg.setVersion(1);
                } else {
                    log.info("saveContentVendor补全差异数据");
                    existVendorTaskAll = vendorTaskRepository.findByItemId(itemId);
                    if (existVendorTaskAll.size() != vendors.size()) {
                        if(existVendorTaskAll.size() > vendors.size()){
                            clearVendorTask(itemId);
                            existVendorTaskAll.clear();
                        }
                        if (existVendorTaskAll.size() == 0) {
                            lostVendor.addAll(vendors);
                        } else {
                            Map<String, VendorContentTask> existVendorMap = existVendorTaskAll.stream().collect(Collectors.toMap(i -> i.getVendor(), i -> i));
                            for (String s : vendors) {
                                if (!existVendorMap.containsKey(s)) {
                                    log.info("添加缺失url:{}", s);
                                    lostVendor.add(s);
                                }
                            }
                        }
                    }
                    log.info("saveContentVendor重任务状态开始");
                    for (VendorContentTask vc : existVendorTaskAll) {//已存在的失败任务重发
                        if(force){
                            log.info("强制任务");
                            if (!vc.getStatus().equals(TaskStatus.SUCCESS)) {
                                MergeTaskItem mit;
                                if (vendorMergeMap.containsKey(vc.getVendor())) {
                                    mit = vendorMergeMap.get(vc.getVendor());
                                } else {
                                    mit = new MergeTaskItem();
                                    mit.setMergeId(UUID.randomUUID().toString().replaceAll("-", ""));

                                }
                                vc.setUpdateTime(new Date());
                                vc.setVersion(1);
                                vc.setStatus(TaskStatus.WAIT);
                                vc.setMergeId(mit.getMergeId());
                                mit.setSize(mit.getSize() + 1);
                                vendorMergeMap.put(vc.getVendor(), mit);
                                toSaveList.add(vc);
                            }
                        }else {
                            log.info("普通任务");
                            if (vc.getStatus().equals(TaskStatus.FAIL)) {
                                MergeTaskItem mit;
                                if (vendorMergeMap.containsKey(vc.getVendor())) {
                                    mit = vendorMergeMap.get(vc.getVendor());
                                } else {
                                    mit = new MergeTaskItem();
                                    mit.setMergeId(UUID.randomUUID().toString().replaceAll("-", ""));

                                }
                                vc.setUpdateTime(new Date());
                                vc.setVersion(1);
                                vc.setStatus(TaskStatus.WAIT);
                                vc.setMergeId(mit.getMergeId());
                                mit.setSize(mit.getSize() + 1);
                                vendorMergeMap.put(vc.getVendor(), mit);
                                toSaveList.add(vc);

                            /*
                            //vendorTaskRepository.save(vc);
                            //log.info("saveContentVendor重新下发失败任务[{}]", vc.getTaskId());
                            TaskMsg vendorTaskMsg = new TaskMsg();
                            vendorTaskMsg.setTaskId(vc.getTaskId());
                            vendorTaskMsg.setOperation(TaskOperationEnum.getVendorOperation(vc.getVendor()));
                            vendorTaskMsg.setVersion(1);
                            vendorTaskMsg.setRetryNum(0);
                            toSendMsg.add(vendorTaskMsg);
                            //vendorTaskMsg.setDelay(1000L);
                            //producer.sendAllMsg(vendorTaskMsg);
                            */
                            }
                        }
                    }

                    log.info("saveContentVendor重任务状态结束");
                }
                if (lostVendor.size() > 0) {//有缺失的数据
                    for (String v : lostVendor) {
                        String mergeId;
                        MergeTaskItem mit;
                        if(vendorMergeMap.containsKey(v)){
                            mit = vendorMergeMap.get(v);
                        }else{
                            mit = new MergeTaskItem();
                            mit.setMergeId(UUID.randomUUID().toString().replaceAll("-", ""));

                        }
                        String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                        VendorContentTask vendorTask = new VendorContentTask();
                        vendorTask.setTaskId(taskId);
                        vendorTask.setItemId(itemId);
                        vendorTask.setRequestId(requestId);
                        vendorTask.setVendor(v);
                        vendorTask.setMergeId(mit.getMergeId());
                        vendorTask.setVersion(0);
                        vendorTask.setType(type);
                        vendorTask.setContent(url);
                        vendorTask.setCreateTime(new Date());
                        vendorTask.setStatus(TaskStatus.WAIT);
                        toSaveList.add(vendorTask);
                        mit.setSize(mit.getSize() + 1);
                        vendorMergeMap.put(v, mit);
                        /*
                        //vendorTaskRepository.save(vendorTask);
                        //log.info("厂商任务[{}]，入库成功", vendorTask.getTaskId());
                        TaskMsg vendorTaskMsg = new TaskMsg();
                        vendorTaskMsg.setTaskId(taskId);
                        vendorTaskMsg.setOperation(TaskOperationEnum.getVendorOperation(v));
                        vendorTaskMsg.setVersion(0);
                        vendorTaskMsg.setRetryNum(0);
                        toSendMsg.add(vendorTaskMsg);
                        //vendorTaskMsg.setDelay(1000L);
                        //producer.sendAllMsg(vendorTaskMsg);
                        */
                    }
                }
            }
            if (toSaveList.size() > 0) {
                log.info("saveContentVendor数量[{}]", toSaveList.size());
                vendorTaskRepository.saveAll(toSaveList);
                //sendListMQ(toSendMsg);
                log.info("saveContentVendor任务入库成功");
                for(String s: vendorMergeMap.keySet()){
                    TaskMsg vendorTaskMsg = new TaskMsg();
                    vendorTaskMsg.setTaskId(vendorMergeMap.get(s).getMergeId());
                    vendorTaskMsg.setOperation(TaskOperationEnum.getVendorOperation(s, type));
                    vendorTaskMsg.setVendor(s);
                    vendorTaskMsg.setVersion(0);
                    vendorTaskMsg.setIsMerge(true);
                    vendorTaskMsg.setType(type);
                    vendorTaskMsg.setRetryNum(0);
                    vendorTaskMsg.setSize(vendorMergeMap.get(s).getSize());
                    producer.sendTaskMsg(vendorTaskMsg);
                    log.info("saveContentVendor发送[{}]", s);
                }
                log.info("saveContentVendor发送MQ完成");

            }else{
                log.info("saveContentVendor[{}]没有可执行任务", requestId);
            }

            /*
            List<TaskMsg> tml = new ArrayList<>();
            for(String s: itemIds) {
                log.info("生成轮询ContentVendor[{}]", s);
                TaskMsg robinTaskMsg = new TaskMsg();
                robinTaskMsg.setTaskId(s);
                robinTaskMsg.setOperation(TaskOperationEnum.content_vendor_robin);
                robinTaskMsg.setVersion(0);
                robinTaskMsg.setRetryNum(0);
                robinTaskMsg.setDelay(2 * 60 * 1000L);//TODO
                tml.add(robinTaskMsg);
            }
            if(tml.size()>0){
                sendListMQ(tml);
            }
            */
        }catch (Exception e){
            log.error("saveContentVendor异常[{}]", e);
            taskMsg.setDelay(limitRetryRate);
            taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
            producer.sendDelayMsg(taskMsg);
            return;
        }
        //最终轮询一次
        log.info("发送轮询ContentHistory[{}]消息成功", requestId);
        TaskMsg robinTaskMsg = new TaskMsg();
        robinTaskMsg.setTaskId(requestId);
        robinTaskMsg.setOperation(TaskOperationEnum.content_vendor_robin);
        robinTaskMsg.setVersion(0);
        robinTaskMsg.setRetryNum(0);
        robinTaskMsg.setDelay(robinRate);//TODO
        producer.sendDelayMsg(robinTaskMsg);
        log.info("saveContentVendor结束[{}]", requestId);
    }

    @Override
    public void contentItemRobin(TaskMsg taskMsg) throws ContentException {
        String requestId = taskMsg.getTaskId();
        log.info("contentItemRobin开始[{}]", requestId);
        try {
            ContentHistory contentHistory = this.findHisttoryByRequestId(requestId);
            if (contentHistory == null) {
                log.error("用户任务不存在,丢弃消息");
                return;
            }
            if(contentHistory.getStatus().equals(HisStatus.SUCCESS)|| contentHistory.getStatus().equals(HisStatus.FAIL)){
                log.info("用户任务[{}]状态[{}]", requestId, contentHistory.getStatus());
                return;
            }

            List<ContentItem> contentItemList = contentItemRepository.findByRequestId(requestId);
            List<String> urls = JSONArray.parseArray(contentHistory.getContent(), String.class);

            if(contentItemList.size() != urls.size()){
                contentHistory.setStatus(HisStatus.FAIL);
                contentHistory.setUpdateTime(new Date());
                contentHistory.setMessage("拆分任务入库数量错误");
                contentHistoryRepository.save(contentHistory);
                log.error("拆分任务入库数量错误");
                return;
            }

            if (taskMsg.getRetryNum() > limitRetry) {
                contentHistory.setStatus(HisStatus.FAIL);
                contentHistory.setUpdateTime(new Date());
                contentHistory.setMessage("拆分任务及其子任务失败超限");
                contentHistoryRepository.save(contentHistory);
                log.error("超出重试次数,设置任务失败并丢弃消息");
                return;
            }

            HisStatus stat = HisStatus.SUCCESS;
            String desc = "";
            for(ContentItem ci:contentItemList ){
                if(ci.getStatus().equals(HisStatus.WAIT)){
                    stat = HisStatus.WAIT;
                }else if(ci.getStatus().equals(HisStatus.FAIL)) {
                    stat = HisStatus.FAIL;
                    desc = StringUtils.isNotBlank(ci.getMessage())?ci.getMessage():"执行任务失败";
                    break;
                }else{
                    desc = StringUtils.isNotBlank(ci.getMessage())?ci.getMessage():"执行任务成功";
                    continue;
                }
            }
            if(stat.equals(HisStatus.FAIL) || stat.equals(HisStatus.SUCCESS)){
                contentHistory.setStatus(stat);
                contentHistory.setUpdateTime(new Date());
                contentHistory.setMessage(desc);
                contentHistoryRepository.save(contentHistory);
                log.info("ItemRobin轮询结果为[{}]", desc);
                return;
            }
            taskMsg.setDelay(3 * 60 *1000L);//TODO
            producer.sendDelayMsg(taskMsg);

        }catch (Exception e){
            log.error("用户任务轮询异常[{}]", e);
            taskMsg.setDelay(30000L);
            taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
            producer.sendDelayMsg(taskMsg);
        }
        log.info("contentItemRobin结束[{}]", requestId);
    }

    @Override
    public void contentVendorRobin(TaskMsg taskMsg) throws ContentException {
        String requestId = taskMsg.getTaskId();
        log.info("contentVendorRobin开始[{}]", requestId);
        try {
            //ContentItem contentItem = contentItemRepository.findByItemId(itemId);
            ContentHistory contentHistory = this.findHisttoryByRequestId(requestId);
            if (contentHistory == null) {
                log.error("用户任务不存在,丢弃消息");
                return;
            }
            if(contentHistory.getStatus().equals(HisStatus.SUCCESS)|| contentHistory.getStatus().equals(HisStatus.FAIL)){
                log.info("任务[{}]状态[{}]", requestId, contentHistory.getStatus());
                return;
            }

            /*
            List<String> vendors = JSONArray.parseArray(contentItem.getVendor(), String.class);
            if(vendorContentTaskList.size() != vendors.size()){
                contentItem.setStatus(HisStatus.FAIL);
                contentItem.setUpdateTime(new Date());
                contentItem.setMessage("厂商任务入库数量错误");
                contentItemRepository.save(contentItem);
                log.error("厂商任务入库数量错误");
                return;
            }
            */
            if (taskMsg.getRetryNum() > limitRetry || taskMsg.getRoundRobinNum() > robinNum) {
                contentHistory.setStatus(HisStatus.FAIL);
                contentHistory.setUpdateTime(new Date());
                contentHistory.setMessage("任务超时失败");
                contentHistoryRepository.save(contentHistory);
                log.error("[{}]contentVendorRobin超出重试次数,设置任务失败并丢弃消息",requestId);
                return;
            }

            Map<String, HisStatus> itemStatusMap = new HashMap<>();
            List<VendorContentTask> vendorContentTaskList = vendorTaskRepository.findByRequestId(requestId);
            HisStatus stat = HisStatus.SUCCESS;
            String desc = "";
            for(VendorContentTask vc:vendorContentTaskList ){
                if(!itemStatusMap.containsKey(vc.getItemId())){
                    itemStatusMap.put(vc.getItemId(), HisStatus.SUCCESS);
                }
                if(vc.getStatus().equals(TaskStatus.WAIT) || vc.getStatus().equals(TaskStatus.PROCESSING) || vc.getStatus().equals(TaskStatus.ROUND_ROBIN)){
                    stat = HisStatus.WAIT;
                    itemStatusMap.put(vc.getItemId(), HisStatus.WAIT);
                }else if(vc.getStatus().equals(TaskStatus.FAIL)) {
                    stat = HisStatus.FAIL;
                    desc = StringUtils.isNotBlank(vc.getMessage())?vc.getMessage():"执行任务失败";
                    itemStatusMap.put(vc.getItemId(), HisStatus.FAIL);
                    break;
                }else{
                    desc = StringUtils.isNotBlank(vc.getMessage())?vc.getMessage():"执行任务成功";
                    continue;
                }
            }

            List<String> toSetList = new ArrayList<>();
            for(String s : itemStatusMap.keySet()){
                if(itemStatusMap.get(s).equals(HisStatus.SUCCESS) || itemStatusMap.get(s).equals(HisStatus.FAIL)) {
                    toSetList.add(s);
                }
            }
            if(toSetList.size()>0){
                log.info("设置Item状态开始");
                List<ContentItem> contentItemList = contentItemRepository.findByItemIdList(toSetList);
                for(ContentItem ci: contentItemList){
                    ci.setUpdateTime(new Date());
                    ci.setStatus(itemStatusMap.get(ci.getItemId()));
                }
                contentItemRepository.saveAll(contentItemList);
                log.info("设置Item状态结束");
            }


            if(stat.equals(HisStatus.FAIL) || stat.equals(HisStatus.SUCCESS)){
                contentHistory.setStatus(stat);
                contentHistory.setUpdateTime(new Date());
                contentHistory.setMessage(desc);
                contentHistoryRepository.save(contentHistory);
                log.info("VendorRobin轮询结果为[{}]", desc);
                return;
            }
            taskMsg.setDelay(robinRate);//TODO
            taskMsg.setRoundRobinNum(taskMsg.getRoundRobinNum() + 1);
            producer.sendDelayMsg(taskMsg);

        }catch (Exception e){
            log.error("任务轮询异常[{}]", e);
            taskMsg.setDelay(limitRetryRate);
            taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
            producer.sendDelayMsg(taskMsg);
        }
        log.info("contentVendorRobin结束[{}]", requestId);
    }

    @Override
    public void clearErrorTask(TaskMsg taskMsg) throws ContentException {
        String requestId = taskMsg.getTaskId();
        //ContentHistory contentHistory = contentHistoryRepository.findByRequestId(requestId);
        clearsubTask(requestId);
    }

    private void clearsubTask(String taskId) throws ContentException {
        log.info("清理请求任务[{}]的子任务开始", taskId);

        List<ContentItem> contentItemList = contentItemRepository.findByRequestId(taskId);

        for(ContentItem it: contentItemList){
            clearVendorTask(it.getItemId());
            contentItemRepository.deleteById(it.getId());
        }
        log.info("清理请求任务[{}]的子任务结束");
    }

    private void clearVendorTask(String taskId) throws ContentException {
        log.info("清理item任务[{}]的厂商任务开始", taskId);
        List<VendorContentTask> vlist = vendorTaskRepository.findByItemId(taskId);
        for(VendorContentTask vl: vlist  ){
            vendorTaskRepository.deleteById(vl.getId());
        }
        log.info("清理item任务[{}]的厂商任务结束");
    }

/*
    @Override
    public void saveContentItem(TaskMsg taskMsg) throws ContentException{
        String requestId = taskMsg.getTaskId();
        ContentHistory contentHistory = this.findHisttoryByRequestId(requestId);

        if(contentHistory == null){
            log.error("用户任务不存在,丢弃消息");
            return;
        }

        if(taskMsg.getRetryNum() > limitRetry){
            contentHistory.setStatus(HisStatus.FAIL);
            contentHistory.setUpdateTime(new Date());
            contentHistory.setMessage("拆分任务及其子任务失败超限");
            contentHistoryRepository.save(contentHistory);
            log.error("超出重试次数,设置任务失败并丢弃消息");
            return;
        }

        List<ContentItem> checkItems = contentItemRepository.findByRequestId(requestId);
        RefreshType type = contentHistory.getType();
        List<ContentItem> contentItemList = new ArrayList<>();

        if(checkItems == null || checkItems.size() != contentHistory.getContentNumber()) {
            log.info("任务:{}入库不全{}，补全数据", requestId, checkItems);

            List<String> urls = JSONArray.parseArray(contentHistory.getContent(), String.class);
            List<String> lostUrls = new ArrayList<>();

            if(checkItems != null && checkItems.size() != 0){
                Map<String, ContentItem> existUrlMap = checkItems.stream().collect(Collectors.toMap(i->i.getContent(), i->i));
                for(String s: urls){
                    if(!existUrlMap.containsKey(s)){
                        log.info("添加缺失url:{}",s);
                        lostUrls.add(s);
                    }
                }
                contentItemList.addAll(checkItems);
            }else{
                lostUrls.addAll(urls);
            }

            Map<String, Set<String>> domainVendorsMap = null;

            log.info("开始查询厂商");
            try {
                domainVendorsMap = getDomainVendorsMapNew(lostUrls);
                if(domainVendorsMap == null){
                    log.error("获取厂商失败");
                    taskMsg.setDelay(30000L);
                    taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
                    producer.sendTaskMsg(taskMsg);
                    return;
                }
            } catch (Exception e) {//TODO 缓存线路
                log.error("获取厂商失败");
                taskMsg.setDelay(30000L);
                taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
                producer.sendTaskMsg(taskMsg);
                return;
            }
            log.info("获取厂商成功");

            URL domainUrl = null;
            for (String u : lostUrls) {//
                String itemId = UUID.randomUUID().toString().replaceAll("-", "");
                ContentItem item = new ContentItem();
                item.setRequestId(requestId);
                item.setItemId(itemId);
                item.setType(type);
                item.setContent(u);
                item.setCreateTime(new Date());
                item.setStatus(HisStatus.WAIT);

                try {
                    domainUrl = new URL(u);
                } catch (Exception e) {
                    log.error("parse url:{} failed, err:{}", u, e.getMessage());
                    contentHistory.setStatus(HisStatus.FAIL);
                    contentHistory.setUpdateTime(new Date());
                    contentHistory.setMessage("内容url解析失败");
                    contentHistoryRepository.save(contentHistory);
                    log.error("内容url解析失败,设置任务失败并丢弃消息");
                    return;
                }
                String host = domainUrl.getHost();

                item.setVendor(JSONObject.toJSONString(domainVendorsMap.get(host)));

                contentItemList.add(item);
            }
            log.info("队列成功");

            //校验入库正确
            checkItems.clear();
            checkItems = contentItemRepository.saveAll(contentItemList);
            contentItemRepository.flush();//确保入库
            log.info("插入成功");
            if (checkItems == null || checkItems.size() != contentItemList.size()) {
                //放回MQ
                log.info("任务:{}, 拆分任务校验不正确", requestId);
                taskMsg.setDelay(30000L);
                taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
                producer.sendDelayMsg(taskMsg);
                return;
            }else{
                log.info("任务:{}, 拆分任务校验正确", requestId);
            }
        }else {
            log.info("任务:{}, 拆分任务入库已完整", requestId);
        }

        int totalTaskSize = checkItems.stream().collect(Collectors.summingInt(i -> JSONArray.parseArray(i.getVendor(),String.class).size() ));
        log.info("需要生成厂商任务数量:[{}]",totalTaskSize);

        List<String> itemIdList = new ArrayList<>();

        List<VendorContentTask> lostVendorContentTask = new ArrayList<>();

        for(ContentItem it : checkItems){

            if(it.getStatus().equals(HisStatus.FAIL)){
                it.setStatus(HisStatus.WAIT);
                it.setUpdateTime(new Date());
                contentItemRepository.saveAndFlush(it);
            }
            itemIdList.add(it.getItemId());
            int vendorNum = JSONArray.parseArray(it.getVendor(), String.class).size();
            List<String> lostVendors = new ArrayList<>();

            if(vendorNum == 0){
                log.error("任务厂商列表为空");
                //TODO
            }else{
                List<VendorContentTask> existVendorContentTask = vendorTaskRepository.findByItemId(it.getItemId());

                if(existVendorContentTask.size() == JSONArray.parseArray(it.getVendor(), String.class).size() ){
                    continue;

                }else if(existVendorContentTask.size() ==0){
                    lostVendors.addAll(JSONArray.parseArray(it.getVendor(), String.class));
                }else{
                    Map<String, VendorContentTask> vendorContentTaskMap = existVendorContentTask.stream().collect(Collectors.toMap(i->i.getVendor(), i->i));
                    for(String s: JSONArray.parseArray(it.getVendor(), String.class)){
                        if(!vendorContentTaskMap.containsKey(s)){
                            lostVendors.add(s);
                        }
                    }
                }
                log.info("生成任务厂商{}", lostVendors);
                for(String s:lostVendors){
                    VendorContentTask vendorTask = new VendorContentTask();
                    String taskId = UUID.randomUUID().toString().replaceAll("-", "");
                    vendorTask.setItemId(it.getItemId());
                    vendorTask.setTaskId(taskId);
                    vendorTask.setVendor(s);
                    vendorTask.setType(type);
                    vendorTask.setContent(it.getContent());
                    vendorTask.setVersion(0);
                    vendorTask.setCreateTime(new Date());
                    vendorTask.setStatus(TaskStatus.WAIT);
                    lostVendorContentTask.add(vendorTask);
                }
            }
        }
        if(lostVendorContentTask.size()>0) {
            vendorTaskRepository.saveAll(lostVendorContentTask);
            vendorTaskRepository.flush();
        }

        List<VendorContentTask> allVendorContentTask = vendorTaskRepository.findByItemIdList(itemIdList);
        int existSize = allVendorContentTask.size();
        if(existSize != totalTaskSize){
            log.error("厂商任务已入库数量{}, 需要{}", existSize, totalTaskSize);
            taskMsg.setDelay(30000L);
            taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
            producer.sendDelayMsg(taskMsg);
        }else{
            log.info("厂商任务已入库数量{},并发送消息", existSize);

            for(VendorContentTask it:allVendorContentTask) {
                if(it.getStatus().equals(TaskStatus.FAIL)) {
                    it.setStatus(TaskStatus.WAIT);
                }
                if(it.getStatus().equals(TaskStatus.WAIT)){
                    if(it.getVersion() != 0){
                        it.setVersion(0);
                        it.setUpdateTime(new Date());
                        vendorTaskRepository.saveAndFlush(it);
                    }
                    TaskMsg vendorTaskMsg = new TaskMsg();
                    vendorTaskMsg.setTaskId(it.getTaskId());
                    vendorTaskMsg.setVersion(0);
                    vendorTaskMsg.setOperation(TaskOperationEnum.getVendorOperation(it.getVendor()));
                    taskService.pushTaskMsg(vendorTaskMsg);
                }
            }
        }
    }
*/

    @Override
    public ApiReceipt setUserContentNumber(ContentLimitDTO command) {
        StringBuilder redisKey = new StringBuilder("contentNumber_").append(command.getSpCode());
        //默认设置为本次修改时间
        command.setLastModify(new Date().getTime());

        String redisValue = JSON.toJSONString(command);

        redisService.set(redisKey.toString(), redisValue, timeout, TimeUnit.DAYS);

        return ApiReceipt.ok();
    }


    /*
    @Override
    public ApiReceipt getUserContentNumber(String spCode) throws IOException {
        UserLimit userLimit = userLimitRepository.findByUserId(spCode);
        StringBuilder redisKey = new StringBuilder("contentNumber_").append(spCode);

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
        keys.add(redisKey.toString());

        List<String> args  = new ArrayList<>();
        args.add(String.valueOf(System.currentTimeMillis()));
        args.add(contentLimit.toJSONString());
        args.add(String.valueOf(todayTimeStamp()));
        // null 失败
        String result = luaScriptService.executeUserLimitScript(keys,args);
        if(null == result){
            //anubisNotifyService.emailNotifyApiException(new AnubisNotifyExceptionRequest("anubis-content",type.name(),new StringBuilder("spCode:").append(spCode).toString(),new ApiReceipt().getRequestId(),"超过每日数量上限"));
            //throw new ContentException("0x004012");
            return ApiReceipt.error();
        }

        return ApiReceipt.ok(JSONObject.parse(result));
    }*/


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
            UserLimit userLimit = userLimitRepository.findByUserId(spCode);
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


    @Override
    public  ContentHistory findHisttoryByRequestId(String id)throws ContentException{

        return contentHistoryRepository.findByRequestId(id);
    }

    
    private void checkDomainStatus(List<String> data, boolean adminFlag, GateWayHeaderDTO dto) throws ContentException {
        //判断当前域名状态，查当前用户的所有域名
        List<Domain> domains = new ArrayList<>();
        //根据domain来过滤
        List<String> domainParam = new ArrayList<>();
        for (String url : data) {
            String host = null;
            try {
                host = (new URL(url)).getHost();
            } catch (MalformedURLException e) {
                log.error("无效的URL [{}]",url);
                throw new ContentException("0x004008", String.format("无效的URL:[%s]，禁止操作", url));
            }
            domainParam.add(host);
        }

        if (adminFlag) {
            //管理员查全部域名
            domains = domainRepository.findByDomainIn(domainParam);
        } else {
            domains = domainRepository.findByUserCodeAndDomainIn(dto.getSpcode(), domainParam);
        }
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
    }
    
    private void checkUserLimit(boolean adminFlag,RefreshType type,String spCode,int size) throws ContentException {
        if(adminFlag) return;
        //

        UserLimit userLimit = userLimitRepository.findByUserId(spCode);
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
            anubisNotifyService.emailNotifyApiException(new AnubisNotifyExceptionRequest("anubis-content",type.name(),new StringBuilder("spCode:").append(spCode).toString(),new ApiReceipt().getRequestId(),"超过每日数量上限"));
            throw new ContentException("0x004012");
        }else if(-1 == result){
            anubisNotifyService.emailNotifyApiException(new AnubisNotifyExceptionRequest("anubis-content",type.name(),new StringBuilder("spCode:").append(spCode).toString(),new ApiReceipt().getRequestId(),"刷新预热lua计数脚本执行异常"));
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

    /*
    public Map<String, Set<String>> getDomainVendorsMap(List<String> contents) throws Exception {
        Map<String, List<String>> domainUrlsMap = new HashMap<String, List<String>>();
        URL domainUrl = null;

        for (String url : contents) {
            try {
                domainUrl = new URL(url);
            } catch (Exception e) {
                log.error("parse url:{} failed, err:{}", url, e.getMessage());
                throw RestfulException.builder().code(ErrEnum.ErrInternal.getCode()).message(e.getMessage()).build();
            }
            String host = domainUrl.getHost();
            List<String> domainUrls = domainUrlsMap.get(host);
            if (domainUrls == null) {
                domainUrls = new ArrayList<String>();
            }
            domainUrls.add(url);
            domainUrlsMap.put(host, domainUrls);
        }

        // 获取域名信息
        List<String> domains = new ArrayList<String>(domainUrlsMap.keySet());
        log.info("domains:{}", domains);
        List<Domain> domainInfos = this.domainRepository.findAllByDomainInAndStatusNot(domains, "DELETE");
        if (domainInfos.size() != domains.size()) {
            log.error("cannot find all domains info from db");
            //throw RestfulException.ErrNoSuchDomain;
        }
        List<DomainVendor> domianVendors = new ArrayList<>();
        List<DomainVendor> tempDomianVendors = this.domainVendorRepository.findByDomainList(domains);
        List<Domain> expireDomainInfos = new ArrayList<>();
        for(DomainVendor d : tempDomianVendors){
            if( new Date().getTime() -  d.getUpdateTime().getTime() < domianVendorExpire){
                domianVendors.add(d);//未过期
            }
        }

        if(domianVendors.size()>0){
            Map<String, DomainVendor> mapDV = domianVendors.stream().collect(Collectors.toMap(i->i.getDomain(), i->i));
            for(Domain d : domainInfos){
                if(!mapDV.containsKey(d.getDomain())){
                    expireDomainInfos.add(d);
                }
            }
        }else{
            expireDomainInfos.addAll(domainInfos);
        }

        // 获取域名所在的厂商
        Set<String> lineIds = new HashSet<String>();
        expireDomainInfos.forEach(d -> {
            if (!StringUtils.isEmpty(d.getLineId())) {
                lineIds.add(d.getLineId());
            } else {//TODO 切覆盖
                List<String> expectedVendor = getExpectedBaseLine(d.getExpectedVendor());
                lineIds.addAll(expectedVendor);
            }
        });



        List<LineResponse.LineDetail> lineInfos;
        if(lineIds.size() > 0) {
            lineInfos = this.lineService.getLineByIds(new ArrayList<String>(lineIds), true);
            if (lineInfos.size() != lineIds.size()) {
                log.error("cannot find all lines info lineIds:{}", lineIds);
                //throw RestfulException.ErrNoSuchDomain;
            }
        }else{
            lineInfos = new ArrayList<>();
        }
        Map<String, List<String>> lineIdVendorMap = new HashMap<String, List<String>>();
        lineInfos.forEach(l -> {
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
            lineIdVendorMap.put(l.getId(), lineVendors);
        });

        // domain: [vendor]
        tempDomianVendors.clear();
        Map<String, Set<String>> domainVendorsMap = new HashMap<String, Set<String>>();
        expireDomainInfos.forEach(d -> {
            Set<String> domainVendors = domainVendorsMap.get(d);
            if (domainVendors == null) {
                domainVendors = new HashSet<String>();
            }
            if (!StringUtils.isEmpty(d.getLineId())) {
                domainVendors.addAll(lineIdVendorMap.get(d.getLineId()));
            } else {
                List<String> expectedVendor = getExpectedBaseLine(d.getExpectedVendor());
                for (String l : expectedVendor) {
                    domainVendors.addAll(lineIdVendorMap.get(l));
                }
            }
            domainVendorsMap.put(d.getDomain(), domainVendors);

            List<String> vendor = new ArrayList<String>(domainVendors);
            DomainVendor dv = new DomainVendor();
            dv.setDomain(d.getDomain());
            dv.setVendors(JSONObject.toJSONString(vendor));
            dv.setUpdateTime(new Date());
            tempDomianVendors.add(dv);
        });
        if(tempDomianVendors != null && tempDomianVendors.size() > 0){
            domainVendorRepository.saveAll(tempDomianVendors);
        }

        for(DomainVendor d :domianVendors){
            domainVendorsMap.put(d.getDomain(), new HashSet<String>(JSONArray.parseArray(d.getVendors(),String.class)));
        }


        log.info("domain-vendors:{}", domainVendorsMap);

        return domainVendorsMap;


        ContentVendorDTO contentReq = new ContentVendorDTO();

        List<ContentVendorDTO.UrlVendor> urlVendors = new ArrayList<ContentVendorDTO.UrlVendor>();
        for( String domain : domainUrlsMap.keySet()) {
            ContentVendorDTO.UrlVendor item = new ContentVendorDTO.UrlVendor();
            item.setUrls(domainUrlsMap.get(domain));
            item.setVendors(new ArrayList<String>(domainVendorsMap.get(domain)));
            urlVendors.add(item);
        }

        contentReq.setTaskId(contentMessage.getTaskId());
        contentReq.setType(contentMessage.getType().name());
        contentReq.setUrlVendors(urlVendors);

        log.info("contentReq->:{}", contentReq );

        return contentReq;

        // vendor: [url]
        Map<String, List<String>> vendorUrlsMap = new HashMap<String, List<String>>();
        domains.forEach(d -> {
            // 域名所在的厂商
            Set<String> dVendors = domainVendorsMap.get(d);
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

    }
    */


    public Map<String, Set<String>> getDomainVendorsMapNew(List<String> contents) throws Exception{
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

        // 获取域名信息
        List<String> domains = new ArrayList<String>(domainUrlsMap.keySet());
        log.info("domains:{}", Utils.objectToString(domains));
        List<Domain> domainInfos = this.domainRepository.findAllByDomainInAndStatusNot(domains, "DELETE");
        if (domainInfos.size() != domains.size()) {
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

        List<LineVendor> vendors = this.lineToVendorRepository.findByLineIdList(new ArrayList<String>(lineIds));
        Map<String, LineVendor> lineIdLineVendorMap = vendors.stream().collect(Collectors.toMap(i->i.getLineId(), i->i));
        Map<String, List<String>> lineIdVendorMap = new HashMap<String, List<String>>();
        for(LineVendor lv :vendors){
            lineIdVendorMap.put(lv.getLineId(), JSONArray.parseArray(lv.getVendors(), String.class));
        }
        if(vendors.size() != lineIds.size()){
            List<String> lostLineVendor = new ArrayList<>();
            for(String s : lineIds){
                if(!lineIdLineVendorMap.containsKey(s)){
                    lostLineVendor.add(s);
                }
            }
            if(lostLineVendor.size()>0) {
                List<LineResponse.LineDetail> lineInfos = this.lineService.getLineByIds(new ArrayList<String>(lostLineVendor), true);
                if(lineInfos.size() != lostLineVendor.size()){
                    log.error("获取Line数量不全");
                    flag = false;
                }
                for(LineResponse.LineDetail l: lineInfos){
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
                    if(lineVendors.size()>0) {//不为空时才入库
                        lineIdVendorMap.put(l.getId(), lineVendors);
                        LineVendor LineVendorAdd = new LineVendor();
                        LineVendorAdd.setLineId(l.getId());
                        LineVendorAdd.setVendors(JSONObject.toJSONString(lineVendors));
                        lineToVendorRepository.save(LineVendorAdd);
                    }else{
                        log.error("厂商数量为空");
                        flag = false;
                    }
                };
            }
        }

        if(!flag){
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
        });
        log.info("domain-vendors:{}", Utils.objectToString(domainVendorsMap));

        return domainVendorsMap;
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
    public ApiReceipt test() throws ContentException {
        
        List<String> domain = new ArrayList<String>();
        domain.add("test0317.qbox.net");
        List<Domain> re =  domainRepository.findAllByDomainInAndStatusNot(domain, "DELTE");
        
        log.info("re->{}", re);


        TaskMsg msg = new TaskMsg();

        msg.setDelay(5000L);
        producer.sendDelayMsg(msg);


        notifyAlarmService.sendVendorAlarm("七牛状态不可用");

        Boolean bl = rabbitListenerConfig.stop(Constants.CONTENT_VENDOR_ALIYUN);

        if(bl){
            log.info("关闭队列成功");
            msg.setOperation(TaskOperationEnum.content_aliyun);
            producer.sendTaskMsg(msg);
            try{
                TimeUnit.SECONDS.sleep(10);
            }catch (Exception e){
                log.info("暂停异常");
            }
        }else{
            log.info("关闭队列失败");
        }
        bl = rabbitListenerConfig.start(Constants.CONTENT_VENDOR_ALIYUN);
        if(bl){
            log.info("启动队列成功");
            msg.setOperation(TaskOperationEnum.content_aliyun);
        }else{
            log.info("启动队列失败");
        }
        
        return null;
    }

    @Override
    public ApiReceipt setUserDefaultContentNumber(ContentDefaultNumDTO command) {
        UserLimit userLimit = userLimitRepository.findByUserId(command.getSpCode());
        if(userLimit == null){
            userLimit = new UserLimit();
        }
        userLimit.setDefaultUrlLimit(command.getUrlRefreshNumber());
        userLimit.setDefaultDirLimit(command.getDirRefreshNumber());
        userLimit.setDefaultPreloadLimit(command.getUrlPreloadNumber());
        userLimit.setUpdateTime(new Date());
        userLimitRepository.save(userLimit);
        return ApiReceipt.ok();
    }

    @Override
    public ApiReceipt getUserDefaultContentNumber(String spCode) throws IOException {
        UserLimit userLimit = userLimitRepository.findByUserId(spCode);
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
 	        }, rl, 2, TimeUnit.SECONDS).exit();
    }


}
