package cn.com.pingan.cdn.rabbitmq.consumer;

import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.service.TaskService;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Classname VendorConsumer
 * @Description TODO
 * @Date 2020/10/21 10:49
 * @Created by Luj
 */
@Component
@Slf4j
public class DelayConsumer {

    @Autowired
    TaskService taskService;

    @RabbitListener(queues = Constants.CONTENT_DELAY_QUEUE)
    public void receive(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("robbit mq receive a delay message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            taskMsg.setDelay(0L);
            taskService.pushTaskMsg(taskMsg);//只做延时，消息交给具体队列处理

        }catch (Exception e){
            log.info("延时任务转发失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }
}
