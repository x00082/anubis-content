package cn.com.pingan.cdn.rabbitmq.producer;


import cn.com.pingan.cdn.exception.ErrEnum;
import cn.com.pingan.cdn.exception.RestfulException;
import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Producer {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Autowired
    private RabbitMessagingTemplate messagingTemplate;

    
/*
    public void send(String message){
        this.rabbitTemplate.convertAndSend(Constants.CONTENT_MESSAGE_EXCHANGE,Constants.CONTENT_MESSAGE_ROUTINE_KEY,message);
    }
*/

    public void sendDelayMsg(TaskMsg msg){
        //msg.setCurrTime(new Date().getTime());
        //msg.setCount(msg.getCount() + 1);
        String dtoStr;
        try {
            dtoStr = JSONObject.toJSONString(msg);
            this.rabbitTemplate.convertAndSend(Constants.CONTENT_DELAY_EXCHANGE,
                    Constants.CONTENT_DELAY_ROUTINE_KEY,
                    dtoStr,
                    new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            /* 设置过期时间 */
                            message.getMessageProperties().setHeader("x-delay", msg.getDelay());
                            return message;
                        }
                    }
            );
        }catch (MessagingException e) {
            log.error("push delay msg to mq failed err:{}", e.getMessage());
            throw new RestfulException(ErrEnum.ErrMQPushMsg.getCode(), ErrEnum.ErrMQPushMsg.getErrMsg());
        }
    }


    public void sendTaskMsg(TaskMsg msg) throws RestfulException {
        String dtoStr;
        try {
            //dtoStr = objectMapper.writeValueAsString(msg);
            dtoStr = JSONObject.toJSONString(msg);
            this.messagingTemplate.convertAndSend(Constants.CONTENT_MESSAGE_EXCHANGE, msg.getOperation().toString(), dtoStr);
        } catch (MessagingException e) {
            log.error("push msg to mq failed err:{}", e.getMessage());
            throw new RestfulException(ErrEnum.ErrMQPushMsg.getCode(), ErrEnum.ErrMQPushMsg.getErrMsg());
        }
    }

    public void sendAllMsg(TaskMsg msg) throws RestfulException {
        if(msg.getDelay() > 0){
            sendDelayMsg(msg);
        }else{
            sendTaskMsg(msg);
        }
    }
}