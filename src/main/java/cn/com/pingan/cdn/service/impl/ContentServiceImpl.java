/**   
 * @Project: anubis-content
 * @File: ContentServiceImpl.java 
 * @Package cn.com.pingan.cdn.service.impl 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午9:44:14 
 */
package cn.com.pingan.cdn.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.common.ContentException;
import cn.com.pingan.cdn.common.RefreshType;
import cn.com.pingan.cdn.config.ContentLimitJsonConfig;
import cn.com.pingan.cdn.config.RedisContentCountLuaScriptService;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.model.mysql.ContentHistory;
import cn.com.pingan.cdn.model.mysql.ContentItem;
import cn.com.pingan.cdn.model.pgsql.Domain;
import cn.com.pingan.cdn.rabbitmq.message.HistoryMessage;
import cn.com.pingan.cdn.rabbitmq.producer.Producer;
import cn.com.pingan.cdn.repository.mysql.ContentHistoryRepository;
import cn.com.pingan.cdn.repository.mysql.ContentItemRepository;
import cn.com.pingan.cdn.repository.pgsql.DomainRepository;
import cn.com.pingan.cdn.service.ContentService;
import cn.com.pingan.cdn.common.Item;

import cn.com.pingan.cdn.common.HisStatus;
import lombok.extern.slf4j.Slf4j;

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
    private ContentItemRepository contentItemRepository;
    
    @Autowired
    private DomainRepository domainRepository;
    
    @Autowired
    Producer producer;
    
    
    private String contentPrefix = "contentNumber_";
    
    @Value("${day.max.refresh:1000}")
    private int maxRefresh;
    @Value("${day.max.preheat:1000}")
    private int maxPreheat;
    @Value("${day.max.dirRefresh:1000}")
    private int maxDirRefresh;
    
    
    @Autowired
    RedisContentCountLuaScriptService luaScriptService;

    @Autowired
    ContentLimitJsonConfig contentLimitJsonConfig;
    

    @Override
    public ApiReceipt saveContent(GateWayHeaderDTO dto, List<String> data, RefreshType type) throws ContentException {
        if (null == data || data.size() == 0) {
            throw new ContentException("0x004008", "内容为空,请正确填写!");
        }
        
        //判断当前用户的刷新次数和预取次数，超过限制则返回异常
        //检查是否超出每日上限
        
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
        //清空data
        data.clear();
        data.addAll(tmpData);//重新赋值
        
        
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

        HistoryMessage msg = new HistoryMessage();
        msg.setType(type);
        msg.setTaskId(taskId);
        msg.setUrls(data);
        producer.sendContent(msg);
        return ApiReceipt.ok().data(taskId);
        
    }

    @Override
    public ApiReceipt saveContentM(GateWayHeaderDTO dto, List<String> data, RefreshType type) throws ContentException {
        // TODO 自动生成的方法存根
        return null;
    }

    /*
    @Override
    ApiReceipt setUserContentNumber(ContentLimitDTO command);
    */
    

    @Override
    public ApiReceipt getUserContentNumber(String spCode) throws IOException {
        // TODO 自动生成的方法存根
        return null;
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
        int result = luaScriptService.executeScript(keys,args);
        if(-100 == result){
            //notifyService.emailNotifyApiException(new ApiExceptionNoticeRequest("anubis-base",type.name(),new StringBuilder("spCode:").append(spCode).toString(),new ApiReceipt().getRequestId(),"超过每日数量上限"));
            throw new ContentException("0x004012");
        }else if(-1 == result){
            //notifyService.emailNotifyApiException(new ApiExceptionNoticeRequest("anubis-base",type.name(),new StringBuilder("spCode:").append(spCode).toString(),new ApiReceipt().getRequestId(),"刷新预热lua计数脚本执行异常"));
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
    
    
    
    @Override
    public ApiReceipt test() throws ContentException {
        
        List<String> domain = new ArrayList<String>();
        domain.add("test0317.qbox.net");
        List<Domain> re =  domainRepository.findAllByDomainInAndStatusNot(domain, "DELTE");
        
        log.info("re->{}", re);
        
        return null;
    }
}
