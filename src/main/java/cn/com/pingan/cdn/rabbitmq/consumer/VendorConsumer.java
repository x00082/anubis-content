package cn.com.pingan.cdn.rabbitmq.consumer;

import cn.com.pingan.cdn.client.AnubisNotifyService;
import cn.com.pingan.cdn.common.TaskOperationEnum;
import cn.com.pingan.cdn.config.RedisLuaScriptService;
import cn.com.pingan.cdn.model.mysql.VendorInfo;
import cn.com.pingan.cdn.rabbitmq.config.RabbitListenerConfig;
import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.service.TaskService;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Classname VendorConsumer
 * @Description TODO
 * @Date 2020/10/21 10:49
 * @Created by Luj
 */
@Component
@Slf4j
public class VendorConsumer {

    private String[] vendorQueues = Constants.VENDOR_QUEUE;

    @Bean
    public String[] vendorQueues() {
        return vendorQueues;
    }

    @Autowired
    RedisLuaScriptService luaScriptService;

    @Autowired
    TaskService taskService;

    @Autowired
    RabbitListenerConfig rabbitListenerConfig;

    @Autowired
    private AnubisNotifyService notifyService;


    void handlerMessage(TaskMsg taskMsg) throws Exception{
        VendorInfo vendorInfo = taskService.findVendorInfo(TaskOperationEnum.getVendorString(taskMsg.getOperation()));
        String vendorStatus = vendorInfo.getStatus();
        if ("down".equals(vendorStatus)) {
            taskMsg.setDelay(1 * 60 * 1000L);
            rabbitListenerConfig.stop(taskMsg.getOperation().name());//关闭监听
            taskService.pushTaskMsg(taskMsg);//放回队列
            //发送通知
        } else {
            taskService.handlerTask(taskMsg);
        }
    }
/*
    @RabbitListener(queues = {"#{vendorQueues}"})
    public void receive(Channel channel, Message message) {//TODO 拆分多个监听
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.info("消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }
*/

    @RabbitListener(queues = Constants.CONTENT_VENDOR_ALIYUN)
    public void receiveALIYUN(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ALIYUN robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN)
    public void receiveBAISHAN(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("BAISHAN robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveBAISHAN消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_CHINACHE)
    public void receiveCHINACHE(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("CHINACHE robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveCHINACHE 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_JDCLOUD)
    public void receiveJDCLOUD(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("JDCLOUD robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveJDCLOUD 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN)
    public void receiveKSYUN(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("KSYUN robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveKSYUN 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_NET)
    public void receiveNET(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("NET robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveNET 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU)
    public void receiveQINIU(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("QINIU robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveQINIU 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT)
    public void receiveTENCENT(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ALiYun robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveTENCENT 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVENUS(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VENUS robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveVENUS 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }

}
