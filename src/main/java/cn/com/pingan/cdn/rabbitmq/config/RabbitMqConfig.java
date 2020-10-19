package cn.com.pingan.cdn.rabbitmq.config;

import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange messageDirectExchaneg(){
        return (DirectExchange) ExchangeBuilder.directExchange(Constants.CONTENT_MESSAGE_EXCHANGE).durable(true).build();
    }
    
    //队列配置
    @Bean("commonQueue")
    public Queue getQueue(){
       return QueueBuilder.durable(Constants.CONTENT_MESSAGE_QUEUE_NAE).build();
    }

    @Bean("commonBind")
    public Binding messageBinding(){
        return BindingBuilder.bind(getQueue()).to(messageDirectExchaneg()).with((Constants.CONTENT_MESSAGE_ROUTINE_KEY));
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