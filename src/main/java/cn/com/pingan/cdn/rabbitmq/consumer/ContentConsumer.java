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
import cn.com.pingan.cdn.service.ContentService;
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
    private ContentService contentService;



    /******************************************拆分原始任务******************************************/
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM, concurrency= "${mq.split.concurrency:20-50}")
    public void receiveItem(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            contentService.saveVendorTask(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItem Ack Fail ");
            }
        }
    }

    /******************************************拆分厂商任务轮询******************************************/
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_HISTORY_ROBIN)
    public void receiveVendorRobin(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            contentService.contentHistoryRobin(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendorRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.DEFAULT_ERROR)
    public void receiveDefaultError(Channel channel, Message message){
        try {
            String msg=new String(message.getBody());
            log.error("DefaultError rabbit mq receive a message{}", msg.toString());

        }catch (Exception e){
            log.info("receiveDefaultError", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveDefaultError Ack Fail ");
            }
        }
    }

}
