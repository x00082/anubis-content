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
        taskService.handlerTask(taskMsg);
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
    public void receiveALiYun(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ALiYun rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_ALIYUN_ROBIN)
    public void receiveALiYunRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ALiYunRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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


    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN)
    public void receiveBaiShan(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("BaiShan rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_ROBIN)
    public void receiveBaiShanRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("BaiShanRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_CHINACHE)
    public void receiveChinaChe(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ChinaChe rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_CHINACHE_ROBIN)
    public void receiveChinaCheRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("ChinaCheRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_JDCLOUD)
    public void receiveJDCloud(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("JDCloud rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_JDCLOUD_ROBIN)
    public void receiveJDCloudRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("JDCloudRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN)
    public void receiveKSYun(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("KSYun rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_ROBIN)
    public void receiveKSYunRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("KSYunRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_NET)
    public void receiveNet(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Net rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_NET_ROBIN)
    public void receiveNetRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("NetRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU)
    public void receiveQiNiu(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("QiNiu rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_ROBIN)
    public void receiveQiNiuRobin(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("QiNiuRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT)
    public void receiveTenCent(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("TenCent rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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
            log.info("转换对象{}", taskMsg);

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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVenus1(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVenus2(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVenus3(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVenus4(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVenus5(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVenus6(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVenus7(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVenus8(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVenus9(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS)
    public void receiveVenus10(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("Venus rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    /******************************************自建轮询******************************************/
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin1(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin2(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    /*
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin3(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin4(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin5(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin6(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin7(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin8(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin9(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN)
    public void receiveVenusRobin10(Channel channel, Message message) {
        try {

            String msg = new String(message.getBody());
            JSONObject msgObj = JSONObject.parseObject(msg);

            log.info("VenusRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

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
    */

}
