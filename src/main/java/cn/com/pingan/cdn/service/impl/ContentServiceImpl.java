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
import cn.com.pingan.cdn.exception.RestfulException;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.model.mysql.ContentHistory;
import cn.com.pingan.cdn.model.mysql.ContentItem;
import cn.com.pingan.cdn.model.mysql.DomainVendor;
import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import cn.com.pingan.cdn.model.pgsql.Domain;
import cn.com.pingan.cdn.rabbitmq.config.RabbitListenerConfig;
import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.rabbitmq.producer.Producer;
import cn.com.pingan.cdn.repository.mysql.ContentHistoryRepository;
import cn.com.pingan.cdn.repository.mysql.ContentItemRepository;
import cn.com.pingan.cdn.repository.mysql.DomainVendorRepository;
import cn.com.pingan.cdn.repository.mysql.VendorTaskRepository;
import cn.com.pingan.cdn.repository.pgsql.DomainRepository;
import cn.com.pingan.cdn.service.ContentService;
import cn.com.pingan.cdn.service.LineService;
import cn.com.pingan.cdn.service.NotifyAlarmService;
import cn.com.pingan.cdn.service.TaskService;
import cn.com.pingan.cdn.utils.Utils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private DomainRepository domainRepository;

    @Autowired
    private ContentItemRepository contentItemRepository;

    @Autowired
    private DomainVendorRepository domainVendorRepository;

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

    @Value("${task.retry.num:3}")
    private int limitRetry;
    
    
    @Autowired
    RedisLuaScriptService luaScriptService;

    @Autowired
    ContentLimitJsonConfig contentLimitJsonConfig;

    //test
    @Autowired
    NotifyAlarmService notifyAlarmService;
    @Autowired
    RabbitListenerConfig rabbitListenerConfig;
    

    @Override
    public ApiReceipt saveContent(GateWayHeaderDTO dto, List<String> data, RefreshType type) throws ContentException {
        if (null == data || data.size() == 0) {
            throw new ContentException("0x004008", "内容为空,请正确填写!");
        }

        //校验data 的数据格式 去重
        List <String> tmpData = new ArrayList<>();
        HashSet h = new HashSet();

        for (String url :data) {
            if(h.add(url.toLowerCase())){
                tmpData.add(url);
            }
            if(url.toLowerCase().startsWith("http://") ||url.toLowerCase().startsWith("https://")){

                if (RefreshType.url == type && url.toLowerCase().endsWith("/")) {
                    throw new ContentException("0x004015");
                }
                if (RefreshType.preheat == type && url.toLowerCase().endsWith("/")) {
                    throw new ContentException("0x004016");
                }
                if (RefreshType.dir == type && !url.toLowerCase().endsWith("/")) {
                    throw new ContentException("0x004017");
                }
            }else {
                throw new ContentException("0x004014");
            }

        }

        data.clear();
        data.addAll(tmpData);
        
        boolean adminFlag = "true".equals(dto.getIsAdmin());
        //检查域名状态
        checkDomainStatus(data, adminFlag, dto);
        
        //计数
        checkUserLimit(adminFlag,type,dto.getSpcode(),data.size());
        
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
        
        contentHistoryRepository.saveAndFlush(contentHistory);
        log.info("contentHistory id:{}", contentHistory.getId());


        TaskMsg historyTaskMsg = new TaskMsg();
        historyTaskMsg.setTaskId(taskId);
        historyTaskMsg.setVersion(0);
        historyTaskMsg.setOperation(TaskOperationEnum.content_item);
        producer.sendTaskMsg(historyTaskMsg);

        log.info("sendTaskMsg:{} to MQ",historyTaskMsg);
        return ApiReceipt.ok().data(taskId);
        
    }

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
            try {
                domainVendorsMap = getDomainVendorsMap(lostUrls);
            } catch (Exception e) {
                //TODO
                taskMsg.setDelay(30000L);
                taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
                producer.sendTaskMsg(taskMsg);
            }

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
                    throw RestfulException.builder().code(ErrEnum.ErrInternal.getCode()).message(e.getMessage()).build();
                }
                String host = domainUrl.getHost();

                item.setVendor(JSONObject.toJSONString(domainVendorsMap.get(host)));

                contentItemList.add(item);
            }

            //校验入库正确
            checkItems.clear();
            checkItems = contentItemRepository.saveAll(contentItemList);
            contentItemRepository.flush();//确保入库

            if (checkItems == null || checkItems.size() != contentItemList.size()) {
                //放回MQ
                log.info("任务:{}, 拆分任务校验不正确", requestId);
                taskMsg.setDelay(30000L);
                taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
                producer.sendDelayMsg(taskMsg);
            }else{
                log.info("任务:{}, 拆分任务校验正确", requestId);
            }
        }else {
            log.info("任务:{}, 拆分任务入库已完整", requestId);
        }

        /*
        List<String> items = checkItems.stream().map(i->i.getItemId()).collect(Collectors.toList());

        List<VendorContentTask> vendorContentTasks = vendorTaskRepository.findByItemIdList(items);
*/
        int totalTaskSize = checkItems.stream().collect(Collectors.summingInt(i -> JSONArray.parseArray(i.getVendor(),String.class).size() ));
        log.info("总的厂商任务数量:[{}]",totalTaskSize);

        List<VendorContentTask> succTasks = new ArrayList<>();
        JxGaga gg = JxGaga.of(Executors.newCachedThreadPool(),checkItems.size());

        for(ContentItem it : checkItems){

            gg.work(() -> {
                List<VendorContentTask> tempVendorContentTask = vendorTaskRepository.findByItemId(it.getItemId());
                if(tempVendorContentTask.size() == JSONArray.parseArray(it.getVendor(), String.class).size() ){
                    return tempVendorContentTask;

                }else{
                    List<String> lostVendors = new ArrayList<>();

                    if(tempVendorContentTask.size() > 0){
                        Map<String, VendorContentTask> vendorContentTaskMap = tempVendorContentTask.stream().collect(Collectors.toMap(i->i.getVendor(), i->i));
                        for(String s: JSONArray.parseArray(it.getVendor(), String.class)){
                            if(!vendorContentTaskMap.containsKey(s)){
                                lostVendors.add(s);
                            }
                        }
                    }else{
                        lostVendors.addAll(JSONArray.parseArray(it.getVendor(), String.class));
                    }
                    List<VendorContentTask> vendorContentTask = new ArrayList<>();
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
                        vendorContentTask.add(vendorTask);
                    }
                    tempVendorContentTask.clear();
                    tempVendorContentTask = vendorTaskRepository.saveAll(vendorContentTask);
                    if(tempVendorContentTask.size() == lostVendors.size()){
                        log.info("拆分任务{}的厂商任务入库成功",it.getItemId());
                        return tempVendorContentTask;
                    }else{
                        log.info("拆分任务{}的厂商任务入库失败",it.getItemId());
                        throw new Exception();
                    }
                }

            }, p -> {succTasks.addAll(p);}, q->{log.error("入库失败");
            } );
        }

        gg.merge(i -> {
            log.info("完成数量:[{}], 需要入库的总数为[{}]",i.size(), totalTaskSize);
                    if(i.size() == totalTaskSize) {
                        i.forEach(j -> {
                            log.info("厂商任务入库成功{}", j);
                            vendorTaskRepository.saveAndFlush(j);
                            TaskMsg vendorTaskMsg = new TaskMsg();
                            vendorTaskMsg.setTaskId(j.getTaskId());
                            vendorTaskMsg.setVersion(0);
                            vendorTaskMsg.setOperation(TaskOperationEnum.getVendorOperation(j.getVendor()));
                            taskService.pushTaskMsg(vendorTaskMsg);

                        });
                    }else{
                        taskMsg.setDelay(30000L);
                        taskMsg.setRetryNum(taskMsg.getRetryNum() + 1);
                        producer.sendDelayMsg(taskMsg);
                    }
        }, succTasks, 10, TimeUnit.SECONDS).exit();
    }


    @Override
    public ApiReceipt getUserContentNumber(String spCode) throws IOException {
        // TODO 自动生成的方法存根
        return null;
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
                throw new ContentException("0x004008", "域名无效，禁止操作");
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
                throw new ContentException("0x004008", "域名无效，禁止操作");
            }
        }
    }
    
    private void checkUserLimit(boolean adminFlag,RefreshType type,String spCode,int size) throws ContentException {
        if(adminFlag) return;
        //
        JSONObject contentLimit = contentLimitJsonConfig.getDefaultContentLimit();
        long lastModify = System.currentTimeMillis();
        if(contentLimit == null){
            contentLimit = new JSONObject();
            contentLimit.put("urlRefreshNumber",new Item(0, maxRefresh));
            contentLimit.put("dirRefreshNumber",new Item(0, maxDirRefresh));
            contentLimit.put("urlPreloadNumber",new Item(0, maxPreheat));

        }
        contentLimit.put("lastModify",lastModify);

        List<String> keys = new ArrayList<>();
        keys.add(contentPrefix + spCode);

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
            if( new Date().getTime() -  d.getUpdateTime().getTime() < 60000L){
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



        List<LineResponse.LineDetail> lineInfos = this.lineService.getLineByIds(new ArrayList<String>(lineIds), true);
        if (lineInfos.size() != lineIds.size()) {
            log.error("cannot find all lines info lineIds:{}", lineIds);
            //throw RestfulException.ErrNoSuchDomain;
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

        /*
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
        */
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
}
