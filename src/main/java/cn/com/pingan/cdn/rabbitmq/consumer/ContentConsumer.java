/**   
 * @Project: anubis-content
 * @File: ContentConsumer.java 
 * @Package cn.com.pingan.cdn.rabbitmq.consumer 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月13日 下午2:55:00 
 */
package cn.com.pingan.cdn.rabbitmq.consumer;


import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.repository.pgsql.DomainRepository;
import cn.com.pingan.cdn.service.ContentService;
import cn.com.pingan.cdn.service.LineService;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

    @Autowired
    private ContentService contentService;

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receive(Channel channel, Message message){
        try {
            
            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

        }catch (Exception e){
            log.info("拆分任务或生产厂商任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("ContentConsumer Ack Fail ");
            }
        }
    }

}
