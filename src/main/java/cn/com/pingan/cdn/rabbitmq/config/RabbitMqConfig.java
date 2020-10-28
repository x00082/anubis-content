package cn.com.pingan.cdn.rabbitmq.config;

import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMqConfig {

    @Bean
    public CustomExchange delayDirectExchaneg(){

        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        CustomExchange customExchange = new CustomExchange(Constants.CONTENT_DELAY_EXCHANGE,
                Constants.DELAYED_EXCHANGE_TYPE, true, false, args);
        //customExchange.setDelayed(true);加了报错
        return customExchange;
    }
    
    //队列配置
    @Bean("delayeQueue")
    public Queue delayeQueue(){
       Queue queue = new Queue(Constants.CONTENT_DELAY_QUEUE,true);
       return queue;
    }

    @Bean("delayeBind")
    public Binding messageBinding(){
        return BindingBuilder.bind(delayeQueue()).to(delayDirectExchaneg()).with(Constants.CONTENT_DELAY_ROUTINE_KEY).noargs();
    }
    
    /*
    @Bean("contentQueue")
    public Queue contentQueue(){
       return QueueBuilder.durable(Constants.CONTENT_MESSAGE_CONTENT_QUEUE_NAE).build();
    }
    
    @Bean("contentBind")
    public Binding contentMessageBinding(){
        return BindingBuilder.bind(contentQueue()).to(messageDirectExchaneg()).with((Constants.CONTENT_MESSAGE_CONTENT_ROUTINE_KEY));
    }
    */
}