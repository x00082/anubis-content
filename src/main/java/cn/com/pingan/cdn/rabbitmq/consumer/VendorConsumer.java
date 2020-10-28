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
    public String[] vendorQueues(){
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

    @RabbitListener(queues = {"#{vendorQueues}"})
    public void receive(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("robbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            VendorInfo vendorInfo = taskService.findVendorInfo(TaskOperationEnum.getVendorString(taskMsg.getOperation()));
            String vendorStatus = vendorInfo.getStatus();
            if("down".equals(vendorStatus)){
                taskMsg.setDelay(1 * 60 * 1000L);
                taskService.pushTaskMsg(taskMsg);//放回队列
                rabbitListenerConfig.stop(taskMsg.getOperation().name());//关闭监听
                //发送通知
            }else {

                if (taskMsg.getIsLimit()) {//查询时不需要限制
                    //QPS限制
                    int qps = vendorInfo.getTotalQps();
                    String redisKey = taskMsg.getOperation().toString();
                    List<String> keys = new ArrayList<>();
                    keys.add(redisKey);
                    List<String> args = new ArrayList<>();
                    args.add(String.valueOf(qps));
                    // 0-成功，-1执行异常，-100超限
                    int result = luaScriptService.executeQpsScript(keys, args);
                    if (-100 == result) {
                        log.warn("redis:{} Limit:{}", keys, qps);
                        taskMsg.setDelay(500L);
                        taskService.pushTaskMsg(taskMsg);
                    } else if (-1 == result) {
                        log.warn("redis:{} Limit:{} 执行异常", keys, qps);
                        taskMsg.setDelay(3000L);
                        taskService.pushTaskMsg(taskMsg);//3秒后重试
                    } else {
                        taskService.handlerTask(taskMsg);
                    }
                } else {
                    taskService.handlerTask(taskMsg);
                }
            }

        }catch (Exception e){
            log.info("消息处理异常", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("VendorConsumer Ack Fail ");
            }
        }
    }
}
