package cn.com.pingan.cdn.rabbitmq.producer;


import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.rabbitmq.message.HistoryMessage;


@Component
public class Producer {

    @Autowired
    private AmqpTemplate rabbitTemplate;
    

    public void send(String message){
        this.rabbitTemplate.convertAndSend(Constants.CONTENT_MESSAGE_EXCHANGE,Constants.CONTENT_MESSAGE_ROUTINE_KEY,message);
    }
    
    public void sendContent(HistoryMessage message){
        String msgJson = JSONObject.toJSONString(message);
        
        this.rabbitTemplate.convertAndSend(Constants.CONTENT_MESSAGE_EXCHANGE,Constants.CONTENT_MESSAGE_ROUTINE_KEY,msgJson);
    }

}