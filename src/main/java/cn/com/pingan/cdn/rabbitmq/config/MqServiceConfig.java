package cn.com.pingan.cdn.rabbitmq.config;

import cn.com.pingan.cdn.common.TaskOperationEnum;
import cn.com.pingan.cdn.rabbitmq.constants.Constants;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * @Classname ServiceConfig
 * @Description TODO
 * @Date 2020/10/19 16:41
 * @Created by Luj
 */
@Configuration
@EnableRabbit
@EnableTransactionManagement
public class MqServiceConfig implements RabbitListenerConfigurer {
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public DirectExchange exchange(RabbitAdmin rabbitAdmin) {
        DirectExchange exchange = new DirectExchange(Constants.CONTENT_MESSAGE_EXCHANGE, true, false);
        rabbitAdmin.declareExchange(exchange);
        return exchange;
    }

    @Bean("taskContainerFactory")
    public SimpleRabbitListenerContainerFactory taskContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        /*
        factory.setConnectionFactory(connectionFactory);
        factory.setBatchListener(true);
        factory.setBatchSize(10);
        factory.setConsumerBatchEnabled(true);
        */
        return factory;
    }

    @Bean("taskQueueNames")
    public List<String> taskQueueNames() {
        return TaskOperationEnum.allTaskOperations();
    }

    @Bean
    public TxMqFactory txMqFactory(RabbitAdmin rabbitAdmin, DirectExchange exchange, List<String> queues) {
        return new TxMqFactory(rabbitAdmin, exchange, queues);
    }

    @Bean
    public MappingJackson2MessageConverter jackson2Converter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(jackson2Converter());
        return factory;
    }

    public void configureRabbitListeners(RabbitListenerEndpointRegistrar arg0) {
        arg0.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }
}
