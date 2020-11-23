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

    /*
    private String[] vendorQueues = Constants.VENDOR_QUEUE;

    @Bean
    public String[] vendorQueues() {
        return vendorQueues;
    }*/

    @Autowired
    RedisLuaScriptService luaScriptService;

    @Autowired
    TaskService taskService;

    @Autowired
    RabbitListenerConfig rabbitListenerConfig;

    @Autowired
    private AnubisNotifyService notifyService;


    void handlerMessage(TaskMsg taskMsg) throws Exception{
        /*
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
        */

        if( -1 != taskMsg.getOperation().toString().indexOf("_robin")){
            taskService.handlerRobinTask(taskMsg);
        }else if( -1 != taskMsg.getOperation().toString().indexOf("_common")){
            taskService.handlerCommonTask(taskMsg);
        }else if(taskMsg.getOperation().equals(TaskOperationEnum.content_vendor_success)){
            taskService.handlerSuccess(taskMsg);
        }else if(taskMsg.getOperation().equals(TaskOperationEnum.content_vendor_fail)){
            taskService.handlerFail(taskMsg);
        } else {
            taskService.handlerRequestTask(taskMsg);
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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_ALIYUN_COMMON)
    public void receiveALiYun(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ALiYun rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveALiYun消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveALiYun Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_ALIYUN_URL)
    public void receiveALiYunUrl(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ALiYunUrl rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveALiYunUrl消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveALiYunUrl Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_ALIYUN_DIR)
    public void receiveALiYunDir(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ALiYunDir rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveALiYunDir消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveALiYunDir Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_ALIYUN_PREHEAT)
    public void receiveALiYunPreheat(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ALiYunPreheat rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveALiYunPreheat消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveALiYunPreheat Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_ALIYUN_ROBIN)
    public void receiveALiYunRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ALiYunRobin rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveALiYunRobin消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveALiYunRobin Ack Fail ");
            }
        }
    }


    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_COMMON)
    public void receiveBaiShan(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("BaiShan rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveBaiShan消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveBaiShan Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_URL)
    public void receiveBaiShanUrl(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("BaiShanUrl rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveBaiShanUrl消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveBaiShanUrl Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_DIR)
    public void receiveBaiShanDIR(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("BaiShanDIR rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveBaiShanDIR消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveBaiShanDIR Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_PREHEAT)
    public void receiveBaiShanPreheat(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("BaiShanPreheat rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveBaiShanPreheat消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveBaiShanPreheat Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_ROBIN)
    public void receiveBaiShanRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("BaiShanRobin rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveBaiShanRobin消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveBaiShanRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_CHINACHE_COMMON)
    public void receiveChinaChe(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ChinaChe rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveChinaChe消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveChinaChe Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_CHINACHE_URL)
    public void receiveChinaCheUrl(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ChinaCheUrl rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveChinaCheUrl消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveChinaCheUrl Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_CHINACHE_DIR)
    public void receiveChinaCheDir(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ChinaCheDir rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveChinaCheDir消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveChinaCheDir Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_CHINACHE_PREHEAT)
    public void receiveChinaChePreheat(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ChinaChePreheat rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveChinaChePreheat消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveChinaChePreheat Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_CHINACHE_ROBIN)
    public void receiveChinaCheRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ChinaCheRobin rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveChinaCheRobin消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveChinaCheRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_JDCLOUD_COMMON)
    public void receiveJDCloud(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("JDCloud rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveJDCloud消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveJDCloud Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_JDCLOUD_URL)
    public void receiveJDCloudUrl(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("JDCloudUrl rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveJDCloudUrl消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveJDCloudUrl Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_JDCLOUD_DIR)
    public void receiveJDCloudDir(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("JDCloudDir rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveJDCloudDir消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveJDCloudDir Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_JDCLOUD_PREHEAT)
    public void receiveJDCloudPreheat(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("JDCloudPreheat rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveJDCloudPreheat消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveJDCloudPreheat Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_JDCLOUD_ROBIN)
    public void receiveJDCloudRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("JDCloudRobin rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveJDCloudRobin消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveJDCloudRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_COMMON)
    public void receiveKSYun(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("KSYun rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveKSYun消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveKSYun Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_URL)
    public void receiveKSYunUrl(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("KSYunUrl rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveKSYunUrl消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveKSYunUrl Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_DIR)
    public void receiveKSYunDir(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("KSYunDir rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveKSYunDir消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveKSYunDir Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_PREHEAT)
    public void receiveKSYunPreheat(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("KSYunPreheat rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveKSYunPreheat消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveKSYunPreheat Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_ROBIN)
    public void receiveKSYunRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("KSYunRobin rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveKSYunRobin消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveKSYunRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_NET_COMMON)
    public void receiveNet(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Net rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveNet消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveNet Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_NET_URL)
    public void receiveNetUrl(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("NetUrl rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveNetUrl消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveNetUrl Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_NET_DIR)
    public void receiveNetDir(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("NetDir rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveNetDir消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveNetDir Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_NET_PREHEAT)
    public void receiveNetPreheat(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("NetPreheat rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveNetPreheat消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveNetPreheat Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_NET_ROBIN)
    public void receiveNetRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("NetRobin rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveNetRobin消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveNetRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_COMMON)
    public void receiveQiNiu(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("QiNiu rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveQiNiu 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveQiNiu Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_URL)
    public void receiveQiNiuUrl(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("receiveQiNiuUrl rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);


        } catch (Exception e) {
            log.error("receiveQiNiuUrl 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveQiNiuUrl Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_DIR)
    public void receiveQiNiuDir(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("receiveQiNiuDir rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveQiNiuDir 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveQiNiuDir Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_PREHEAT)
    public void receiveQiNiuPreheat(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("receiveQiNiuPreheat rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveQiNiuPreheat 消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveQiNiuPreheat Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_ROBIN)
    public void receiveQiNiuRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("QiNiuRobin rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveQiNiuRobin消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveQiNiuRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT_COMMON)
    public void receiveTenCent(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("TenCent rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveTenCent消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveTenCent Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT_URL)
    public void receiveTenCentUrl(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("TenCent rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveTenCent消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveTenCent Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT_DIR)
    public void receiveTenCentDir(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("TenCent rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveTenCent消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveTenCent Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT_PREHEAT)
    public void receiveTenCentPreheat(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("TenCent rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveTenCent消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveTenCent Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT_ROBIN)
    public void receiveTenCentRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("TenCentRobin rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveTenCentRobin消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveTenCentRobin Ack Fail ");
            }
        }
    }

    /******************************************自建请求******************************************/
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_COMMON)
    public void receiveVenus1(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveVenus消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveVenus Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_URL)
    public void receiveVenusUrl(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusUrl rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveVenusUrl消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveVenusUrl Ack Fail ");
            }
        }
    }
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_DIR)
    public void receiveVenusDir(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusDir rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveVenusDir消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveVenusDir Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_PREHEAT)
    public void receiveVenusPrheat(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusPrheat rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveVenusPrheat消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveVenusPrheat Ack Fail ");
            }
        }
    }

    /******************************************自建轮询******************************************/
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin1(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        } catch (Exception e) {
            log.error("receiveVenusRobin消息处理异常", e);
        } finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("receiveVenusRobin Ack Fail ");
            }
        }
    }




    /******************************************成功******************************************/
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_SUCCESS, concurrency = "1")
    public void receiveSuccess(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Success rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveSuccess Ack Fail ");
            }
        }
    }

    /******************************************失败******************************************/
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_FAIL, concurrency = "1")
    public void receiveFail(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Fail rabbit mq receive a message{}", msg.toString());
            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            handlerMessage(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveFail Ack Fail ");
            }
        }
    }

}
