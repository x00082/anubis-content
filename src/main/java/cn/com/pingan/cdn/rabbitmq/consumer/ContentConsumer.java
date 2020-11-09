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



    /******************************************拆分原始任务******************************************/
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receiveItem1(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receiveItem2(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

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

    /*
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receiveItem3(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receiveItem4(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receiveItem5(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receiveItem6(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receiveItem7(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receiveItem8(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receiveItem9(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

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


    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM)
    public void receiveItem10(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Item rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentItem(taskMsg);

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
    */




    /******************************************拆分原始轮询******************************************/
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM_ROBIN)
    public void receiveItemRobin1(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("ItemRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentItemRobin(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItemRobin Ack Fail ");
            }
        }
    }


    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM_ROBIN)
    public void receiveItemRobin2(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("ItemRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentItemRobin(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItemRobin Ack Fail ");
            }
        }
    }

    /*
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM_ROBIN)
    public void receiveItemRobin3(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("ItemRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentItemRobin(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItemRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM_ROBIN)
    public void receiveItemRobin4(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("ItemRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentItemRobin(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItemRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM_ROBIN)
    public void receiveItemRobin5(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("ItemRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentItemRobin(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItemRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM_ROBIN)
    public void receiveItemRobin6(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("ItemRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentItemRobin(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItemRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM_ROBIN)
    public void receiveItemRobin7(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("ItemRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentItemRobin(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItemRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM_ROBIN)
    public void receiveItemRobin8(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("ItemRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentItemRobin(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItemRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM_ROBIN)
    public void receiveItemRobin9(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("ItemRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentItemRobin(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItemRobin Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_ITEM_ROBIN)
    public void receiveItemRobin10(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("ItemRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentItemRobin(taskMsg);

        }catch (Exception e){
            log.info("用户拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveItemRobin Ack Fail ");
            }
        }
    }
    */

    /******************************************拆分厂商任务******************************************/
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR)
    public void receiveVendor1(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Vendor rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentVendor(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendor Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR)
    public void receiveVendor2(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Vendor rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentVendor(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendor Ack Fail ");
            }
        }
    }

    /*
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR)
    public void receiveVendor3(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Vendor rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentVendor(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendor Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR)
    public void receiveVendor4(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Vendor rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentVendor(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendor Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR)
    public void receiveVendor5(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Vendor rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentVendor(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendor Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR)
    public void receiveVendor6(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Vendor rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentVendor(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendor Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR)
    public void receiveVendor7(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Vendor rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentVendor(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendor Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR)
    public void receiveVendor8(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Vendor rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentVendor(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendor Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR)
    public void receiveVendor9(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Vendor rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentVendor(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendor Ack Fail ");
            }
        }
    }

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR)
    public void receiveVendor10(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("Vendor rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.saveContentVendor(taskMsg);

        }catch (Exception e){
            log.info("厂商拆分任务失败", e);
        }finally {

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                log.error("receiveVendor Ack Fail ");
            }
        }
    }
    */


    /******************************************拆分厂商任务轮询******************************************/
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_ROBIN)
    public void receiveVendorRobin1(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentVendorRobin(taskMsg);

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


    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_ROBIN)
    public void receiveVendorRobin2(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentVendorRobin(taskMsg);

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

    /*
    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_ROBIN)
    public void receiveVendorRobin3(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentVendorRobin(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_ROBIN)
    public void receiveVendorRobin4(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentVendorRobin(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_ROBIN)
    public void receiveVendorRobin5(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentVendorRobin(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_ROBIN)
    public void receiveVendorRobin6(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentVendorRobin(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_ROBIN)
    public void receiveVendorRobin7(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentVendorRobin(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_ROBIN)
    public void receiveVendorRobin8(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentVendorRobin(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_ROBIN)
    public void receiveVendorRobin9(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentVendorRobin(taskMsg);

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

    @RabbitListener(queues = Constants.CONTENT_MESSAGE_VENDOR_ROBIN)
    public void receiveVendorRobin10(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("VendorRobin rabbit mq receive a message{}", msg.toString());

            TaskMsg taskMsg = JSONObject.toJavaObject(msgObj, TaskMsg.class);
            log.info("转换对象{}", taskMsg);

            contentService.contentVendorRobin(taskMsg);

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

*/
    @RabbitListener(queues = Constants.DEFAULT_ERROR)
    public void receiveDefaultError(Channel channel, Message message){
        try {

            String msg=new String(message.getBody());
            JSONObject msgObj=JSONObject.parseObject(msg);

            log.info("DefaultError rabbit mq receive a message{}", msg.toString());

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
