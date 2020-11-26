package cn.com.pingan.cdn.rabbitmq.config;

import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import org.springframework.amqp.core.*;
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


    @Bean("fanoutDelayQueue")
    public Queue fanoutDelayQueue(){
       return new Queue(Constants.CONTENT_FANOUT_DELAY_QUEUE,true);
    }
    
    @Bean("FanoutDelayBind")
    public Binding FanoutDelayBind(){
        return BindingBuilder.bind(fanoutDelayQueue()).to(delayDirectExchaneg()).with(Constants.CONTENT_FANOUT_DELAY_ROUTINE_KEY).noargs();
    }

}