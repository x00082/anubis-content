package cn.com.pingan.cdn.rabbitmq.config;

import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ShareRabbitMqConfig {

    @Bean
    public FanoutExchange fanoutExchaneg(){
        return new FanoutExchange(Constants.CONTENT_FANOUT_EXCHANGE);
    }
}