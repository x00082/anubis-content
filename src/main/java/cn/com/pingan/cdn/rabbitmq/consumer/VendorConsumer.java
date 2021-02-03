package cn.com.pingan.cdn.rabbitmq.consumer;

import cn.com.pingan.cdn.client.AnubisNotifyService;
import cn.com.pingan.cdn.common.TaskOperationEnum;

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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_COMMON, concurrency= "${mq.vendor.common.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_URL, concurrency= "${mq.vendor.request.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_DIR, concurrency= "${mq.vendor.request.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_PREHEAT, concurrency= "${mq.vendor.request.concurrency:20-50}")
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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_BAISHAN_ROBIN, concurrency= "${mq.vendor.robin.concurrency:20-50}")
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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_COMMON, concurrency= "${mq.vendor.common.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_URL, concurrency= "${mq.vendor.request.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_DIR, concurrency= "${mq.vendor.request.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_PREHEAT, concurrency= "${mq.vendor.request.concurrency:20-50}")
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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_KSYUN_ROBIN, concurrency= "${mq.vendor.robin.concurrency:20-50}")
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


    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_COMMON, concurrency= "${mq.vendor.common.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_URL, concurrency= "${mq.vendor.request.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_DIR, concurrency= "${mq.vendor.request.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_PREHEAT, concurrency= "${mq.vendor.request.concurrency:20-50}")
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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_QINIU_ROBIN, concurrency= "${mq.vendor.robin.concurrency:20-50}")
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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT_COMMON, concurrency= "${mq.vendor.common.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT_URL, concurrency= "${mq.vendor.request.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT_DIR, concurrency= "${mq.vendor.request.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT_PREHEAT, concurrency= "${mq.vendor.request.concurrency:20-50}")
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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_TENCENT_ROBIN, concurrency= "${mq.vendor.robin.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_COMMON, concurrency= "${mq.vendor.common.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_URL, concurrency= "${mq.vendor.request.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_DIR, concurrency= "${mq.vendor.request.concurrency:20-50}")
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

    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_PREHEAT, concurrency= "${mq.vendor.request.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_VENDOR_VENUS_ROBIN, concurrency= "${mq.vendor.robin.concurrency:20-50}")
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
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_SUCCESS, concurrency= "${mq.vendor.success.concurrency:1}")
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
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_FAIL, concurrency= "${mq.vendor.fail.concurrency:1}")
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
