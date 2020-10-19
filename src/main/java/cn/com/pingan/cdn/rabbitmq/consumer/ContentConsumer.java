/**   
 * @Project: anubis-content
 * @File: ContentConsumer.java 
 * @Package cn.com.pingan.cdn.rabbitmq.consumer 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月13日 下午2:55:00 
 */
package cn.com.pingan.cdn.rabbitmq.consumer;



import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;


import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.common.LineResponse;
import cn.com.pingan.cdn.model.pgsql.Domain;
import cn.com.pingan.cdn.repository.pgsql.DomainRepository;
import cn.com.pingan.cdn.request.content.ContentVendorDTO;
import cn.com.pingan.cdn.request.content.ContentVendorDTO.UrlVendor;
import cn.com.pingan.cdn.rabbitmq.message.HistoryMessage;
import cn.com.pingan.cdn.service.LineService;
import lombok.extern.slf4j.Slf4j;

/** 
 * @ClassName: ContentConsumer 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月13日 下午2:55:00 
 *  
 */
@Component
@Slf4j
public class ContentConsumer {

    @Autowired
    private DomainRepository domainRepo;
    
    @Autowired
    private LineService lineService;
    
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_QUEUE_NAE)
    public void receive(Channel channel, Message message){
        try {
            
            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("robbit mq receive a message{}", msg.toString());
            
            HistoryMessage contentMessage = JSONObject.toJavaObject(msgObj, HistoryMessage.class);
            log.info("转换对象{}", contentMessage);
            
            ContentVendorDTO contentVendorDTO = getContentVendorDTO(contentMessage);

        }catch (Exception e){
            log.info("发送刷新请求失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("ContentConsumer Ack Fail ");
            }
        }
    }
    
    public ContentVendorDTO getContentVendorDTO(HistoryMessage contentMessage) throws Exception {
        Map<String, List<String>> domainUrlsMap = new HashMap<String, List<String>>();
        URL domainUrl = null;
        
        List<String> urls = contentMessage.getUrls();
        
        for (String url : urls) {
            try {
                domainUrl = new URL(url);
            } catch (Exception e) {
                log.error("parse url:{} failed, err:{}", url, e.getMessage());
                //throw RestfulException.builder().code(ErrCodeEnum.ErrInternal.getCode()).message(e.getMessage()).build();
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
        List<Domain> domainInfos = this.domainRepo.findAllByDomainInAndStatusNot(domains, "DELETE");
        if (domainInfos.size() != domains.size()) {
            log.error("cannot find all domains info from db");
            //throw RestfulException.ErrNoSuchDomain;
        }
        // 获取域名所在的厂商
        Set<String> lineIds = new HashSet<String>();
        domainInfos.forEach(d -> {
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
        Map<String, Set<String>> domainVendorsMap = new HashMap<String, Set<String>>();
        domainInfos.forEach(d -> {
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
        });
        log.info("domain-vendors:{}", domainVendorsMap);
        
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
        /*
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
            return ContentConsumer.stringToArrayObject( expectedVendor, String.class);
        }catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }
    
    public static <T> ArrayList<T> stringToArrayObject(String v, Class<T> valueType) throws Exception {
        ArrayList<T> t = null;
        try {
            if (StringUtils.isEmpty(v)) {
                t = new ArrayList<T>();
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, valueType);
                t = objectMapper.readValue(v, javaType);
            }
        } catch (Exception e) {
            //throw e;
        }
        return t;
    }
    
}
