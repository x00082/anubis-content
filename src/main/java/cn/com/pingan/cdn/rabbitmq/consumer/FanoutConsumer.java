package cn.com.pingan.cdn.rabbitmq.consumer;

import cn.com.pingan.cdn.common.FanoutType;
import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.rabbitmq.message.FanoutMsg;
import cn.com.pingan.cdn.service.ContentService;
import cn.com.pingan.cdn.service.TaskService;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Classname FanoutConsumer
 * @Description TODO
 * @Date 2020/11/19 15:50
 * @Created by Luj
 */
@Slf4j
@Component
public class FanoutConsumer {

    @Autowired
    TaskService taskService;

    @Autowired
    private ContentService contentService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(), //注意这里不要定义队列名称,系统会随机产生
            exchange = @Exchange(value = Constants.CONTENT_FANOUT_EXCHANGE,type = ExchangeTypes.FANOUT))
    )
    public void process(Channel channel, Message message) {
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("FANOUT rabbit mq receive a message{}", msg.toString());

            FanoutMsg taskMsg = JSONObject.toJavaObject(msgObj, FanoutMsg.class);
            log.info("转换对象{}", taskMsg);
            if (taskMsg.getOperation().equals(FanoutType.fflush_vendor)) {
                taskService.fflushVendorInfoMap(taskMsg.getKey());
            }else if(taskMsg.getOperation().equals(FanoutType.fflush_domain_vendor)){
                contentService.fflushDomainVendor(taskMsg);
            }

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
}
